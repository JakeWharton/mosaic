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

internal inline fun ByteArray.indexOfOrElse(
	value: Byte,
	start: Int,
	end: Int,
	orElse: () -> Int,
): Int {
	return indexOfFirstOrElse(start, end, { it == value }, orElse)
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

@Deprecated("Use overload with orElse")
internal fun ByteArray.parseIntDigits(start: Int, end: Int): Int {
	return parseIntDigits(start, end, orElse = { -1 })
}

internal inline fun ByteArray.parseIntDigits(start: Int, end: Int, orElse: () -> Int): Int {
	error@ do {
		if (end > start) {
			var value = 0
			for (i in start until end) {
				val digit = this[i].toInt()
				if (digit !in '0'.code..'9'.code) break@error

				value *= 10
				value += digit - '0'.code
			}
			return value
		}
	} while (false)

	return orElse()
}
