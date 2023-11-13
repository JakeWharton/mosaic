package com.jakewharton.mosaic.layout

import androidx.compose.runtime.Stable
import com.jakewharton.mosaic.modifier.Modifier
import com.jakewharton.mosaic.ui.unit.Constraints
import com.jakewharton.mosaic.ui.unit.IntSize
import com.jakewharton.mosaic.ui.unit.constrain
import com.jakewharton.mosaic.ui.unit.constrainHeight
import com.jakewharton.mosaic.ui.unit.constrainWidth

/**
 * Declare the preferred width of the content to be exactly [width]. The incoming measurement
 * [Constraints] may override this value, forcing the content to be either smaller or larger.
 *
 * For a modifier that sets the width of the content regardless of the incoming constraints see
 * [Modifier.requiredWidth]. See [height] or [size] to set other preferred dimensions.
 * See [widthIn], [heightIn] or [sizeIn] to set a preferred size range.
 */
@Stable
public fun Modifier.width(width: Int): Modifier = this.then(
	SizeModifier(
		minWidth = width,
		maxWidth = width,
		enforceIncoming = true,
	),
)

/**
 * Declare the preferred height of the content to be exactly [height]. The incoming measurement
 * [Constraints] may override this value, forcing the content to be either smaller or larger.
 *
 * For a modifier that sets the height of the content regardless of the incoming constraints see
 * [Modifier.requiredHeight]. See [width] or [size] to set other preferred dimensions.
 * See [widthIn], [heightIn] or [sizeIn] to set a preferred size range.
 */
@Stable
public fun Modifier.height(height: Int): Modifier = this.then(
	SizeModifier(
		minHeight = height,
		maxHeight = height,
		enforceIncoming = true,
	),
)

/**
 * Declare the preferred size of the content to be exactly [size] square. The incoming measurement
 * [Constraints] may override this value, forcing the content to be either smaller or larger.
 *
 * For a modifier that sets the size of the content regardless of the incoming constraints, see
 * [Modifier.requiredSize]. See [width] or [height] to set width or height alone.
 * See [widthIn], [heightIn] or [sizeIn] to set a preferred size range.
 */
@Stable
public fun Modifier.size(size: Int): Modifier = this.then(
	SizeModifier(
		minWidth = size,
		maxWidth = size,
		minHeight = size,
		maxHeight = size,
		enforceIncoming = true,
	),
)

/**
 * Declare the preferred size of the content to be exactly [width] by [height]. The incoming
 * measurement [Constraints] may override this value, forcing the content to be either smaller or
 * larger.
 *
 * For a modifier that sets the size of the content regardless of the incoming constraints, see
 * [Modifier.requiredSize]. See [width] or [height] to set width or height alone.
 * See [widthIn], [heightIn] or [sizeIn] to set a preferred size range.
 */
@Stable
public fun Modifier.size(width: Int, height: Int): Modifier = this.then(
	SizeModifier(
		minWidth = width,
		maxWidth = width,
		minHeight = height,
		maxHeight = height,
		enforceIncoming = true,
	),
)

/**
 * Declare the preferred size of the content to be exactly [size]. The incoming
 * measurement [Constraints] may override this value, forcing the content to be either smaller or
 * larger.
 *
 * For a modifier that sets the size of the content regardless of the incoming constraints, see
 * [Modifier.requiredSize]. See [width] or [height] to set width or height alone.
 * See [widthIn], [heightIn] or [sizeIn] to set a preferred size range.
 */
@Stable
public fun Modifier.size(size: IntSize): Modifier = size(size.width, size.height)

/**
 * Constrain the width of the content to be between [min] and [max] as permitted
 * by the incoming measurement [Constraints]. If the incoming constraints are more restrictive
 * the requested size will obey the incoming constraints and attempt to be as close as possible
 * to the preferred size.
 */
@Stable
public fun Modifier.widthIn(
	min: Int = Unspecified,
	max: Int = Unspecified,
): Modifier = this.then(
	SizeModifier(
		minWidth = min,
		maxWidth = max,
		enforceIncoming = true,
	),
)

