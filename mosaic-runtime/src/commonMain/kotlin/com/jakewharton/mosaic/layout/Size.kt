package com.jakewharton.mosaic.layout

import androidx.compose.runtime.Stable
import com.jakewharton.mosaic.modifier.Modifier
import com.jakewharton.mosaic.ui.Alignment
import com.jakewharton.mosaic.ui.unit.Constraints
import com.jakewharton.mosaic.ui.unit.IntOffset
import com.jakewharton.mosaic.ui.unit.IntSize
import com.jakewharton.mosaic.ui.unit.constrain
import com.jakewharton.mosaic.ui.unit.constrainHeight
import com.jakewharton.mosaic.ui.unit.constrainWidth
import kotlin.math.roundToInt

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

/**
 * Have the content fill (possibly only partially) the [Constraints.maxWidth] of the incoming
 * measurement constraints, by setting the [minimum width][Constraints.minWidth] and the
 * [maximum width][Constraints.maxWidth] to be equal to the [maximum width][Constraints.maxWidth]
 * multiplied by [fraction]. Note that, by default, the [fraction] is 1, so the modifier will
 * make the content fill the whole available width. If the incoming maximum width is
 * [Constraints.Infinity] this modifier will have no effect.
 *
 * @param fraction The fraction of the maximum width to use, between `0` and `1`, inclusive.
 */
@Stable
public fun Modifier.fillMaxWidth(fraction: Float = 1f): Modifier {
	require(fraction in 0.0f..1.0f) { "Fraction must be >= 0 and <= 1" }
	return this.then(if (fraction == 1f) FillWholeMaxWidth else FillModifier.width(fraction))
}

private val FillWholeMaxWidth = FillModifier.width(1f)

/**
 * Have the content fill (possibly only partially) the [Constraints.maxHeight] of the incoming
 * measurement constraints, by setting the [minimum height][Constraints.minHeight] and the
 * [maximum height][Constraints.maxHeight] to be equal to the
 * [maximum height][Constraints.maxHeight] multiplied by [fraction]. Note that, by default,
 * the [fraction] is 1, so the modifier will make the content fill the whole available height.
 * If the incoming maximum height is [Constraints.Infinity] this modifier will have no effect.
 *
 * @param fraction The fraction of the maximum height to use, between `0` and `1`, inclusive.
 */
@Stable
public fun Modifier.fillMaxHeight(fraction: Float = 1f): Modifier {
	require(fraction in 0.0f..1.0f) { "Fraction must be >= 0 and <= 1" }
	return this.then(if (fraction == 1f) FillWholeMaxHeight else FillModifier.height(fraction))
}

private val FillWholeMaxHeight = FillModifier.height(1f)

/**
 * Have the content fill (possibly only partially) the [Constraints.maxWidth] and
 * [Constraints.maxHeight] of the incoming measurement constraints, by setting the
 * [minimum width][Constraints.minWidth] and the [maximum width][Constraints.maxWidth] to be
 * equal to the [maximum width][Constraints.maxWidth] multiplied by [fraction], as well as
 * the [minimum height][Constraints.minHeight] and the [maximum height][Constraints.minHeight]
 * to be equal to the [maximum height][Constraints.maxHeight] multiplied by [fraction].
 * Note that, by default, the [fraction] is 1, so the modifier will make the content fill
 * the whole available space.
 * If the incoming maximum width or height is [Constraints.Infinity] this modifier will have no
 * effect in that dimension.
 *
 * @param fraction The fraction of the maximum size to use, between `0` and `1`, inclusive.
 */
@Stable
public fun Modifier.fillMaxSize(fraction: Float = 1f): Modifier {
	require(fraction in 0.0f..1.0f) { "Fraction must be >= 0 and <= 1" }
	return this.then(if (fraction == 1f) FillWholeMaxSize else FillModifier.size(fraction))
}

private val FillWholeMaxSize = FillModifier.size(1f)

