package com.jakewharton.mosaic.tty

import assertk.assertThat
import assertk.assertions.isEqualTo

fun Tty.writeFully(data: String) {
	val bytes = data.encodeToByteArray()
	val written = write(bytes, 0, bytes.size)
	assertThat(written).isEqualTo(bytes.size)
}

fun TestTty.writeFully(data: String) {
	val bytes = data.encodeToByteArray()
	val written = write(bytes, 0, bytes.size)
	assertThat(written).isEqualTo(bytes.size)
}

fun Tty.readExactly(count: Int): String {
	var offset = 0
	val incoming = ByteArray(1024)
	while (offset < count) {
		val read = read(incoming, offset, count)
		if (read == -1) {
			throw RuntimeException("eof")
		}
		offset += read
	}
	return incoming.decodeToString(endIndex = count)
}
