@file:JvmName("Box")

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
import com.jakewharton.mosaic.ui.unit.IntSize
import kotlin.jvm.JvmName

@Composable
public fun Box(
	modifier: Modifier = Modifier,
	contentAlignment: Alignment = Alignment.TopStart,
	content: @Composable BoxScope.() -> Unit,
) {
	Layout(
		content = { BoxScopeInstance.content() },
		modifiers = modifier,
		debugInfo = { "Box()" },
		measurePolicy = BoxMeasurePolicy(contentAlignment)
	)
}

internal class BoxMeasurePolicy(
	private val contentAlignment: Alignment = Alignment.TopStart
) : MeasurePolicy {

	override fun MeasureScope.measure(measurables: List<Measurable>): MeasureResult {
		var width = 0
		var height = 0
		val placeables = measurables.map { measurable ->
			measurable.measure().also {
				width = maxOf(width, it.width)
				height = maxOf(height, it.height)
			}
		}
		return layout(width, height) {
			placeables.forEachIndexed { index, placeable ->
				val alignment = measurables[index].boxParentData?.alignment ?: contentAlignment
				val offset =
					alignment.align(IntSize(placeable.width, placeable.height), IntSize(width, height))
				placeable.place(offset.x, offset.y)
			}
		}
	}
}

/**
 * A BoxScope provides a scope for the children of [Box].
 */
@LayoutScopeMarker
@Immutable
public interface BoxScope {
	/**
	 * Pull the content element to a specific [Alignment] within the [Box]. This alignment will
	 * have priority over the [Box]'s `alignment` parameter.
	 */
	@Stable
	public fun Modifier.align(alignment: Alignment): Modifier
}

private object BoxScopeInstance : BoxScope {

	@Stable
	override fun Modifier.align(alignment: Alignment) = this.then(
		AlignModifier(alignment = alignment)
	)
}

private class AlignModifier(
	private val alignment: Alignment
) : ParentDataModifier {

	override fun modifyParentData(parentData: Any?): Any {
		return ((parentData as? BoxParentData) ?: BoxParentData()).also {
			it.alignment = alignment
		}
	}

	override fun toString(): String = "Align($alignment)"
}

private data class BoxParentData(
	var alignment: Alignment = Alignment.TopStart
)

private val Measurable.boxParentData: BoxParentData?
	get() = this.parentData as? BoxParentData
