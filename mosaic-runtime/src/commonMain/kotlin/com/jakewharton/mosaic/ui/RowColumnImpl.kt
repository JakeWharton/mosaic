package com.jakewharton.mosaic.ui

import androidx.compose.runtime.Immutable
import com.jakewharton.mosaic.layout.IntrinsicMeasurable
import com.jakewharton.mosaic.layout.Measurable
import com.jakewharton.mosaic.layout.MeasurePolicy
import com.jakewharton.mosaic.layout.MeasureResult
import com.jakewharton.mosaic.layout.MeasureScope
import com.jakewharton.mosaic.layout.ParentDataModifier
import com.jakewharton.mosaic.layout.Placeable
import com.jakewharton.mosaic.ui.LayoutOrientation.Horizontal
import com.jakewharton.mosaic.ui.LayoutOrientation.Vertical
import com.jakewharton.mosaic.ui.unit.Constraints
import kotlin.jvm.JvmInline
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

internal data class RowColumnMeasurePolicy(
	private val orientation: LayoutOrientation,
	private val horizontalArrangement: Arrangement.Horizontal?,
	private val verticalArrangement: Arrangement.Vertical?,
	private val arrangementSpacing: Int,
	private val crossAxisSize: SizeMode,
	private val crossAxisAlignment: CrossAxisAlignment,
) : MeasurePolicy {
	override fun MeasureScope.measure(
		measurables: List<Measurable>,
		constraints: Constraints,
	): MeasureResult {
		val placeables = arrayOfNulls<Placeable?>(measurables.size)
		val rowColumnMeasureHelper =
			RowColumnMeasurementHelper(
				orientation,
				horizontalArrangement,
				verticalArrangement,
				arrangementSpacing,
				crossAxisSize,
				crossAxisAlignment,
				measurables,
				placeables,
			)

		val measureResult = rowColumnMeasureHelper
			.measureWithoutPlacing(constraints, 0, measurables.size)

		val layoutWidth: Int
		val layoutHeight: Int
		if (orientation == Horizontal) {
			layoutWidth = measureResult.mainAxisSize
			layoutHeight = measureResult.crossAxisSize
		} else {
			layoutWidth = measureResult.crossAxisSize
			layoutHeight = measureResult.mainAxisSize
		}
		return layout(layoutWidth, layoutHeight) {
			rowColumnMeasureHelper.placeHelper(
				this,
				measureResult,
				0,
			)
		}
	}

	override fun minIntrinsicWidth(
		measurables: List<IntrinsicMeasurable>,
		height: Int,
	) = minIntrinsicWidthMeasureBlock(orientation)(
		measurables,
		height,
		arrangementSpacing,
	)

	override fun minIntrinsicHeight(
		measurables: List<IntrinsicMeasurable>,
		width: Int,
	) = minIntrinsicHeightMeasureBlock(orientation)(
		measurables,
		width,
		arrangementSpacing,
	)

	override fun maxIntrinsicWidth(
		measurables: List<IntrinsicMeasurable>,
		height: Int,
	) = maxIntrinsicWidthMeasureBlock(orientation)(
		measurables,
		height,
		arrangementSpacing,
	)

	override fun maxIntrinsicHeight(
		measurables: List<IntrinsicMeasurable>,
		width: Int,
	) = maxIntrinsicHeightMeasureBlock(orientation)(
		measurables,
		width,
		arrangementSpacing,
	)
}

/**
 * [Row] will be [Horizontal], [Column] is [Vertical].
 */
internal enum class LayoutOrientation {
	Horizontal,
	Vertical,
}

/**
 * Used to specify the alignment of a layout's children, in cross axis direction.
 */
@Immutable
internal sealed class CrossAxisAlignment {
	/**
	 * Aligns to [size].
	 *
	 * @param size The remaining space (total size - content size) in the container.
	 * @param placeable The item being aligned.
	 */
	internal abstract fun align(
		size: Int,
		placeable: Placeable,
	): Int

	companion object {
		/**
		 * Align children with vertical alignment.
		 */
		internal fun vertical(vertical: Alignment.Vertical): CrossAxisAlignment =
			VerticalCrossAxisAlignment(vertical)

		/**
		 * Align children with horizontal alignment.
		 */
		internal fun horizontal(horizontal: Alignment.Horizontal): CrossAxisAlignment =
			HorizontalCrossAxisAlignment(horizontal)
	}

