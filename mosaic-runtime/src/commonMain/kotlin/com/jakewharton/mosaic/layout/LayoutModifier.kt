/*
 * Copyright 2020 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.jakewharton.mosaic.layout

import com.jakewharton.mosaic.modifier.Modifier
import com.jakewharton.mosaic.ui.IntrinsicsMeasureScope
import com.jakewharton.mosaic.ui.unit.Constraints

/**
 * A [Modifier.Element] that changes how its wrapped content is measured and laid out.
 * It has the same measurement and layout functionality as the [com.jakewharton.mosaic.ui.Layout]
 * component, while wrapping exactly one layout due to it being a modifier. In contrast,
 * the [com.jakewharton.mosaic.ui.Layout] component is used to define the layout behavior of
 * multiple children.
 *
 * @see com.jakewharton.mosaic.ui.Layout
 */
public interface LayoutModifier : Modifier.Element {
	/**
	 * The function used to measure the modifier. The [measurable] corresponds to the
	 * wrapped content, and it can be measured with the desired constraints according
	 * to the logic of the [LayoutModifier]. The modifier needs to choose its own
	 * size, which can depend on the size chosen by the wrapped content (the obtained
	 * [Placeable]), if the wrapped content was measured. The size needs to be returned
	 * as part of a [MeasureResult], alongside the placement logic of the
	 * [Placeable], which defines how the wrapped content should be positioned inside
	 * the [LayoutModifier]. A convenient way to create the [MeasureResult]
	 * is to use the [MeasureScope.layout] factory function.
	 *
	 * A [LayoutModifier] uses the same measurement and layout concepts and principles as a
	 * [com.jakewharton.mosaic.ui.Layout], the only difference is that they apply to exactly one child.
	 * For a more detailed explanation of measurement and layout, see [MeasurePolicy].
	 */
	public fun MeasureScope.measure(
		measurable: Measurable,
		constraints: Constraints,
	): MeasureResult

	/**
	 * The function used to calculate [IntrinsicMeasurable.minIntrinsicWidth].
	 */
	public fun minIntrinsicWidth(
		measurable: IntrinsicMeasurable,
		height: Int
	): Int = MeasuringIntrinsics.minWidth(this, measurable, height)

	/**
	 * The lambda used to calculate [IntrinsicMeasurable.minIntrinsicHeight].
	 */
	public fun minIntrinsicHeight(
		measurable: IntrinsicMeasurable,
		width: Int
	): Int = MeasuringIntrinsics.minHeight(this, measurable, width)

	/**
	 * The function used to calculate [IntrinsicMeasurable.maxIntrinsicWidth].
	 */
	public fun maxIntrinsicWidth(
		measurable: IntrinsicMeasurable,
		height: Int
	): Int = MeasuringIntrinsics.maxWidth(this, measurable, height)

	/**
	 * The lambda used to calculate [IntrinsicMeasurable.maxIntrinsicHeight].
	 */
	public fun maxIntrinsicHeight(
		measurable: IntrinsicMeasurable,
		width: Int
	): Int = MeasuringIntrinsics.maxHeight(this, measurable, width)

	// Force subclasses to add a debugging implementation.
	override fun toString(): String
}

private object MeasuringIntrinsics {
	fun minWidth(
		modifier: LayoutModifier,
		intrinsicMeasurable: IntrinsicMeasurable,
		h: Int
	): Int {
		val measurable = DefaultIntrinsicMeasurable(
			intrinsicMeasurable,
			IntrinsicMinMax.Min,
			IntrinsicWidthHeight.Width
		)
		val constraints = Constraints(maxHeight = h)
		val layoutResult = with(modifier) {
			IntrinsicsMeasureScope.measure(measurable, constraints)
		}
		return layoutResult.width
	}

	fun minHeight(
		modifier: LayoutModifier,
		intrinsicMeasurable: IntrinsicMeasurable,
		w: Int
	): Int {
		val measurable = DefaultIntrinsicMeasurable(
			intrinsicMeasurable,
			IntrinsicMinMax.Min,
			IntrinsicWidthHeight.Height
		)
		val constraints = Constraints(maxWidth = w)
		val layoutResult = with(modifier) {
			IntrinsicsMeasureScope.measure(measurable, constraints)
		}
		return layoutResult.height
	}

	fun maxWidth(
		modifier: LayoutModifier,
		intrinsicMeasurable: IntrinsicMeasurable,
		h: Int
	): Int {
		val measurable = DefaultIntrinsicMeasurable(
			intrinsicMeasurable,
			IntrinsicMinMax.Max,
			IntrinsicWidthHeight.Width
		)
		val constraints = Constraints(maxHeight = h)
		val layoutResult = with(modifier) {
			IntrinsicsMeasureScope.measure(measurable, constraints)
		}
		return layoutResult.width
	}

	fun maxHeight(
		modifier: LayoutModifier,
		intrinsicMeasurable: IntrinsicMeasurable,
		w: Int
	): Int {
		val measurable = DefaultIntrinsicMeasurable(
			intrinsicMeasurable,
			IntrinsicMinMax.Max,
			IntrinsicWidthHeight.Height
		)
		val constraints = Constraints(maxWidth = w)
		val layoutResult = with(modifier) {
			IntrinsicsMeasureScope.measure(measurable, constraints)
		}
		return layoutResult.height
	}

	private class DefaultIntrinsicMeasurable(
		val measurable: IntrinsicMeasurable,
		val minMax: IntrinsicMinMax,
		val widthHeight: IntrinsicWidthHeight
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
				return EmptyPlaceable(width, constraints.maxHeight)
			}
			val height = if (minMax == IntrinsicMinMax.Max) {
				measurable.maxIntrinsicHeight(constraints.maxWidth)
			} else {
				measurable.minIntrinsicHeight(constraints.maxWidth)
			}
			return EmptyPlaceable(constraints.maxWidth, height)
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

	private class EmptyPlaceable(
		override val width: Int,
		override val height: Int,
	) : Placeable() {

		override fun placeAt(x: Int, y: Int) {}
	}

	private enum class IntrinsicMinMax { Min, Max }
	private enum class IntrinsicWidthHeight { Width, Height }
}

/**
 * Creates a [LayoutModifier] that allows changing how the wrapped element is measured and laid out.
 *
 * This is a convenience API of creating a custom [LayoutModifier] modifier, without having to
 * create a class or an object that implements the [LayoutModifier] interface. The intrinsic
 * measurements follow the default logic provided by the [LayoutModifier].
 *
 * @see com.jakewharton.mosaic.ui.Layout
 */
public fun Modifier.layout(
	measure: MeasureScope.(Measurable, Constraints) -> MeasureResult
): Modifier = this then LayoutModifierElement(measure)

private class LayoutModifierElement(
	var measureBlock: MeasureScope.(Measurable, Constraints) -> MeasureResult
) : LayoutModifier {
	override fun MeasureScope.measure(
		measurable: Measurable,
		constraints: Constraints
	): MeasureResult = measureBlock(measurable, constraints)

	override fun toString(): String {
		return "LayoutModifierImpl(measureBlock=$measureBlock)"
	}
}
