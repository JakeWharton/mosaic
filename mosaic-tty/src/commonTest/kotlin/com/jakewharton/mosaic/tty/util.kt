package com.jakewharton.mosaic.tty

import assertk.assertThat
import assertk.assertions.isEqualTo

fun TestTty.writeInput(data: String) {
	val bytes = data.encodeToByteArray()
	val written = writeInput(bytes, 0, bytes.size)
	assertThat(written).isEqualTo(bytes.size)
}

fun Tty.readInput(count: Int): String {
	var offset = 0
	val incoming = ByteArray(1024)
	while (offset < count) {
		val read = readInput(incoming, offset, count)
		if (read == -1) {
			throw RuntimeException("eof")
		}
		offset += read
	}
	return incoming.decodeToString(endIndex = count)
}
