package com.jakewharton.mosaic.layout

import com.jakewharton.mosaic.ui.DefaultIntrinsicMeasurable
import com.jakewharton.mosaic.ui.IntrinsicMinMax
import com.jakewharton.mosaic.ui.IntrinsicWidthHeight
import com.jakewharton.mosaic.ui.IntrinsicsMeasureScope
import com.jakewharton.mosaic.ui.unit.Constraints

public fun interface MeasurePolicy {

	public fun MeasureScope.measure(
		measurables: List<Measurable>,
		constraints: Constraints,
	): MeasureResult

	/**
	 * The function used to calculate [IntrinsicMeasurable.minIntrinsicWidth]. It represents
	 * the minimum width this layout can take, given a specific height, such that the content
	 * of the layout can be painted correctly.
	 */
	public fun minIntrinsicWidth(
		measurables: List<IntrinsicMeasurable>,
		height: Int,
	): Int {
		val mapped = measurables.map {
			DefaultIntrinsicMeasurable(it, IntrinsicMinMax.Min, IntrinsicWidthHeight.Width)
		}
		val constraints = Constraints(maxHeight = height)
		val layoutReceiver = IntrinsicsMeasureScope
		val layoutResult = layoutReceiver.measure(mapped, constraints)
		return layoutResult.width
	}

	/**
	 * The function used to calculate [IntrinsicMeasurable.minIntrinsicHeight]. It represents
	 * the minimum height this layout can take, given a specific width, such that the content
	 * of the layout will be painted correctly.
	 */
	public fun minIntrinsicHeight(
		measurables: List<IntrinsicMeasurable>,
		width: Int,
	): Int {
		val mapped = measurables.map {
			DefaultIntrinsicMeasurable(it, IntrinsicMinMax.Min, IntrinsicWidthHeight.Height)
		}
		val constraints = Constraints(maxWidth = width)
		val layoutReceiver = IntrinsicsMeasureScope
		val layoutResult = layoutReceiver.measure(mapped, constraints)
		return layoutResult.height
	}

	/**
	 * The function used to calculate [IntrinsicMeasurable.maxIntrinsicWidth]. It represents the
	 * minimum width such that increasing it further will not decrease the minimum intrinsic height.
	 */
	public fun maxIntrinsicWidth(
		measurables: List<IntrinsicMeasurable>,
		height: Int,
	): Int {
		val mapped = measurables.map {
			DefaultIntrinsicMeasurable(it, IntrinsicMinMax.Max, IntrinsicWidthHeight.Width)
		}
		val constraints = Constraints(maxHeight = height)
		val layoutReceiver = IntrinsicsMeasureScope
		val layoutResult = layoutReceiver.measure(mapped, constraints)
		return layoutResult.width
	}

	/**
	 * The function used to calculate [IntrinsicMeasurable.maxIntrinsicHeight]. It represents the
	 * minimum height such that increasing it further will not decrease the minimum intrinsic width.
	 */
	public fun maxIntrinsicHeight(
		measurables: List<IntrinsicMeasurable>,
		width: Int,
	): Int {
		val mapped = measurables.map {
			DefaultIntrinsicMeasurable(it, IntrinsicMinMax.Max, IntrinsicWidthHeight.Height)
		}
		val constraints = Constraints(maxWidth = width)
		val layoutReceiver = IntrinsicsMeasureScope
		val layoutResult = layoutReceiver.measure(mapped, constraints)
		return layoutResult.height
	}
}
