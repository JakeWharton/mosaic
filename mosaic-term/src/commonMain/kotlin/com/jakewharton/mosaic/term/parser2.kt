package com.jakewharton.mosaic.term

import kotlinx.io.Source
import kotlinx.io.readByteString
import kotlinx.io.readString

fun Source.readInputEvent(): InputEvent {
	// TODO Determine if byte-to-int conversion is better than directly comparing bytes.
	//  I suspect bytes are always widened to ints at the register level.
	val b1 = readByte().toInt()
	if (b1 == 0x1B) {
		// There is no definitive way to differentiate between an escape key press that is immediately
		// followed by manually-typed characters (grouped together by network lag, for example) and
		// an actual escape sequence. The best heuristic that we have is to simply check whether there
		// are more bytes available from the input. If there are no available bytes, it's a manual
		// escape key press. If there are bytes available, it's probably an escape sequence.
		// TODO this shouldn't be a request as that can block
		if (request(1L)) {
			// TODO Do something more efficient than readByte() here. It wastefully re-checks
			//  that a single byte is available, but we already know that from the request.
			val b2 = readByte().toInt()
			// CSI sequences are so common we special case dispatch.
			if (b2 == 0x5B) {
				return parseCsi()
			}
			return parseEscapeSequence(b2)
		}
	} else if (b1 >= 0x20) {
		// TODO Non-UTF-8 support?
		// TODO validate continuation bytes?
		val codepoint = when {
			b1 and 0b10000000 == 0 -> b1
			b1 and 0b11100000 == 0b11000000 -> {
				b1.and(0b00011111).shl(6) or
					readByte().toInt().and(0b00111111)
			}
			b1 and 0b11110000 == 0b11100000 -> {
				b1.and(0b00001111).shl(12) or
					readByte().toInt().and(0b00111111).shl(6) or
					readByte().toInt().and(0b00111111)
			}
			b1 and 0b11111000 == 0b11110000 -> {
				b1.and(0b00000111).shl(18) or
					readByte().toInt().and(0b00111111).shl(12) or
					readByte().toInt().and(0b00111111).shl(6) or
					readByte().toInt().and(0b00111111)
			}
			else -> TODO("Invalid UTF-8")
		}
		// TODO multi-codepoint grapheme support, via method call
		return CodepointEvent(codepoint)
	}

	return parseC0ControlCode(b1)
}

/** Parse [value] in the range [0x00, 0x20). */
private fun parseC0ControlCode(value: Int): InputEvent {
	return when (value) {
		0x00 -> CodepointEvent('@'.code, ctrl = true)

		// Backspace key canonicalization.
		0x08 -> CodepointEvent(0x7F)

		// Enter key canonicalization.
		0x0A -> CodepointEvent(0x0D)

		0x09, 0x0D, 0x1A, 0x1B -> CodepointEvent(value)

		else -> CodepointEvent(value + 0x60, ctrl = true)
	}
}

private fun Source.parseEscapeSequence(b2: Int): InputEvent {
	return when (b2) {
		0x4E -> TODO("parseSs2")
		0x4F -> TODO("parseSs3")
		0x50 -> parseDcs()
		0x56 -> TODO("parseSpa")
		0x57 -> TODO("parseEpa")
		0x58 -> TODO("parseSos")
		0x5A -> TODO("parseDecId")
		0x5B -> parseCsi()
		0x5C -> TODO("parseSt") // Can this be received bare?
		0x5D -> TODO("parseOsc")
		0x5E -> TODO("parsePm")
		0x5F -> TODO("parseApc")
		else -> CodepointEvent(b2, alt = true)
	}
}

private fun Source.parseCsi(): InputEvent {
	val end = indexOfFirst {
		it.toInt() in 0x40..0xFF
	}
	val buffer = buffer
	return when (val final = buffer[end].toInt()) {
		'I'.code -> {
			val event = FocusEvent(true)
			if (end == 0L) {
				FocusEvent(true)
			} else {
				InputEventWithWarning(
					// TODO
					event, "CSI 'I' but found "
				)
			}
		}
		'O'.code -> {
			if (end != 0L) {
				TODO("Return InputEventWithWarning")
			}
			FocusEvent(false)
		}

		'c'.code -> {
			if (buffer[0].toInt() != '?'.code) {
				UnknownEvent("CSI 'c' but missing leading '?'", readByteString(end.toInt()))
			}
			buffer.skip(1)
			PrimaryDeviceAttributes(buffer.readString(end - 1))
		}
		else -> UnknownEvent(
			context = "CSI final character not recognized",
			bytes = readByteString(end.toInt())
		)
	}
}

private fun Source.parseDcs(): InputEvent {
	TODO()
}