/**
 * Allow the content to measure at its desired width without regard for the incoming measurement
 * [minimum width constraint][Constraints.minWidth], and, if [unbounded] is true, also without
 * regard for the incoming measurement [maximum width constraint][Constraints.maxWidth]. If
 * the content's measured size is smaller than the minimum width constraint, [align]
 * it within that minimum width space. If the content's measured size is larger than the maximum
 * width constraint (only possible when [unbounded] is true), [align] over the maximum
 * width space.
 */
@Stable
public fun Modifier.wrapContentWidth(
	align: Alignment.Horizontal = Alignment.CenterHorizontally,
	unbounded: Boolean = false,
): Modifier = this.then(
	if (align == Alignment.CenterHorizontally && !unbounded) {
		WrapContentWidthCenter
	} else if (align == Alignment.Start && !unbounded) {
		WrapContentWidthStart
	} else {
		WrapContentModifier.width(align, unbounded)
	},
)

private val WrapContentWidthCenter =
	WrapContentModifier.width(Alignment.CenterHorizontally, false)
private val WrapContentWidthStart = WrapContentModifier.width(Alignment.Start, false)

/**
 * Allow the content to measure at its desired height without regard for the incoming measurement
 * [minimum height constraint][Constraints.minHeight], and, if [unbounded] is true, also without
 * regard for the incoming measurement [maximum height constraint][Constraints.maxHeight]. If the
 * content's measured size is smaller than the minimum height constraint, [align] it within
 * that minimum height space. If the content's measured size is larger than the maximum height
 * constraint (only possible when [unbounded] is true), [align] over the maximum height space.
 */
@Stable
public fun Modifier.wrapContentHeight(
	align: Alignment.Vertical = Alignment.CenterVertically,
	unbounded: Boolean = false,
): Modifier = this.then(
	if (align == Alignment.CenterVertically && !unbounded) {
		WrapContentHeightCenter
	} else if (align == Alignment.Top && !unbounded) {
		WrapContentHeightTop
	} else {
		WrapContentModifier.height(align, unbounded)
	},
)

private val WrapContentHeightCenter =
	WrapContentModifier.height(Alignment.CenterVertically, false)
private val WrapContentHeightTop = WrapContentModifier.height(Alignment.Top, false)

/**
 * Allow the content to measure at its desired size without regard for the incoming measurement
 * [minimum width][Constraints.minWidth] or [minimum height][Constraints.minHeight] constraints,
 * and, if [unbounded] is true, also without regard for the incoming maximum constraints.
 * If the content's measured size is smaller than the minimum size constraint, [align] it
 * within that minimum sized space. If the content's measured size is larger than the maximum
 * size constraint (only possible when [unbounded] is true), [align] within the maximum space.
 */
@Stable
public fun Modifier.wrapContentSize(
	align: Alignment = Alignment.Center,
	unbounded: Boolean = false,
): Modifier = this.then(
	if (align == Alignment.Center && !unbounded) {
		WrapContentSizeCenter
	} else if (align == Alignment.TopStart && !unbounded) {
		WrapContentSizeTopStart
	} else {
		WrapContentModifier.size(align, unbounded)
	},
)

private val WrapContentSizeCenter = WrapContentModifier.size(Alignment.Center, false)
private val WrapContentSizeTopStart = WrapContentModifier.size(Alignment.TopStart, false)

/**
 * Constrain the size of the wrapped layout only when it would be otherwise unconstrained:
 * the [minWidth] and [minHeight] constraints are only applied when the incoming corresponding
 * constraint is `0`.
 * The modifier can be used, for example, to define a default min size of a component,
 * while still allowing it to be overidden with smaller min sizes across usages.
 */