/**
 * Constrain the height of the content to be between [min] and [max] as permitted
 * by the incoming measurement [Constraints]. If the incoming constraints are more restrictive
 * the requested size will obey the incoming constraints and attempt to be as close as possible
 * to the preferred size.
 */
@Stable
public fun Modifier.heightIn(
	min: Int = Unspecified,
	max: Int = Unspecified,
): Modifier = this.then(
	SizeModifier(
		minHeight = min,
		maxHeight = max,
		enforceIncoming = true,
	),
)

/**
 * Constrain the width of the content to be between [minWidth] and [maxWidth] and the height
 * of the content to be between [minHeight] and [maxHeight] as permitted by the incoming
 * measurement [Constraints]. If the incoming constraints are more restrictive the requested size
 * will obey the incoming constraints and attempt to be as close as possible to the preferred size.
 */
@Stable
public fun Modifier.sizeIn(
	minWidth: Int = Unspecified,
	minHeight: Int = Unspecified,
	maxWidth: Int = Unspecified,
	maxHeight: Int = Unspecified,
): Modifier = this.then(
	SizeModifier(
		minWidth = minWidth,
		minHeight = minHeight,
		maxWidth = maxWidth,
		maxHeight = maxHeight,
		enforceIncoming = true,
	),
)

/**
 * Declare the width of the content to be exactly [width]. The incoming measurement
 * [Constraints] will not override this value. If the content chooses a size that does not
 * satisfy the incoming [Constraints], the parent layout will be reported a size coerced
 * in the [Constraints], and the position of the content will be automatically offset to be
 * centered on the space assigned to the child by the parent layout under the assumption that
 * [Constraints] were respected.
 *
 * See [requiredWidthIn] and [requiredSizeIn] to set a size range.
 * See [width] to set a preferred width, which is only respected when the incoming
 * constraints allow it.
 */
@Stable
public fun Modifier.requiredWidth(width: Int): Modifier = this.then(
	SizeModifier(
		minWidth = width,
		maxWidth = width,
		enforceIncoming = false,
	),
)

/**
 * Declare the height of the content to be exactly [height]. The incoming measurement
 * [Constraints] will not override this value. If the content chooses a size that does not
 * satisfy the incoming [Constraints], the parent layout will be reported a size coerced
 * in the [Constraints], and the position of the content will be automatically offset to be
 * centered on the space assigned to the child by the parent layout under the assumption that
 * [Constraints] were respected.
 *
 * See [requiredHeightIn] and [requiredSizeIn] to set a size range.
 * See [height] to set a preferred height, which is only respected when the incoming
 * constraints allow it.
 */
@Stable
public fun Modifier.requiredHeight(height: Int): Modifier = this.then(
	SizeModifier(
		minHeight = height,
		maxHeight = height,
		enforceIncoming = false,
	),
)

/**
 * Declare the size of the content to be exactly [size] width and height. The incoming measurement
 * [Constraints] will not override this value. If the content chooses a size that does not
 * satisfy the incoming [Constraints], the parent layout will be reported a size coerced
 * in the [Constraints], and the position of the content will be automatically offset to be
 * centered on the space assigned to the child by the parent layout under the assumption that
 * [Constraints] were respected.
 *
 * See [requiredSizeIn] to set a size range.
 * See [size] to set a preferred size, which is only respected when the incoming
 * constraints allow it.
 */
@Stable
public fun Modifier.requiredSize(size: Int): Modifier = this.then(
	SizeModifier(
		minWidth = size,
		maxWidth = size,
		minHeight = size,
		maxHeight = size,
		enforceIncoming = false,
	),
)

