package com.jakewharton.mosaic

import com.jakewharton.mosaic.ui.Color
import com.jakewharton.mosaic.ui.TextStyle
import com.jakewharton.mosaic.ui.UnderlineStyle
import com.jakewharton.mosaic.ui.isNotEmptyTextStyle
import com.jakewharton.mosaic.ui.isSpecifiedColor
import com.jakewharton.mosaic.ui.isSpecifiedTextStyle
import com.jakewharton.mosaic.ui.isSpecifiedUnderlineStyle
import de.cketti.codepoints.appendCodePoint
import kotlin.jvm.JvmField

@MosaicUnstableApi
public class TextCanvas(
	public val width: Int,
	public val height: Int,
) {
	@JvmField
	@PublishedApi
	internal var translationX: Int = 0
	@JvmField
	@PublishedApi
	internal var translationY: Int = 0

	@JvmField
	internal val cells = Array(width * height) { TextPixel(' '.code) }

	public inline fun withTranslation(x: Int, y: Int, block: TextCanvas.() -> Unit) {
		val oldTranslationX = translationX
		val oldTranslationY = translationY
		translationX = x
		translationY = y
		block()
		translationX = oldTranslationX
		translationY = oldTranslationY
	}

	public operator fun get(row: Int, column: Int): TextPixel {
		val x = translationX + column
		val y = translationY + row
		check(x in 0 until width)
		check(y in 0 until height)
		return cells[y * width + x]
	}
}

@MosaicUnstableApi
public class TextPixel(
	public var codePoint: Int,
) {
	public var background: Color = Color.Unspecified
	public var foreground: Color = Color.Unspecified
	public var textStyle: TextStyle = TextStyle.Empty
	public var underlineStyle: UnderlineStyle = UnderlineStyle.None
	public var underlineColor: Color = Color.Unspecified

	override fun toString(): String = buildString {
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
		if (textStyle.isSpecifiedTextStyle && textStyle.isNotEmptyTextStyle) {
			append(" textStyle=")
			append(textStyle)
		}
		if (underlineStyle.isSpecifiedUnderlineStyle) {
			append(" underlineStyle=")
			append(underlineStyle)
		}
		if (underlineColor.isSpecifiedColor) {
			append(" underlineColor=")
			append(underlineColor)
		}
		append(')')
	}
}
