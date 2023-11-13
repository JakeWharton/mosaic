package com.jakewharton.mosaic.ui.unit

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import kotlin.jvm.JvmInline

/**
 * Immutable constraints for measuring layouts, used by [layouts][com.jakewharton.mosaic.ui.Layout]
 * or [layout modifiers][com.jakewharton.mosaic.layout.LayoutModifier] to measure their layout
 * children. The parent chooses the [Constraints] defining a range, in pixels, within which
 * the measured layout should choose a size:
 *
 * - `minWidth` <= `chosenWidth` <= `maxWidth`
 * - `minHeight` <= `chosenHeight` <= `maxHeight`
 *
 * For more details about how layout measurement works, see
 * [com.jakewharton.mosaic.layout.MeasurePolicy] or
 * [com.jakewharton.mosaic.layout.LayoutModifier.measure].
 *
 * A set of [Constraints] can have infinite maxWidth and/or maxHeight. This is a trick often
 * used by parents to ask their children for their preferred size: unbounded constraints force
 * children whose default behavior is to fill the available space (always size to
 * maxWidth/maxHeight) to have an opinion about their preferred size. Most commonly, when measured
 * with unbounded [Constraints], these children will fallback to size themselves to wrap their
 * content, instead of expanding to fill the available space (this is not always true
 * as it depends on the child layout model, but is a common behavior for core layout components).
 *
 * [Constraints] uses a [Long] to represent four values, [minWidth], [minHeight], [maxWidth],
 * and [maxHeight]. The range of the values varies to allow for at most 256K in one dimension.
 * There are four possible maximum ranges, 13 bits/18 bits, and 15 bits/16 bits for either width
 * or height, depending on the needs. For example, a width could range up to 18 bits
 * and the height up to 13 bits. Alternatively, the width could range up to 16 bits and the height
 * up to 15 bits. The height and width requirements can be reversed, with a height of up to 18 bits
 * and width of 13 bits or height of 16 bits and width of 15 bits. Any constraints exceeding
 * this range will fail.
 */
