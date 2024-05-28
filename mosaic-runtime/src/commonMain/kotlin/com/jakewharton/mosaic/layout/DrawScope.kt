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
import com.jakewharton.mosaic.text.AnnotatedString
import com.jakewharton.mosaic.text.SpanStyle
import com.jakewharton.mosaic.text.getLocalRawSpanStyles
import com.jakewharton.mosaic.ui.Color
import com.jakewharton.mosaic.ui.TextStyle
import com.jakewharton.mosaic.ui.isSpecifiedColor
import com.jakewharton.mosaic.ui.isSpecifiedTextStyle
import de.cketti.codepoints.codePointAt

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
		foreground: Color = Color.Unspecified,
		background: Color = Color.Unspecified,
		textStyle: TextStyle = TextStyle.Unspecified,
	)

	public fun drawText(
		row: Int,
		column: Int,
		string: AnnotatedString,
		foreground: Color = Color.Unspecified,
		background: Color = Color.Unspecified,
		textStyle: TextStyle = TextStyle.Unspecified,
	)
}

internal open class TextCanvasDrawScope(
	private val canvas: TextCanvas,
	override val width: Int,
	override val height: Int,
) : DrawScope {
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
		foreground: Color,
		background: Color,
		textStyle: TextStyle,
	) {
		drawText(row, column, string, foreground, background, textStyle, null)
	}

	override fun drawText(
		row: Int,
		column: Int,
		string: AnnotatedString,
		foreground: Color,
		background: Color,
		textStyle: TextStyle,
	) {
		drawText(row, column, string.text, foreground, background, textStyle) { start, end ->
			string.getLocalRawSpanStyles(start, end)
		}
	}

	private fun drawText(
		row: Int,
		column: Int,
		text: String,
		foreground: Color,
		background: Color,
		textStyle: TextStyle,
		spanStylesProvider: ((start: Int, end: Int) -> List<SpanStyle>)?,
	) {
		var pixelIndex = 0
		var characterColumn = column
		while (pixelIndex < text.length) {
			val character = canvas[row, characterColumn++]

			val pixelEnd = if (text[pixelIndex].isHighSurrogate()) {
				pixelIndex + 2
			} else {
				pixelIndex + 1
			}

			character.codePoint = text.codePointAt(pixelIndex)
			val spanStyles = spanStylesProvider?.invoke(pixelIndex, pixelEnd)
			pixelIndex = pixelEnd

			fun maybeUpdateCharacter(background: Color, foreground: Color, style: TextStyle) {
				if (background.isSpecifiedColor) {
					character.background = background
				}
				if (foreground.isSpecifiedColor) {
					character.foreground = foreground
				}
				if (style.isSpecifiedTextStyle) {
					character.textStyle = style
				}
			}

			maybeUpdateCharacter(background, foreground, textStyle)
			spanStyles?.forEach {
				maybeUpdateCharacter(it.background, it.color, it.textStyle)
			}
		}
	}
}
