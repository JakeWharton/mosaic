package com.jakewharton.mosaic.terminal

import com.jakewharton.mosaic.terminal.event.Event
import com.jakewharton.mosaic.terminal.event.KeyEscape
import com.jakewharton.mosaic.terminal.event.UnknownEvent

private const val BufferSize = 8 * 1024
private const val BareEscapeDisambiguationReadTimeoutMillis = 100

internal class TerminalParser(
	private val stdinReader: StdinReader,
	private val isInRawMode: Boolean,
) {
	private val buffer = ByteArray(BufferSize)
	private var offset = 0
	private var limit = 0

	fun next(): Event {
		val buffer = buffer
		var offset = offset
		var limit = limit

		while (true) {
			if (offset < limit) {
				parseEvent(buffer, offset, limit)?.let { event ->
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
					BareEscapeDisambiguationReadTimeoutMillis
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

	private fun parseEvent(buffer: ByteArray, start: Int, limit: Int): Event? {
		val b1 = buffer[start].toInt()
		if (b1 == 0x1B) {
			val b2Index = start + 1
			// If this escape is at the end of the buffer, request another read to ensure we can
			// differentiate between a bare escape and one starting a sequence. Note: The caller is
			// expected to handle the case of a bare escape, as we will otherwise endlessly return null.
			if (b2Index == limit) return null

			when (val b2 = buffer[b2Index].toInt()) {
				// 0x4F -> parseSs3(buffer, start, limit)
				// 0x50 -> parseDcs(buffer, start, limit)
				0x58 -> return parseUntilStringTerminator(buffer, start, limit)
				0x5B -> return parseCsi(buffer, start, limit)
				// 0x5D -> TODO("Unhandled event")
				// 0x5E -> TODO("Unhandled event")
				// 0x5F -> parseApc(buffer, start, limit)
				// else -> CodepointEvent(b2, alt = true)
				else -> return TODO("Unhandled event")
			}
		} else {
			when (b1) {
				else -> return TODO("Unhandled event")
			}
		}
	}

	private fun parseCsi(buffer: ByteArray, start: Int, limit: Int): Event? {
		val end = buffer.indexOfFirstOrElse(
			// Skip leading 0x1B5B.
			start = start + 2,
			end = limit,
			predicate = { it.toInt() in 0x40..0xFF },
			orElse = { return null },
		)
		when (val final = buffer[end]) {
			else -> {
				offset = end
				return UnknownEvent(
					context = "CSI with unknown final byte",
					bytes = buffer.copyOfRange(start, end),
				)
			}
		}
	}

	private fun parseUntilStringTerminator(
		buffer: ByteArray,
		start: Int,
		limit: Int,
		handler: (stIndex: Int) -> Event? = { null },
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
				val end = slashIndex + 2
				offset = end
				return handler(escIndex)
					?: UnknownEvent(
						context = "Unsupported string sequence",
						bytes = buffer.copyOfRange(searchFrom, end),
					)
			}
			searchFrom = slashIndex
		}
	}
}
