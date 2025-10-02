package com.jakewharton.mosaic.tty

fun ByteArray.writeFullyTo(write: (ByteArray, Int, Int) -> Int) {
	var offset = 0
	var count = size
	while (count > 0) {
		val written = write(this, offset, count)
		if (written == -1) {
			throw RuntimeException("eof")
		}
		offset += written
		count -= written
	}
}

fun ByteArray.readFully(read: (ByteArray, Int, Int) -> Int): ByteArray = apply {
	var offset = 0
	var count = size
	while (count > 0) {
		val read = read(this, offset, count)
		if (read == -1) {
			throw RuntimeException("eof")
		}
		offset += read
		count -= read
	}
}
