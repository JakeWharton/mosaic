package com.jakewharton.mosaic.tty

import java.io.EOFException
import java.io.IOException
import java.io.OutputStream

internal class TtyOutputStream(
	private val tty: Tty,
	private val error: Boolean,
) : OutputStream() {
	private var closed = false

	override fun write(b: Int) {
		val buffer = byteArrayOf(b.toByte())
		write(buffer, 0, 1)
	}

	override fun write(b: ByteArray, off: Int, len: Int) {
		var off = off
		if (!closed) {
			while (true) {
				val written = if (!error) {
					tty.writeOutput(b, off, len)
				} else {
					tty.writeError(b, off, len)
				}
				if (written == -1) break
				off += written
				if (off == len) return
			}
			throw EOFException()
		}
		throw IOException("closed")
	}

	override fun close() {
		closed = true
	}
}
