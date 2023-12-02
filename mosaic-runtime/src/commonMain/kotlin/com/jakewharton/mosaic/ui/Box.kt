@file:JvmName("Box")

package com.jakewharton.mosaic.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import com.jakewharton.mosaic.layout.Measurable
import com.jakewharton.mosaic.layout.MeasurePolicy
import com.jakewharton.mosaic.layout.MeasureResult
import com.jakewharton.mosaic.layout.MeasureScope
import com.jakewharton.mosaic.layout.ParentDataModifier
import com.jakewharton.mosaic.layout.Placeable
import com.jakewharton.mosaic.modifier.Modifier
import com.jakewharton.mosaic.ui.unit.Constraints
import com.jakewharton.mosaic.ui.unit.IntSize
import kotlin.jvm.JvmName
import kotlin.math.max

@Composable
public fun Box(
	modifier: Modifier = Modifier,
	contentAlignment: Alignment = Alignment.TopStart,
	propagateMinConstraints: Boolean = false,
	content: @Composable BoxScope.() -> Unit,
) {
	val measurePolicy = rememberBoxMeasurePolicy(contentAlignment, propagateMinConstraints)
	Layout(
		content = { BoxScopeInstance.content() },
		modifier = modifier,
		debugInfo = { "Box(alignment=$contentAlignment, propagateMinConstraints=$propagateMinConstraints)" },
		measurePolicy = measurePolicy,
	)
}

@Composable
internal fun rememberBoxMeasurePolicy(
	alignment: Alignment,
	propagateMinConstraints: Boolean,
): MeasurePolicy = if (alignment == Alignment.TopStart && !propagateMinConstraints) {
	DefaultBoxMeasurePolicy
} else {
	remember(alignment, propagateMinConstraints) {
		BoxMeasurePolicy(alignment, propagateMinConstraints)
	}
}

private val DefaultBoxMeasurePolicy: MeasurePolicy = BoxMeasurePolicy(Alignment.TopStart, false)

internal class BoxMeasurePolicy(
	private val alignment: Alignment = Alignment.TopStart,
	private val propagateMinConstraints: Boolean = false,
) : MeasurePolicy {
	override fun MeasureScope.measure(
		measurables: List<Measurable>,
		constraints: Constraints,
	): MeasureResult {
		if (measurables.isEmpty()) {
			return layout(
				constraints.minWidth,
				constraints.minHeight,
			) {}
		}

		val contentConstraints = if (propagateMinConstraints) {
			constraints
		} else {
			constraints.copy(minWidth = 0, minHeight = 0)
		}

		if (measurables.size == 1) {
			val measurable = measurables[0]
			val boxWidth: Int
			val boxHeight: Int
			val placeable: Placeable
			if (!measurable.matchesParentSize) {
				placeable = measurable.measure(contentConstraints)
				boxWidth = max(constraints.minWidth, placeable.width)
				boxHeight = max(constraints.minHeight, placeable.height)
			} else {
				boxWidth = constraints.minWidth
				boxHeight = constraints.minHeight
				placeable = measurable.measure(
					Constraints.fixed(constraints.minWidth, constraints.minHeight),
				)
			}
			return layout(boxWidth, boxHeight) {
				placeInBox(placeable, measurable, boxWidth, boxHeight, alignment)
			}
		}

		val placeables = arrayOfNulls<Placeable>(measurables.size)
		// First measure non match parent size children to get the size of the Box.
		var hasMatchParentSizeChildren = false
		var boxWidth = constraints.minWidth
		var boxHeight = constraints.minHeight
		measurables.forEachIndexed { index, measurable ->
			if (!measurable.matchesParentSize) {
				val placeable = measurable.measure(contentConstraints)
				placeables[index] = placeable
				boxWidth = max(boxWidth, placeable.width)
				boxHeight = max(boxHeight, placeable.height)
			} else {
				hasMatchParentSizeChildren = true
			}
		}

		// Now measure match parent size children, if any.
		if (hasMatchParentSizeChildren) {
			// The infinity check is needed for default intrinsic measurements.
			val matchParentSizeConstraints = Constraints(
				minWidth = if (boxWidth != Constraints.Infinity) boxWidth else 0,
				minHeight = if (boxHeight != Constraints.Infinity) boxHeight else 0,
				maxWidth = boxWidth,
				maxHeight = boxHeight,
			)
			measurables.forEachIndexed { index, measurable ->
				if (measurable.matchesParentSize) {
					placeables[index] = measurable.measure(matchParentSizeConstraints)
				}
			}
		}

		// Specify the size of the Box and position its children.
		return layout(boxWidth, boxHeight) {
			placeables.forEachIndexed { index, placeable ->
				placeable as Placeable
				val measurable = measurables[index]
				placeInBox(placeable, measurable, boxWidth, boxHeight, alignment)
			}
		}
	}
}