@Immutable
@JvmInline
public value class Constraints(
	@PublishedApi internal val value: Long,
) {
	/**
	 * Indicates how the bits are assigned. One of:
	 * * MinFocusWidth
	 * * MaxFocusWidth
	 * * MinFocusHeight
	 * * MaxFocusHeight
	 */
	private val focusIndex
		get() = (value and FocusMask).toInt()

	/**
	 * The minimum width that the measurement can take, in pixels.
	 */
	public val minWidth: Int
		get() {
			val mask = WidthMask[focusIndex]
			return ((value shr 2).toInt() and mask)
		}

	/**
	 * The maximum width that the measurement can take, in pixels. This will either be
	 * a positive value greater than or equal to [minWidth] or [Constraints.Infinity].
	 */
	public val maxWidth: Int
		get() {
			val mask = WidthMask[focusIndex]
			val width = ((value shr 33).toInt() and mask)
			return if (width == 0) Infinity else width - 1
		}

	/**
	 * The minimum height that the measurement can take, in pixels.
	 */
	public val minHeight: Int
		get() {
			val focus = focusIndex
			val mask = HeightMask[focus]
			val offset = MinHeightOffsets[focus]
			return (value shr offset).toInt() and mask
		}

	/**
	 * The maximum height that the measurement can take, in pixels. This will either be
	 * a positive value greater than or equal to [minHeight] or [Constraints.Infinity].
	 */
	public val maxHeight: Int
		get() {
			val focus = focusIndex
			val mask = HeightMask[focus]
			val offset = MinHeightOffsets[focus] + 31
			val height = (value shr offset).toInt() and mask
			return if (height == 0) Infinity else height - 1
		}

	/**
	 * `false` when [maxWidth] is [Infinity] and `true` if [maxWidth] is a non-[Infinity] value.
	 * @see hasBoundedHeight
	 */
	public val hasBoundedWidth: Boolean
		get() {
			val mask = WidthMask[focusIndex]
			return ((value shr 33).toInt() and mask) != 0
		}

	/**
	 * `false` when [maxHeight] is [Infinity] and `true` if [maxHeight] is a non-[Infinity] value.
	 * @see hasBoundedWidth
	 */
	public val hasBoundedHeight: Boolean
		get() {
			val focus = focusIndex
			val mask = HeightMask[focus]
			val offset = MinHeightOffsets[focus] + 31
			return ((value shr offset).toInt() and mask) != 0
		}

	/**
	 * Whether there is exactly one width value that satisfies the constraints.
	 */
	@Stable
	public val hasFixedWidth: Boolean get() = maxWidth == minWidth

	/**
	 * Whether there is exactly one height value that satisfies the constraints.
	 */
	@Stable
	public val hasFixedHeight: Boolean get() = maxHeight == minHeight

	/**
	 * Whether the area of a component respecting these constraints will definitely be 0.
	 * This is true when at least one of maxWidth and maxHeight are 0.
	 */
	@Stable
	public val isZero: Boolean get() = maxWidth == 0 || maxHeight == 0

	/**
	 * Copies the existing [Constraints], replacing some of [minWidth], [minHeight], [maxWidth],
	 * or [maxHeight] as desired. [minWidth] and [minHeight] must be positive and
	 * [maxWidth] and [maxHeight] must be greater than or equal to [minWidth] and [minHeight],
	 * respectively, or [Infinity].
	 */
	public fun copy(
		minWidth: Int = this.minWidth,
		maxWidth: Int = this.maxWidth,
		minHeight: Int = this.minHeight,
		maxHeight: Int = this.maxHeight,
	): Constraints {
		require(minHeight >= 0 && minWidth >= 0) {
			"minHeight($minHeight) and minWidth($minWidth) must be >= 0"
		}
		require(maxWidth >= minWidth || maxWidth == Infinity) {
			"maxWidth($maxWidth) must be >= minWidth($minWidth)"
		}
		require(maxHeight >= minHeight || maxHeight == Infinity) {
			"maxHeight($maxHeight) must be >= minHeight($minHeight)"
		}
		return createConstraints(minWidth, maxWidth, minHeight, maxHeight)
	}

	override fun toString(): String {
		val maxWidth = maxWidth
		val maxWidthStr = if (maxWidth == Infinity) "Infinity" else maxWidth.toString()
		val maxHeight = maxHeight
		val maxHeightStr = if (maxHeight == Infinity) "Infinity" else maxHeight.toString()
		return "Constraints(minWidth = $minWidth, maxWidth = $maxWidthStr, " +
			"minHeight = $minHeight, maxHeight = $maxHeightStr)"
	}

	public companion object {
		/**
		 * A value that [maxWidth] or [maxHeight] will be set to when the constraint should
		 * be considered infinite. [hasBoundedWidth] or [hasBoundedHeight] will be
		 * `false` when [maxWidth] or [maxHeight] is [Infinity], respectively.
		 */
		public const val Infinity: Int = Int.MAX_VALUE

		/**
		 * The bit distribution when the focus of the bits should be on the width, but only
		 * a minimal difference in focus.
		 *
		 * 16 bits assigned to width, 15 bits assigned to height.
		 */
		private const val MinFocusWidth = 0x00L

		/**
		 * The bit distribution when the focus of the bits should be on the width, and a
		 * maximal number of bits assigned to the width.
		 *
		 * 18 bits assigned to width, 13 bits assigned to height.
		 */
		private const val MaxFocusWidth = 0x01L

		/**
		 * The bit distribution when the focus of the bits should be on the height, but only
		 * a minimal difference in focus.
		 *
		 * 15 bits assigned to width, 16 bits assigned to height.
		 */
		private const val MinFocusHeight = 0x02L

		/**
		 * The bit distribution when the focus of the bits should be on the height, and a
		 * a maximal number of bits assigned to the height.
		 *
		 * 13 bits assigned to width, 18 bits assigned to height.
		 */
		private const val MaxFocusHeight = 0x03L

		/**
		 * The mask to retrieve the focus ([MinFocusWidth], [MaxFocusWidth],
		 * [MinFocusHeight], [MaxFocusHeight]).
		 */
		private const val FocusMask = 0x03L

		/**
		 * The number of bits used for the focused dimension when there is minimal focus.
		 */
		private const val MinFocusBits = 16

		/**
		 * The mask to use for the focused dimension when there is minimal focus.
		 */
		private const val MinFocusMask = 0xFFFF // 64K (16 bits)

		/**
		 * The number of bits used for the non-focused dimension when there is minimal focus.
		 */
		private const val MinNonFocusBits = 15

		/**
		 * The mask to use for the non-focused dimension when there is minimal focus.
		 */
		private const val MinNonFocusMask = 0x7FFF // 32K (15 bits)

		/**
		 * The number of bits to use for the focused dimension when there is maximal focus.
		 */
		private const val MaxFocusBits = 18

		/**
		 * The mask to use for the focused dimension when there is maximal focus.
		 */
		private const val MaxFocusMask = 0x3FFFF // 256K (18 bits)

		/**
		 * The number of bits to use for the non-focused dimension when there is maximal focus.
		 */
		private const val MaxNonFocusBits = 13

		/**
		 * The mask to use for the non-focused dimension when there is maximal focus.
		 */
		private const val MaxNonFocusMask = 0x1FFF // 8K (13 bits)

		/**
		 * Minimum Height shift offsets into Long value, indexed by FocusMask
		 * Max offsets are these + 31
		 * Width offsets are always either 2 (min) or 33 (max)
		 */
		private val MinHeightOffsets = intArrayOf(
			// MinFocusWidth: 2 + 16
			18,
			// MaxFocusWidth: 2 + 18
			20,
			// MinFocusHeight: 2 + 15
			17,
			// MaxFocusHeight: 2 + 13
			15,
		)

		/**
		 * The mask to use for both minimum and maximum width.
		 */
		private val WidthMask = intArrayOf(
			// MinFocusWidth (16 bits)
			MinFocusMask,
			// MaxFocusWidth (18 bits)
			MaxFocusMask,
			// MinFocusHeight (15 bits)
			MinNonFocusMask,
			// MaxFocusHeight (13 bits)
			MaxNonFocusMask,
		)

		/**
		 * The mask to use for both minimum and maximum height.
		 */
		private val HeightMask = intArrayOf(
			// MinFocusWidth (15 bits)
			MinNonFocusMask,
			// MaxFocusWidth (13 bits)
			MaxNonFocusMask,
			// MinFocusHeight (16 bits)
			MinFocusMask,
			// MaxFocusHeight (18 bits)
			MaxFocusMask,
		)

		/**
		 * Creates constraints for fixed size in both dimensions.
		 */
		@Stable
		public fun fixed(
			width: Int,
			height: Int,
		): Constraints {
			require(width >= 0 && height >= 0) {
				"width($width) and height($height) must be >= 0"
			}
			return createConstraints(width, width, height, height)
		}

		/**
		 * Creates constraints for fixed width and unspecified height.
		 */
		@Stable
		public fun fixedWidth(
			width: Int,
		): Constraints {
			require(width >= 0) {
				"width($width) must be >= 0"
			}
			return createConstraints(
				minWidth = width,
				maxWidth = width,
				minHeight = 0,
				maxHeight = Infinity,
			)
		}

		/**
		 * Creates constraints for fixed height and unspecified width.
		 */
		@Stable
		public fun fixedHeight(
			height: Int,
		): Constraints {
			require(height >= 0) {
				"height($height) must be >= 0"
			}
			return createConstraints(
				minWidth = 0,
				maxWidth = Infinity,
				minHeight = height,
				maxHeight = height,
			)
		}

		/**
		 * Creates a [Constraints], only checking that the values fit in the packed Long.
		 */
		internal fun createConstraints(
			minWidth: Int,
			maxWidth: Int,
			minHeight: Int,
			maxHeight: Int,
		): Constraints {
			val heightVal = if (maxHeight == Infinity) minHeight else maxHeight
			val heightBits = bitsNeedForSize(heightVal)

			val widthVal = if (maxWidth == Infinity) minWidth else maxWidth
			val widthBits = bitsNeedForSize(widthVal)

			if (widthBits + heightBits > 31) {
				throw IllegalArgumentException(
					"Can't represent a width of $widthVal and height " +
						"of $heightVal in Constraints",
				)
			}

			val focus = when (widthBits) {
				MinNonFocusBits -> MinFocusHeight
				MinFocusBits -> MinFocusWidth
				MaxNonFocusBits -> MaxFocusHeight
				MaxFocusBits -> MaxFocusWidth
				else -> throw IllegalStateException("Should only have the provided constants.")
			}

			val maxWidthValue = if (maxWidth == Infinity) 0 else maxWidth + 1
			val maxHeightValue = if (maxHeight == Infinity) 0 else maxHeight + 1

			val minHeightOffset = MinHeightOffsets[focus.toInt()]
			val maxHeightOffset = minHeightOffset + 31

			val value = focus or
				(minWidth.toLong() shl 2) or
				(maxWidthValue.toLong() shl 33) or
				(minHeight.toLong() shl minHeightOffset) or
				(maxHeightValue.toLong() shl maxHeightOffset)
			return Constraints(value)
		}

		private fun bitsNeedForSize(size: Int): Int {
			return when {
				size < MaxNonFocusMask -> MaxNonFocusBits
				size < MinNonFocusMask -> MinNonFocusBits
				size < MinFocusMask -> MinFocusBits
				size < MaxFocusMask -> MaxFocusBits
				else -> throw IllegalArgumentException(
					"Can't represent a size of $size in " +
						"Constraints",
				)
			}
		}
	}
}