/**
 * Declare the size of the content to be exactly [width] and [height]. The incoming measurement
 * [Constraints] will not override this value. If the content chooses a size that does not
 * satisfy the incoming [Constraints], the parent layout will be reported a size coerced
 * in the [Constraints], and the position of the content will be automatically offset to be
 * centered on the space assigned to the child by the parent layout under the assumption that
 * [Constraints] were respected.
 *
 * See [requiredSizeIn] to set a size range.
 * See [size] to set a preferred size, which is only respected when the incoming
 * constraints allow it.
 */
@Stable
public fun Modifier.requiredSize(width: Int, height: Int): Modifier = this.then(
	SizeModifier(
		minWidth = width,
		maxWidth = width,
		minHeight = height,
		maxHeight = height,
		enforceIncoming = false,
	),
)

/**
 * Declare the size of the content to be exactly [size]. The incoming measurement
 * [Constraints] will not override this value. If the content chooses a size that does not
 * satisfy the incoming [Constraints], the parent layout will be reported a size coerced
 * in the [Constraints], and the position of the content will be automatically offset to be
 * centered on the space assigned to the child by the parent layout under the assumption that
 * [Constraints] were respected.
 *
 * See [requiredSizeIn] to set a size range.
 * See [size] to set a preferred size, which is only respected when the incoming
 * constraints allow it.
 */
@Stable
public fun Modifier.requiredSize(size: IntSize): Modifier = requiredSize(size.width, size.height)

/**
 * Constrain the width of the content to be between [min] and [max].
 * If the content chooses a size that does not satisfy the incoming [Constraints], the
 * parent layout will be reported a size coerced in the [Constraints], and the position
 * of the content will be automatically offset to be centered on the space assigned to
 * the child by the parent layout under the assumption that [Constraints] were respected.
 */
@Stable
public fun Modifier.requiredWidthIn(
	min: Int = Unspecified,
	max: Int = Unspecified,
): Modifier = this.then(
	SizeModifier(
		minWidth = min,
		maxWidth = max,
		enforceIncoming = false,
	),
)

/**
 * Constrain the height of the content to be between [min] and [max].
 * If the content chooses a size that does not satisfy the incoming [Constraints], the
 * parent layout will be reported a size coerced in the [Constraints], and the position
 * of the content will be automatically offset to be centered on the space assigned to
 * the child by the parent layout under the assumption that [Constraints] were respected.
 */
@Stable
public fun Modifier.requiredHeightIn(
	min: Int = Unspecified,
	max: Int = Unspecified,
): Modifier = this.then(
	SizeModifier(
		minHeight = min,
		maxHeight = max,
		enforceIncoming = false,
	),
)

/**
 * Constrain the width of the content to be between [minWidth] and [maxWidth], and the
 * height of the content to be between [minHeight] and [maxHeight].
 * If the content chooses a size that does not satisfy the incoming [Constraints], the
 * parent layout will be reported a size coerced in the [Constraints], and the position
 * of the content will be automatically offset to be centered on the space assigned to
 * the child by the parent layout under the assumption that [Constraints] were respected.
 */
@Stable
public fun Modifier.requiredSizeIn(
	minWidth: Int = Unspecified,
	minHeight: Int = Unspecified,
	maxWidth: Int = Unspecified,
	maxHeight: Int = Unspecified,
): Modifier = this.then(
	SizeModifier(
		minWidth = minWidth,
		minHeight = minHeight,
		maxWidth = maxWidth,
		maxHeight = maxHeight,
		enforceIncoming = false,
	),
)

