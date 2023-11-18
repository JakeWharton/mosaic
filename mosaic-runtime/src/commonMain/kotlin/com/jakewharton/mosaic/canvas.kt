package com.jakewharton.mosaic

import com.jakewharton.mosaic.ui.Color
import com.jakewharton.mosaic.ui.TextStyle
import com.jakewharton.mosaic.ui.TextStyle.Companion.Bold
import com.jakewharton.mosaic.ui.TextStyle.Companion.Dim
import com.jakewharton.mosaic.ui.TextStyle.Companion.Invert
import com.jakewharton.mosaic.ui.TextStyle.Companion.Italic
import com.jakewharton.mosaic.ui.TextStyle.Companion.None
import com.jakewharton.mosaic.ui.TextStyle.Companion.Strikethrough
import com.jakewharton.mosaic.ui.TextStyle.Companion.Underline

internal interface TextCanvas {
	val width: Int
	val height: Int
	var translationX: Int
	var translationY: Int

	operator fun get(row: Int, column: Int): TextPixel
}

internal class TextSurface(
	initialWidth: Int,
	initialHeight: Int,
) : TextCanvas {
	private var realWidth: Int = initialWidth
	private var realHeight: Int = initialHeight

	override val width: Int get() = realWidth
	override val height: Int get() = realHeight

	override var translationX = 0
	override var translationY = 0

	private val rows = MutableList(height) { createBlankRow(width) }

	override operator fun get(row: Int, column: Int): TextPixel {
		val x = translationX + column
		val y = translationY + row
		if (x < 0 || y < 0) {
			return reusableDirtyPixel
		}
		val widthDiff = x - width + 1
		if (widthDiff > 0) {
			if (widthDiff == 1) {
				rows.forEach { it.add(newBlankPixel) }
			} else {
				rows.forEach { it.addAll(createBlankRow(widthDiff)) }
			}
			realWidth += widthDiff
		}
		val heightDiff = y - height + 1
		if (heightDiff > 0) {
			if (heightDiff == 1) {
				rows.add(createBlankRow(width))
			} else {
				rows.addAll(MutableList(heightDiff) { createBlankRow(width) })
			}
			realHeight += heightDiff
		}
		return rows[y][x]
	}

	private inline fun createBlankRow(size: Int): MutableList<TextPixel> {
		return MutableList(size) { newBlankPixel }
	}

	fun appendRowTo(appendable: Appendable, row: Int) {
		// Reused heap allocation for building ANSI attributes inside the loop.
		val attributes = mutableListOf<Int>()

		val rowPixels = rows[row]
		var lastPixel = reusableBlankPixel
		for (columnIndex in 0 until width) {
			val pixel = rowPixels[columnIndex]
			if (pixel.foreground != lastPixel.foreground) {
				attributes += pixel.foreground?.fg ?: 39
			}
			if (pixel.background != lastPixel.background) {
				attributes += pixel.background?.bg ?: 49
			}

			fun maybeToggleStyle(style: TextStyle, on: Int, off: Int) {
				if (style in pixel.style) {
					if (style !in lastPixel.style) {
						attributes += on
					}
				} else if (style in lastPixel.style) {
					attributes += off
				}
			}
			if (pixel.style != lastPixel.style) {
				maybeToggleStyle(Bold, 1, 22)
				maybeToggleStyle(Dim, 2, 22)
				maybeToggleStyle(Italic, 3, 23)
				maybeToggleStyle(Underline, 4, 24)
				maybeToggleStyle(Invert, 7, 27)
				maybeToggleStyle(Strikethrough, 9, 29)
			}
			if (attributes.isNotEmpty()) {
				attributes.joinTo(appendable, separator = ";", prefix = "\u001B[", postfix = "m")
				attributes.clear() // This list is reused!
			}

			appendable.append(pixel.value)
			lastPixel = pixel
		}

		if (lastPixel.background != null ||
			lastPixel.foreground != null ||
			lastPixel.style != None
		) {
			appendable.append("\u001B[0m")
		}
	}

	fun render(): String = buildString {
		for (rowIndex in 0 until height) {
			if (rowIndex > 0) {
				append('\n')
			}
			appendRowTo(this, rowIndex)
		}
	}
}

/**
 * Returns always a new blank [TextPixel].
 */
private val newBlankPixel: TextPixel get() = TextPixel(' ')

/**
 * It is used in places where it is important that the [TextPixel]
 * has its original state and **will not change**.
 */
private val reusableBlankPixel: TextPixel = newBlankPixel

/**
 * It is used in places where the [TextPixel] state is not important
 * and it can change.
 */
private val reusableDirtyPixel: TextPixel = newBlankPixel

internal class TextPixel(var value: String) {
	var background: Color? = null
	var foreground: Color? = null
	var style = None

	constructor(char: Char) : this(char.toString())

	override fun toString() = buildString {
		append("TextPixel(\"")
		append(value)
		append("\"")
		if (background != null) {
			append(" bg=")
			append(background)
		}
		if (foreground != null) {
			append(" fg=")
			append(foreground)
		}
		// TODO style
		append(')')
	}
}