	private data class VerticalCrossAxisAlignment(
		val vertical: Alignment.Vertical,
	) : CrossAxisAlignment() {
		override fun align(
			size: Int,
			placeable: Placeable,
		): Int {
			return vertical.align(0, size)
		}
	}

	private data class HorizontalCrossAxisAlignment(
		val horizontal: Alignment.Horizontal,
	) : CrossAxisAlignment() {
		override fun align(
			size: Int,
			placeable: Placeable,
		): Int {
			return horizontal.align(0, size)
		}
	}
}

/**
 * Box [Constraints], but which abstract away width and height in favor of main axis and cross axis.
 */
@JvmInline
internal value class OrientationIndependentConstraints private constructor(
	private val value: Constraints,
) {
	inline val mainAxisMin: Int get() = value.minWidth
	inline val mainAxisMax: Int get() = value.maxWidth
	inline val crossAxisMin: Int get() = value.minHeight
	inline val crossAxisMax: Int get() = value.maxHeight

	constructor(
		mainAxisMin: Int,
		mainAxisMax: Int,
		crossAxisMin: Int,
		crossAxisMax: Int,
	) : this(
		Constraints(
			minWidth = mainAxisMin,
			maxWidth = mainAxisMax,
			minHeight = crossAxisMin,
			maxHeight = crossAxisMax,
		),
	)

	constructor(c: Constraints, orientation: LayoutOrientation) : this(
		if (orientation === Horizontal) c.minWidth else c.minHeight,
		if (orientation === Horizontal) c.maxWidth else c.maxHeight,
		if (orientation === Horizontal) c.minHeight else c.minWidth,
		if (orientation === Horizontal) c.maxHeight else c.maxWidth,
	)

	// Given an orientation, resolves the current instance to traditional constraints.
	fun toBoxConstraints(orientation: LayoutOrientation) =
		if (orientation === Horizontal) {
			Constraints(mainAxisMin, mainAxisMax, crossAxisMin, crossAxisMax)
		} else {
			Constraints(crossAxisMin, crossAxisMax, mainAxisMin, mainAxisMax)
		}

	fun copy(
		mainAxisMin: Int = this.mainAxisMin,
		mainAxisMax: Int = this.mainAxisMax,
		crossAxisMin: Int = this.crossAxisMin,
		crossAxisMax: Int = this.crossAxisMax,
	): OrientationIndependentConstraints =
		OrientationIndependentConstraints(
			mainAxisMin,
			mainAxisMax,
			crossAxisMin,
			crossAxisMax,
		)
}

internal val IntrinsicMeasurable.rowColumnParentData: RowColumnParentData?
	get() = parentData as? RowColumnParentData

internal val RowColumnParentData?.weight: Float
	get() = this?.weight ?: 0f

internal val RowColumnParentData?.fill: Boolean
	get() = this?.fill ?: true

private fun minIntrinsicWidthMeasureBlock(orientation: LayoutOrientation) =
	if (orientation == Horizontal) {
		IntrinsicMeasureBlocks.HorizontalMinWidth
	} else {
		IntrinsicMeasureBlocks.VerticalMinWidth
	}

private fun minIntrinsicHeightMeasureBlock(orientation: LayoutOrientation) =
	if (orientation == Horizontal) {
		IntrinsicMeasureBlocks.HorizontalMinHeight
	} else {
		IntrinsicMeasureBlocks.VerticalMinHeight
	}

private fun maxIntrinsicWidthMeasureBlock(orientation: LayoutOrientation) =
	if (orientation == Horizontal) {
		IntrinsicMeasureBlocks.HorizontalMaxWidth
	} else {
		IntrinsicMeasureBlocks.VerticalMaxWidth
	}

private fun maxIntrinsicHeightMeasureBlock(orientation: LayoutOrientation) =
	if (orientation == Horizontal) {
		IntrinsicMeasureBlocks.HorizontalMaxHeight
	} else {
		IntrinsicMeasureBlocks.VerticalMaxHeight
	}

