@file:JvmName("Layout")

package com.jakewharton.mosaic.ui

import androidx.compose.runtime.Composable
import com.jakewharton.mosaic.layout.Measurable
import com.jakewharton.mosaic.layout.MeasurePolicy
import com.jakewharton.mosaic.layout.MeasureResult
import com.jakewharton.mosaic.layout.MeasureScope
import com.jakewharton.mosaic.modifier.Modifier
import kotlin.jvm.JvmName

internal fun interface NoContentMeasurePolicy {
	fun NoContentMeasureScope.measure(): MeasureResult
}

internal sealed class NoContentMeasureScope {
	fun layout(
		width: Int,
		height: Int,
	): MeasureResult {
		return LayoutResult(width, height)
	}

	private class LayoutResult(
		override val width: Int,
		override val height: Int,
	) : MeasureResult {
		override fun placeChildren() {}
	}

	internal companion object : NoContentMeasureScope()
}

@Composable
@MosaicComposable
internal fun Layout(
	modifiers: Modifier = Modifier,
	debugInfo: () -> String = { "Layout()" },
	measurePolicy: NoContentMeasurePolicy,
) {
	Node(
		measurePolicy = NoContentMeasurePolicyMeasurePolicy(measurePolicy),
		modifiers = modifiers,
		debugPolicy = { debugInfo() + " x=$x y=$y w=$width h=$height${modifiers.toDebugString()}" },
		factory = NodeFactory,
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
	modifiers: Modifier = Modifier,
	debugInfo: () -> String = { "Layout()" },
	measurePolicy: MeasurePolicy,
) {
	Node(
		content = content,
		measurePolicy = measurePolicy,
		modifiers = modifiers,
		debugPolicy = {
			buildString {
				append(debugInfo())
				append(" x=$x y=$y w=$width h=$height${modifiers.toDebugString()}")
				children.joinTo(this, separator = "") {
					"\n" + it.toString().prependIndent("  ")
				}
			}
		},
		factory = NodeFactory,
	)
}

private fun Modifier.toDebugString(): String {
	return if (this == Modifier) {
		""
	} else {
		" " + toString()
	}
}