private class SizeModifier(
	private val minWidth: Int = Unspecified,
	private val minHeight: Int = Unspecified,
	private val maxWidth: Int = Unspecified,
	private val maxHeight: Int = Unspecified,
	private val enforceIncoming: Boolean,
) : LayoutModifier {
	private val targetConstraints: Constraints
		get() {
			val maxWidth = if (maxWidth != Unspecified) {
				maxWidth.coerceAtLeast(0)
			} else {
				Constraints.Infinity
			}
			val maxHeight = if (maxHeight != Unspecified) {
				maxHeight.coerceAtLeast(0)
			} else {
				Constraints.Infinity
			}
			val minWidth = if (minWidth != Unspecified) {
				minWidth.coerceAtMost(maxWidth).coerceAtLeast(0).let {
					if (it != Constraints.Infinity) it else 0
				}
			} else {
				0
			}
			val minHeight = if (minHeight != Unspecified) {
				minHeight.coerceAtMost(maxHeight).coerceAtLeast(0).let {
					if (it != Constraints.Infinity) it else 0
				}
			} else {
				0
			}
			return Constraints(
				minWidth = minWidth,
				minHeight = minHeight,
				maxWidth = maxWidth,
				maxHeight = maxHeight,
			)
		}

	override fun MeasureScope.measure(
		measurable: Measurable,
		constraints: Constraints,
	): MeasureResult {
		val wrappedConstraints = targetConstraints.let { targetConstraints ->
			if (enforceIncoming) {
				constraints.constrain(targetConstraints)
			} else {
				val resolvedMinWidth = if (minWidth != Unspecified) {
					targetConstraints.minWidth
				} else {
					constraints.minWidth.coerceAtMost(targetConstraints.maxWidth)
				}
				val resolvedMaxWidth = if (maxWidth != Unspecified) {
					targetConstraints.maxWidth
				} else {
					constraints.maxWidth.coerceAtLeast(targetConstraints.minWidth)
				}
				val resolvedMinHeight = if (minHeight != Unspecified) {
					targetConstraints.minHeight
				} else {
					constraints.minHeight.coerceAtMost(targetConstraints.maxHeight)
				}
				val resolvedMaxHeight = if (maxHeight != Unspecified) {
					targetConstraints.maxHeight
				} else {
					constraints.maxHeight.coerceAtLeast(targetConstraints.minHeight)
				}
				Constraints(
					resolvedMinWidth,
					resolvedMaxWidth,
					resolvedMinHeight,
					resolvedMaxHeight,
				)
			}
		}
		val placeable = measurable.measure(wrappedConstraints)
		return layout(placeable.width, placeable.height) {
			placeable.place(0, 0)
		}
	}

	override fun minIntrinsicWidth(
		measurable: IntrinsicMeasurable,
		height: Int,
	): Int {
		val constraints = targetConstraints
		return if (constraints.hasFixedWidth) {
			constraints.maxWidth
		} else {
			constraints.constrainWidth(measurable.minIntrinsicWidth(height))
		}
	}

	override fun minIntrinsicHeight(
		measurable: IntrinsicMeasurable,
		width: Int,
	): Int {
		val constraints = targetConstraints
		return if (constraints.hasFixedHeight) {
			constraints.maxHeight
		} else {
			constraints.constrainHeight(measurable.minIntrinsicHeight(width))
		}
	}

	override fun maxIntrinsicWidth(
		measurable: IntrinsicMeasurable,
		height: Int,
	): Int {
		val constraints = targetConstraints
		return if (constraints.hasFixedWidth) {
			constraints.maxWidth
		} else {
			constraints.constrainWidth(measurable.maxIntrinsicWidth(height))
		}
	}

	override fun maxIntrinsicHeight(
		measurable: IntrinsicMeasurable,
		width: Int,
	): Int {
		val constraints = targetConstraints
		return if (constraints.hasFixedHeight) {
			constraints.maxHeight
		} else {
			constraints.constrainHeight(measurable.maxIntrinsicHeight(width))
		}
	}

	override fun toString(): String {
		val params = buildList {
			if (minWidth != Unspecified) {
				add("minW=$minWidth")
			}
			if (minHeight != Unspecified) {
				add("minH=$minHeight")
			}
			if (maxWidth != Unspecified) {
				add("maxW=$maxWidth")
			}
			if (maxHeight != Unspecified) {
				add("maxH=$maxHeight")
			}
			add("enforceIncoming=$enforceIncoming")
		}
		return "SizeModifier(${params.joinToString(", ")})"
	}
}

private const val Unspecified = Int.MIN_VALUE
