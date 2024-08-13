package com.jakewharton.mosaic.term

import kotlinx.io.bytestring.ByteString
import kotlinx.io.bytestring.toHexString

data class ParseResult(
	val event: InputEvent,
	val bytesConsumed: Int,
)

/**
 * Parse and return a single event from [buf]. If null is returned, callers should copy the data
 * in [buf] to index 0, and request an additional read of data.
 */
internal fun parseInputEvent(buf: UByteArray, start: Int, limit: Int): ParseResult? {
	val b1 = buf[start].toInt()
	if (b1 == 0x1B) {
		val b2Index = start + 1
		// If this escape is at the end of the buffer, request another read to ensure we can
		// differentiate between a bare escape and one starting a sequence.
		if (b2Index == limit) return null
		if (b2Index < limit) {
			return when (val b2 = buf[b2Index].toInt()) {
				0x4F -> parseSs3(buf, start, limit)
				0x50 -> parseDcs(buf, start, limit)
				0x58 -> parseUntilStringTerminator(buf, start, limit)
				0x5B -> parseCsi(buf, start, limit)
				0x5D -> TODO("Unhandled event")
				0x5E -> TODO("Unhandled event")
				0x5F -> parseApc(buf, start, limit)
				else -> ParseResult(CodepointEvent(b2, alt = true), 2)
			}
		}
	}

	return when (b1) {
		0x00 -> ParseResult(CodepointEvent('@'.code, ctrl = true), 1)

		// Backspace key canonicalization.
		0x08 -> ParseResult(CodepointEvent(0x7F), 1)

		// Enter key canonicalization.
		0x0A -> ParseResult(CodepointEvent(0x0D), 1)

		0x09, 0x0D, 0x1A -> ParseResult(CodepointEvent(b1), 1)

		in 0x01..0x07,
		0x0B,
		0x0C,
		in 0x0E..0x1A,
		-> ParseResult(CodepointEvent(b1 + 0x60, ctrl = true), 1)

		else -> {
			// TODO Non-UTF-8 support?
			// TODO validate continuation bytes?
			val bytesConsumed: Int
			val codepoint: Int
			when {
				b1 and 0b10000000 == 0 -> {
					bytesConsumed = 1
					codepoint = b1
				}
				b1 and 0b11100000 == 0b11000000 -> {
					val b2Index = start + 1
					if (b2Index >= limit) return null
					bytesConsumed = 2
					codepoint = b1.and(0b00011111).shl(6) or
						buf[b2Index].toInt().and(0b00111111)
				}
				b1 and 0b11110000 == 0b11100000 -> {
					val b3Index = start + 2
					if (b3Index >= limit) return null
					bytesConsumed = 3
					codepoint = b1.and(0b00001111).shl(12) or
						buf[start + 1].toInt().and(0b00111111).shl(6) or
						buf[b3Index].toInt().and(0b00111111)
				}
				b1 and 0b11111000 == 0b11110000 -> {
					val b4Index = start + 3
					if (b4Index >= limit) return null
					bytesConsumed = 4
					codepoint = b1.and(0b00000111).shl(18) or
						buf[start + 1].toInt().and(0b00111111).shl(12) or
						buf[start + 2].toInt().and(0b00111111).shl(6) or
						buf[b4Index].toInt().and(0b00111111)
				}
				else -> TODO("Invalid UTF-8")
			}
			// TODO multi-codepoint grapheme support
			ParseResult(CodepointEvent(codepoint), bytesConsumed)
		}
	}
}

