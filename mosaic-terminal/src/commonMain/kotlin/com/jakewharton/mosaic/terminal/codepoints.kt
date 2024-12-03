package com.jakewharton.mosaic.terminal

internal fun Appendable.appendCodepoint(codepoint: Int) {
	if (codepoint < 0x10000) {
		append(codepoint.toChar())
	} else {
		append(Char.MIN_HIGH_SURROGATE + ((codepoint - 0x10000) ushr 10))
		append(Char.MIN_LOW_SURROGATE + (codepoint and 0x3ff))
	}
}
