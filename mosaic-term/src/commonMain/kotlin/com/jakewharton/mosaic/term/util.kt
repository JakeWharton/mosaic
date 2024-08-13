package com.jakewharton.mosaic.term

internal fun UByteArray.parseIntDigits(start: Int, end: Int): Int {
	var value = 0
	for (i in start until end) {
		value *= 10
		value += this[i].toInt() - '0'.code
	}
	return value
}

internal inline fun Int.orElseIndex(value: Int) = if (this < 0) value else this
