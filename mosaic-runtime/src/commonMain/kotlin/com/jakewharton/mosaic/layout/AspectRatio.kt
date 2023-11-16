package com.jakewharton.mosaic.layout

import androidx.compose.runtime.Stable
import com.jakewharton.mosaic.modifier.Modifier
import com.jakewharton.mosaic.ui.unit.Constraints
import com.jakewharton.mosaic.ui.unit.IntSize
import com.jakewharton.mosaic.ui.unit.isSatisfiedBy
import kotlin.math.roundToInt

/**
 * Attempts to size the content to match a specified aspect ratio by trying to match one of the
 * incoming constraints in the following order: [Constraints.maxWidth], [Constraints.maxHeight],
 * [Constraints.minWidth], [Constraints.minHeight] if [matchHeightConstraintsFirst] is `false`
 * (which is the default), or [Constraints.maxHeight], [Constraints.maxWidth],
 * [Constraints.minHeight], [Constraints.minWidth] if [matchHeightConstraintsFirst] is `true`.
 * The size in the other dimension is determined by the aspect ratio. The combinations will be
 * tried in this order until one non-empty is found to satisfy the constraints. If no valid
 * size is obtained this way, it means that there is no non-empty size satisfying both
 * the constraints and the aspect ratio, so the constraints will not be respected
 * and the content will be sized such that the [Constraints.maxWidth] or [Constraints.maxHeight]
 * is matched (depending on [matchHeightConstraintsFirst]).
 *
 * @param ratio the desired width/height positive ratio
 */
@Stable
public fun Modifier.aspectRatio(
	ratio: Float,
	matchHeightConstraintsFirst: Boolean = false,
): Modifier = this.then(
	AspectRatioModifier(
		ratio,
		matchHeightConstraintsFirst,
	),
)

private class AspectRatioModifier(
	private val aspectRatio: Float,
	private val matchHeightConstraintsFirst: Boolean,
) : LayoutModifier {
	override fun MeasureScope.measure(
		measurable: Measurable,
		constraints: Constraints,
	): MeasureResult {
		val size = constraints.findSize()
		val wrappedConstraints = if (size != IntSize.Zero) {
			Constraints.fixed(size.width, size.height)
		} else {
			constraints
		}
		val placeable = measurable.measure(wrappedConstraints)
		return layout(placeable.width, placeable.height) {
			placeable.place(0, 0)
		}
	}

	override fun minIntrinsicWidth(
		measurable: IntrinsicMeasurable,
		height: Int,
	) = if (height != Constraints.Infinity) {
		(height * aspectRatio).roundToInt()
	} else {
		measurable.minIntrinsicWidth(height)
	}

	override fun maxIntrinsicWidth(
		measurable: IntrinsicMeasurable,
		height: Int,
	) = if (height != Constraints.Infinity) {
		(height * aspectRatio).roundToInt()
	} else {
		measurable.maxIntrinsicWidth(height)
	}

	override fun minIntrinsicHeight(
		measurable: IntrinsicMeasurable,
		width: Int,
	) = if (width != Constraints.Infinity) {
		(width / aspectRatio).roundToInt()
	} else {
		measurable.minIntrinsicHeight(width)
	}

	override fun maxIntrinsicHeight(
		measurable: IntrinsicMeasurable,
		width: Int,
	) = if (width != Constraints.Infinity) {
		(width / aspectRatio).roundToInt()
	} else {
		measurable.maxIntrinsicHeight(width)
	}

	override fun toString(): String =
		"AspectRatio($aspectRatio, matchHeightConstraintsFirst=$matchHeightConstraintsFirst)"

	private fun Constraints.findSize(): IntSize {
		if (!matchHeightConstraintsFirst) {
			tryMaxWidth().also { if (it != IntSize.Zero) return it }
			tryMaxHeight().also { if (it != IntSize.Zero) return it }
			tryMinWidth().also { if (it != IntSize.Zero) return it }
			tryMinHeight().also { if (it != IntSize.Zero) return it }
			tryMaxWidth(enforceConstraints = false).also { if (it != IntSize.Zero) return it }
			tryMaxHeight(enforceConstraints = false).also { if (it != IntSize.Zero) return it }
			tryMinWidth(enforceConstraints = false).also { if (it != IntSize.Zero) return it }
			tryMinHeight(enforceConstraints = false).also { if (it != IntSize.Zero) return it }
		} else {
			tryMaxHeight().also { if (it != IntSize.Zero) return it }
			tryMaxWidth().also { if (it != IntSize.Zero) return it }
			tryMinHeight().also { if (it != IntSize.Zero) return it }
			tryMinWidth().also { if (it != IntSize.Zero) return it }
			tryMaxHeight(enforceConstraints = false).also { if (it != IntSize.Zero) return it }
			tryMaxWidth(enforceConstraints = false).also { if (it != IntSize.Zero) return it }
			tryMinHeight(enforceConstraints = false).also { if (it != IntSize.Zero) return it }
			tryMinWidth(enforceConstraints = false).also { if (it != IntSize.Zero) return it }
		}
		return IntSize.Zero
	}

	private fun Constraints.tryMaxWidth(enforceConstraints: Boolean = true): IntSize {
		val maxWidth = this.maxWidth
		if (maxWidth != Constraints.Infinity) {
			val height = (maxWidth / aspectRatio).roundToInt()
			if (height > 0) {
				val size = IntSize(maxWidth, height)
				if (!enforceConstraints || isSatisfiedBy(size)) {
					return size
				}
			}
		}
		return IntSize.Zero
	}

	private fun Constraints.tryMaxHeight(enforceConstraints: Boolean = true): IntSize {
		val maxHeight = this.maxHeight
		if (maxHeight != Constraints.Infinity) {
			val width = (maxHeight * aspectRatio).roundToInt()
			if (width > 0) {
				val size = IntSize(width, maxHeight)
				if (!enforceConstraints || isSatisfiedBy(size)) {
					return size
				}
			}
		}
		return IntSize.Zero
	}

	private fun Constraints.tryMinWidth(enforceConstraints: Boolean = true): IntSize {
		val minWidth = this.minWidth
		val height = (minWidth / aspectRatio).roundToInt()
		if (height > 0) {
			val size = IntSize(minWidth, height)
			if (!enforceConstraints || isSatisfiedBy(size)) {
				return size
			}
		}
		return IntSize.Zero
	}

	private fun Constraints.tryMinHeight(enforceConstraints: Boolean = true): IntSize {
		val minHeight = this.minHeight
		val width = (minHeight * aspectRatio).roundToInt()
		if (width > 0) {
			val size = IntSize(width, minHeight)
			if (!enforceConstraints || isSatisfiedBy(size)) {
				return size
			}
		}
		return IntSize.Zero
	}
}