private fun Placeable.PlacementScope.placeInBox(
	placeable: Placeable,
	measurable: Measurable,
	boxWidth: Int,
	boxHeight: Int,
	alignment: Alignment,
) {
	val childAlignment = measurable.boxParentData?.alignment ?: alignment
	val position = childAlignment.align(
		IntSize(placeable.width, placeable.height),
		IntSize(boxWidth, boxHeight),
	)
	placeable.place(position)
}

/**
 * A box with no content that can participate in layout, drawing
 * due to the [modifier] applied to it.
 *
 * @param modifier The modifier to be applied to the layout.
 */
@Composable
public fun Box(modifier: Modifier = Modifier) {
	Layout(
		content = EmptyBoxContent,
		modifier = modifier,
		debugInfo = { "Box()" },
		measurePolicy = EmptyBoxMeasurePolicy,
	)
}

private val EmptyBoxContent: @Composable () -> Unit = {}

private val EmptyBoxMeasurePolicy = MeasurePolicy { _, constraints ->
	layout(constraints.minWidth, constraints.minHeight) {}
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

	/**
	 * Size the element to match the size of the [Box] after all other content elements have
	 * been measured.
	 *
	 * The element using this modifier does not take part in defining the size of the [Box].
	 * Instead, it matches the size of the [Box] after all other children (not using
	 * matchParentSize() modifier) have been measured to obtain the [Box]'s size.
	 * In contrast, a general-purpose [Modifier.fillMaxSize] modifier, which makes an element
	 * occupy all available space, will take part in defining the size of the [Box]. Consequently,
	 * using it for an element inside a [Box] will make the [Box] itself always fill the
	 * available space.
	 */
	@Stable
	public fun Modifier.matchParentSize(): Modifier
}

private object BoxScopeInstance : BoxScope {

	@Stable
	override fun Modifier.align(alignment: Alignment): Modifier = this.then(
		AlignModifier(
			alignment = alignment,
			matchParentSize = false,
		),
	)

	@Stable
	override fun Modifier.matchParentSize(): Modifier = this.then(
		AlignModifier(
			alignment = Alignment.Center,
			matchParentSize = true,
		),
	)
}

private class AlignModifier(
	private val alignment: Alignment,
	private val matchParentSize: Boolean,
) : ParentDataModifier {

	override fun modifyParentData(parentData: Any?): Any {
		return (parentData as? BoxParentData)?.also {
			it.alignment = alignment
			it.matchParentSize = matchParentSize
		} ?: BoxParentData(alignment, matchParentSize)
	}

	override fun toString(): String = "Align($alignment)"
}

private data class BoxParentData(
	var alignment: Alignment,
	var matchParentSize: Boolean,
)

private val Measurable.boxParentData: BoxParentData?
	get() = this.parentData as? BoxParentData

private val Measurable.matchesParentSize: Boolean
	get() = boxParentData?.matchParentSize ?: false
