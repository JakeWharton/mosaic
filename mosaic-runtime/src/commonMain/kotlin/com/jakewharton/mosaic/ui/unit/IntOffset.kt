@file:Suppress("NOTHING_TO_INLINE")

package com.jakewharton.mosaic.ui.unit

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import kotlin.jvm.JvmInline
import kotlin.math.roundToInt

/**
 * Constructs a [IntOffset] from [x] and [y] position [Int] values.
 */
@Stable
public fun IntOffset(x: Int, y: Int): IntOffset =
	IntOffset(packInts(x, y))

/**
 * A two-dimensional position using [Int] cells for units
 */
@Immutable
@JvmInline
public value class IntOffset internal constructor(@PublishedApi internal val packedValue: Long) {

	/**
	 * The horizontal aspect of the position in [Int] cells.
	 */
	@Stable
	public val x: Int
		get() = unpackInt1(packedValue)

	/**
	 * The vertical aspect of the position in [Int] cells.
	 */
	@Stable
	public val y: Int
		get() = unpackInt2(packedValue)

	@Stable
	public operator fun component1(): Int = x

	@Stable
	public operator fun component2(): Int = y

	/**
	 * Returns a copy of this IntOffset instance optionally overriding the
	 * x or y parameter
	 */
	public fun copy(x: Int = this.x, y: Int = this.y): IntOffset = IntOffset(x, y)

	/**
	 * Subtract a [IntOffset] from another one.
	 */
	@Stable
	public inline operator fun minus(other: IntOffset): IntOffset =
		IntOffset(x - other.x, y - other.y)

	/**
	 * Add a [IntOffset] to another one.
	 */
	@Stable
	public inline operator fun plus(other: IntOffset): IntOffset =
		IntOffset(x + other.x, y + other.y)

	/**
	 * Returns a new [IntOffset] representing the negation of this point.
	 */
	@Stable
	public inline operator fun unaryMinus(): IntOffset = IntOffset(-x, -y)

	/**
	 * Multiplication operator.
	 *
	 * Returns an IntOffset whose coordinates are the coordinates of the
	 * left-hand-side operand (an IntOffset) multiplied by the scalar
	 * right-hand-side operand (a Float). The result is rounded to the nearest integer.
	 */
	@Stable
	public operator fun times(operand: Float): IntOffset = IntOffset(
		(x * operand).roundToInt(),
		(y * operand).roundToInt(),
	)

	/**
	 * Division operator.
	 *
	 * Returns an IntOffset whose coordinates are the coordinates of the
	 * left-hand-side operand (an IntOffset) divided by the scalar right-hand-side
	 * operand (a Float). The result is rounded to the nearest integer.
	 */
	@Stable
	public operator fun div(operand: Float): IntOffset = IntOffset(
		(x / operand).roundToInt(),
		(y / operand).roundToInt(),
	)

	/**
	 * Modulo (remainder) operator.
	 *
	 * Returns an IntOffset whose coordinates are the remainder of dividing the
	 * coordinates of the left-hand-side operand (an IntOffset) by the scalar
	 * right-hand-side operand (an Int).
	 */
	@Stable
	public operator fun rem(operand: Int): IntOffset = IntOffset(x % operand, y % operand)

	@Stable
	override fun toString(): String = "($x, $y)"

	public companion object {
		public val Zero: IntOffset = IntOffset(0, 0)
	}
}
