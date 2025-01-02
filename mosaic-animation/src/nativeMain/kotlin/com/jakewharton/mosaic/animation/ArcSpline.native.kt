package com.jakewharton.mosaic.animation

import kotlin.math.PI

@Suppress("NOTHING_TO_INLINE")
internal actual inline fun toRadians(value: Double): Double {
	return value * (PI / 180.0)
}

@Suppress("NOTHING_TO_INLINE")
internal actual inline fun binarySearch(array: FloatArray, position: Float): Int {
	return array.indexOfFirst { it == position }
}
