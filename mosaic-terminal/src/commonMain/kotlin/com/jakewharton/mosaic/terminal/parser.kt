package com.jakewharton.mosaic.terminal

import com.jakewharton.mosaic.terminal.event.Event
import com.jakewharton.mosaic.terminal.event.KeyEscape

private const val bufferSize = 8 * 1024
private const val bareEscapeDisambiguationReadTimeoutMillis = 100

internal class TerminalParser(
	private val stdinReader: StdinReader,
	private val isInRawMode: Boolean,
) {
	private val buffer = ByteArray(bufferSize)
	private var offset = 0
	private var limit = 0

	fun next(): Event {
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
				val read = stdinReader.read(buffer, limit, bufferSize)
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
					bufferSize,
					bareEscapeDisambiguationReadTimeoutMillis
				)
				if (read == 0) {
					// We know the offset is 0, so resetting the limit effectively consumes the byte.
					this.limit = 0
					return KeyEscape
				}
			} else {
				read = stdinReader.read(buffer, limit, bufferSize)
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
//				0x4F -> parseSs3(buffer, start, limit)
//				0x50 -> parseDcs(buffer, start, limit)
//				0x58 -> parseUntilStringTerminator(buffer, start, limit)
//				0x5B -> parseCsi(buffer, start, limit)
//				0x5D -> TODO("Unhandled event")
//				0x5E -> TODO("Unhandled event")
//				0x5F -> parseApc(buffer, start, limit)
//				else -> CodepointEvent(b2, alt = true)
				else -> return TODO("Unhandled event")
			}
		}
		when (b1) {
			else -> return TODO("Unhandled event")
		}
	}
}
