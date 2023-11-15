@file:JvmName("Row")

package com.jakewharton.mosaic.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import com.jakewharton.mosaic.layout.MeasurePolicy
import com.jakewharton.mosaic.modifier.Modifier
import kotlin.jvm.JvmName

@Composable
public fun Row(
	modifier: Modifier = Modifier,
	horizontalArrangement: Arrangement.Horizontal = Arrangement.Start,
	verticalAlignment: Alignment.Vertical = Alignment.Top,
	content: @Composable RowScope.() -> Unit,
) {
	val measurePolicy = rowMeasurePolicy(horizontalArrangement, verticalAlignment)
	Layout(
		content = { RowScopeInstance.content() },
		modifiers = modifier,
		debugInfo = { "Row(arrangement=$horizontalArrangement, alignment=$verticalAlignment)" },
		measurePolicy = measurePolicy,
	)
}

internal val DefaultRowMeasurePolicy: MeasurePolicy = RowColumnMeasurePolicy(
	orientation = LayoutOrientation.Horizontal,
	horizontalArrangement = Arrangement.Start,
	verticalArrangement = null,
	arrangementSpacing = Arrangement.Start.spacing,
	crossAxisAlignment = CrossAxisAlignment.vertical(Alignment.Top),
	crossAxisSize = SizeMode.Wrap,
)

@Composable
internal fun rowMeasurePolicy(
	horizontalArrangement: Arrangement.Horizontal,
	verticalAlignment: Alignment.Vertical,
): MeasurePolicy =
	if (horizontalArrangement == Arrangement.Start && verticalAlignment == Alignment.Top) {
		DefaultRowMeasurePolicy
	} else {
		remember(horizontalArrangement, verticalAlignment) {
			RowColumnMeasurePolicy(
				orientation = LayoutOrientation.Horizontal,
				horizontalArrangement = horizontalArrangement,
				verticalArrangement = null,
				arrangementSpacing = horizontalArrangement.spacing,
				crossAxisAlignment = CrossAxisAlignment.vertical(verticalAlignment),
				crossAxisSize = SizeMode.Wrap,
			)
		}
	}

/**
 * Scope for the children of [Row].
 */
@LayoutScopeMarker
@Immutable
public interface RowScope {

	/**
	 * Size the element's width proportional to its [weight] relative to other weighted sibling
	 * elements in the [Row]. The parent will divide the horizontal space remaining after measuring
	 * unweighted child elements and distribute it according to this weight.
	 * When [fill] is true, the element will be forced to occupy the whole width allocated to it.
	 * Otherwise, the element is allowed to be smaller - this will result in [Row] being smaller,
	 * as the unused allocated width will not be redistributed to other siblings.
	 *
	 * @param weight The proportional width to give to this element, as related to the total of
	 * all weighted siblings. Must be positive.
	 * @param fill When `true`, the element will occupy the whole width allocated.
	 */
	@Stable
	public fun Modifier.weight(
		weight: Float,
		fill: Boolean = true,
	): Modifier

	/**
	 * Align the element vertically within the [Row]. This alignment will have priority over
	 * the [Row]'s `verticalAlignment` parameter.
	 */
	@Stable
	public fun Modifier.align(alignment: Alignment.Vertical): Modifier
}

private object RowScopeInstance : RowScope {

	@Stable
	override fun Modifier.weight(weight: Float, fill: Boolean): Modifier {
		require(weight > 0.0) { "invalid weight $weight; must be greater than zero" }
		return this.then(
			LayoutWeightModifier(
				// Coerce Float.POSITIVE_INFINITY to Float.MAX_VALUE to avoid errors
				weight = weight.coerceAtMost(Float.MAX_VALUE),
				fill = fill,
			),
		)
	}

	@Stable
	override fun Modifier.align(alignment: Alignment.Vertical) = this.then(
		VerticalAlignModifier(vertical = alignment),
	)
}
