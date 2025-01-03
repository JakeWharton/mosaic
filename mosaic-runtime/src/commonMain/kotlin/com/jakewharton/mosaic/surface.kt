package com.jakewharton.mosaic

import androidx.collection.MutableIntList
import androidx.collection.mutableIntListOf
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

private val blankPixel = TextPixel(' ')

public interface TextCanvas {
	public val height: Int
	public val width: Int

	public fun render(ansiLevel: AnsiLevel): String
	public fun appendRowTo(appendable: Appendable, row: Int, ansiLevel: AnsiLevel)
}

internal class TextSurface(
	override val width: Int,
	override val height: Int,
) : TextCanvas {
	var translationX = 0
	var translationY = 0

	private val cells = Array(width * height) { TextPixel(' ') }

	operator fun get(row: Int, column: Int): TextPixel {
		val x = translationX + column
		val y = row + translationY
		check(x in 0 until width)
		check(y in 0 until height)
		return cells[y * width + x]
	}

	override fun appendRowTo(appendable: Appendable, row: Int, ansiLevel: AnsiLevel) {
		// Reused heap allocation for building ANSI attributes inside the loop.
		val attributes = mutableIntListOf()

		var lastPixel = blankPixel

		val rowStart = row * width
		val rowStop = rowStart + width
		for (columnIndex in rowStart until rowStop) {
			val pixel = cells[columnIndex]

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
					appendable.append(CSI)
					attributes.forEachIndexed { index, element ->
						if (index > 0) {
							appendable.append(ansiSeparator)
						}
						appendable.append(element.toString())
					}
					appendable.append(ansiClosingCharacter)
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

	private fun MutableIntList.addColor(
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

	override fun render(ansiLevel: AnsiLevel): String = buildString {
		if (height > 0) {
			for (rowIndex in 0 until height) {
				appendRowTo(this, rowIndex, ansiLevel)
				append("\n")
			}
			// Remove trailing newline.
			setLength(length - 1)
		}
	}
}

internal class TextPixel(var codePoint: Int) {
	var background: Color = Color.Unspecified
	var foreground: Color = Color.Unspecified
	var textStyle: TextStyle = TextStyle.Empty

	constructor(char: Char) : this(char.code)

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
