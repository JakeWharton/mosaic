@file:JvmName("Column")

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
import kotlin.jvm.JvmName

@Composable
public fun Column(
	modifier: Modifier = Modifier,
	horizontalAlignment: Alignment.Horizontal = Alignment.Start,
	content: @Composable ColumnScope.() -> Unit,
) {
	Layout(
		content = { ColumnScopeInstance.content() },
		modifiers = modifier,
		debugInfo = { "Column()" },
		measurePolicy = ColumnMeasurePolicy(horizontalAlignment),
	)
}

private class ColumnMeasurePolicy(
	private val horizontalAlignment: Alignment.Horizontal,
) : MeasurePolicy {

	override fun MeasureScope.measure(measurables: List<Measurable>): MeasureResult {
		var width = 0
		var height = 0
		val placeables = measurables.map { measurable ->
			measurable.measure().also { placeable ->
				width = maxOf(width, placeable.width)
				height += placeable.height
			}
		}
		return layout(width, height) {
			var y = 0
			placeables.forEachIndexed { index, placeable ->
				val alignment = measurables[index].columnParentData?.alignment ?: horizontalAlignment
				val x = alignment.align(placeable.width, width)
				placeable.place(x, y)
				y += placeable.height
			}
		}
	}
}

/**
 * Scope for the children of [Column].
 */
@LayoutScopeMarker
@Immutable
public interface ColumnScope {

	/**
	 * Align the element horizontally within the [Column]. This alignment will have priority over
	 * the [Column]'s `horizontalAlignment` parameter.
	 */
	@Stable
	public fun Modifier.align(alignment: Alignment.Horizontal): Modifier
}

private object ColumnScopeInstance : ColumnScope {

	@Stable
	override fun Modifier.align(alignment: Alignment.Horizontal) = this.then(
		HorizontalAlignModifier(horizontal = alignment)
	)
}

private class HorizontalAlignModifier(
	private val horizontal: Alignment.Horizontal,
) : ParentDataModifier {

	override fun modifyParentData(parentData: Any?): Any {
		return ((parentData as? ColumnParentData) ?: ColumnParentData()).also {
			it.alignment = horizontal
		}
	}

	override fun toString(): String = "HorizontalAlign($horizontal)"
}

private data class ColumnParentData(
	var alignment: Alignment.Horizontal = Alignment.Start,
)

private val Measurable.columnParentData: ColumnParentData?
	get() = this.parentData as? ColumnParentData
