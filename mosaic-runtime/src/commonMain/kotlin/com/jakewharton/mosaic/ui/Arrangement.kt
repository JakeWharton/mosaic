package com.jakewharton.mosaic.ui

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import kotlin.math.min
import kotlin.math.roundToInt

/**
 * Used to specify the arrangement of the layout's children in layouts like [Row] or [Column] in
 * the main axis direction (horizontal and vertical, respectively).
 *
 * Below is an illustration of different horizontal arrangements in [Row]s:
 * ![Row arrangements](https://developer.android.com/images/reference/androidx/compose/foundation/layout/row_arrangement_visualization.gif)
 *
 * Different vertical arrangements in [Column]s:
 * ![Column arrangements](https://developer.android.com/images/reference/androidx/compose/foundation/layout/column_arrangement_visualization.gif)
 */
@Immutable
public object Arrangement {
	/**
	 * Used to specify the horizontal arrangement of the layout's children in layouts like [Row].
	 */
	@Stable
	public interface Horizontal {
		/**
		 * Spacing that should be added between any two adjacent layout children.
		 */
		public val spacing: Int get() = 0

		/**
		 * Horizontally places the layout children.
		 *
		 * @param totalSize Available space that can be occupied by the children, in pixels.
		 * @param sizes An array of sizes of all children, in pixels.
		 * @param outPositions An array of the size of [sizes] that returns the calculated
		 * positions relative to the left, in pixels.
		 */
		public fun arrange(
			totalSize: Int,
			sizes: IntArray,
			outPositions: IntArray,
		)
	}

	/**
	 * Used to specify the vertical arrangement of the layout's children in layouts like [Column].
	 */
	@Stable
	public interface Vertical {
		/**
		 * Spacing that should be added between any two adjacent layout children.
		 */
		public val spacing: Int get() = 0

		/**
		 * Vertically places the layout children.
		 *
		 * @param totalSize Available space that can be occupied by the children, in pixels.
		 * @param sizes An array of sizes of all children, in pixels.
		 * @param outPositions An array of the size of [sizes] that returns the calculated
		 * positions relative to the top, in pixels.
		 */
		public fun arrange(
			totalSize: Int,
			sizes: IntArray,
			outPositions: IntArray,
		)
	}

	/**
	 * Used to specify the horizontal arrangement of the layout's children in horizontal layouts
	 * like [Row], or the vertical arrangement of the layout's children in vertical layouts like
	 * [Column].
	 */
	@Stable
	public interface HorizontalOrVertical :
		Horizontal,
		Vertical {
		/**
		 * Spacing that should be added between any two adjacent layout children.
		 */
		override val spacing: Int get() = 0
	}

	/**
	 * Place children horizontally such that they are as close as possible to the beginning of the
	 * horizontal axis (left).
	 * Visually: 123####.
	 */
	@Stable
	public val Start: Horizontal = object : Horizontal {
		override fun arrange(
			totalSize: Int,
			sizes: IntArray,
			outPositions: IntArray,
		) = placeLeftOrTop(sizes, outPositions, reverseInput = false)

		override fun toString() = "Arrangement#Start"
	}

	/**
	 * Place children horizontally such that they are as close as possible to the end of the main
	 * axis.
	 * Visually: ####123.
	 */
	@Stable
	public val End: Horizontal = object : Horizontal {
		override fun arrange(
			totalSize: Int,
			sizes: IntArray,
			outPositions: IntArray,
		) = placeRightOrBottom(totalSize, sizes, outPositions, reverseInput = false)

		override fun toString() = "Arrangement#End"
	}

	/**
	 * Place children vertically such that they are as close as possible to the top of the main
	 * axis.
	 * Visually: (top) 123#### (bottom)
	 */
	@Stable
	public val Top: Vertical = object : Vertical {
		override fun arrange(
			totalSize: Int,
			sizes: IntArray,
			outPositions: IntArray,
		) = placeLeftOrTop(sizes, outPositions, reverseInput = false)

		override fun toString() = "Arrangement#Top"
	}

	/**
	 * Place children vertically such that they are as close as possible to the bottom of the main
	 * axis.
	 * Visually: (top) ####123 (bottom)
	 */
	@Stable
	public val Bottom: Vertical = object : Vertical {
		override fun arrange(
			totalSize: Int,
			sizes: IntArray,
			outPositions: IntArray,
		) = placeRightOrBottom(totalSize, sizes, outPositions, reverseInput = false)

		override fun toString() = "Arrangement#Bottom"
	}

