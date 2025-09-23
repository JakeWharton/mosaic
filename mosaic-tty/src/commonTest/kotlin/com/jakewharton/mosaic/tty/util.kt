package com.jakewharton.mosaic.tty

import assertk.assertThat
import assertk.assertions.isEqualTo

fun Tty.write(data: String) {
	val bytes = data.encodeToByteArray()
	val written = write(bytes, 0, bytes.size)
	assertThat(written).isEqualTo(bytes.size)
}

fun TestTty.write(data: String) {
	val bytes = data.encodeToByteArray()
	val written = write(bytes, 0, bytes.size)
	assertThat(written).isEqualTo(bytes.size)
}

fun Tty.read(count: Int): String {
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
