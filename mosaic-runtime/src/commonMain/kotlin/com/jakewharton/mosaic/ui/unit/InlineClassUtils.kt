@file:Suppress("NOTHING_TO_INLINE")

package com.jakewharton.mosaic.ui.unit

/**
 * Packs two Int values into one Long value for use in inline classes.
 */
internal inline fun packInts(val1: Int, val2: Int): Long {
	return val1.toLong().shl(32) or (val2.toLong() and 0xFFFFFFFF)
}

/**
 * Unpacks the first Int value in [packInts] from its returned ULong.
 */
internal inline fun unpackInt1(value: Long): Int {
	return value.shr(32).toInt()
}

/**
 * Unpacks the second Int value in [packInts] from its returned ULong.
 */
internal inline fun unpackInt2(value: Long): Int {
	return value.and(0xFFFFFFFF).toInt()
}