/**
 * Create a [Constraints]. [minWidth] and [minHeight] must be positive and
 * [maxWidth] and [maxHeight] must be greater than or equal to [minWidth] and [minHeight],
 * respectively, or [Infinity][Constraints.Infinity].
 */
@Stable
public fun Constraints(
	minWidth: Int = 0,
	maxWidth: Int = Constraints.Infinity,
	minHeight: Int = 0,
	maxHeight: Int = Constraints.Infinity,
): Constraints {
	require(maxWidth >= minWidth) {
		"maxWidth($maxWidth) must be >= than minWidth($minWidth)"
	}
	require(maxHeight >= minHeight) {
		"maxHeight($maxHeight) must be >= than minHeight($minHeight)"
	}
	require(minWidth >= 0 && minHeight >= 0) {
		"minWidth($minWidth) and minHeight($minHeight) must be >= 0"
	}
	return Constraints.createConstraints(minWidth, maxWidth, minHeight, maxHeight)
}

/**
 * Takes [otherConstraints] and returns the result of coercing them in the current constraints.
 * Note this means that any size satisfying the resulting constraints will satisfy the current
 * constraints, but they might not satisfy the [otherConstraints] when the two set of constraints
 * are disjoint.
 * Examples (showing only width, height works the same):
 * (minWidth=2, maxWidth=10).constrain(minWidth=7, maxWidth=12) -> (minWidth = 7, maxWidth = 10)
 * (minWidth=2, maxWidth=10).constrain(minWidth=11, maxWidth=12) -> (minWidth=10, maxWidth=10)
 * (minWidth=2, maxWidth=10).constrain(minWidth=5, maxWidth=7) -> (minWidth=5, maxWidth=7)
 */
