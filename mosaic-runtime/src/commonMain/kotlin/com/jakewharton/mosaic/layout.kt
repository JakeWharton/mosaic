package com.jakewharton.mosaic

import androidx.compose.runtime.Composable
import com.jakewharton.mosaic.Measurable.MeasureScope
import com.jakewharton.mosaic.Placeable.PlacementScope

@Composable
internal fun Layout(
	debugInfo: () -> String = { "Layout()" },
	measurePolicy: MeasurePolicy,
	drawPolicy: DrawPolicy,
) {
	Node(
		measurePolicy = measurePolicy,
		drawPolicy = drawPolicy,
		staticDrawPolicy = StaticDrawPolicy.None,
		debugPolicy = { debugInfo() + " x=$x y=$y w=$width h=$height" },
	)
}

@Composable
public fun Layout(
	content: @Composable () -> Unit,
	debugInfo: () -> String = { "Layout()" },
	measurePolicy: MeasurePolicy,
) {
	Node(
		content = content,
		measurePolicy = measurePolicy,
		drawPolicy = null,
		staticDrawPolicy = StaticDrawPolicy.Children,
		debugPolicy = {
			buildString {
				append(debugInfo())
				append(" x=$x y=$y w=$width h=$height")
				children.joinTo(this, separator = "") {
					"\n" + it.toString().prependIndent("  ")
				}
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

internal object NotMeasured : MeasureResult {
	override val width get() = 0
	override val height get() = 0
	override fun placeChildren() = throw UnsupportedOperationException("Not measured")
}
