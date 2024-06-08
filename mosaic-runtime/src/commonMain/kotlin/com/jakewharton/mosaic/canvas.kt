@file:Suppress("NOTHING_TO_INLINE")

package com.jakewharton.mosaic

import com.jakewharton.mosaic.ui.AnsiLevel
import com.jakewharton.mosaic.ui.Color
import com.jakewharton.mosaic.ui.TextStyle
import com.jakewharton.mosaic.ui.TextStyle.Companion.Bold
import com.jakewharton.mosaic.ui.TextStyle.Companion.Dim
import com.jakewharton.mosaic.ui.TextStyle.Companion.Invert
import com.jakewharton.mosaic.ui.TextStyle.Companion.Italic
import com.jakewharton.mosaic.ui.TextStyle.Companion.Strikethrough
import com.jakewharton.mosaic.ui.TextStyle.Companion.Underline
import com.jakewharton.mosaic.ui.isNotEmptyTextStyle
import com.jakewharton.mosaic.ui.isSpecifiedColor
import com.jakewharton.mosaic.ui.isUnspecifiedColor
import de.cketti.codepoints.appendCodePoint

internal interface TextCanvas {
	val width: Int
	val height: Int
	var translationX: Int
	var translationY: Int

	operator fun get(row: Int, column: Int): TextPixel
}

private val blankPixel = TextPixel(' ')

internal class TextSurface(
	private val ansiLevel: AnsiLevel,
	initialWidth: Int = 0,
	initialHeight: Int = 0,
) : TextCanvas {
	override var width: Int = initialWidth
		private set
	override var height: Int = initialHeight
		private set

	override var translationX = 0
	override var translationY = 0

	private var fullWidth: Int = width
	private var fullHeight: Int = height

	private var rows = Array(fullHeight) { Array(fullWidth) { TextPixel(' ') } }

	override operator fun get(row: Int, column: Int): TextPixel {
		return rows[translationY + row][translationX + column]
	}

	fun reset() {
		width = 0
		height = 0
		translationX = 0
		translationY = 0
		for (rowIndex in 0 until fullHeight) {
			val row = rows[rowIndex]
			for (columnIndex in 0 until fullWidth) {
				row[columnIndex].reset()
			}
		}
	}

	fun resize(newWidth: Int, newHeight: Int) {
		val widthDiff = newWidth - fullWidth
		val heightDiff = newHeight - fullHeight
		width = newWidth
		height = newHeight
		if (widthDiff > 0) {
			rows.forEachIndexed { index, row ->
				rows[index] = row.copyOf(newWidth) { TextPixel(' ') }
			}
			fullWidth = newWidth
		}
		if (heightDiff > 0) {
			rows = rows.copyOf(newHeight) { Array(fullWidth) { TextPixel(' ') } }
			fullHeight = newHeight
		}
	}

	private fun <T> Array<T>.copyOf(newSize: Int, initializer: (index: Int) -> T): Array<T> {
		if (newSize <= size) {
			// In this case, we don't want to copy it again if the size hasn't increased.
			return this
		}
		val oldSize = size
		val new = copyOf(newSize)
		for (i in oldSize until newSize) {
			new[i] = initializer(i)
		}
		@Suppress("UNCHECKED_CAST")
		return new as Array<T>
	}

	fun appendRowTo(appendable: Appendable, row: Int) {
		// Reused heap allocation for building ANSI attributes inside the loop.
		val attributes = mutableListOf<Int>()

		val rowPixels = rows[row]
		var lastPixel = blankPixel
		for (columnIndex in 0 until width) {
			val pixel = rowPixels[columnIndex]

			if (ansiLevel != AnsiLevel.NONE) {
				if (pixel.foreground != lastPixel.foreground) {
					attributes.addColor(
						pixel.foreground,
						ansiLevel,
						ansiFgColorSelector,
						ansiFgColorReset,
						ansiFgColorOffset,
					)
				}
				if (pixel.background != lastPixel.background) {
					attributes.addColor(
						pixel.background,
						ansiLevel,
						ansiBgColorSelector,
						ansiBgColorReset,
						ansiBgColorOffset,
					)
				}

				fun maybeToggleStyle(style: TextStyle, on: Int, off: Int) {
					if (style in pixel.textStyle) {
						if (style !in lastPixel.textStyle) {
							attributes += on
						}
					} else if (style in lastPixel.textStyle) {
						attributes += off
					}
				}
				if (pixel.textStyle != lastPixel.textStyle) {
					maybeToggleStyle(Bold, 1, 22)
					maybeToggleStyle(Dim, 2, 22)
					maybeToggleStyle(Italic, 3, 23)
					maybeToggleStyle(Underline, 4, 24)
					maybeToggleStyle(Invert, 7, 27)
					maybeToggleStyle(Strikethrough, 9, 29)
				}
				if (attributes.isNotEmpty()) {
					attributes.joinTo(
						appendable,
						separator = ansiSeparator,
						prefix = CSI,
						postfix = ansiClosingCharacter,
					)
					attributes.clear() // This list is reused!
				}
			}

			appendable.appendCodePoint(pixel.codePoint)
			lastPixel = pixel
		}

		if (lastPixel.background.isSpecifiedColor ||
			lastPixel.foreground.isSpecifiedColor ||
			lastPixel.textStyle.isNotEmptyTextStyle
		) {
			appendable.append(ansiReset)
			appendable.append(ansiClosingCharacter)
		}
	}

	private fun MutableList<Int>.addColor(
		color: Color,
		ansiLevel: AnsiLevel,
		select: Int,
		reset: Int,
		offset: Int,
	) {
		if (color.isUnspecifiedColor) {
			add(reset)
			return
		}
		when (ansiLevel) {
			AnsiLevel.NONE -> add(reset)
			AnsiLevel.ANSI16 -> {
				val ansi16Code = color.toAnsi16Code()
				if (ansi16Code == ansiFgColorReset || ansi16Code == ansiBgColorReset) {
					add(reset)
				} else {
					add(ansi16Code + offset)
				}
			}
			AnsiLevel.ANSI256 -> {
				add(select)
				add(ansiSelectorColor256)
				add(color.toAnsi256Code())
			}
			AnsiLevel.TRUECOLOR -> {
				add(select)
				add(ansiSelectorColorRgb)
				add(color.redInt)
				add(color.greenInt)
				add(color.blueInt)
			}
		}
	}

	fun render(): String = buildString {
		for (rowIndex in 0 until height) {
			if (rowIndex > 0) {
				append("\r\n")
			}
			appendRowTo(this, rowIndex)
		}
	}
}

internal class TextPixel(var codePoint: Int) {
	var background: Color = Color.Unspecified
	var foreground: Color = Color.Unspecified
	var textStyle: TextStyle = TextStyle.Empty

	constructor(char: Char) : this(char.code)

	fun reset() {
		codePoint = ' '.code
		background = Color.Unspecified
		foreground = Color.Unspecified
		textStyle = TextStyle.Empty
	}

	override fun toString() = buildString {
		append("TextPixel(\"")
		appendCodePoint(codePoint)
		append("\"")
		if (background.isSpecifiedColor) {
			append(" bg=")
			append(background)
		}
		if (foreground.isSpecifiedColor) {
			append(" fg=")
			append(foreground)
		}
		// TODO style
		append(')')
	}
}
