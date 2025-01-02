@file:Suppress("NOTHING_TO_INLINE")

package com.jakewharton.mosaic.ui

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import kotlin.jvm.JvmInline

@Immutable
@JvmInline
public value class Color internal constructor(
	@PublishedApi
	internal val value: Int,
) {
	override fun toString(): String = "Color($redInt, $greenInt, $blueInt)"

	/**
	 * Returns the value of the red component as integer between 0 and 255.
	 *
	 * @see redFloat
	 *
	 * @see blueInt
	 * @see greenInt
	 */
	@Stable
	internal val redInt: Int
		get() = ((value shr 16) and 0xFF)

	/**
	 * Returns the value of the red component as float between 0.0 and 1.0.
	 *
	 * @see redInt
	 *
	 * @see blueFloat
	 * @see greenFloat
	 */
	@PublishedApi
	@Stable
	internal val redFloat: Float
		get() = redInt / 255.0f

	/**
	 * Returns the value of the green component as integer between 0 and 255.
	 *
	 * @see greenFloat
	 *
	 * @see redInt
	 * @see blueInt
	 */
	@Stable
	internal val greenInt: Int
		get() = ((value shr 8) and 0xFF)

	/**
	 * Returns the value of the green component as float between 0.0 and 1.0.
	 *
	 * @see greenInt
	 *
	 * @see redFloat
	 * @see blueFloat
	 */
	@PublishedApi
	@Stable
	internal val greenFloat: Float
		get() = greenInt / 255.0f

	/**
	 * Returns the value of the blue component as integer between 0 and 255.
	 *
	 * @see blueFloat
	 *
	 * @see redInt
	 * @see greenInt
	 */
	@Stable
	internal val blueInt: Int
		inline get() = (value and 0xFF)

	/**
	 * Returns the value of the blue component as float between 0.0 and 1.0.
	 *
	 * @see blueInt
	 *
	 * @see redFloat
	 * @see greenFloat
	 */
	@PublishedApi
	@Stable
	internal val blueFloat: Float
		get() = blueInt / 255.0f

	@Stable
	public inline operator fun component1(): Float = redFloat

	@Stable
	public inline operator fun component2(): Float = greenFloat

	@Stable
	public inline operator fun component3(): Float = blueFloat

	public companion object {
		@Stable
		public val Unspecified: Color = Color(UnspecifiedColor)

		@Stable
		public val Black: Color = Color(0x000000)

		@Stable
		public val Red: Color = Color(0xFF0000)

		@Stable
		public val Green: Color = Color(0x00FF00)

		@Stable
		public val Blue: Color = Color(0x0000FF)

		@Stable
		public val Yellow: Color = Color(0xFFFF00)

		@Stable
		public val Magenta: Color = Color(0xFF00FF)

		@Stable
		public val Cyan: Color = Color(0x00FFFF)

		@Stable
		public val White: Color = Color(0xFFFFFF)
	}
}

@PublishedApi
internal const val UnspecifiedColor: Int = Int.MIN_VALUE

/**
 * Creates a new [Color] instance from an RGB color components.
 *
 * @param red The red component of the color, between 0.0 and 1.0.
 * @param green The green component of the color, between 0.0 and 1.0.
 * @param blue The blue component of the color, between 0.0 and 1.0.
 *
 * @return A non-null instance of [Color]
 */
@Stable
public fun Color(red: Float, green: Float, blue: Float): Color {
	require(red in 0.0f..1.0f)
	require(green in 0.0f..1.0f)
	require(blue in 0.0f..1.0f)

	return Color(
		red = (red * 255.0f + 0.5f).toInt(),
		green = (green * 255.0f + 0.5f).toInt(),
		blue = (blue * 255.0f + 0.5f).toInt(),
	)
}

/**
 * Creates a new [Color] instance from an RGB color components.
 *
 * @param red The red component of the color, between 0 and 255.
 * @param green The green component of the color, between 0 and 255.
 * @param blue The blue component of the color, between 0 and 255.
 *
 * @return A non-null instance of [Color]
 */
@Stable
public fun Color(red: Int, green: Int, blue: Int): Color {
	require(red in 0..0xFF)
	require(green in 0..0xFF)
	require(blue in 0..0xFF)

	val rgb =
		((red and 0xFF) shl 16) or
			((green and 0xFF) shl 8) or
			(blue and 0xFF)

	return Color(rgb)
}

/**
 * `false` when this is [Color.Unspecified].
 */
@Stable
public inline val Color.isSpecifiedColor: Boolean get() = value != UnspecifiedColor

/**
 * `true` when this is [Color.Unspecified].
 */
@Stable
public inline val Color.isUnspecifiedColor: Boolean get() = value == UnspecifiedColor

/**
 * If this color [isSpecifiedColor] then this is returned, otherwise [block] is executed and its result
 * is returned.
 */
public inline fun Color.takeOrElse(block: () -> Color): Color =
	if (isSpecifiedColor) this else block()
