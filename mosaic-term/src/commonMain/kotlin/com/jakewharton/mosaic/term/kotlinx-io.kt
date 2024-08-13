package com.jakewharton.mosaic.term

import kotlinx.io.InternalIoApi
import kotlinx.io.Sink
import kotlinx.io.Source
import kotlinx.io.writeDecimalLong

// TODO file an issue
internal inline fun Sink.writeDecimalInt(value: Int) {
	writeDecimalLong(value.toLong())
}

@OptIn(InternalIoApi::class) // https://github.com/Kotlin/kotlinx-io/issues/363
internal inline fun Source.indexOfFirst(
	start: Long = 0,
	end: Long = Long.MAX_VALUE,
	predicate: (Byte) -> Boolean
): Long {
	val buffer = buffer
	// TODO annoying to test for < Long.MAX_VALUE when no explicit end
	for (index in start until end) {
		if (!request(index)) {
			break
		}
		if (predicate(buffer[index])) {
			return index
		}
	}
	return -1
}
