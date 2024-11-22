package com.jakewharton.mosaic.terminal

import com.jakewharton.mosaic.terminal.event.BracketedPasteEvent
import com.jakewharton.mosaic.terminal.event.CodepointEvent
import com.jakewharton.mosaic.terminal.event.DecModeReportEvent
import com.jakewharton.mosaic.terminal.event.DeviceStatusReportEvent
import com.jakewharton.mosaic.terminal.event.Event
import com.jakewharton.mosaic.terminal.event.FocusEvent
import com.jakewharton.mosaic.terminal.event.KeyEscape
import com.jakewharton.mosaic.terminal.event.KittyGraphicsEvent
import com.jakewharton.mosaic.terminal.event.KittyKeyboardQueryEvent
import com.jakewharton.mosaic.terminal.event.MouseEvent
import com.jakewharton.mosaic.terminal.event.PrimaryDeviceAttributesEvent
import com.jakewharton.mosaic.terminal.event.ResizeEvent
import com.jakewharton.mosaic.terminal.event.SystemThemeEvent
import com.jakewharton.mosaic.terminal.event.TerminalVersionEvent
import com.jakewharton.mosaic.terminal.event.UnknownEvent

private const val BufferSize = 8 * 1024
private const val BareEscapeDisambiguationReadTimeoutMillis = 100