	/**
	 * Place children such that they are as close as possible to the middle of the main axis.
	 * Visually: ##123## for LTR.
	 */
	@Stable
	public val Center: HorizontalOrVertical = object : HorizontalOrVertical {
		override val spacing = 0

		override fun arrange(
			totalSize: Int,
			sizes: IntArray,
			outPositions: IntArray,
		) = placeCenter(totalSize, sizes, outPositions, reverseInput = false)

		override fun toString() = "Arrangement#Center"
	}

	/**
	 * Place children such that they are spaced evenly across the main axis, including free
	 * space before the first child and after the last child.
	 * Visually: #1#2#3# for LTR.
	 */
	@Stable
	public val SpaceEvenly: HorizontalOrVertical = object : HorizontalOrVertical {
		override val spacing = 0

		override fun arrange(
			totalSize: Int,
			sizes: IntArray,
			outPositions: IntArray,
		) = placeSpaceEvenly(totalSize, sizes, outPositions, reverseInput = false)

		override fun toString() = "Arrangement#SpaceEvenly"
	}

	/**
	 * Place children such that they are spaced evenly across the main axis, without free
	 * space before the first child or after the last child.
	 * Visually: 1##2##3 for LTR.
	 */
	@Stable
	public val SpaceBetween: HorizontalOrVertical = object : HorizontalOrVertical {
		override val spacing = 0

		override fun arrange(
			totalSize: Int,
			sizes: IntArray,
			outPositions: IntArray,
		) = placeSpaceBetween(totalSize, sizes, outPositions, reverseInput = false)

		override fun toString() = "Arrangement#SpaceBetween"
	}

	/**
	 * Place children such that they are spaced evenly across the main axis, including free
	 * space before the first child and after the last child, but half the amount of space
	 * existing otherwise between two consecutive children.
	 * Visually: #1##2##3# for LTR.
	 */
	@Stable
	public val SpaceAround: HorizontalOrVertical = object : HorizontalOrVertical {
		override val spacing = 0

		override fun arrange(
			totalSize: Int,
			sizes: IntArray,
			outPositions: IntArray,
		) = placeSpaceAround(totalSize, sizes, outPositions, reverseInput = false)

		override fun toString() = "Arrangement#SpaceAround"
	}

	/**
	 * Place children such that each two adjacent ones are spaced by a fixed [space] distance across
	 * the main axis. The spacing will be subtracted from the available space that the children
	 * can occupy. The [space] can be negative, in which case children will overlap.
	 *
	 * To change alignment of the spaced children horizontally or vertically, use [spacedBy]
	 * overloads with `alignment` parameter.
	 *
	 * @param space The space between adjacent children.
	 */
	@Stable
	public fun spacedBy(space: Int): HorizontalOrVertical =
		SpacedAligned(space, true) { size -> Alignment.Start.align(0, size) }

	/**
	 * Place children horizontally such that each two adjacent ones are spaced by a fixed [space]
	 * distance. The spacing will be subtracted from the available width that the children
	 * can occupy. An [alignment] can be specified to align the spaced children horizontally
	 * inside the parent, in case there is empty width remaining. The [space] can be negative,
	 * in which case children will overlap.
	 *
	 * @param space The space between adjacent children.
	 * @param alignment The alignment of the spaced children inside the parent.
	 */
	@Stable
	public fun spacedBy(space: Int, alignment: Alignment.Horizontal): Horizontal =
		SpacedAligned(space, true) { size -> alignment.align(0, size) }

	/**
	 * Place children vertically such that each two adjacent ones are spaced by a fixed [space]
	 * distance. The spacing will be subtracted from the available height that the children
	 * can occupy. An [alignment] can be specified to align the spaced children vertically
	 * inside the parent, in case there is empty height remaining. The [space] can be negative,
	 * in which case children will overlap.
	 *
	 * @param space The space between adjacent children.
	 * @param alignment The alignment of the spaced children inside the parent.
	 */
	@Stable
	public fun spacedBy(space: Int, alignment: Alignment.Vertical): Vertical =
		SpacedAligned(space, false) { size -> alignment.align(0, size) }

	/**
	 * Place children horizontally one next to the other and align the obtained group
	 * according to an [alignment].
	 *
	 * @param alignment The alignment of the children inside the parent.
	 */
	@Stable
	public fun aligned(alignment: Alignment.Horizontal): Horizontal =
		SpacedAligned(0, true) { size -> alignment.align(0, size) }

	/**
	 * Place children vertically one next to the other and align the obtained group
	 * according to an [alignment].
	 *
	 * @param alignment The alignment of the children inside the parent.
	 */
	@Stable
	public fun aligned(alignment: Alignment.Vertical): Vertical =
		SpacedAligned(0, false) { size -> alignment.align(0, size) }