private fun parseCsi(buf: UByteArray, start: Int, limit: Int): ParseResult? {
	if (start + 3 > limit) return null

	val end = buf.indexOfFirst(start + 2, limit) {
		it in 0x40.toUByte()..0xFF.toUByte()
	}
	if (end == -1) return null
	val byteCount = end - start + 1

	val event = when (val final = buf[end].toInt()) {
		// These codepoints are defined by Kitty in the Unicode private space.
		'A'.code -> parseCsiLegacy(buf, start, end, 57352 /* up */)
		'B'.code -> parseCsiLegacy(buf, start, end, 57353 /* down */)
		'C'.code -> parseCsiLegacy(buf, start, end, 57351 /* right */)
		'D'.code -> parseCsiLegacy(buf, start, end, 57350 /* left */)
		'E'.code -> parseCsiLegacy(buf, start, end, 57427 /* kp_begin */)
		'F'.code -> parseCsiLegacy(buf, start, end, 57457 /* end */)
		'H'.code -> parseCsiLegacy(buf, start, end, 57456 /* home */)
		'P'.code -> parseCsiLegacy(buf, start, end, 57364 /* f1 */)
		'Q'.code -> parseCsiLegacy(buf, start, end, 57365 /* f2 */)
		'R'.code -> parseCsiLegacy(buf, start, end, 57366 /* f3 */)
		'S'.code -> parseCsiLegacy(buf, start, end, 57367 /* f4 */)

		'~'.code -> {
			val delim1 = buf.indexOf(';'.code.toUByte(), start + 2, end)
			val number = buf.parseIntDigits(start = 2, end = delim1.orElseIndex(end))
			val codepoint = when (number) {
				2 -> 57348 /* insert */
				3 -> 57349 /* delete */
				5 -> 57354 /* page up */
				6 -> 57355 /* page down */
				7 -> 57356 /* home */
				8 -> 57357 /* end */
				11 -> 57364 /* f1 */
				12 -> 57365 /* f2 */
				13 -> 57366 /* f3 */
				14 -> 57367 /* f4 */
				15 -> 57368 /* f5 */
				17 -> 57369 /* f6 */
				18 -> 57370 /* f7 */
				19 -> 57371 /* f8 */
				20 -> 57372 /* f9 */
				21 -> 57373 /* f10 */
				23 -> 57374 /* f11 */
				24 -> 57375 /* f12 */
				200 -> return ParseResult(PasteEvent(start = true), byteCount)
				201 -> return ParseResult(PasteEvent(start = false), byteCount)
				57427 -> 57427 /* kp_begin */
				else -> TODO("Need UnknownEvent")
			}

			// TODO parse rest of CSI

			CodepointEvent(codepoint)
		}

		// TODO validate no in-between bytes?
		'I'.code -> FocusEvent(true)
		'O'.code -> FocusEvent(false)

		'm'.code,
		'M'.code -> {
			if (buf[start + 2] != '<'.code.toUByte()) TODO("return Unknown? error?")

			val delim1 = buf.indexOf(';'.code.toUByte(), start + 3, end)
			val delim2 = buf.indexOf(';'.code.toUByte(), delim1 + 1, end)

			val buttonBits = buf.parseIntDigits(start = start + 3, end = delim1)

			// Incoming values are 1-based.
			val x = buf.parseIntDigits(delim1 + 1, delim2) - 1
			val y = buf.parseIntDigits(delim2 + 1, end) - 1

			val button = when (val button = buttonBits and 0b11000011) {
				0 -> MouseEvent.Button.Left
				1 -> MouseEvent.Button.Middle
				2 -> MouseEvent.Button.Right
				3 -> MouseEvent.Button.None
				64 -> MouseEvent.Button.WheelUp
				65 -> MouseEvent.Button.WheelDown
				128 -> MouseEvent.Button.Button8
				129 -> MouseEvent.Button.Button9
				130 -> MouseEvent.Button.Button10
				131 -> MouseEvent.Button.Button11
				else -> throw AssertionError("Unknown button $button")
			}
			val motion = (buttonBits and 0b00100000) != 0
			val type = when {
				motion && button != MouseEvent.Button.None -> MouseEvent.Type.Drag
				motion && button == MouseEvent.Button.None -> MouseEvent.Type.Motion
				final == 'm'.code -> MouseEvent.Type.Release
				else -> MouseEvent.Type.Press
			}
			val shift = (buttonBits and 0b00000100) != 0
			val alt = (buttonBits and 0b00001000) != 0
			val ctrl = (buttonBits and 0b00010000) != 0

			MouseEvent(
				x = x,
				y = y,
				type = type,
				button = button,
				shift = shift,
				alt = alt,
				ctrl = ctrl,
			)
		}

		'c'.code -> {
			if (end - start < 4) return null
			if (buf[start + 2].toInt() != '?'.code) TODO("malformed event")
			PrimaryDeviceAttributes(
				buf.copyOfRange(3, end).toByteArray().decodeToString()
			)
		}

		't'.code -> {
			val modeDelim = buf.indexOf(';'.code.toUByte(), start + 2, end)
			val rowDelim = buf.indexOf(';'.code.toUByte(), modeDelim + 1, end)
			val colDelim = buf.indexOf(';'.code.toUByte(), rowDelim + 1, end)
			val heightDelim = buf.indexOf(';'.code.toUByte(), colDelim + 1, end)
			val widthDelim = buf.indexOf(';'.code.toUByte(), heightDelim + 1, end)
			val mode = buf.parseIntDigits(start + 2, modeDelim)
			// TODO validate 48
			val rows = buf.parseIntDigits(modeDelim + 1, rowDelim)
			val cols = buf.parseIntDigits(rowDelim + 1, colDelim)
			val height = buf.parseIntDigits(colDelim + 1, heightDelim)
			val width = buf.parseIntDigits(heightDelim + 1, end)
			ResizeEvent(rows, cols, height, width)
		}

		'y'.code -> {
			if (end < start + 7) return null
			if (buf[end - 1].toInt() != '$'.code) {
				return ParseResult(
					event = UnknownEvent(
						context = "TODO", // TODO
						bytes = buf.copyOfRange(start, end),
					),
					bytesConsumed = byteCount,
				)
			}

			if (buf[start + 2].toInt() == '?'.code) {
				if (end < start + 8) return null
				val semi = buf.indexOf(';'.code.toUByte(), start + 3, end)
				val pd = buf.parseIntDigits(start + 3, semi)
				val ps = buf.parseIntDigits(semi + 1, end - 1)
				val setting = when (ps) {
					0 -> DecModeReport.Setting.NotRecognized
					1 -> DecModeReport.Setting.Set
					2 -> DecModeReport.Setting.Reset
					3 -> DecModeReport.Setting.PermanentlySet
					4 -> DecModeReport.Setting.PermanentlyReset
					else -> return ParseResult(
						event = UnknownEvent(
							context = "TODO", // TODO
							bytes = buf.copyOfRange(start, end),
						),
						bytesConsumed = byteCount,
					)
				}
				DecModeReport(
					mode = pd,
					setting = setting,
				)
			} else {
				TODO("ANSI mode reporter")
			}
		}

		else -> UnknownEvent(
			context = "CSI final character $final not recognized",
			bytes = buf.copyOfRange(start, end),
		)
	}

	return ParseResult(event, byteCount)
}

