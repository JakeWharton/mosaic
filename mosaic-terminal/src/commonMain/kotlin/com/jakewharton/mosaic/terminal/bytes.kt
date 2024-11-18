package com.jakewharton.mosaic.terminal

internal fun ByteArray.indexOf(value: Byte, start: Int = 0, end: Int = size): Int {
	return indexOfFirstOrElse(start, end, { it == value })
}

internal inline fun ByteArray.indexOfFirstOrElse(
	start: Int = 0,
	end: Int = size,
	crossinline predicate: (Byte) -> Boolean,
	orElse: () -> Int = { -1 },
): Int {
	for (i in start until end) {
		if (predicate(this[i])) {
			return i
		}
	}
	return orElse()
}