private object IntrinsicMeasureBlocks {
	val HorizontalMinWidth: (List<IntrinsicMeasurable>, Int, Int) -> Int =
		{ measurables, availableHeight, mainAxisSpacing ->
			intrinsicSize(
				measurables,
				{ h -> minIntrinsicWidth(h) },
				{ w -> maxIntrinsicHeight(w) },
				availableHeight,
				mainAxisSpacing,
				Horizontal,
				Horizontal,
			)
		}
	val VerticalMinWidth: (List<IntrinsicMeasurable>, Int, Int) -> Int =
		{ measurables, availableHeight, mainAxisSpacing ->
			intrinsicSize(
				measurables,
				{ h -> minIntrinsicWidth(h) },
				{ w -> maxIntrinsicHeight(w) },
				availableHeight,
				mainAxisSpacing,
				Vertical,
				Horizontal,
			)
		}
	val HorizontalMinHeight: (List<IntrinsicMeasurable>, Int, Int) -> Int =
		{ measurables, availableWidth, mainAxisSpacing ->
			intrinsicSize(
				measurables,
				{ w -> minIntrinsicHeight(w) },
				{ h -> maxIntrinsicWidth(h) },
				availableWidth,
				mainAxisSpacing,
				Horizontal,
				Vertical,
			)
		}
	val VerticalMinHeight: (List<IntrinsicMeasurable>, Int, Int) -> Int =
		{ measurables, availableWidth, mainAxisSpacing ->
			intrinsicSize(
				measurables,
				{ w -> minIntrinsicHeight(w) },
				{ h -> maxIntrinsicWidth(h) },
				availableWidth,
				mainAxisSpacing,
				Vertical,
				Vertical,
			)
		}
	val HorizontalMaxWidth: (List<IntrinsicMeasurable>, Int, Int) -> Int =
		{ measurables, availableHeight, mainAxisSpacing ->
			intrinsicSize(
				measurables,
				{ h -> maxIntrinsicWidth(h) },
				{ w -> maxIntrinsicHeight(w) },
				availableHeight,
				mainAxisSpacing,
				Horizontal,
				Horizontal,
			)
		}
	val VerticalMaxWidth: (List<IntrinsicMeasurable>, Int, Int) -> Int =
		{ measurables, availableHeight, mainAxisSpacing ->
			intrinsicSize(
				measurables,
				{ h -> maxIntrinsicWidth(h) },
				{ w -> maxIntrinsicHeight(w) },
				availableHeight,
				mainAxisSpacing,
				Vertical,
				Horizontal,
			)
		}
	val HorizontalMaxHeight: (List<IntrinsicMeasurable>, Int, Int) -> Int =
		{ measurables, availableWidth, mainAxisSpacing ->
			intrinsicSize(
				measurables,
				{ w -> maxIntrinsicHeight(w) },
				{ h -> maxIntrinsicWidth(h) },
				availableWidth,
				mainAxisSpacing,
				Horizontal,
				Vertical,
			)
		}
	val VerticalMaxHeight: (List<IntrinsicMeasurable>, Int, Int) -> Int =
		{ measurables, availableWidth, mainAxisSpacing ->
			intrinsicSize(
				measurables,
				{ w -> maxIntrinsicHeight(w) },
				{ h -> maxIntrinsicWidth(h) },
				availableWidth,
				mainAxisSpacing,
				Vertical,
				Vertical,
			)
		}
}

private fun intrinsicSize(
	children: List<IntrinsicMeasurable>,
	intrinsicMainSize: IntrinsicMeasurable.(Int) -> Int,
	intrinsicCrossSize: IntrinsicMeasurable.(Int) -> Int,
	crossAxisAvailable: Int,
	mainAxisSpacing: Int,
	layoutOrientation: LayoutOrientation,
	intrinsicOrientation: LayoutOrientation,
) = if (layoutOrientation == intrinsicOrientation) {
	intrinsicMainAxisSize(children, intrinsicMainSize, crossAxisAvailable, mainAxisSpacing)
} else {
	intrinsicCrossAxisSize(
		children,
		intrinsicCrossSize,
		intrinsicMainSize,
		crossAxisAvailable,
		mainAxisSpacing,
	)
}

private fun intrinsicMainAxisSize(
	children: List<IntrinsicMeasurable>,
	mainAxisSize: IntrinsicMeasurable.(Int) -> Int,
	crossAxisAvailable: Int,
	mainAxisSpacing: Int,
): Int {
	if (children.isEmpty()) return 0
	var weightUnitSpace = 0
	var fixedSpace = 0
	var totalWeight = 0f
	children.forEach { child ->
		val weight = child.rowColumnParentData.weight
		val size = child.mainAxisSize(crossAxisAvailable)
		if (weight == 0f) {
			fixedSpace += size
		} else if (weight > 0f) {
			totalWeight += weight
			weightUnitSpace = max(weightUnitSpace, (size / weight).roundToInt())
		}
	}
	return (weightUnitSpace * totalWeight).roundToInt() + fixedSpace +
		(children.size - 1) * mainAxisSpacing
}