private fun parseApc(buf: UByteArray, start: Int, end: Int): ParseResult? {
	// TODO https://stackoverflow.com/a/71632523/132047
	return parseUntilStringTerminator(buf, start, end) { stIndex ->
		if (stIndex > start + 2 && buf[start + 2].toInt() == 'G'.code) {
			val delim = buf.indexOf(';'.code.toUByte(), start + 2, stIndex)
			if (delim == -1 || delim < start + 5 || buf[start + 3].toInt() != 'i'.code || buf[start + 4].toInt() != '='.code) {
				null // TODO unknown + context
			} else {
				KittyGraphicsEvent(
					id = buf.parseIntDigits(start + 4, delim),
					message = buf.toByteArray().decodeToString(delim + 1, stIndex),
				)
			}
		} else {
			null // TODO unknown + context
		}
	}
}

private fun parseSs3(buf: UByteArray, start: Int, end: Int): ParseResult? {
	if (start + 3 > end) return null
	val b3 = buf[start + 2].toInt()
	val codepoint = when (b3) {
		'A'.code -> 57352 /* up */
		'B'.code -> 57353 /* down */
		'C'.code -> 57351 /* right */
		'D'.code -> 57350 /* left */
		'F'.code -> 57357 /* end */
		'H'.code -> 57356 /* home */
		'P'.code -> 57364 /* f1 */
		'Q'.code -> 57365 /* f2 */
		'R'.code -> 57366 /* f3 */
		'S'.code -> 57367 /* f3 */
		0x1b -> {
			// libvaxis added a guard against this case
			// https://github.com/rockorager/libvaxis/commit/b68864c3babf2767c15c52911179e8ee9158e1d2
			return ParseResult(
				event = UnknownEvent(
					context = "First two bytes match SS3 but character byte was an escape",
					bytes = buf.copyOfRange(start, start + 2)
				),
				bytesConsumed = 2,
			)
		}
		else -> {
			return ParseResult(
				event = UnknownEvent(
					context = "Unsupported SS3 character byte",
					bytes = buf.copyOfRange(start, start + 3),
				),
				bytesConsumed = 3,
			)
		}
	}
	return ParseResult(
		event = CodepointEvent(codepoint),
		bytesConsumed = 3,
	)
}

