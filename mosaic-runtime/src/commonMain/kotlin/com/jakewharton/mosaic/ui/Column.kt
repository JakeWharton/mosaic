@file:JvmName("Column")

package com.jakewharton.mosaic.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import com.jakewharton.mosaic.layout.MeasurePolicy
import com.jakewharton.mosaic.modifier.Modifier
import kotlin.jvm.JvmName

@Composable
public fun Column(
	modifier: Modifier = Modifier,
	verticalArrangement: Arrangement.Vertical = Arrangement.Top,
	horizontalAlignment: Alignment.Horizontal = Alignment.Start,
	content: @Composable ColumnScope.() -> Unit,
) {
	val measurePolicy = columnMeasurePolicy(verticalArrangement, horizontalAlignment)
	Layout(
		content = { ColumnScopeInstance.content() },
		modifier = modifier,
		debugInfo = { "Column(arrangement=$verticalArrangement, alignment=$horizontalAlignment)" },
		measurePolicy = measurePolicy,
	)
}

internal val DefaultColumnMeasurePolicy: MeasurePolicy = RowColumnMeasurePolicy(
	orientation = LayoutOrientation.Vertical,
	verticalArrangement = Arrangement.Top,
	horizontalArrangement = null,
	arrangementSpacing = Arrangement.Top.spacing,
	crossAxisAlignment = CrossAxisAlignment.horizontal(Alignment.Start),
	crossAxisSize = SizeMode.Wrap,
)

@Composable
internal fun columnMeasurePolicy(
	verticalArrangement: Arrangement.Vertical,
	horizontalAlignment: Alignment.Horizontal,
): MeasurePolicy =
	if (verticalArrangement == Arrangement.Top && horizontalAlignment == Alignment.Start) {
		DefaultColumnMeasurePolicy
	} else {
		remember(verticalArrangement, horizontalAlignment) {
			RowColumnMeasurePolicy(
				orientation = LayoutOrientation.Vertical,
				verticalArrangement = verticalArrangement,
				horizontalArrangement = null,
				arrangementSpacing = verticalArrangement.spacing,
				crossAxisAlignment = CrossAxisAlignment.horizontal(horizontalAlignment),
				crossAxisSize = SizeMode.Wrap,
			)
		}
	}

/**
 * Scope for the children of [Column].
 */
@LayoutScopeMarker
@Immutable
public interface ColumnScope {

	/**
	 * Size the element's height proportional to its [weight] relative to other weighted sibling
	 * elements in the [Column]. The parent will divide the vertical space remaining after measuring
	 * unweighted child elements and distribute it according to this weight.
	 * When [fill] is true, the element will be forced to occupy the whole height allocated to it.
	 * Otherwise, the element is allowed to be smaller - this will result in [Column] being smaller,
	 * as the unused allocated height will not be redistributed to other siblings.
	 *
	 * @param weight The proportional height to give to this element, as related to the total of
	 * all weighted siblings. Must be positive.
	 * @param fill When `true`, the element will occupy the whole height allocated.
	 */
	@Stable
	public fun Modifier.weight(
		weight: Float,
		fill: Boolean = true,
	): Modifier

	/**
	 * Align the element horizontally within the [Column]. This alignment will have priority over
	 * the [Column]'s `horizontalAlignment` parameter.
	 */
	@Stable
	public fun Modifier.align(alignment: Alignment.Horizontal): Modifier
}

private object ColumnScopeInstance : ColumnScope {

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
	override fun Modifier.align(alignment: Alignment.Horizontal) = this.then(
		HorizontalAlignModifier(horizontal = alignment),
	)
}
