package com.jakewharton.mosaic.tty.terminal

import kotlin.contracts.InvocationKind.EXACTLY_ONCE
import kotlin.contracts.contract

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

internal inline fun ByteArray.parseIntDigits(start: Int, end: Int, orElse: () -> Int): Int {
	error@ do {
		if (end > start) {
			var value = 0
			for (i in start until end) {
				value *= 10

				val digit = this[i].toInt()
				if (digit !in '0'.code..'9'.code) break@error
				// '0' is 0b110000, so the low 4 bits give us the digit value.
				value += digit and 0b1111
			}
			return value
		}
	} while (false)

	return orElse()
}

internal inline fun ByteArray.parseHexDigits(start: Int, end: Int, orElse: () -> Int): Int {
	error@ do {
		val size = end - start
		// Negative or odd size is invalid.
		if ((size and 0x80000001.toInt()) == 0) {
			var value = 0
			for (i in start until end) {
				value = value shl 4

				val digit = this[i].toInt()
				if (digit in '0'.code..'9'.code) {
					// '0' is 0b110000, so the low 4 bits give us the digit value.
					// We can do a bitwise OR because we know these bits are empty from the shift above.
					value = value or (digit and 0b1111)
				} else if (digit in 'a'.code..'f'.code) {
					value += digit - 'a'.code + 10
				} else if (digit in 'A'.code..'F'.code) {
					value += digit - 'A'.code + 10
				} else {
					break@error
				}
			}
			return value
		}
	} while (false)

	return orElse()
}

internal inline fun ByteArray.parseHexString(start: Int, end: Int, orElse: () -> String): String {
	error@ do {
		val size = end - start
		// Negative or odd size is invalid.
		if ((size and 0x80000001.toInt()) == 0) {
			return buildString(size / 2) {
				for (i in start until end step 2) {
					val digit1 = this@parseHexString[i].toInt()
					val digit2 = this@parseHexString[i + 1].toInt()

					val value1 = if (digit1 in '0'.code..'9'.code) {
						// '0' is 0b110000, so the low 4 bits give us the digit value.
						digit1 and 0b1111
					} else if (digit1 in 'a'.code..'f'.code) {
						digit1 - 'a'.code + 10
					} else if (digit1 in 'A'.code..'F'.code) {
						digit1 - 'A'.code + 10
					} else {
						break@error
					}
					val value2 = if (digit2 in '0'.code..'9'.code) {
						// '0' is 0b110000, so the low 4 bits give us the digit value.
						digit2 and 0b1111
					} else if (digit2 in 'a'.code..'f'.code) {
						digit2 - 'a'.code + 10
					} else if (digit2 in 'A'.code..'F'.code) {
						digit2 - 'A'.code + 10
					} else {
						break@error
					}

					// We can do a bitwise OR because we know each value is at most 0b1111.
					append(((value1 shl 4) or value2).toChar())
				}
			}
		}
	} while (false)

	return orElse()
}

internal inline fun ByteArray.parseUtf8(
	start: Int,
	limit: Int,
	onUnderflow: () -> Nothing,
	onSuccess: (nextIndex: Int) -> Unit,
	onError: () -> Nothing,
): Int {
	contract {
		callsInPlace(onSuccess, EXACTLY_ONCE)
	}

	if (start == limit) onUnderflow()
	val b1 = this[start].toInt() and 0xFF
	val b2Index = start + 1

	val codepoint: Int
	val nextIndex: Int
	when {
		b1 < 0x80 -> {
			nextIndex = b2Index
			codepoint = b1
		}

		b1 in 0xC0..0xDF -> {
			if (b2Index == limit) onUnderflow()
			val b2 = this[b2Index].toInt() and 0xFF
			if (b2 and 0b11000000 != 0b10000000) onError()
			nextIndex = start + 2
			codepoint = b1.and(0b00011111).shl(6) or
				b2.and(0b00111111)
			if (codepoint < 0x80) onError()
		}

		b1 in 0xE0..0xEF -> {
			val b3Index = start + 2
			if (b3Index >= limit) onUnderflow()
			val b2 = this[b2Index].toInt() and 0xFF
			val b3 = this[b3Index].toInt() and 0xFF
			if (b2 and 0b11000000 != 0b10000000) onError()
			if (b3 and 0b11000000 != 0b10000000) onError()
			nextIndex = start + 3
			codepoint = b1.and(0b00001111).shl(12) or
				b2.and(0b00111111).shl(6) or
				b3.and(0b00111111)
			if (codepoint < 0x800) onError()
			if (codepoint in 0xD800..0xDFFF) onError()
		}

		b1 in 0xF0..0xF7 -> {
			val b4Index = start + 3
			if (b4Index >= limit) onUnderflow()
			val b2 = this[b2Index].toInt() and 0xFF
			val b3 = this[start + 2].toInt() and 0xFF
			val b4 = this[b4Index].toInt() and 0xFF
			if (b2 and 0b11000000 != 0b10000000) onError()
			if (b3 and 0b11000000 != 0b10000000) onError()
			if (b4 and 0b11000000 != 0b10000000) onError()
			nextIndex = start + 4
			codepoint = b1.and(0b00000111).shl(18) or
				b2.and(0b00111111).shl(12) or
				b3.and(0b00111111).shl(6) or
				b4.and(0b00111111)
			if (codepoint < 0x10000) onError()
			if (codepoint > 0x10FFFF) onError()
		}

		else -> onError()
	}
	onSuccess(nextIndex)
	return codepoint
}
