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

@file:Suppress("NOTHING_TO_INLINE")

package com.jakewharton.mosaic.layout

import com.jakewharton.mosaic.TextPixel
import com.jakewharton.mosaic.TextSurface
import com.jakewharton.mosaic.UnspecifiedCodePoint
import com.jakewharton.mosaic.isSpecifiedCodePoint
import com.jakewharton.mosaic.isUnspecifiedCodePoint
import com.jakewharton.mosaic.text.AnnotatedString
import com.jakewharton.mosaic.text.SpanStyle
import com.jakewharton.mosaic.text.getLocalRawSpanStyles
import com.jakewharton.mosaic.ui.Color
import com.jakewharton.mosaic.ui.TextStyle
import com.jakewharton.mosaic.ui.isSpecifiedColor
import com.jakewharton.mosaic.ui.isSpecifiedTextStyle
import com.jakewharton.mosaic.ui.isUnspecifiedColor
import com.jakewharton.mosaic.ui.isUnspecifiedTextStyle
import com.jakewharton.mosaic.ui.unit.IntOffset
import com.jakewharton.mosaic.ui.unit.IntSize
import de.cketti.codepoints.codePointAt
import kotlin.math.max

public interface DrawScope {
	public val width: Int
	public val height: Int

	private val size: IntSize get() = IntSize(width, height)

	/**
	 * Draws a rectangle with the given offset and size. If no offset from the top left is provided,
	 * it is drawn starting from the origin of the current translation. If no size is provided,
	 * the size of the current environment is used.
	 *
	 * @param char Char to be applied to the rectangle
	 * @param foreground Foreground color to be applied to the rectangle
	 * @param background Background color to be applied to the rectangle
	 * @param textStyle Text style color to be applied to the rectangle
	 * @param topLeft Offset from the local origin of 0, 0 relative to the current translation
	 * @param size Dimensions of the rectangle to draw
	 * @param drawStyle Whether or not the rectangle is stroked or filled in
	 */
	public fun drawRect(
		char: Char,
		foreground: Color = Color.Unspecified,
		background: Color = Color.Unspecified,
		textStyle: TextStyle = TextStyle.Unspecified,
		topLeft: IntOffset = IntOffset.Zero,
		size: IntSize = this.size.offsetSize(topLeft),
		drawStyle: DrawStyle = DrawStyle.Fill,
	)

