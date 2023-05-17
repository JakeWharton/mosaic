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

import com.jakewharton.mosaic.TextCanvas
import com.jakewharton.mosaic.ui.Color
import com.jakewharton.mosaic.ui.TextStyle

public interface DrawScope {
	public val width: Int
	public val height: Int

	public fun drawRect(
		color: Color,
		row: Int = 0,
		column: Int = 0,
		width: Int = this.width,
		height: Int = this.height,
	)

	public fun drawText(
		row: Int,
		column: Int,
		string: String,
		foreground: Color? = null,
		background: Color? = null,
		style: TextStyle? = null,
	)
}

internal open class TextCanvasDrawScope(
	private val canvas: TextCanvas,
	override val width: Int,
	override val height: Int,
): DrawScope {
	override fun drawRect(
		color: Color,
		row: Int,
		column: Int,
		width: Int,
		height: Int,
	) {
		for (y in row until row + height) {
			for (x in column until column + width) {
				canvas[y, x].background = color
			}
		}
	}

	override fun drawText(
		row: Int,
		column: Int,
		string: String,
		foreground: Color?,
		background: Color?,
		style: TextStyle?,
	) {
		var pixelIndex = 0
		var characterColumn = column
		while (pixelIndex < string.length) {
			val character = canvas[row, characterColumn++]

			val pixelEnd = if (string[pixelIndex].isHighSurrogate()) {
				pixelIndex + 2
			} else {
				pixelIndex + 1
			}
			character.value = string.substring(pixelIndex, pixelEnd)
			pixelIndex = pixelEnd

			if (background != null) {
				character.background = background
			}
			if (foreground != null) {
				character.foreground = foreground
			}
			if (style != null) {
				character.style = style
			}
		}
	}
}
