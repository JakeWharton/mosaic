package com.jakewharton.mosaic

import androidx.compose.runtime.Composable
import com.jakewharton.mosaic.Measurable.MeasureScope
import com.jakewharton.mosaic.Placeable.PlacementScope

@Composable
public fun Layout(
	content: @Composable () -> Unit,
	debugInfo: () -> String = { "Layout()" },
	measurePolicy: MeasurePolicy,
) {
	Node(
		content = content,
		measurePolicy = measurePolicy,
		drawPolicy = DrawPolicy.Children,
		staticDrawPolicy = StaticDrawPolicy.Children,
		debugPolicy = {
			children.joinToString(prefix = debugInfo(), separator = "") {
				"\n" + it.toString().prependIndent("  ")
			}
		},
	)
}

public fun interface MeasurePolicy {
	public fun MeasureScope.measure(measurables: List<Measurable>): MeasureResult
}

public sealed interface Measurable {
	public fun measure(): Placeable

	public sealed class MeasureScope {
		public fun layout(
			width: Int,
			height: Int,
			placementBlock: PlacementScope.() -> Unit,
		): MeasureResult {
			return LayoutResult(width, height, placementBlock)
		}

		internal companion object : MeasureScope()
	}
}

public abstract class Placeable {
	public abstract val width: Int
	public abstract val height: Int

	protected abstract fun placeAt(x: Int, y: Int)

	public sealed class PlacementScope {
		public fun Placeable.place(x: Int, y: Int) {
			placeAt(x, y)
		}

		internal companion object : PlacementScope()
	}
}

public sealed interface MeasureResult {
	public val width: Int
	public val height: Int
	public fun placeChildren()
}

private class LayoutResult(
	override val width: Int,
	override val height: Int,
	private val placementBlock: PlacementScope.() -> Unit,
) : MeasureResult {
	override fun placeChildren() = PlacementScope.placementBlock()
}
