package com.jakewharton.mosaic.ui

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import com.jakewharton.mosaic.ui.unit.IntOffset
import com.jakewharton.mosaic.ui.unit.IntSize
import dev.drewhamilton.poko.Poko
import kotlin.math.roundToInt

/**
 * An interface to calculate the position of a sized box inside an available space. [Alignment] is
 * often used to define the alignment of a layout inside a parent layout.
 *
 * @see BiasAlignment
 */
@Stable
public fun interface Alignment {
	/**
	 * Calculates the position of a box of size [size] relative to the top left corner of an area
	 * of size [space]. The returned offset can be negative or larger than `space - size`,
	 * meaning that the box will be positioned partially or completely outside the area.
	 */
	public fun align(size: IntSize, space: IntSize): IntOffset

	/**
	 * An interface to calculate the position of box of a certain width inside an available width.
	 * [Alignment.Horizontal] is often used to define the horizontal alignment of a layout inside a
	 * parent layout.
	 */
	@Stable
	public fun interface Horizontal {
		/**
		 * Calculates the horizontal position of a box of width [size] relative to the left
		 * side of an area of width [space]. The returned offset can be negative or larger than
		 * `space - size` meaning that the box will be positioned partially or completely outside
		 * the area.
		 */
		public fun align(size: Int, space: Int): Int
	}

	/**
	 * An interface to calculate the position of a box of a certain height inside an available
	 * height. [Alignment.Vertical] is often used to define the vertical alignment of a
	 * layout inside a parent layout.
	 */
	@Stable
	public fun interface Vertical {
		/**
		 * Calculates the vertical position of a box of height [size] relative to the top edge of
		 * an area of height [space]. The returned offset can be negative or larger than
		 * `space - size` meaning that the box will be positioned partially or completely outside
		 * the area.
		 */
		public fun align(size: Int, space: Int): Int
	}

	/**
	 * A collection of common [Alignment]s aware of layout direction.
	 */
	public companion object {
		// 2D Alignments.
		@Stable
		public val TopStart: Alignment = BiasAlignment(-1f, -1f)

		@Stable
		public val TopCenter: Alignment = BiasAlignment(0f, -1f)

		@Stable
		public val TopEnd: Alignment = BiasAlignment(1f, -1f)

		@Stable
		public val CenterStart: Alignment = BiasAlignment(-1f, 0f)

		@Stable
		public val Center: Alignment = BiasAlignment(0f, 0f)

		@Stable
		public val CenterEnd: Alignment = BiasAlignment(1f, 0f)

		@Stable
		public val BottomStart: Alignment = BiasAlignment(-1f, 1f)

		@Stable
		public val BottomCenter: Alignment = BiasAlignment(0f, 1f)

		@Stable
		public val BottomEnd: Alignment = BiasAlignment(1f, 1f)

		// 1D Alignment.Verticals.
		@Stable
		public val Top: Vertical = BiasAlignment.Vertical(-1f)

		@Stable
		public val CenterVertically: Vertical = BiasAlignment.Vertical(0f)

		@Stable
		public val Bottom: Vertical = BiasAlignment.Vertical(1f)

		// 1D Alignment.Horizontals.
		@Stable
		public val Start: Horizontal = BiasAlignment.Horizontal(-1f)

		@Stable
		public val CenterHorizontally: Horizontal = BiasAlignment.Horizontal(0f)

		@Stable
		public val End: Horizontal = BiasAlignment.Horizontal(1f)
	}
}

/**
 * An [Alignment] specified by bias: for example, a bias of -1 represents alignment to the
 * start/top, a bias of 0 will represent centering, and a bias of 1 will represent end/bottom.
 * Any value can be specified to obtain an alignment. Inside the [-1, 1] range, the obtained
 * alignment will position the aligned size fully inside the available space, while outside the
 * range it will the aligned size will be positioned partially or completely outside.
 *
 * @see Alignment
 */
@[Immutable Poko]
public class BiasAlignment(
	public val horizontalBias: Float,
	public val verticalBias: Float,
) : Alignment {

	override fun align(size: IntSize, space: IntSize): IntOffset {
		// Convert to cells first and only round at the end, to avoid rounding twice while calculating
		// the new positions
		val centerX = (space.width - size.width).toFloat() / 2f
		val centerY = (space.height - size.height).toFloat() / 2f

		val x = centerX * (1 + horizontalBias)
		val y = centerY * (1 + verticalBias)
		return IntOffset(x.roundToInt(), y.roundToInt())
	}

	override fun toString(): String {
		val horizontalBiasStr = horizontalBias.toString().removeSuffix(".0")
		val verticalBiasStr = verticalBias.toString().removeSuffix(".0")
		return "Alignment(horizontalBias=$horizontalBiasStr, verticalBias=$verticalBiasStr)"
	}

	/**
	 * An [Alignment.Horizontal] specified by bias: for example, a bias of -1 represents alignment
	 * to the start, a bias of 0 will represent centering, and a bias of 1 will represent end.
	 * Any value can be specified to obtain an alignment. Inside the [-1, 1] range, the obtained
	 * alignment will position the aligned size fully inside the available space, while outside the
	 * range it will the aligned size will be positioned partially or completely outside.
	 *
	 * @see Vertical
	 */
	@[Immutable Poko]
	public class Horizontal(public val bias: Float) : Alignment.Horizontal {
		override fun align(size: Int, space: Int): Int {
			// Convert to cells first and only round at the end, to avoid rounding twice while
			// calculating the new positions.
			val center = (space - size).toFloat() / 2f
			return (center * (1 + bias)).roundToInt()
		}

		override fun toString(): String {
			val biasStr = bias.toString().removeSuffix(".0")
			return "Horizontal(bias=$biasStr)"
		}
	}

	/**
	 * An [Alignment.Vertical] specified by bias: for example, a bias of -1 represents alignment
	 * to the top, a bias of 0 will represent centering, and a bias of 1 will represent bottom.
	 * Any value can be specified to obtain an alignment. Inside the [-1, 1] range, the obtained
	 * alignment will position the aligned size fully inside the available space, while outside the
	 * range it will the aligned size will be positioned partially or completely outside.
	 *
	 * @see Horizontal
	 */
	@[Immutable Poko]
	public class Vertical(public val bias: Float) : Alignment.Vertical {
		override fun align(size: Int, space: Int): Int {
			// Convert to cells first and only round at the end, to avoid rounding twice while
			// calculating the new positions.
			val center = (space - size).toFloat() / 2f
			return (center * (1 + bias)).roundToInt()
		}

		override fun toString(): String {
			val biasStr = bias.toString().removeSuffix(".0")
			return "Vertical(bias=$biasStr)"
		}
	}
}
