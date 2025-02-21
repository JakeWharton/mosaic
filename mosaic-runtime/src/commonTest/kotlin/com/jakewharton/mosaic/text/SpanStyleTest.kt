package com.jakewharton.mosaic.text

import assertk.assertThat
import assertk.assertions.isEqualTo
import com.jakewharton.mosaic.ui.Color
import com.jakewharton.mosaic.ui.TextStyle
import kotlin.test.Test

class SpanStyleTest {

	@Test fun constructorWithDefaultValues() {
		val style = SpanStyle()

		assertThat(style.color).isEqualTo(Color.Unspecified)
		assertThat(style.textStyle).isEqualTo(TextStyle.Unspecified)
		assertThat(style.background).isEqualTo(Color.Unspecified)
	}

	@Test fun constructorWithCustomizedColor() {
		val color = Color.Red

		val style = SpanStyle(color = color)

		assertThat(style.color).isEqualTo(color)
	}

	@Test fun constructorWithCustomizedTextStyle() {
		val textStyle = TextStyle.Strikethrough

		val style = SpanStyle(textStyle = textStyle)

		assertThat(style.textStyle).isEqualTo(textStyle)
	}

	@Test fun constructorWithCustomizedBackground() {
		val color = Color.Red

		val style = SpanStyle(background = color)

		assertThat(style.background).isEqualTo(color)
	}

	@Test fun mergeWithEmptyOtherShouldReturnThis() {
		val style = SpanStyle()

		val newSpanStyle = style.merge()

		assertThat(newSpanStyle).isEqualTo(style)
	}

	@Test fun mergeWithOthersColorIsNullShouldUseThisColor() {
		val style = SpanStyle(color = Color.Red)

		val newSpanStyle = style.merge(SpanStyle(color = Color.Unspecified))

		assertThat(newSpanStyle.color).isEqualTo(style.color)
	}

	@Test fun mergeWithOthersColorIsSetShouldUseOthersColor() {
		val style = SpanStyle(color = Color.Red)
		val otherStyle = SpanStyle(color = Color.Green)

		val newSpanStyle = style.merge(otherStyle)

		assertThat(newSpanStyle.color).isEqualTo(otherStyle.color)
	}

	@Test fun mergeWithOthersTextStyleIsNullShouldUseThisTextStyle() {
		val style = SpanStyle(textStyle = TextStyle.Strikethrough)

		val newSpanStyle = style.merge(SpanStyle(textStyle = TextStyle.Unspecified))

		assertThat(newSpanStyle.textStyle).isEqualTo(style.textStyle)
	}

	@Test fun mergeWithOthersTextStyleIsSetShouldUseOthersTextStyle() {
		val style = SpanStyle(textStyle = TextStyle.Bold)
		val otherStyle = SpanStyle(textStyle = TextStyle.Strikethrough)

		val newSpanStyle = style.merge(otherStyle)

		assertThat(newSpanStyle.textStyle).isEqualTo(otherStyle.textStyle)
	}

	@Test fun mergeWithOthersBackgroundIsNullShouldUseThisBackground() {
		val style = SpanStyle(background = Color.Red)

		val newSpanStyle = style.merge(SpanStyle(background = Color.Unspecified))

		assertThat(newSpanStyle.background).isEqualTo(style.background)
	}

	@Test fun mergeWithOthersBackgroundIsSetShouldUseOthersBackground() {
		val style = SpanStyle(background = Color.Red)
		val otherStyle = SpanStyle(background = Color.Green)

		val newSpanStyle = style.merge(otherStyle)

		assertThat(newSpanStyle.background).isEqualTo(otherStyle.background)
	}

	@Test fun plusOperatorMerges() {
		val style = SpanStyle(
			color = Color.Red,
			textStyle = TextStyle.Strikethrough,
		) + SpanStyle(
			color = Color.Green,
			background = Color.Blue,
		)

		assertThat(style).isEqualTo(
			SpanStyle(
				// Overridden by RHS
				color = Color.Green,
				// From LHS
				textStyle = TextStyle.Strikethrough,
				// From RHS
				background = Color.Blue,
			),
		)
	}
}