private fun intrinsicCrossAxisSize(
	children: List<IntrinsicMeasurable>,
	mainAxisSize: IntrinsicMeasurable.(Int) -> Int,
	crossAxisSize: IntrinsicMeasurable.(Int) -> Int,
	mainAxisAvailable: Int,
	mainAxisSpacing: Int,
): Int {
	if (children.isEmpty()) return 0
	var fixedSpace = min((children.size - 1) * mainAxisSpacing, mainAxisAvailable)
	var crossAxisMax = 0
	var totalWeight = 0f
	children.forEach { child ->
		val weight = child.rowColumnParentData.weight
		if (weight == 0f) {
			// Ask the child how much main axis space it wants to occupy. This cannot be more
			// than the remaining available space.
			val remaining = if (mainAxisAvailable == Constraints.Infinity) {
				Constraints.Infinity
			} else {
				mainAxisAvailable - fixedSpace
			}
			val mainAxisSpace = min(
				child.mainAxisSize(Constraints.Infinity),
				remaining,
			)
			fixedSpace += mainAxisSpace
			// Now that the assigned main axis space is known, ask about the cross axis space.
			crossAxisMax = max(crossAxisMax, child.crossAxisSize(mainAxisSpace))
		} else if (weight > 0f) {
			totalWeight += weight
		}
	}

	// For weighted children, calculate how much main axis space weight=1 would represent.
	val weightUnitSpace = if (totalWeight == 0f) {
		0
	} else if (mainAxisAvailable == Constraints.Infinity) {
		Constraints.Infinity
	} else {
		(max(mainAxisAvailable - fixedSpace, 0) / totalWeight).roundToInt()
	}

	children.forEach { child ->
		val weight = child.rowColumnParentData.weight
		// Now the main axis for weighted children is known, so ask about the cross axis space.
		if (weight > 0f) {
			crossAxisMax = max(
				crossAxisMax,
				child.crossAxisSize(
					if (weightUnitSpace != Constraints.Infinity) {
						(weightUnitSpace * weight).roundToInt()
					} else {
						Constraints.Infinity
					},
				),
			)
		}
	}
	return crossAxisMax
}

internal class LayoutWeightModifier(
	private val weight: Float,
	private val fill: Boolean,
) : ParentDataModifier {

	override fun modifyParentData(parentData: Any?): Any {
		return ((parentData as? RowColumnParentData) ?: RowColumnParentData()).also {
			it.weight = weight
			it.fill = fill
		}
	}

	override fun toString(): String = "LayoutWeight(weight=$weight, fill=$fill)"
}

internal class HorizontalAlignModifier(
	private val horizontal: Alignment.Horizontal,
) : ParentDataModifier {

	override fun modifyParentData(parentData: Any?): Any {
		return ((parentData as? RowColumnParentData) ?: RowColumnParentData()).also {
			it.crossAxisAlignment = CrossAxisAlignment.horizontal(horizontal)
		}
	}

	override fun toString(): String = "HorizontalAlign($horizontal)"
}

internal class VerticalAlignModifier(
	private val vertical: Alignment.Vertical,
) : ParentDataModifier {

	override fun modifyParentData(parentData: Any?): Any {
		return ((parentData as? RowColumnParentData) ?: RowColumnParentData()).also {
			it.crossAxisAlignment = CrossAxisAlignment.vertical(vertical)
		}
	}

	override fun toString(): String = "VerticalAlign($vertical)"
}

/**
 * Parent data associated with children.
 */
internal data class RowColumnParentData(
	var weight: Float = 0f,
	var fill: Boolean = true,
	var crossAxisAlignment: CrossAxisAlignment? = null,
)

/**
 * Used to specify how a layout chooses its own size when multiple behaviors are possible.
 */
internal enum class SizeMode {
	/**
	 * Minimize the amount of free space by wrapping the children,
	 * subject to the incoming layout constraints.
	 */
	Wrap,

	/**
	 * Maximize the amount of free space by expanding to fill the available space,
	 * subject to the incoming layout constraints.
	 */
	Expand,
}
