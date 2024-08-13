package com.jakewharton.mosaic.term

// https://youtrack.jetbrains.com/issue/KT-7067
internal fun UByteArray.indexOf(value: UByte, start: Int, end: Int): Int {
	return indexOfFirst(start, end) { it == value }
}

// https://youtrack.jetbrains.com/issue/KT-7067
internal inline fun UByteArray.indexOfFirst(start: Int, end: Int, predicate: (UByte) -> Boolean): Int {
	for (i in start until end) {
		if (predicate(this[i])) {
			return i
		}
	}
	return -1
}
