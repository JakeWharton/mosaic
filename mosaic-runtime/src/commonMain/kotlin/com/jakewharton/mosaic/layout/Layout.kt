@file:JvmName("Layout")

package com.jakewharton.mosaic.layout

import androidx.compose.runtime.Composable
import com.jakewharton.mosaic.DrawPolicy
import com.jakewharton.mosaic.Node
import com.jakewharton.mosaic.StaticDrawPolicy
import kotlin.jvm.JvmName

@Composable
internal fun Layout(
	debugInfo: () -> String = { "Layout()" },
	measurePolicy: NoContentMeasurePolicy,
	drawPolicy: DrawPolicy,
) {
	Node(
		measurePolicy = NoContentMeasurePolicyMeasurePolicy(measurePolicy),
		drawPolicy = drawPolicy,
		staticDrawPolicy = StaticDrawPolicy.None,
		debugPolicy = { debugInfo() + " x=$x y=$y w=$width h=$height" },
	)
}

private class NoContentMeasurePolicyMeasurePolicy(
	private val noContentMeasurePolicy: NoContentMeasurePolicy,
) : MeasurePolicy {
	override fun MeasureScope.measure(measurables: List<Measurable>): MeasureResult {
		check(measurables.isEmpty())
		return noContentMeasurePolicy.run { NoContentMeasureScope.measure() }
	}
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
