package com.jakewharton.mosaic.animation

import com.jakewharton.mosaic.ui.unit.IntOffset
import com.jakewharton.mosaic.ui.unit.IntSize

/**
 * Visibility threshold for [IntOffset]. This defines the amount of value change that is considered
 * to be no longer visible. The animation system uses this to signal to some default [spring]
 * animations to stop when the value is close enough to the target.
 */
public val IntOffset.Companion.VisibilityThreshold: IntOffset
	get() = IntOffset(1, 1)

/**
 * Visibility threshold for [Int]. This defines the amount of value change that is considered to be
 * no longer visible. The animation system uses this to signal to some default [spring] animations
 * to stop when the value is close enough to the target.
 */
public val Int.Companion.VisibilityThreshold: Int
	get() = 1

/**
 * Visibility threshold for [IntSize]. This defines the amount of value change that is considered to
 * be no longer visible. The animation system uses this to signal to some default [spring]
 * animations to stop when the value is close enough to the target.
 */
public val IntSize.Companion.VisibilityThreshold: IntSize
	get() = IntSize(1, 1)

// The floats coming out of this map are fed to APIs that expect objects (generics), so it's
// better to store them as boxed floats here instead of causing unboxing/boxing every time
// the values are read out and forwarded to other APIs
@Suppress("PrimitiveInCollection")
internal val VisibilityThresholdMap: Map<TwoWayConverter<*, *>, Float> =
	mapOf(
		Int.Companion.VectorConverter to 1f,
		IntSize.VectorConverter to 1f,
		IntOffset.VectorConverter to 1f,
		Float.Companion.VectorConverter to 0.01f,
	)
