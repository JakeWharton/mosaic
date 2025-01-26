@file:JvmName("Layout")

package com.jakewharton.mosaic.ui

import androidx.compose.runtime.Composable
import com.jakewharton.mosaic.layout.IntrinsicMeasurable
import com.jakewharton.mosaic.layout.Measurable
import com.jakewharton.mosaic.layout.MeasurePolicy
import com.jakewharton.mosaic.layout.MeasureResult
import com.jakewharton.mosaic.layout.MeasureScope
import com.jakewharton.mosaic.layout.Placeable
import com.jakewharton.mosaic.modifier.Modifier
import com.jakewharton.mosaic.ui.unit.Constraints
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
	modifier: Modifier = Modifier,
	debugInfo: () -> String = { "Layout()" },
	measurePolicy: NoContentMeasurePolicy,
) {
	Node(
		measurePolicy = NoContentMeasurePolicyMeasurePolicy(measurePolicy),
		debugPolicy = { debugInfo() + " x=$x y=$y w=$width h=$height${modifier.toDebugString()}" },
		modifier = modifier,
	)
}

private class NoContentMeasurePolicyMeasurePolicy(
	private val noContentMeasurePolicy: NoContentMeasurePolicy,
) : MeasurePolicy {
	override fun MeasureScope.measure(
		measurables: List<Measurable>,
		constraints: Constraints,
	): MeasureResult {
		check(measurables.isEmpty())
		return noContentMeasurePolicy.run { NoContentMeasureScope.measure() }
	}
}

@Composable
@Suppress("ktlint:compose:content-trailing-lambda") // Matches Compose UI order.
public fun Layout(
	content: @Composable () -> Unit,
	modifier: Modifier = Modifier,
	debugInfo: () -> String = { "Layout()" },
	measurePolicy: MeasurePolicy,
) {
	Node(
		measurePolicy = measurePolicy,
		debugPolicy = {
			buildString {
				append(debugInfo())
				append(" x=$x y=$y w=$width h=$height${modifier.toDebugString()}")
				children.joinTo(this, separator = "") {
					"\n" + it.toString().prependIndent("  ")
				}
			}
		},
		modifier = modifier,
		content = content,
	)
}

private fun Modifier.toDebugString(): String {
	return if (this == Modifier) {
		""
	} else {
		" " + toString()
	}
}

/**
 * Identifies an [IntrinsicMeasurable] as a min or max intrinsic measurement.
 */
internal enum class IntrinsicMinMax {
	Min,
	Max,
}

/**
 * Identifies an [IntrinsicMeasurable] as a width or height intrinsic measurement.
 */
internal enum class IntrinsicWidthHeight {
	Width,
	Height,
}

/**
 * Used to return a fixed sized item for intrinsics measurements in [Layout]
 */
private class FixedSizeIntrinsicsPlaceable(
	override val width: Int,
	override val height: Int,
) : Placeable() {
	override fun placeAt(x: Int, y: Int) {}
}

/**
 * A wrapper around a [Measurable] for intrinsic measurements in [Layout]. Consumers of
 * [Layout] don't identify intrinsic methods, but we can give a reasonable implementation
 * by using their [measure], substituting the intrinsics gathering method
 * for the [Measurable.measure] call.
 */
internal class DefaultIntrinsicMeasurable(
	val measurable: IntrinsicMeasurable,
	private val minMax: IntrinsicMinMax,
	private val widthHeight: IntrinsicWidthHeight,
) : Measurable {
	override val parentData: Any?
		get() = measurable.parentData

	override fun measure(constraints: Constraints): Placeable {
		if (widthHeight == IntrinsicWidthHeight.Width) {
			val width = if (minMax == IntrinsicMinMax.Max) {
				measurable.maxIntrinsicWidth(constraints.maxHeight)
			} else {
				measurable.minIntrinsicWidth(constraints.maxHeight)
			}
			return FixedSizeIntrinsicsPlaceable(width, constraints.maxHeight)
		}
		val height = if (minMax == IntrinsicMinMax.Max) {
			measurable.maxIntrinsicHeight(constraints.maxWidth)
		} else {
			measurable.minIntrinsicHeight(constraints.maxWidth)
		}
		return FixedSizeIntrinsicsPlaceable(constraints.maxWidth, height)
	}

	override fun minIntrinsicWidth(height: Int): Int {
		return measurable.minIntrinsicWidth(height)
	}

	override fun maxIntrinsicWidth(height: Int): Int {
		return measurable.maxIntrinsicWidth(height)
	}

	override fun minIntrinsicHeight(width: Int): Int {
		return measurable.minIntrinsicHeight(width)
	}

	override fun maxIntrinsicHeight(width: Int): Int {
		return measurable.maxIntrinsicHeight(width)
	}
}

/**
 * Receiver scope for [Layout]'s and [LayoutModifier]'s layout lambda when used in an intrinsics
 * call.
 */
internal object IntrinsicsMeasureScope : MeasureScope {

	override fun layout(
		width: Int,
		height: Int,
		placementBlock: Placeable.PlacementScope.() -> Unit,
	): MeasureResult {
		return object : MeasureResult {
			override val width: Int
				get() = width
			override val height: Int
				get() = height

			override fun placeChildren() {
				// Intrinsics should never be placed
			}
		}
	}
}
