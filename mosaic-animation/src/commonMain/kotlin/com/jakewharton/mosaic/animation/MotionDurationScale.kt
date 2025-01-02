package com.jakewharton.mosaic.animation

import androidx.compose.runtime.Stable
import kotlin.coroutines.CoroutineContext

/**
 * Provides a duration scale for motion such as animations. When the duration [scaleFactor] is 0,
 * the motion will end in the next frame callback. Otherwise, the duration [scaleFactor] will be
 * used as a multiplier to scale the duration of the motion. The larger the scale, the longer the
 * motion will take to finish, and therefore the slower it will be perceived.
 *
 * ## Testing
 *
 * To control the motion duration scale in tests, create an implementation of this interface and
 * pass it to the `effectContext` parameter either where you call `runComposeUiTest` or where you
 * create your test rule.
 */
@Stable
public interface MotionDurationScale : CoroutineContext.Element {
	/**
	 * Defines the multiplier for the duration of the motion. This value should be non-negative.
	 *
	 * A [scaleFactor] of 1.0f would play the motion in real time. 0f would cause motion to finish
	 * in the next frame callback. Larger [scaleFactor] will result in longer durations for the
	 * motion/animation (i.e. slower animation). For example, a [scaleFactor] of 10f would cause an
	 * animation with a duration of 100ms to finish in 1000ms.
	 */
	public val scaleFactor: Float

	override val key: CoroutineContext.Key<*>
		get() = Key

	public companion object Key : CoroutineContext.Key<MotionDurationScale>
}
