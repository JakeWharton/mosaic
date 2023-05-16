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

/**
 * A [Modifier.Element] that changes how its wrapped content is measured and laid out.
 * It has the same measurement and layout functionality as the [com.jakewharton.mosaic.layout.Layout]
 * component, while wrapping exactly one layout due to it being a modifier. In contrast,
 * the [com.jakewharton.mosaic.layout.Layout] component is used to define the layout behavior of
 * multiple children.
 *
 * @see com.jakewharton.mosaic.layout.Layout
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
	 * [Layout], the only difference is that they apply to exactly one child. For a more detailed
	 * explanation of measurement and layout, see [MeasurePolicy].
	 */
	public fun MeasureScope.measure(
		measurable: Measurable,
	): MeasureResult
}

/**
 * Creates a [LayoutModifier] that allows changing how the wrapped element is measured and laid out.
 *
 * This is a convenience API of creating a custom [LayoutModifier] modifier, without having to
 * create a class or an object that implements the [LayoutModifier] interface. The intrinsic
 * measurements follow the default logic provided by the [LayoutModifier].
 *
 * @see com.jakewharton.mosaic.layout.LayoutModifier
 */
public fun Modifier.layout(
	measure: MeasureScope.(Measurable) -> MeasureResult
): Modifier = this then LayoutModifierElement(measure)

private class LayoutModifierElement(
	var measureBlock: MeasureScope.(Measurable) -> MeasureResult
) : LayoutModifier {
	override fun MeasureScope.measure(
		measurable: Measurable,
	) = measureBlock(measurable)

	override fun toString(): String {
		return "LayoutModifierImpl(measureBlock=$measureBlock)"
	}
}
