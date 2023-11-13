@file:JvmName("Row")

package com.jakewharton.mosaic.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import com.jakewharton.mosaic.layout.Measurable
import com.jakewharton.mosaic.layout.MeasurePolicy
import com.jakewharton.mosaic.layout.MeasureResult
import com.jakewharton.mosaic.layout.MeasureScope
import com.jakewharton.mosaic.layout.ParentDataModifier
import com.jakewharton.mosaic.modifier.Modifier
import com.jakewharton.mosaic.ui.unit.Constraints
import kotlin.jvm.JvmName

@Composable
public fun Row(
	modifier: Modifier = Modifier,
	verticalAlignment: Alignment.Vertical = Alignment.Top,
	content: @Composable RowScope.() -> Unit,
) {
	Layout(
		content = { RowScopeInstance.content() },
		modifiers = modifier,
		debugInfo = { "Row(alignment=$verticalAlignment)" },
		measurePolicy = RowMeasurePolicy(verticalAlignment),
	)
}

private class RowMeasurePolicy(
	private val verticalAlignment: Alignment.Vertical,
) : MeasurePolicy {

	override fun MeasureScope.measure(
		measurables: List<Measurable>,
		constraints: Constraints,
	): MeasureResult {
		var width = 0
		var height = 0
		val placeables = measurables.map { measurable ->
			measurable.measure(constraints).also { placeable ->
				width += placeable.width
				height = maxOf(height, placeable.height)
			}
		}
		return layout(width, height) {
			var x = 0
			placeables.forEachIndexed { index, placeable ->
				val alignment = measurables[index].rowParentData?.alignment ?: verticalAlignment
				val y = alignment.align(placeable.height, height)
				placeable.place(x, y)
				x += placeable.width
			}
		}
	}
}

/**
 * Scope for the children of [Row].
 */
@LayoutScopeMarker
@Immutable
public interface RowScope {

	/**
	 * Align the element vertically within the [Row]. This alignment will have priority over
	 * the [Row]'s `verticalAlignment` parameter.
	 */
	@Stable
	public fun Modifier.align(alignment: Alignment.Vertical): Modifier
}

private object RowScopeInstance : RowScope {

	@Stable
	override fun Modifier.align(alignment: Alignment.Vertical) = this.then(
		VerticalAlignModifier(vertical = alignment),
	)
}

private class VerticalAlignModifier(
	private val vertical: Alignment.Vertical,
) : ParentDataModifier {

	override fun modifyParentData(parentData: Any?): Any {
		return ((parentData as? RowParentData) ?: RowParentData()).also {
			it.alignment = vertical
		}
	}

	override fun toString(): String = "VerticalAlign($vertical)"
}

private data class RowParentData(
	var alignment: Alignment.Vertical = Alignment.Top,
)

private val Measurable.rowParentData: RowParentData?
	get() = this.parentData as? RowParentData