	/**
	 * Draws a rectangle with the given offset and size. If no offset from the top left is provided,
	 * it is drawn starting from the origin of the current translation. If no size is provided,
	 * the size of the current environment is used.
	 *
	 * @param codePoint Code point to be applied to the rectangle
	 * @param foreground Foreground color to be applied to the rectangle
	 * @param background Background color to be applied to the rectangle
	 * @param textStyle Text style color to be applied to the rectangle
	 * @param topLeft Offset from the local origin of 0, 0 relative to the current translation
	 * @param size Dimensions of the rectangle to draw
	 * @param drawStyle Whether or not the rectangle is stroked or filled in
	 */
	public fun drawRect(
		codePoint: Int = UnspecifiedCodePoint,
		foreground: Color = Color.Unspecified,
		background: Color = Color.Unspecified,
		textStyle: TextStyle = TextStyle.Unspecified,
		topLeft: IntOffset = IntOffset.Zero,
		size: IntSize = this.size.offsetSize(topLeft),
		drawStyle: DrawStyle = DrawStyle.Fill,
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

	/**
	 * Helper method to offset the provided size with the offset in box width and height
	 */
	private fun IntSize.offsetSize(offset: IntOffset): IntSize =
		IntSize(this.width - offset.x, this.height - offset.y)
}

internal open class TextCanvasDrawScope(
	private val canvas: TextSurface,
	override val width: Int,
	override val height: Int,
) : DrawScope {
	override fun drawRect(
		char: Char,
		foreground: Color,
		background: Color,
		textStyle: TextStyle,
		topLeft: IntOffset,
		size: IntSize,
		drawStyle: DrawStyle,
	) {
		drawRect(char.code, foreground, background, textStyle, topLeft, size, drawStyle)
	}

	override fun drawRect(
		codePoint: Int,
		foreground: Color,
		background: Color,
		textStyle: TextStyle,
		topLeft: IntOffset,
		size: IntSize,
		drawStyle: DrawStyle,
	) {
		if (codePoint.isUnspecifiedCodePoint &&
			foreground.isUnspecifiedColor &&
			background.isUnspecifiedColor &&
			textStyle.isUnspecifiedTextStyle ||
			size.width <= 0 ||
			size.height <= 0 ||
			topLeft.x >= width ||
			topLeft.y >= height ||
			topLeft.x + size.width < 0 ||
			topLeft.y + size.height < 0
		) {
			// exit: rectangle with the specified parameters cannot be seen
			return
		}

		when (drawStyle) {
			DrawStyle.Fill -> drawSolidRect(codePoint, foreground, background, textStyle, topLeft, size)
			is DrawStyle.Stroke -> {
				val strokeWidth = max(1, drawStyle.width)
				if (strokeWidth * 2 >= size.width || strokeWidth * 2 >= size.height) {
					// fast path: stroke width is large, it turns out a full fill
					drawSolidRect(codePoint, foreground, background, textStyle, topLeft, size)
					return
				}

				// left line
				drawSolidRect(
					codePoint,
					foreground,
					background,
					textStyle,
					topLeft,
					IntSize(strokeWidth, size.height),
				)
				// top line
				drawSolidRect(
					codePoint,
					foreground,
					background,
					textStyle,
					IntOffset(topLeft.x + strokeWidth, topLeft.y),
					IntSize(size.width - strokeWidth * 2, strokeWidth),
				)
				// right line
				drawSolidRect(
					codePoint,
					foreground,
					background,
					textStyle,
					IntOffset(topLeft.x + size.width - strokeWidth, topLeft.y),
					IntSize(strokeWidth, size.height),
				)
				// bottom line
				drawSolidRect(
					codePoint,
					foreground,
					background,
					textStyle,
					IntOffset(topLeft.x + strokeWidth, topLeft.y + size.height - strokeWidth),
					IntSize(size.width - strokeWidth * 2, strokeWidth),
				)
			}
		}
	}

	private inline fun drawSolidRect(
		codePoint: Int,
		foreground: Color,
		background: Color,
		textStyle: TextStyle,
		topLeft: IntOffset,
		size: IntSize,
	) {
		for (y in topLeft.y until topLeft.y + size.height) {
			for (x in topLeft.x until topLeft.x + size.width) {
				drawTextPixel(x, y, codePoint, foreground, background, textStyle)
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

			character.updateTextPixel(text.codePointAt(pixelIndex), foreground, background, textStyle)
			spanStylesProvider?.invoke(pixelIndex, pixelEnd)?.forEach {
				character.updateTextPixel(UnspecifiedCodePoint, it.color, it.background, it.textStyle)
			}

			pixelIndex = pixelEnd
		}
	}

	private inline fun drawTextPixel(
		x: Int,
		y: Int,
		codePoint: Int = UnspecifiedCodePoint,
		foreground: Color = Color.Unspecified,
		background: Color = Color.Unspecified,
		textStyle: TextStyle = TextStyle.Unspecified,
	) {
		canvas[y, x].updateTextPixel(codePoint, foreground, background, textStyle)
	}

	private inline fun TextPixel.updateTextPixel(
		codePoint: Int,
		foreground: Color,
		background: Color,
		textStyle: TextStyle,
	) {
		if (codePoint.isSpecifiedCodePoint) {
			this.codePoint = codePoint
		}
		if (foreground.isSpecifiedColor) {
			this.foreground = foreground
		}
		if (background.isSpecifiedColor) {
			this.background = background
		}
		if (textStyle.isSpecifiedTextStyle) {
			this.textStyle = textStyle
		}
	}
}
