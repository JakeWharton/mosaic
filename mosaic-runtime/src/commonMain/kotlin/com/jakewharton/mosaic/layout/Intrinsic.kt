package com.jakewharton.mosaic.layout

import androidx.compose.runtime.Stable
import com.jakewharton.mosaic.modifier.Modifier
import com.jakewharton.mosaic.ui.unit.Constraints
import com.jakewharton.mosaic.ui.unit.IntOffset
import com.jakewharton.mosaic.ui.unit.constrain

/**
 * Declare the preferred width of the content to be the same as the min or max intrinsic width of
 * the content. The incoming measurement [Constraints] may override this value, forcing the content
 * to be either smaller or larger.
 *
 * See [height] for options of sizing to intrinsic height. Also see [width] and [widthIn] for other
 * options to set the preferred width.
 */
@Stable
public fun Modifier.width(intrinsicSize: IntrinsicSize): Modifier =
	this then IntrinsicWidthModifier(width = intrinsicSize, enforceIncoming = true)

/**
 * Declare the preferred height of the content to be the same as the min or max intrinsic height of
 * the content. The incoming measurement [Constraints] may override this value, forcing the content
 * to be either smaller or larger.
 *
 * See [width] for other options of sizing to intrinsic width. Also see [height] and [heightIn] for
 * other options to set the preferred height.
 */
@Stable
public fun Modifier.height(intrinsicSize: IntrinsicSize): Modifier =
	this then IntrinsicHeightModifier(height = intrinsicSize, enforceIncoming = true)

/**
 * Declare the width of the content to be exactly the same as the min or max intrinsic width of the
 * content. The incoming measurement [Constraints] will not override this value. If the content
 * intrinsic width does not satisfy the incoming [Constraints], the parent layout will be reported a
 * size coerced in the [Constraints], and the position of the content will be automatically offset
 * to be centered on the space assigned to the child by the parent layout under the assumption that
 * [Constraints] were respected.
 *
 * See [height] for options of sizing to intrinsic height. See [width] and [widthIn] for options to
 * set the preferred width. See [requiredWidth] and [requiredWidthIn] for other options to set the
 * required width.
 */
@Stable
public fun Modifier.requiredWidth(intrinsicSize: IntrinsicSize): Modifier =
	this then IntrinsicWidthModifier(width = intrinsicSize, enforceIncoming = false)

/**
 * Declare the height of the content to be exactly the same as the min or max intrinsic height of
 * the content. The incoming measurement [Constraints] will not override this value. If the content
 * intrinsic height does not satisfy the incoming [Constraints], the parent layout will be reported
 * a size coerced in the [Constraints], and the position of the content will be automatically offset
 * to be centered on the space assigned to the child by the parent layout under the assumption that
 * [Constraints] were respected.
 *
 * See [width] for options of sizing to intrinsic width. See [height] and [heightIn] for options to
 * set the preferred height. See [requiredHeight] and [requiredHeightIn] for other options to set
 * the required height.
 */
@Stable
public fun Modifier.requiredHeight(intrinsicSize: IntrinsicSize): Modifier =
	this then IntrinsicHeightModifier(height = intrinsicSize, enforceIncoming = false)

/** Intrinsic size used in [width] or [height] which can refer to width or height. */
public enum class IntrinsicSize {
	Min,
	Max,
}

private class IntrinsicWidthModifier(
	private val width: IntrinsicSize,
	override val enforceIncoming: Boolean,
) : IntrinsicSizeModifier() {
	override fun MeasureScope.calculateContentConstraints(
		measurable: Measurable,
		constraints: Constraints,
	): Constraints {
		var measuredWidth =
			if (width == IntrinsicSize.Min) {
				measurable.minIntrinsicWidth(constraints.maxHeight)
			} else {
				measurable.maxIntrinsicWidth(constraints.maxHeight)
			}
		if (measuredWidth < 0) {
			measuredWidth = 0
		}
		return Constraints.fixedWidth(measuredWidth)
	}

	override fun minIntrinsicWidth(
		measurable: IntrinsicMeasurable,
		height: Int,
	) =
		if (width == IntrinsicSize.Min) {
			measurable.minIntrinsicWidth(height)
		} else {
			measurable.maxIntrinsicWidth(height)
		}

	override fun maxIntrinsicWidth(
		measurable: IntrinsicMeasurable,
		height: Int,
	) =
		if (width == IntrinsicSize.Min) {
			measurable.minIntrinsicWidth(height)
		} else {
			measurable.maxIntrinsicWidth(height)
		}

	override fun toString(): String {
		return "IntrinsicWidthModifier(width=$width, enforceIncoming=$enforceIncoming)"
	}
}

private class IntrinsicHeightModifier(
	private val height: IntrinsicSize,
	override val enforceIncoming: Boolean,
) : IntrinsicSizeModifier() {
	override fun MeasureScope.calculateContentConstraints(
		measurable: Measurable,
		constraints: Constraints,
	): Constraints {
		var measuredHeight =
			if (height == IntrinsicSize.Min) {
				measurable.minIntrinsicHeight(constraints.maxWidth)
			} else {
				measurable.maxIntrinsicHeight(constraints.maxWidth)
			}
		if (measuredHeight < 0) {
			measuredHeight = 0
		}
		return Constraints.fixedHeight(measuredHeight)
	}

	override fun minIntrinsicHeight(
		measurable: IntrinsicMeasurable,
		width: Int,
	) =
		if (height == IntrinsicSize.Min) {
			measurable.minIntrinsicHeight(width)
		} else {
			measurable.maxIntrinsicHeight(width)
		}

	override fun maxIntrinsicHeight(
		measurable: IntrinsicMeasurable,
		width: Int,
	) =
		if (height == IntrinsicSize.Min) {
			measurable.minIntrinsicHeight(width)
		} else {
			measurable.maxIntrinsicHeight(width)
		}

	override fun toString(): String {
		return "IntrinsicHeightModifier(height=$height, enforceIncoming=$enforceIncoming)"
	}
}

private abstract class IntrinsicSizeModifier : LayoutModifier {

	abstract val enforceIncoming: Boolean

	abstract fun MeasureScope.calculateContentConstraints(
		measurable: Measurable,
		constraints: Constraints,
	): Constraints

	final override fun MeasureScope.measure(
		measurable: Measurable,
		constraints: Constraints,
	): MeasureResult {
		val contentConstraints = calculateContentConstraints(measurable, constraints)
		val placeable =
			measurable.measure(
				if (enforceIncoming) {
					constraints.constrain(contentConstraints)
				} else {
					contentConstraints
				},
			)
		return layout(placeable.width, placeable.height) { placeable.place(IntOffset.Zero) }
	}

	override fun minIntrinsicWidth(
		measurable: IntrinsicMeasurable,
		height: Int,
	) = measurable.minIntrinsicWidth(height)

	override fun minIntrinsicHeight(
		measurable: IntrinsicMeasurable,
		width: Int,
	) = measurable.minIntrinsicHeight(width)

	override fun maxIntrinsicWidth(
		measurable: IntrinsicMeasurable,
		height: Int,
	) = measurable.maxIntrinsicWidth(height)

	override fun maxIntrinsicHeight(
		measurable: IntrinsicMeasurable,
		width: Int,
	) = measurable.maxIntrinsicHeight(width)
}
