package com.jakewharton.mosaic.ui.unit

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import kotlin.jvm.JvmInline

/**
 * Constructs an [IntSize] from width and height [Int] values.
 */
@Stable
public fun IntSize(width: Int, height: Int): IntSize = IntSize(packInts(width, height))

/**
 * A two-dimensional size class used for measuring in [Int] cells.
 */
@Immutable
@JvmInline
public value class IntSize internal constructor(@PublishedApi internal val packedValue: Long) {

	/**
	 * The horizontal aspect of the size in [Int] cells.
	 */
	@Stable
	public val width: Int
		get() = unpackInt1(packedValue)

	/**
	 * The vertical aspect of the size in [Int] cells.
	 */
	@Stable
	public val height: Int
		get() = unpackInt2(packedValue)

	@Stable
	public inline operator fun component1(): Int = width

	@Stable
	public inline operator fun component2(): Int = height

	/**
	 * Returns an IntSize scaled by multiplying [width] and [height] by [other]
	 */
	@Stable
	public operator fun times(other: Int): IntSize =
		IntSize(width = width * other, height = height * other)

	/**
	 * Returns an IntSize scaled by dividing [width] and [height] by [other]
	 */
	@Stable
	public operator fun div(other: Int): IntSize =
		IntSize(width = width / other, height = height / other)

	@Stable
	override fun toString(): String = "$width x $height"

	public companion object {
		/**
		 * IntSize with a zero (0) width and height.
		 */
		public val Zero: IntSize = IntSize(0L)
	}
}