public class TerminalParser(
	private val stdinReader: StdinReader,
	private val isInRawMode: Boolean,
) {
	private val buffer = ByteArray(BufferSize)
	private var offset = 0
	private var limit = 0

	public fun next(): Event {
		val buffer = buffer
		var offset = offset
		var limit = limit

		while (true) {
			if (offset < limit) {
				parse(buffer, offset, limit)?.let { event ->
					return event
				}

				// Underflow! Copy data to start of buffer (if not already there) in preparation for a read.
				if (offset > 0) {
					buffer.copyInto(buffer, 0, startIndex = offset, endIndex = limit)

					// Do not write the new limit to the member property because the read code below will.
					limit = limit - offset

					offset = 0
					this.offset = 0
				}
			}

			if (isInRawMode) {
				// Common case: we're in raw mode and can block filling the buffer as we never need to
				// do a disambiguation read on a bare escape (it would have come as a keyboard event).
				val read = stdinReader.read(buffer, limit, BufferSize)
				limit += read
				this.limit = limit
				continue
			}

			val read: Int
			if (limit == 1 && buffer[0].toInt() == 0x1B) {
				// If we are not in raw mode and our only byte is an escape, perform a quick disambiguation
				// read to see if we have any more bytes. This will allow us to determine whether the bare
				// escape was truly an escape, or just the start of an escape sequence.
				read = stdinReader.readWithTimeout(
					buffer,
					limit,
					BufferSize,
					BareEscapeDisambiguationReadTimeoutMillis,
				)
				if (read == 0) {
					// We know the offset is 0, so resetting the limit effectively consumes the byte.
					this.limit = 0
					return KeyEscape
				}
			} else {
				read = stdinReader.read(buffer, limit, BufferSize)
			}
			limit += read
			this.limit = limit
		}
	}

	private fun parse(buffer: ByteArray, start: Int, limit: Int): Event? {
		val b1 = buffer[start].toInt()
		if (b1 == 0x1B) {
			val b2Index = start + 1
			// If this escape is at the end of the buffer, request another read to ensure we can
			// differentiate between a bare escape and one starting a sequence. Note: The caller is
			// expected to handle the case of a bare escape, as we will otherwise endlessly return null.
			if (b2Index == limit) return null

			when (val b2 = buffer[b2Index].toInt()) {
				0x4F -> return parseSs3(buffer, start, limit)
				0x50 -> return parseDcs(buffer, start, limit)
				0x58 -> return parseSos(buffer, start, limit)
				0x5B -> return parseCsi(buffer, start, limit)
				0x5D -> return parseOsc(buffer, start, limit)
				0x5E -> return parsePm(buffer, start, limit)
				0x5F -> return parseApc(buffer, start, limit)
				else -> {
					offset = start + 2
					return CodepointEvent(b2, alt = true)
				}
			}
		} else {
			return parseGround(buffer, start, limit, b1)
		}
	}

	private fun parseGround(buffer: ByteArray, start: Int, limit: Int, b1: Int): Event? {
		val b2Index = start + 1

		when (b1) {
			0x00 -> {
				offset = b2Index
				return CodepointEvent('@'.code, ctrl = true)
			}

			// Backspace key canonicalization.
			0x08 -> {
				offset = b2Index
				return CodepointEvent(0x7F)
			}

			// Enter key canonicalization.
			0x0A -> {
				offset = b2Index
				return CodepointEvent(0x0D)
			}

			0x09, 0x0D, 0x1A -> {
				offset = b2Index
				return CodepointEvent(b1)
			}

			in 0x01..0x07,
			0x0B,
			0x0C,
			in 0x0E..0x1A,
			-> {
				offset = b2Index
				return CodepointEvent(b1 + 0x60, ctrl = true)
			}

			else -> {
				// TODO Non-UTF-8 support?
				// TODO validate continuation bytes?
				val codepoint = when {
					b1 and 0b10000000 == 0 -> {
						offset = b2Index
						b1
					}
					b1 and 0b11100000 == 0b11000000 -> {
						if (b2Index == limit) return null
						offset = start + 2
						b1.and(0b00011111).shl(6) or
							buffer[b2Index].toInt().and(0b00111111)
					}
					b1 and 0b11110000 == 0b11100000 -> {
						val b3Index = start + 2
						if (b3Index >= limit) return null
						offset = start + 3
						b1.and(0b00001111).shl(12) or
							buffer[b2Index].toInt().and(0b00111111).shl(6) or
							buffer[b3Index].toInt().and(0b00111111)
					}
					b1 and 0b11111000 == 0b11110000 -> {
						val b4Index = start + 3
						if (b4Index >= limit) return null
						offset = start + 4
						b1.and(0b00000111).shl(18) or
							buffer[b2Index].toInt().and(0b00111111).shl(12) or
							buffer[start + 2].toInt().and(0b00111111).shl(6) or
							buffer[b4Index].toInt().and(0b00111111)
					}
					else -> TODO("Invalid UTF-8")
				}
				// TODO multi-codepoint grapheme support
				return CodepointEvent(codepoint)
			}
		}
	}

	private fun parseApc(buffer: ByteArray, start: Int, limit: Int): Event? {
		// TODO https://stackoverflow.com/a/71632523/132047
		return parseUntilStringTerminator(buffer, start, limit) { stIndex, end ->
			val b3Index = start + 2
			if (stIndex > b3Index && buffer[b3Index].toInt() == 'G'.code) {
				val delimiter = buffer.indexOf(';'.code.toByte(), b3Index, stIndex)
				val b5Index = start + 4
				if (delimiter == -1 ||
					delimiter <= b5Index ||
					buffer[start + 3].toInt() != 'i'.code ||
					buffer[b5Index].toInt() != '='.code
				) {
					UnknownEvent(buffer.copyOfRange(start, end))
				} else {
					KittyGraphicsEvent(
						id = buffer.parseIntDigits(b5Index, delimiter),
						message = buffer.decodeToString(delimiter + 1, stIndex),
					)
				}
			} else {
				UnknownEvent(buffer.copyOfRange(start, end))
			}
		}
	}

	private fun parseCsi(buffer: ByteArray, start: Int, limit: Int): Event? {
		val b3Index = start + 2
		val finalIndex = buffer.indexOfFirstOrElse(
			// Skip leading 0x1B5B.
			start = b3Index,
			end = limit,
			predicate = { it.toInt() in 0x40..0xFF },
			orElse = { return null },
		)

		val end = finalIndex + 1
		offset = end

		error@ do {
			when (buffer[finalIndex].toInt()) {
				'A'.code -> return parseCsiLegacy(buffer, start, end, CodepointEvent.Up)
				'B'.code -> return parseCsiLegacy(buffer, start, end, CodepointEvent.Down)
				'C'.code -> return parseCsiLegacy(buffer, start, end, CodepointEvent.Right)
				'D'.code -> return parseCsiLegacy(buffer, start, end, CodepointEvent.Left)
				'E'.code -> return parseCsiLegacy(buffer, start, end, CodepointEvent.KpBegin)
				'F'.code -> return parseCsiLegacy(buffer, start, end, CodepointEvent.End)
				'H'.code -> return parseCsiLegacy(buffer, start, end, CodepointEvent.Home)
				'P'.code -> return parseCsiLegacy(buffer, start, end, CodepointEvent.F1)
				'Q'.code -> return parseCsiLegacy(buffer, start, end, CodepointEvent.F2)
				'R'.code -> return parseCsiLegacy(buffer, start, end, CodepointEvent.F3)
				'S'.code -> return parseCsiLegacy(buffer, start, end, CodepointEvent.F4)

				'~'.code -> {
					val delimiter =
						buffer.indexOfOrDefault(';'.code.toByte(), b3Index, finalIndex, finalIndex)
					val number = buffer.parseIntDigits(start = b3Index, end = delimiter)
					val codepoint = when (number) {
						2 -> CodepointEvent.Insert
						3 -> CodepointEvent.Delete
						5 -> CodepointEvent.PageUp
						6 -> CodepointEvent.PageDown
						7 -> CodepointEvent.Home
						8 -> CodepointEvent.End
						11 -> CodepointEvent.F1
						12 -> CodepointEvent.F2
						13 -> CodepointEvent.F3
						14 -> CodepointEvent.F4
						15 -> CodepointEvent.F5
						17 -> CodepointEvent.F6
						18 -> CodepointEvent.F7
						19 -> CodepointEvent.F8
						20 -> CodepointEvent.F9
						21 -> CodepointEvent.F10
						23 -> CodepointEvent.F11
						24 -> CodepointEvent.F12
						200 -> return BracketedPasteEvent(start = true)
						201 -> return BracketedPasteEvent(start = false)
						57427 -> CodepointEvent.KpBegin
						else -> break@error
					}

					// TODO parse rest of CSI ... ~
					return CodepointEvent(codepoint)
				}

				'I'.code -> return FocusEvent(focused = true)
				'O'.code -> return FocusEvent(focused = false)

				'm'.code,
				'M'.code,
				-> {
					if (buffer[b3Index].toInt() != '<'.code) {
						break@error
					}

					val delim1 = buffer.indexOf(';'.code.toByte(), start + 3, finalIndex)
					val delim2 = buffer.indexOf(';'.code.toByte(), delim1 + 1, finalIndex)

					val buttonBits = buffer.parseIntDigits(start = start + 3, end = delim1)

					// Incoming values are 1-based.
					val x = buffer.parseIntDigits(delim1 + 1, delim2) - 1
					val y = buffer.parseIntDigits(delim2 + 1, end) - 1

					val button = when (buttonBits and 0b11000011) {
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
						else -> break@error
					}
					val motion = (buttonBits and 0b00100000) != 0
					val type = when {
						motion && button != MouseEvent.Button.None -> MouseEvent.Type.Drag
						motion && button == MouseEvent.Button.None -> MouseEvent.Type.Motion
						buffer[finalIndex].toInt() == 'm'.code -> MouseEvent.Type.Release
						else -> MouseEvent.Type.Press
					}
					val shift = (buttonBits and 0b00000100) != 0
					val alt = (buttonBits and 0b00001000) != 0
					val ctrl = (buttonBits and 0b00010000) != 0

					return MouseEvent(
						x = x,
						y = y,
						type = type,
						button = button,
						shift = shift,
						alt = alt,
						ctrl = ctrl,
					)
				}

				'n'.code -> {
					if (buffer[b3Index].toInt() == '?'.code) {
						val delimiter =
							buffer.indexOfOrDefault(';'.code.toByte(), start + 3, finalIndex, finalIndex)
						when (buffer.parseIntDigits(start + 3, delimiter)) {
							997 -> {
								if (delimiter + 2 == finalIndex) {
									val p2 = buffer[delimiter + 1].toInt()
									if (p2 == '1'.code) {
										return SystemThemeEvent(isDark = true)
									}
									if (p2 == '2'.code) {
										return SystemThemeEvent(isDark = false)
									}
								}
							}

							else -> {
								return DeviceStatusReportEvent(
									data = buffer.decodeToString(start + 3, finalIndex),
								)
							}
						}
					}
				}

				'c'.code -> {
					if (buffer[b3Index].toInt() == '?'.code) {
						val data = buffer.decodeToString(start + 3, finalIndex)
						// TODO Parse parameters from data
						return PrimaryDeviceAttributesEvent(data)
					}
				}

				't'.code -> {
					// TODO validation. while(true) + indexOfOrElse + break + UnknownEvent
					val modeDelim = buffer.indexOf(';'.code.toByte(), b3Index, finalIndex)
					val rowDelim = buffer.indexOf(';'.code.toByte(), modeDelim + 1, finalIndex)
					val colDelim = buffer.indexOf(';'.code.toByte(), rowDelim + 1, finalIndex)
					val heightDelim = buffer.indexOf(';'.code.toByte(), colDelim + 1, finalIndex)
					val widthDelim = buffer.indexOf(';'.code.toByte(), heightDelim + 1, finalIndex)
					val mode = buffer.parseIntDigits(b3Index, modeDelim)
					// TODO validate 48
					val rows = buffer.parseIntDigits(modeDelim + 1, rowDelim)
					val cols = buffer.parseIntDigits(rowDelim + 1, colDelim)
					val height = buffer.parseIntDigits(colDelim + 1, heightDelim)
					val width = buffer.parseIntDigits(heightDelim + 1, finalIndex)
					return ResizeEvent(rows, cols, height, width)
				}

				'u'.code -> {
					if (buffer[b3Index].toInt() == '?'.code) {
						val b4Index = start + 3
						if (b4Index != finalIndex) {
							val flags = buffer.parseIntDigits(b4Index, finalIndex)
							return KittyKeyboardQueryEvent(flags)
						}
					}
				}

				'y'.code -> {
					// CSI ? Ps ; Pm $ y
					val dollarIndex = finalIndex - 1
					if (buffer[dollarIndex].toInt() == '$'.code) {
						if (buffer[b3Index].toInt() == '?'.code) {
							if (end - start < 8) break@error

							val b4Index = start + 3
							val semi = buffer.indexOf(';'.code.toByte(), b4Index, dollarIndex)
							if (semi == -1) break@error // TODO indexOfOrElse break@error

							val mode = buffer.parseIntDigits(b4Index, semi)
							val settingValue = buffer.parseIntDigits(semi + 1, dollarIndex)
							// TODO parseIntDigits orElse break@error

							val setting = when (settingValue) {
								0 -> DecModeReportEvent.Setting.NotRecognized
								1 -> DecModeReportEvent.Setting.Set
								2 -> DecModeReportEvent.Setting.Reset
								3 -> DecModeReportEvent.Setting.PermanentlySet
								4 -> DecModeReportEvent.Setting.PermanentlyReset
								else -> break@error
							}
							return DecModeReportEvent(mode, setting)
						} else {
							// TODO ANSI mode reporter
						}
					}
				}
			}
		} while (false)

		return UnknownEvent(buffer.copyOfRange(start, end))
	}

	private fun parseCsiLegacy(buffer: ByteArray, start: Int, limit: Int, codepoint: Int): CodepointEvent {
		// TODO parse other shit
		//  val delimiter = buffer.indexOf(';'.code.toByte(), start + 2, limit)
		return CodepointEvent(codepoint)
	}

	private fun parseDcs(buffer: ByteArray, start: Int, limit: Int): Event? {
		return parseUntilStringTerminator(buffer, start, limit) { stIndex, end ->
			val b4Index = start + 3
			if (stIndex > b4Index &&
				buffer[start + 2].toInt() == '>'.code &&
				buffer[b4Index].toInt() == '|'.code
			) {
				TerminalVersionEvent(buffer.decodeToString(start + 4, stIndex))
			} else {
				UnknownEvent(buffer.copyOfRange(start, end))
			}
		}
	}

	private fun parseOsc(buffer: ByteArray, start: Int, limit: Int): Event? {
		TODO()
	}

	private fun parsePm(buffer: ByteArray, start: Int, limit: Int): Event? {
		return parseUntilStringTerminator(buffer, start, limit) { _, end ->
			UnknownEvent(buffer.copyOfRange(start, end))
		}
	}

	private fun parseSos(buffer: ByteArray, start: Int, limit: Int): Event? {
		return parseUntilStringTerminator(buffer, start, limit) { _, end ->
			UnknownEvent(buffer.copyOfRange(start, end))
		}
	}

	private fun parseSs3(buffer: ByteArray, start: Int, limit: Int): Event? {
		val end = start + 4
		if (end > limit) return null

		offset = end

		val b3Index = start + 2
		val codepoint = when (buffer[b3Index].toInt()) {
			'A'.code -> CodepointEvent.Up
			'B'.code -> CodepointEvent.Down
			'C'.code -> CodepointEvent.Right
			'D'.code -> CodepointEvent.Left
			'F'.code -> CodepointEvent.End
			'H'.code -> CodepointEvent.Home
			'P'.code -> CodepointEvent.F1
			'Q'.code -> CodepointEvent.F2
			'R'.code -> CodepointEvent.F3
			'S'.code -> CodepointEvent.F3
			0x1b -> {
				// libvaxis added a guard against this case
				// https://github.com/rockorager/libvaxis/commit/b68864c3babf2767c15c52911179e8ee9158e1d2
				offset = b3Index
				return UnknownEvent(buffer.copyOfRange(start, b3Index))
			}
			else -> {
				return UnknownEvent(buffer.copyOfRange(start, end))
			}
		}
		return CodepointEvent(codepoint)
	}

	private inline fun parseUntilStringTerminator(
		buffer: ByteArray,
		start: Int,
		limit: Int,
		crossinline handler: (stIndex: Int, end: Int) -> Event,
	): Event? {
		// TODO test string with 0x1b inside of it

		// Skip leading discriminator.
		var searchFrom = start + 2

		while (true) {
			val escIndex = buffer.indexOfFirstOrElse(
				start = searchFrom,
				end = limit,
				predicate = { it == 0x1B.toByte() },
				orElse = { return null },
			)
			// If found at end of range, underflow.
			val slashIndex = escIndex + 1
			if (slashIndex == limit) return null

			if (buffer[slashIndex] == '\\'.code.toByte()) {
				val end = escIndex + 2
				offset = end
				return handler(escIndex, end)
			}
			searchFrom = slashIndex
		}
	}
}