@Stable
public fun Modifier.defaultMinSize(
	minWidth: Int = Unspecified,
	minHeight: Int = Unspecified,
): Modifier = this.then(UnspecifiedConstraintsModifier(minWidth = minWidth, minHeight = minHeight))

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

	override fun equals(other: Any?): Boolean {
		if (this === other) return true
		if (other !is SizeModifier) return false

		if (minWidth != other.minWidth) return false
		if (minHeight != other.minHeight) return false
		if (maxWidth != other.maxWidth) return false
		if (maxHeight != other.maxHeight) return false
		if (enforceIncoming != other.enforceIncoming) return false

		return true
	}

	override fun hashCode(): Int {
		var result = minWidth.hashCode()
		result = 31 * result + minHeight.hashCode()
		result = 31 * result + maxWidth.hashCode()
		result = 31 * result + maxHeight.hashCode()
		result = 31 * result + enforceIncoming.hashCode()
		return result
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

private class FillModifier(
	private val direction: Direction,
	private val fraction: Float,
) : LayoutModifier {
	override fun MeasureScope.measure(
		measurable: Measurable,
		constraints: Constraints,
	): MeasureResult {
		val minWidth: Int
		val maxWidth: Int
		if (constraints.hasBoundedWidth && direction != Direction.Vertical) {
			val width = (constraints.maxWidth * fraction).roundToInt()
				.coerceIn(constraints.minWidth, constraints.maxWidth)
			minWidth = width
			maxWidth = width
		} else {
			minWidth = constraints.minWidth
			maxWidth = constraints.maxWidth
		}
		val minHeight: Int
		val maxHeight: Int
		if (constraints.hasBoundedHeight && direction != Direction.Horizontal) {
			val height = (constraints.maxHeight * fraction).roundToInt()
				.coerceIn(constraints.minHeight, constraints.maxHeight)
			minHeight = height
			maxHeight = height
		} else {
			minHeight = constraints.minHeight
			maxHeight = constraints.maxHeight
		}
		val placeable = measurable.measure(
			Constraints(minWidth, maxWidth, minHeight, maxHeight),
		)

		return layout(placeable.width, placeable.height) {
			placeable.place(0, 0)
		}
	}

	override fun equals(other: Any?): Boolean {
		if (this === other) return true
		if (other !is FillModifier) return false

		if (direction != other.direction) return false
		if (fraction != other.fraction) return false

		return true
	}

	override fun hashCode(): Int {
		var result = direction.hashCode()
		result = 31 * result + fraction.hashCode()
		return result
	}

	override fun toString(): String = "Fill(direction=$direction, fraction=$fraction)"

	companion object {
		@Stable
		fun width(fraction: Float) = FillModifier(
			direction = Direction.Horizontal,
			fraction = fraction,
		)

		@Stable
		fun height(fraction: Float) = FillModifier(
			direction = Direction.Vertical,
			fraction = fraction,
		)

		@Stable
		fun size(fraction: Float) = FillModifier(
			direction = Direction.Both,
			fraction = fraction,
		)
	}
}

private class WrapContentModifier(
	private val direction: Direction,
	private val unbounded: Boolean,
	private val alignmentCallback: (IntSize) -> IntOffset,
	private val align: Any,
) : LayoutModifier {

	override fun MeasureScope.measure(
		measurable: Measurable,
		constraints: Constraints,
	): MeasureResult {
		val wrappedConstraints = Constraints(
			minWidth = if (direction != Direction.Vertical) 0 else constraints.minWidth,
			minHeight = if (direction != Direction.Horizontal) 0 else constraints.minHeight,
			maxWidth = if (direction != Direction.Vertical && unbounded) {
				Constraints.Infinity
			} else {
				constraints.maxWidth
			},
			maxHeight = if (direction != Direction.Horizontal && unbounded) {
				Constraints.Infinity
			} else {
				constraints.maxHeight
			},
		)
		val placeable = measurable.measure(wrappedConstraints)
		val wrapperWidth = placeable.width.coerceIn(constraints.minWidth, constraints.maxWidth)
		val wrapperHeight = placeable.height.coerceIn(constraints.minHeight, constraints.maxHeight)
		return layout(
			wrapperWidth,
			wrapperHeight,
		) {
			val position = alignmentCallback(
				IntSize(wrapperWidth - placeable.width, wrapperHeight - placeable.height),
			)
			placeable.place(position)
		}
	}

	override fun equals(other: Any?): Boolean {
		if (this === other) return true
		if (other === null) return false
		if (this::class != other::class) return false

		other as WrapContentModifier

		if (direction != other.direction) return false
		if (unbounded != other.unbounded) return false
		if (align != other.align) return false

		return true
	}

	override fun hashCode(): Int {
		var result = direction.hashCode()
		result = 31 * result + unbounded.hashCode()
		result = 31 * result + align.hashCode()
		return result
	}

	override fun toString(): String = "WrapContent(direction=$direction, unbounded=$unbounded)"

	companion object {
		@Stable
		fun width(
			align: Alignment.Horizontal,
			unbounded: Boolean,
		) = WrapContentModifier(
			direction = Direction.Horizontal,
			unbounded = unbounded,
			alignmentCallback = { size -> IntOffset(align.align(0, size.width), 0) },
			align = align,
		)

		@Stable
		fun height(
			align: Alignment.Vertical,
			unbounded: Boolean,
		) = WrapContentModifier(
			direction = Direction.Vertical,
			unbounded = unbounded,
			alignmentCallback = { size -> IntOffset(0, align.align(0, size.height)) },
			align = align,
		)

		@Stable
		fun size(
			align: Alignment,
			unbounded: Boolean,
		) = WrapContentModifier(
			direction = Direction.Both,
			unbounded = unbounded,
			alignmentCallback = { size -> align.align(IntSize.Zero, size) },
			align = align,
		)
	}
}

private class UnspecifiedConstraintsModifier(
	private val minWidth: Int = Unspecified,
	private val minHeight: Int = Unspecified,
) : LayoutModifier {
	override fun MeasureScope.measure(
		measurable: Measurable,
		constraints: Constraints,
	): MeasureResult {
		val wrappedConstraints = Constraints(
			if (minWidth != Unspecified && constraints.minWidth == 0) {
				minWidth.coerceAtMost(constraints.maxWidth).coerceAtLeast(0)
			} else {
				constraints.minWidth
			},
			constraints.maxWidth,
			if (minHeight != Unspecified && constraints.minHeight == 0) {
				minHeight.coerceAtMost(constraints.maxHeight).coerceAtLeast(0)
			} else {
				constraints.minHeight
			},
			constraints.maxHeight,
		)
		val placeable = measurable.measure(wrappedConstraints)
		return layout(placeable.width, placeable.height) {
			placeable.place(0, 0)
		}
	}

	override fun minIntrinsicWidth(
		measurable: IntrinsicMeasurable,
		height: Int,
	) = measurable.minIntrinsicWidth(height).coerceAtLeast(
		if (minWidth != Unspecified) minWidth else 0,
	)

	override fun maxIntrinsicWidth(
		measurable: IntrinsicMeasurable,
		height: Int,
	) = measurable.maxIntrinsicWidth(height).coerceAtLeast(
		if (minWidth != Unspecified) minWidth else 0,
	)

	override fun minIntrinsicHeight(
		measurable: IntrinsicMeasurable,
		width: Int,
	) = measurable.minIntrinsicHeight(width).coerceAtLeast(
		if (minHeight != Unspecified) minHeight else 0,
	)

	override fun maxIntrinsicHeight(
		measurable: IntrinsicMeasurable,
		width: Int,
	) = measurable.maxIntrinsicHeight(width).coerceAtLeast(
		if (minHeight != Unspecified) minHeight else 0,
	)

	override fun equals(other: Any?): Boolean {
		if (other !is UnspecifiedConstraintsModifier) return false
		return minWidth == other.minWidth && minHeight == other.minHeight
	}

	override fun hashCode() = minWidth.hashCode() * 31 + minHeight.hashCode()

	override fun toString(): String {
		val params = buildList {
			if (minWidth != Unspecified) {
				add("minW=$minWidth")
			}
			if (minHeight != Unspecified) {
				add("minH=$minHeight")
			}
		}
		return "UnspecifiedConstraints(${params.joinToString(", ")})"
	}
}

internal enum class Direction {
	Vertical,
	Horizontal,
	Both,
}

private const val Unspecified = Int.MIN_VALUE