	@Immutable
	public object Absolute {
		/**
		 * Place children horizontally such that they are as close as possible to the left edge of
		 * the [Row].
		 *
		 * Unlike [Arrangement.Start], when the layout direction is RTL, the children will not be
		 * mirrored and as such children will appear in the order they are composed inside the [Row].
		 *
		 * Visually: 123####
		 */
		@Stable
		public val Left: Horizontal = object : Horizontal {
			override fun arrange(
				totalSize: Int,
				sizes: IntArray,
				outPositions: IntArray,
			) = placeLeftOrTop(sizes, outPositions, reverseInput = false)

			override fun toString() = "AbsoluteArrangement#Left"
		}

		/**
		 * Place children such that they are as close as possible to the middle of the [Row].
		 *
		 * Visually: ##123##
		 */
		@Stable
		public val Center: Horizontal = object : Horizontal {
			override fun arrange(
				totalSize: Int,
				sizes: IntArray,
				outPositions: IntArray,
			) = placeCenter(totalSize, sizes, outPositions, reverseInput = false)

			override fun toString() = "AbsoluteArrangement#Center"
		}

		/**
		 * Place children horizontally such that they are as close as possible to the right edge of
		 * the [Row].
		 *
		 * Visually: ####123
		 */
		@Stable
		public val Right: Horizontal = object : Horizontal {
			override fun arrange(
				totalSize: Int,
				sizes: IntArray,
				outPositions: IntArray,
			) = placeRightOrBottom(totalSize, sizes, outPositions, reverseInput = false)

			override fun toString() = "AbsoluteArrangement#Right"
		}

		/**
		 * Place children such that they are spaced evenly across the main axis, without free
		 * space before the first child or after the last child.
		 *
		 * Visually: 1##2##3
		 */
		@Stable
		public val SpaceBetween: Horizontal = object : Horizontal {
			override fun arrange(
				totalSize: Int,
				sizes: IntArray,
				outPositions: IntArray,
			) = placeSpaceBetween(totalSize, sizes, outPositions, reverseInput = false)

			override fun toString() = "AbsoluteArrangement#SpaceBetween"
		}

		/**
		 * Place children such that they are spaced evenly across the main axis, including free
		 * space before the first child and after the last child.
		 *
		 * Visually: #1#2#3#
		 */
		@Stable
		public val SpaceEvenly: Horizontal = object : Horizontal {
			override fun arrange(
				totalSize: Int,
				sizes: IntArray,
				outPositions: IntArray,
			) = placeSpaceEvenly(totalSize, sizes, outPositions, reverseInput = false)

			override fun toString() = "AbsoluteArrangement#SpaceEvenly"
		}

		/**
		 * Place children such that they are spaced evenly horizontally, including free
		 * space before the first child and after the last child, but half the amount of space
		 * existing otherwise between two consecutive children.
		 *
		 * Visually: #1##2##3##4#
		 */
		@Stable
		public val SpaceAround: Horizontal = object : Horizontal {
			override fun arrange(
				totalSize: Int,
				sizes: IntArray,
				outPositions: IntArray,
			) = placeSpaceAround(totalSize, sizes, outPositions, reverseInput = false)

			override fun toString() = "AbsoluteArrangement#SpaceAround"
		}

		/**
		 * Place children such that each two adjacent ones are spaced by a fixed [space] distance across
		 * the main axis. The spacing will be subtracted from the available space that the children
		 * can occupy.
		 *
		 * @param space The space between adjacent children.
		 */
		@Stable
		public fun spacedBy(space: Int): HorizontalOrVertical =
			SpacedAligned(space, false, null)

		/**
		 * Place children horizontally such that each two adjacent ones are spaced by a fixed [space]
		 * distance. The spacing will be subtracted from the available width that the children
		 * can occupy. An [alignment] can be specified to align the spaced children horizontally
		 * inside the parent, in case there is empty width remaining.
		 *
		 * @param space The space between adjacent children.
		 * @param alignment The alignment of the spaced children inside the parent.
		 */
		@Stable
		public fun spacedBy(space: Int, alignment: Alignment.Horizontal): Horizontal =
			SpacedAligned(space, false) { size -> alignment.align(0, size) }

		/**
		 * Place children vertically such that each two adjacent ones are spaced by a fixed [space]
		 * distance. The spacing will be subtracted from the available height that the children
		 * can occupy. An [alignment] can be specified to align the spaced children vertically
		 * inside the parent, in case there is empty height remaining.
		 *
		 * @param space The space between adjacent children.
		 * @param alignment The alignment of the spaced children inside the parent.
		 */
		@Stable
		public fun spacedBy(space: Int, alignment: Alignment.Vertical): Vertical =
			SpacedAligned(space, false) { size -> alignment.align(0, size) }

