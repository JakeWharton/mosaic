package com.jakewharton.mosaic.tty

import java.io.IOException
import java.io.InputStream

internal class TtyInputStream(
	private val tty: Tty,
) : InputStream() {
	private var closed = false

	override fun read(): Int {
		val buffer = ByteArray(1)
		while (true) {
			val read = read(buffer, 0, 1)
			if (read == -1) return -1
			if (read == 1) return buffer[0].toInt()
		}
	}

	override fun read(b: ByteArray, off: Int, len: Int): Int {
		if (!closed) {
			return tty.readInput(b, off, len)
		}
		throw IOException("closed")
	}

	override fun close() {
		closed = true
	}
}
