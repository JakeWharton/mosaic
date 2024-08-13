package com.example

class Utf8ParserDebugger {
	fun oneByte(b1: Int) {}
	fun twoBytes(b1: Int, b2: Int) {}
	fun threeBytes(b1: Int, b2: Int, b3: Int) {}
	fun fourBytes(b1: Int, b2: Int, b3: Int, b4: Int) {}
}

data class Utf8Codepoint(val codepoint: Int, val bytesConsumed: Int)

fun parseCodepoint(
	buf: UByteArray,
	start: Int,
	limit: Int,
): Utf8Codepoint? = whevs(buf, start, limit, null)

fun debugCodepoint(
	buf: UByteArray,
	start: Int,
	limit: Int,
	debugger: Utf8ParserDebugger,
) = whevs(buf, start, limit, debugger)

private inline fun whevs(
	buf: UByteArray,
	start: Int,
	limit: Int,
	debugger: Utf8ParserDebugger?
): Utf8Codepoint? {
	val b1 = buf[start].toInt()

	val bytesConsumed: Int
	val codepoint: Int
	when {
		b1 and 0b10000000 == 0 -> {
			bytesConsumed = 1
			codepoint = b1
			debugger?.oneByte(b1)
		}
		b1 and 0b11100000 == 0b11000000 -> {
			if (start + 1 >= limit) return null
			bytesConsumed = 2
			val b2 = buf[start + 1].toInt()
			debugger?.twoBytes(b1, b2)
			codepoint = b1.and(0b00011111).shl(6) or
				b2.and(0b00111111)
		}
		b1 and 0b11110000 == 0b11100000 -> {
			if (start + 2 >= limit) return null
			bytesConsumed = 3
			val b2 = buf[start + 1].toInt()
			val b3 = buf[start + 2].toInt()
			debugger?.threeBytes(b1, b2, b3)
			codepoint = b1.and(0b00001111).shl(12) or
				b2.and(0b00111111).shl(6) or
				b3.and(0b00111111)
		}
		b1 and 0b11111000 == 0b11110000 -> {
			if (start + 3 >= limit) return null
			bytesConsumed = 4
			val b2 = buf[start + 1].toInt()
			val b3 = buf[start + 2].toInt()
			val b4 = buf[start + 3].toInt()
			debugger?.fourBytes(b1, b2, b3, b4)
			codepoint = b1.and(0b00000111).shl(18) or
				b2.and(0b00111111).shl(12) or
				b3.and(0b00111111).shl(6) or
				b4.and(0b00111111)
		}
		else -> TODO("Invalid UTF-8")
	}
	return Utf8Codepoint(codepoint, bytesConsumed)
}