private fun parseUntilStringTerminator(
	buf: UByteArray,
	start: Int,
	end: Int,
	handler: (stIndex: Int) -> InputEvent? = { null },
): ParseResult? {
	// TODO test underflow
	// TODO test string with 0x1b inside of it

	// Skip leading discriminator bytes.
	var searchFrom = start + 2

	while (true) {
		val candidate = buf.indexOf(0x1B.toUByte(), searchFrom, end)
		// Not found in range, underflow.
		if (candidate == -1) break
		// Found at end of range, underflow.
		if (candidate == end - 1) break

		if (buf[candidate + 1] == '\\'.code.toUByte()) {
			val sequenceEnd = candidate + 2
			val event = handler(candidate)
				// TODO prevent returning null?
				?: UnknownEvent(
					context = "Unsupported string sequence",
					bytes = buf.copyOfRange(start, sequenceEnd).toByteArray().toByteString(),
				)
			return ParseResult(event, sequenceEnd - start)
		}
		searchFrom = candidate + 1
	}
	return null
}

private fun parseDcs(buf: UByteArray, start: Int, end: Int): ParseResult? {
	return parseUntilStringTerminator(buf, start, end) { stIndex ->
		if (stIndex > start + 4 && buf[start + 2].toInt() == '>'.code && buf[start + 3].toInt() == '|'.code) {
			DeviceStatusReportString(buf.toByteArray().decodeToString(start + 4, stIndex))
		} else {
			null
		}
	}
}

private fun parseCsiLegacy(buf: UByteArray, start: Int, end: Int, codepoint: Int): CodepointEvent {
	val delim = buf.indexOf(';'.code.toUByte(), start + 2, end)
	// TODO parse other shit
	return CodepointEvent(codepoint)
}

sealed interface InputEvent

data class InputEventWithWarning(
	val event: InputEvent,
	val warning: String,
) : InputEvent

data class CodepointEvent(
	val codepoint: Int,
	val shift: Boolean = false,
	val alt: Boolean = false,
	val ctrl: Boolean = false,
) : InputEvent {
	override fun toString() = buildString {
		append("CodepointEvent(")
		if (shift) append("Shift+")
		if (ctrl) append("Ctrl+")
		if (alt) append("Alt+")
		append("0x")
		append(codepoint.toString(16).uppercase())
		append(')')
	}
}

data class GraphemeEvent(
	val text: String,
) : InputEvent

data class MouseEvent(
	val x: Int,
	val y: Int,
	val type: Type,
	val button: Button,
	val shift: Boolean,
	val alt: Boolean,
	val ctrl: Boolean,
) : InputEvent {
	enum class Button {
		Left,
		Middle,
		Right,
		None,
		WheelUp,
		WheelDown,
		Button8,
		Button9,
		Button10,
		Button11,
	}
	enum class Type {
		Press,
		Release,
		Motion,
		Drag,
	}

	override fun toString() = buildString {
		append("MouseEvent(")
		append(x)
		append(',')
		append(y)

		if (type != Type.Motion) {
			append(' ')
			append(type)
			if (button != Button.None) {
				append(' ')
				append(button)
			}
		}

		var prefix = ' '
		if (shift) {
			append(" Shift")
			prefix = '+'
		}
		if (ctrl) {
			append(prefix)
			append("Ctrl")
			prefix = '+'
		}
		if (alt) {
			append(prefix)
			append("Alt")
		}
		append(')')
	}
}

data class FocusEvent(
	val focused: Boolean,
) : InputEvent

data class PasteEvent(
	val start: Boolean,
) : InputEvent

data class UnknownEvent(
	val context: String,
	val bytes: ByteString,
) : InputEvent {
	override fun toString() = buildString {
		append("UnknownEvent(")
		append(context)
		append(' ')
		append(bytes)
		append(')')
	}
}

data class PrimaryDeviceAttributes(
	val content: String,
) : InputEvent

data class DecModeReport(
	val mode: Int,
	val setting: Setting,
) : InputEvent {
	enum class Setting {
		NotRecognized,
		Set,
		Reset,
		PermanentlySet,
		PermanentlyReset,
	}
}

data class DeviceStatusReportString(
	val content: String,
) : InputEvent

data class KittyGraphicsEvent(
	val id: Int,
	val message: String,
) : InputEvent

data class ResizeEvent(
	val rows: Int,
	val cols: Int,
	val height: Int,
	val width: Int,
) : InputEvent