		/**
		 * Place children horizontally one next to the other and align the obtained group
		 * according to an [alignment].
		 *
		 * @param alignment The alignment of the children inside the parent.
		 */
		@Stable
		public fun aligned(alignment: Alignment.Horizontal): Horizontal =
			SpacedAligned(0, false) { size -> alignment.align(0, size) }
	}

	/**
	 * Arrangement with spacing between adjacent children and alignment for the spaced group.
	 * Should not be instantiated directly, use [spacedBy] instead.
	 */
	@Immutable
	internal data class SpacedAligned(
		val space: Int,
		val rtlMirror: Boolean,
		val alignment: ((Int) -> Int)?,
	) : HorizontalOrVertical {

		override val spacing = space

		override fun arrange(
			totalSize: Int,
			sizes: IntArray,
			outPositions: IntArray,
		) {
			if (sizes.isEmpty()) return

			var occupied = 0
			var lastSpace = 0
			val reversed = rtlMirror
			sizes.forEachIndexed(reversed) { index, it ->
				outPositions[index] = min(occupied, totalSize - it)
				lastSpace = min(space, totalSize - outPositions[index] - it)
				occupied = outPositions[index] + it + lastSpace
			}
			occupied -= lastSpace

			if (alignment != null && occupied < totalSize) {
				val groupPosition = alignment.invoke(totalSize - occupied)
				for (index in outPositions.indices) {
					outPositions[index] += groupPosition
				}
			}
		}

		override fun toString() =
			"${if (rtlMirror) "" else "Absolute"}Arrangement#spacedAligned($space, $alignment)"
	}

	internal fun placeRightOrBottom(
		totalSize: Int,
		size: IntArray,
		outPosition: IntArray,
		reverseInput: Boolean,
	) {
		val consumedSize = size.fold(0) { a, b -> a + b }
		var current = totalSize - consumedSize
		size.forEachIndexed(reverseInput) { index, it ->
			outPosition[index] = current
			current += it
		}
	}

	internal fun placeLeftOrTop(size: IntArray, outPosition: IntArray, reverseInput: Boolean) {
		var current = 0
		size.forEachIndexed(reverseInput) { index, it ->
			outPosition[index] = current
			current += it
		}
	}

	internal fun placeCenter(
		totalSize: Int,
		size: IntArray,
		outPosition: IntArray,
		reverseInput: Boolean,
	) {
		val consumedSize = size.fold(0) { a, b -> a + b }
		var current = (totalSize - consumedSize).toFloat() / 2
		size.forEachIndexed(reverseInput) { index, it ->
			outPosition[index] = current.roundToInt()
			current += it.toFloat()
		}
	}

	internal fun placeSpaceEvenly(
		totalSize: Int,
		size: IntArray,
		outPosition: IntArray,
		reverseInput: Boolean,
	) {
		val consumedSize = size.fold(0) { a, b -> a + b }
		val gapSize = (totalSize - consumedSize).toFloat() / (size.size + 1)
		var current = gapSize
		size.forEachIndexed(reverseInput) { index, it ->
			outPosition[index] = current.roundToInt()
			current += it.toFloat() + gapSize
		}
	}

	internal fun placeSpaceBetween(
		totalSize: Int,
		size: IntArray,
		outPosition: IntArray,
		reverseInput: Boolean,
	) {
		if (size.isEmpty()) return

		val consumedSize = size.fold(0) { a, b -> a + b }
		val noOfGaps = maxOf(size.lastIndex, 1)
		val gapSize = (totalSize - consumedSize).toFloat() / noOfGaps

		var current = 0f
		if (reverseInput && size.size == 1) {
			// If the layout direction is right-to-left and there is only one gap,
			// we start current with the gap size. That forces the single item to be right-aligned.
			current = gapSize
		}
		size.forEachIndexed(reverseInput) { index, it ->
			outPosition[index] = current.roundToInt()
			current += it.toFloat() + gapSize
		}
	}

	internal fun placeSpaceAround(
		totalSize: Int,
		size: IntArray,
		outPosition: IntArray,
		reverseInput: Boolean,
	) {
		val consumedSize = size.fold(0) { a, b -> a + b }
		val gapSize = if (size.isNotEmpty()) {
			(totalSize - consumedSize).toFloat() / size.size
		} else {
			0f
		}
		var current = gapSize / 2
		size.forEachIndexed(reverseInput) { index, it ->
			outPosition[index] = current.roundToInt()
			current += it.toFloat() + gapSize
		}
	}

	private inline fun IntArray.forEachIndexed(reversed: Boolean, action: (Int, Int) -> Unit) {
		if (!reversed) {
			forEachIndexed(action)
		} else {
			for (i in (size - 1) downTo 0) {
				action(i, get(i))
			}
		}
	}
}