public fun Constraints.constrain(otherConstraints: Constraints): Constraints = Constraints(
	minWidth = otherConstraints.minWidth.coerceIn(minWidth, maxWidth),
	maxWidth = otherConstraints.maxWidth.coerceIn(minWidth, maxWidth),
	minHeight = otherConstraints.minHeight.coerceIn(minHeight, maxHeight),
	maxHeight = otherConstraints.maxHeight.coerceIn(minHeight, maxHeight),
)

/**
 * Takes a size and returns the closest size to it that satisfies the constraints.
 */
@Stable
public fun Constraints.constrain(size: IntSize): IntSize = IntSize(
	width = size.width.coerceIn(minWidth, maxWidth),
	height = size.height.coerceIn(minHeight, maxHeight),
)

/**
 * Takes a width and returns the closest size to it that satisfies the constraints.
 */
@Stable
public fun Constraints.constrainWidth(width: Int): Int = width.coerceIn(minWidth, maxWidth)

/**
 * Takes a height and returns the closest size to it that satisfies the constraints.
 */
@Stable
public fun Constraints.constrainHeight(height: Int): Int = height.coerceIn(minHeight, maxHeight)

/**
 * Takes a size and returns whether it satisfies the current constraints.
 */
@Stable
public fun Constraints.isSatisfiedBy(size: IntSize): Boolean {
	return size.width in minWidth..maxWidth && size.height in minHeight..maxHeight
}

/**
 * Returns the Constraints obtained by offsetting the current instance with the given values.
 */
@Stable
public fun Constraints.offset(horizontal: Int = 0, vertical: Int = 0): Constraints = Constraints(
	(minWidth + horizontal).coerceAtLeast(0),
	addMaxWithMinimum(maxWidth, horizontal),
	(minHeight + vertical).coerceAtLeast(0),
	addMaxWithMinimum(maxHeight, vertical),
)

private fun addMaxWithMinimum(max: Int, value: Int): Int {
	return if (max == Constraints.Infinity) {
		max
	} else {
		(max + value).coerceAtLeast(0)
	}
}
