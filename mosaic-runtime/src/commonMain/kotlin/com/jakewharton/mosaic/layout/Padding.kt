/*
 * Copyright 2019 The Android Open Source Project
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

import androidx.compose.runtime.Stable
import com.jakewharton.mosaic.modifier.Modifier

@Stable
public fun Modifier.padding(
	left: Int = 0,
	top: Int = 0,
	right: Int = 0,
	bottom: Int = 0,
): Modifier = this.then(
	PaddingModifier(
		left = left,
		top = top,
		right = right,
		bottom = bottom,
	)
)

@Stable
public fun Modifier.padding(
	horizontal: Int = 0,
	vertical: Int = 0,
): Modifier = this.then(
	PaddingModifier(
		left = horizontal,
		top = vertical,
		right = horizontal,
		bottom = vertical,
	)
)

@Stable
public fun Modifier.padding(all: Int): Modifier =
	this.then(
		PaddingModifier(
			left = all,
			top = all,
			right = all,
			bottom = all,
		)
	)

private class PaddingModifier(
	val left: Int = 0,
	val top: Int = 0,
	val right: Int = 0,
	val bottom: Int = 0,
) : LayoutModifier {
	init {
		require(left >= 0 && top >= 0f && right >= 0f && bottom >= 0f) {
			"Padding must be non-negative"
		}
	}

	override fun MeasureScope.measure(measurable: Measurable): MeasureResult {
		val horizontal = left + right
		val vertical = top + bottom

		val placeable = measurable.measure()

		val width = placeable.width + horizontal
		val height = placeable.height + vertical
		return layout(width, height) {
			placeable.place(left, top)
		}
	}

	override fun hashCode(): Int {
		var result = left
		result = 31 * result + top
		result = 31 * result + right
		result = 31 * result + bottom
		return result
	}

	override fun equals(other: Any?): Boolean {
		return other is PaddingModifier &&
			left == other.left &&
			top == other.top &&
			right == other.right &&
			bottom == other.bottom
	}

	override fun toString() = when {
		left == right && left == top && left == bottom -> "Padding($left)"
		left == right && top == bottom -> "Padding(h=$left, v=$top)"
		else -> "Padding(l=$left, t=$top, r=$right, b=$bottom)"
	}
}
