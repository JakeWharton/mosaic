@file:Suppress("NOTHING_TO_INLINE")

package com.jakewharton.mosaic.animation.internal

/** Packs two Float values into one Long value for use in inline classes. */
internal inline fun packFloats(val1: Float, val2: Float): Long {
	val v1 = val1.toRawBits().toLong()
	val v2 = val2.toRawBits().toLong()
	return (v1 shl 32) or (v2 and 0xFFFFFFFF)
}

/** Unpacks the first Float value in [packFloats] from its returned Long. */
internal inline fun unpackFloat1(value: Long): Float {
	return Float.fromBits((value shr 32).toInt())
}

/** Unpacks the second Float value in [packFloats] from its returned Long. */
internal inline fun unpackFloat2(value: Long): Float {
	return Float.fromBits((value and 0xFFFFFFFF).toInt())
}
