package com.jakewharton.mosaic.terminal

// TODO https://youtrack.jetbrains.com/issue/KT-7067
internal fun ByteArray.indexOf(value: Byte, start: Int, end: Int): Int {
	return indexOfOrDefault(value, start, end, -1)
}

internal fun ByteArray.indexOfOrDefault(
	value: Byte,
	start: Int,
	end: Int,
	default: Int,
): Int {
	return indexOfFirstOrElse(start, end, { it == value }, { default })
}

internal inline fun ByteArray.indexOfFirstOrElse(
	start: Int,
	end: Int,
	crossinline predicate: (Byte) -> Boolean,
	orElse: () -> Int,
): Int {
	for (i in start until end) {
		if (predicate(this[i])) {
			return i
		}
	}
	return orElse()
}

internal fun ByteArray.parseIntDigits(start: Int, end: Int): Int {
	var value = 0
	for (i in start until end) {
		value *= 10
		value += this[i].toInt() - '0'.code
	}
	return value
}
