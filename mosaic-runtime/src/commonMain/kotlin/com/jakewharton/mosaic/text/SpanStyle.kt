package com.jakewharton.mosaic.text

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import com.jakewharton.mosaic.ui.Color
import com.jakewharton.mosaic.ui.TextStyle
import com.jakewharton.mosaic.ui.UnderlineStyle
import com.jakewharton.mosaic.ui.takeOrElse

// TODO Poko
@Immutable
public class SpanStyle(
	public val color: Color = Color.Unspecified,
	public val background: Color = Color.Unspecified,
	public val textStyle: TextStyle = TextStyle.Unspecified,
	public val underlineStyle: UnderlineStyle = UnderlineStyle.Unspecified,
	public val underlineColor: Color = Color.Unspecified,
) {

	/**
	 * Returns a new span style that is a combination of this style and the given [other] style.
	 *
	 * [other] span style's null or inherit properties are replaced with the non-null properties of
	 * this span style. Another way to think of it is that the "missing" properties of the [other]
	 * style are _filled_ by the properties of this style.
	 *
	 * If the given span style is null, returns this span style.
	 */
	@Stable
	public fun merge(other: SpanStyle? = null): SpanStyle {
		if (other == null) return this
		return SpanStyle(
			color = other.color.takeOrElse { this.color },
			background = other.background.takeOrElse { this.background },
			textStyle = other.textStyle.takeOrElse { this.textStyle },
			underlineStyle = other.underlineStyle.takeOrElse { this.underlineStyle },
			underlineColor = other.underlineColor.takeOrElse { this.underlineColor },
		)
	}

	/**
	 * Plus operator overload that applies a [merge].
	 */
	@Stable
	public operator fun plus(other: SpanStyle): SpanStyle = this.merge(other)

	public fun copy(
		color: Color = this.color,
		background: Color = this.background,
		textStyle: TextStyle = this.textStyle,
		underlineStyle: UnderlineStyle = this.underlineStyle,
		underlineColor: Color = this.underlineColor,
	): SpanStyle {
		return SpanStyle(
			color = color,
			background = background,
			textStyle = textStyle,
			underlineStyle = underlineStyle,
			underlineColor = underlineColor,
		)
	}

	override fun equals(other: Any?): Boolean {
		if (this === other) return true
		if (other == null || this::class != other::class) return false

		other as SpanStyle

		if (color != other.color) return false
		if (background != other.background) return false
		if (textStyle != other.textStyle) return false
		if (underlineStyle != other.underlineStyle) return false
		if (underlineColor != other.underlineColor) return false

		return true
	}

	override fun hashCode(): Int {
		var result = color.hashCode()
		result = 31 * result + background.hashCode()
		result = 31 * result + textStyle.hashCode()
		result = 31 * result + underlineStyle.hashCode()
		result = 31 * result + underlineColor.hashCode()
		return result
	}

	override fun toString(): String {
		return "SpanStyle(" +
			"color=$color, " +
			"background=$background, " +
			"textStyle=$textStyle, " +
			"underlineStyle=$underlineStyle, " +
			"underlineColor=$underlineColor, " +
			")"
	}
}
