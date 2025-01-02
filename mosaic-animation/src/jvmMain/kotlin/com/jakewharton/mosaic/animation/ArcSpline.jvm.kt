package com.jakewharton.mosaic.animation

@Suppress("NOTHING_TO_INLINE")
internal actual inline fun toRadians(value: Double): Double {
	return Math.toRadians(value)
}

@Suppress("NOTHING_TO_INLINE")
internal actual inline fun binarySearch(array: FloatArray, position: Float): Int {
	return array.binarySearch(position)
}
