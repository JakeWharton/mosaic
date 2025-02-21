package com.jakewharton.mosaic.text

import assertk.assertFailure
import assertk.assertThat
import assertk.assertions.hasSize
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import assertk.assertions.isInstanceOf
import com.jakewharton.mosaic.text.AnnotatedString.Range
import com.jakewharton.mosaic.ui.Color
import com.jakewharton.mosaic.ui.TextStyle
import kotlin.test.Test

class AnnotatedStringBuilderTest {

	@Test fun defaultConstructor() {
		val annotatedString = AnnotatedString.Builder().toAnnotatedString()

		assertThat(annotatedString.text).isEmpty()
		assertThat(annotatedString.spanStyles).isEmpty()
	}

	@Test fun constructorWithString() {
		val text = "a"
		val annotatedString = AnnotatedString.Builder(text).toAnnotatedString()

		assertThat(annotatedString.text).isEqualTo(text)
		assertThat(annotatedString.spanStyles).isEmpty()
	}

	@Test fun constructorWithAnnotatedString_hasSameAnnotatedStringAttributes() {
		val text = createAnnotatedString(text = "a")
		val annotatedString = AnnotatedString.Builder(text).toAnnotatedString()

		assertThat(annotatedString.text).isEqualTo(text.text)
		assertThat(annotatedString.spanStyles).isEqualTo(text.spanStyles)
	}

	@Test fun addStyle_withSpanStyle_addsStyle() {
		val style = SpanStyle(color = Color.Red)
		val start = 0
		val end = 1
		val annotatedString = with(AnnotatedString.Builder("ab")) {
			addStyle(style, start, end)
			toAnnotatedString()
		}

		val expectedSpanStyles = listOf(
			Range(style, start, end),
		)

		assertThat(annotatedString.spanStyles).isEqualTo(expectedSpanStyles)
	}

	@Test fun append_withString_appendsTheText() {
		val text = "a"
		val appendedText = "b"
		val annotatedString = with(AnnotatedString.Builder(text)) {
			append(appendedText)
			toAnnotatedString()
		}

		val expectedString = "$text$appendedText"

		assertThat(annotatedString.text).isEqualTo(expectedString)
		assertThat(annotatedString.spanStyles).isEmpty()
	}

	@Test fun append_withString_andMultipleCalls_appendsAllOfTheText() {
		val annotatedString = with(AnnotatedString.Builder("a")) {
			append("b")
			append("c")
			toAnnotatedString()
		}

		assertThat(annotatedString.text).isEqualTo("abc")
	}

	@Test fun append_withAnnotatedString_appendsTheText() {
		val color = Color.Red
		val text = "a"
		val annotatedString = createAnnotatedString(
			text = text,
			color = color,
		)

		val appendedColor = Color.Blue
		val appendedText = "b"
		val appendedAnnotatedString = createAnnotatedString(
			text = appendedText,
			color = appendedColor,
		)

		val buildResult = with(AnnotatedString.Builder(annotatedString)) {
			append(appendedAnnotatedString)
			toAnnotatedString()
		}

		val expectedString = "$text$appendedText"
		val expectedSpanStyles = listOf(
			Range(
				item = SpanStyle(color),
				start = 0,
				end = text.length,
			),
			Range(
				item = SpanStyle(appendedColor),
				start = text.length,
				end = expectedString.length,
			),
		)

		assertThat(buildResult.text).isEqualTo(expectedString)
		assertThat(buildResult.spanStyles).isEqualTo(expectedSpanStyles)
	}

	@Test fun append_withAnnotatedStringAndRange_appendsTheText() {
		val text = "a"
		val annotatedString = AnnotatedString(
			text = text,
			spanStylesOrNull = listOf(
				text.inclusiveRangeOf('a', 'a', item = SpanStyle(color = Color.Red)),
			),
		)

		// We want to test the cross product of the following cases:
		// - Range beginning at start, ending at end-1, completely overlapping [start,end), and
		//   completely inside (start, end-1).
		// - SpanStyle, ParagraphStyle, annotation
		val appendedText = "bcdef"
		val appendedSpanStyles = listOf(
			appendedText.inclusiveRangeOf('b', 'f', item = SpanStyle(color = Color.Blue)),
			appendedText.inclusiveRangeOf('c', 'f', item = SpanStyle(color = Color.Green)),
			appendedText.inclusiveRangeOf('b', 'e', item = SpanStyle(color = Color.Yellow)),
			appendedText.inclusiveRangeOf('c', 'e', item = SpanStyle(color = Color.Magenta)),
		)
		val appendedAnnotatedString = AnnotatedString(
			text = appendedText,
			spanStylesOrNull = appendedSpanStyles,
		)

		val buildResult = with(AnnotatedString.Builder(annotatedString)) {
			// Append everything but the first and last characters of the appended string.
			append(
				appendedAnnotatedString,
				start = appendedText.indexOf('c'),
				end = appendedText.indexOf('e') + 1,
			)
			toAnnotatedString()
		}

		val expectedString = "acde"
		val expectedSpanStyles = listOf(
			expectedString.inclusiveRangeOf('a', 'a', item = SpanStyle(color = Color.Red)),
			expectedString.inclusiveRangeOf('c', 'e', item = SpanStyle(color = Color.Blue)),
			expectedString.inclusiveRangeOf('c', 'e', item = SpanStyle(color = Color.Green)),
			expectedString.inclusiveRangeOf('c', 'e', item = SpanStyle(color = Color.Yellow)),
			expectedString.inclusiveRangeOf('c', 'e', item = SpanStyle(color = Color.Magenta)),
		)

		assertThat(buildResult.text).isEqualTo(expectedString)
		assertThat(buildResult.spanStyles).isEqualTo(expectedSpanStyles)
	}

	@Test fun append_withCharSequence_appendsTheText_whenAnnotatedString() {
		val color = Color.Red
		val text = "a"
		val annotatedString = createAnnotatedString(
			text = text,
			color = color,
		)

		val appendedColor = Color.Blue
		val appendedText = "b"
		val appendedAnnotatedString = createAnnotatedString(
			text = appendedText,
			color = appendedColor,
		)

		val buildResult = with(AnnotatedString.Builder(annotatedString)) {
			// Cast forces dispatch to the more general method, using the return value ensures
			// the right method was selected.
			append(appendedAnnotatedString as CharSequence)
				.toAnnotatedString()
		}

		val expectedString = "$text$appendedText"
		val expectedSpanStyles = listOf(
			Range(
				item = SpanStyle(color),
				start = 0,
				end = text.length,
			),
			Range(
				item = SpanStyle(appendedColor),
				start = text.length,
				end = expectedString.length,
			),
		)

		assertThat(buildResult.text).isEqualTo(expectedString)
		assertThat(buildResult.spanStyles).isEqualTo(expectedSpanStyles)
	}

	@Test fun append_withCharSequence_appendsTheText_whenNotAnnotatedString() {
		val text = "a"
		val appendedText: CharSequence = "bc"
		val annotatedString = with(AnnotatedString.Builder(text)) {
			append(appendedText)
			toAnnotatedString()
		}

		val expectedString = "abc"

		assertThat(annotatedString.text).isEqualTo(expectedString)
		assertThat(annotatedString.spanStyles).isEmpty()
	}

	@Test fun append_withCharSequenceAndRange_appendsTheText_whenNotAnnotatedString() {
		val text = "a"
		val appendedText: CharSequence = "bcde"
		val annotatedString = with(AnnotatedString.Builder(text)) {
			append(appendedText, 1, 3)
			toAnnotatedString()
		}

		val expectedString = "acd"

		assertThat(annotatedString.text).isEqualTo(expectedString)
		assertThat(annotatedString.spanStyles).isEmpty()
	}

	@Test fun pushStyle() {
		val text = "Test"
		val style = SpanStyle(color = Color.Red)
		val buildResult = AnnotatedString.Builder().apply {
			pushStyle(style)
			append(text)
			pop()
		}.toAnnotatedString()

		assertThat(buildResult.text).isEqualTo(text)
		assertThat(buildResult.spanStyles).hasSize(1)
		assertThat(buildResult.spanStyles[0].item).isEqualTo(style)
		assertThat(buildResult.spanStyles[0].start).isEqualTo(0)
		assertThat(buildResult.spanStyles[0].end).isEqualTo(buildResult.length)
	}

	@Test fun pushStyle_without_pop() {
		val styles = arrayOf(
			SpanStyle(color = Color.Red),
			SpanStyle(background = Color.Blue),
			SpanStyle(textStyle = TextStyle.Strikethrough),
		)

		val buildResult = with(AnnotatedString.Builder()) {
			styles.forEachIndexed { index, spanStyle ->
				// pop is intentionally not called here
				pushStyle(spanStyle)
				append("Style$index")
			}
			toAnnotatedString()
		}

		assertThat(buildResult.text).isEqualTo("Style0Style1Style2")
		assertThat(buildResult.spanStyles).hasSize(3)

		styles.forEachIndexed { index, spanStyle ->
			assertThat(buildResult.spanStyles[index].item).isEqualTo(spanStyle)
			assertThat(buildResult.spanStyles[index].end).isEqualTo(buildResult.length)
		}

		assertThat(buildResult.spanStyles[0].start).isEqualTo(0)
		assertThat(buildResult.spanStyles[1].start).isEqualTo("Style0".length)
		assertThat(buildResult.spanStyles[2].start).isEqualTo("Style0Style1".length)
	}

	@Test fun pushStyle_with_multiple_styles() {
		val spanStyle1 = SpanStyle(color = Color.Red)
		val spanStyle2 = SpanStyle(background = Color.Blue)

		val buildResult = with(AnnotatedString.Builder()) {
			pushStyle(spanStyle1)
			append("Test")
			pushStyle(spanStyle2)
			append(" me")
			pop()
			pop()
			toAnnotatedString()
		}

		assertThat(buildResult.text).isEqualTo("Test me")
		assertThat(buildResult.spanStyles).hasSize(2)

		assertThat(buildResult.spanStyles[0].item).isEqualTo(spanStyle1)
		assertThat(buildResult.spanStyles[0].start).isEqualTo(0)
		assertThat(buildResult.spanStyles[0].end).isEqualTo(buildResult.length)

		assertThat(buildResult.spanStyles[1].item).isEqualTo(spanStyle2)
		assertThat(buildResult.spanStyles[1].start).isEqualTo("Test".length)
		assertThat(buildResult.spanStyles[1].end).isEqualTo(buildResult.length)
	}

	@Test fun pushStyle_with_multiple_styles_on_top_of_each_other() {
		val styles = arrayOf(
			SpanStyle(color = Color.Red),
			SpanStyle(background = Color.Blue),
			SpanStyle(textStyle = TextStyle.Strikethrough),
		)

		val buildResult = with(AnnotatedString.Builder()) {
			styles.forEach { spanStyle ->
				// pop is intentionally not called here
				pushStyle(spanStyle)
			}
			toAnnotatedString()
		}

		assertThat(buildResult.text).isEmpty()
		assertThat(buildResult.spanStyles).hasSize(3)
		styles.forEachIndexed { index, spanStyle ->
			assertThat(buildResult.spanStyles[index].item).isEqualTo(spanStyle)
			assertThat(buildResult.spanStyles[index].start).isEqualTo(buildResult.length)
			assertThat(buildResult.spanStyles[index].end).isEqualTo(buildResult.length)
		}
	}

	@Test fun pushStyle_with_multiple_stacks_should_construct_styles_in_the_same_order() {
		val styles = arrayOf(
			SpanStyle(color = Color.Red),
			SpanStyle(background = Color.Blue),
			SpanStyle(textStyle = TextStyle.Strikethrough),
			SpanStyle(color = Color.Green),
		)

		val buildResult = with(AnnotatedString.Builder()) {
			pushStyle(styles[0])
			append("layer1-1")
			pushStyle(styles[1])
			append("layer2-1")
			pushStyle(styles[2])
			append("layer3-1")
			pop()
			pushStyle(styles[3])
			append("layer3-2")
			pop()
			append("layer2-2")
			pop()
			append("layer1-2")
			toAnnotatedString()
		}

		assertThat(buildResult.spanStyles).hasSize(4)
		styles.forEachIndexed { index, spanStyle ->
			assertThat(buildResult.spanStyles[index].item).isEqualTo(spanStyle)
		}
	}

	@Test fun pushStyle_with_multiple_nested_styles_should_return_styles_in_same_order() {
		val styles = arrayOf(
			SpanStyle(color = Color.Red),
			SpanStyle(background = Color.Blue),
			SpanStyle(textStyle = TextStyle.Strikethrough),
			SpanStyle(color = Color.Green),
		)

		val buildResult = with(AnnotatedString.Builder()) {
			pushStyle(styles[0])
			append("layer1-1")
			pushStyle(styles[1])
			append("layer2-1")
			pop()
			pushStyle(styles[2])
			append("layer2-2")
			pushStyle(styles[3])
			append("layer3-1")
			pop()
			append("layer2-3")
			pop()
			append("layer1-2")
			pop()
			toAnnotatedString()
		}

		assertThat(buildResult.spanStyles).hasSize(4)
		styles.forEachIndexed { index, spanStyle ->
			assertThat(buildResult.spanStyles[index].item).isEqualTo(spanStyle)
		}
	}

	@Test fun pop_when_empty_does_not_throw_exception() {
		assertFailure {
			AnnotatedString.Builder().pop()
		}.isInstanceOf<IllegalStateException>()
	}

	@Test fun pop_in_the_middle() {
		val spanStyle1 = SpanStyle(color = Color.Red)
		val spanStyle2 = SpanStyle(color = Color.Blue)

		val buildResult = with(AnnotatedString.Builder()) {
			append("Style0")
			pushStyle(spanStyle1)
			append("Style1")
			pop()
			pushStyle(spanStyle2)
			append("Style2")
			pop()
			append("Style3")
			toAnnotatedString()
		}

		assertThat(buildResult.text).isEqualTo("Style0Style1Style2Style3")
		assertThat(buildResult.spanStyles).hasSize(2)

		// the order is first applied is in the second
		assertThat(buildResult.spanStyles[0].item).isEqualTo((spanStyle1))
		assertThat(buildResult.spanStyles[0].start).isEqualTo(("Style0".length))
		assertThat(buildResult.spanStyles[0].end).isEqualTo(("Style0Style1".length))

		assertThat(buildResult.spanStyles[1].item).isEqualTo((spanStyle2))
		assertThat(buildResult.spanStyles[1].start).isEqualTo(("Style0Style1".length))
		assertThat(buildResult.spanStyles[1].end).isEqualTo(("Style0Style1Style2".length))
	}

	@Test fun push_increments_the_style_index() {
		val style = SpanStyle(color = Color.Red)
		with(AnnotatedString.Builder()) {
			val styleIndex0 = pushStyle(style)
			val styleIndex1 = pushStyle(style)
			val styleIndex2 = pushStyle(style)

			assertThat(styleIndex0).isEqualTo(0)
			assertThat(styleIndex1).isEqualTo(1)
			assertThat(styleIndex2).isEqualTo(2)
		}
	}

	@Test fun push_reduces_the_style_index_after_pop() {
		val spanStyle = SpanStyle(color = Color.Red)

		with(AnnotatedString.Builder()) {
			val styleIndex0 = pushStyle(spanStyle)
			val styleIndex1 = pushStyle(spanStyle)

			assertThat(styleIndex0).isEqualTo(0)
			assertThat(styleIndex1).isEqualTo(1)

			// a pop should reduce the next index to one
			pop()

			val styleIndex = pushStyle(spanStyle)
			assertThat(styleIndex).isEqualTo(1)
		}
	}

	@Test fun pop_until_throws_exception_for_invalid_index() {
		val style = SpanStyle(color = Color.Red)
		with(AnnotatedString.Builder()) {
			val styleIndex = pushStyle(style)

			assertFailure {
				// should throw exception
				pop(styleIndex + 1)
			}.isInstanceOf<IllegalStateException>()
		}
	}

	@Test fun pop_until_index_pops_correctly() {
		val style = SpanStyle(color = Color.Red)
		with(AnnotatedString.Builder()) {
			pushStyle(style)
			// store the index of second push
			val styleIndex = pushStyle(style)
			pushStyle(style)
			// pop up to and including styleIndex
			pop(styleIndex)
			// push again to get a new index to compare
			val newStyleIndex = pushStyle(style)

			assertThat(newStyleIndex).isEqualTo(styleIndex)
		}
	}

	@Test fun withStyle_applies_style_to_block() {
		val style = SpanStyle(color = Color.Red)
		val buildResult = with(AnnotatedString.Builder()) {
			withStyle(style) {
				append("Style")
			}
			toAnnotatedString()
		}

		assertThat(buildResult.spanStyles).isEqualTo(
			listOf(Range(style, 0, buildResult.length)),
		)
	}

	@Test fun append_char_appends() {
		val buildResult = with(AnnotatedString.Builder("a")) {
			append('b')
			append('c')
			toAnnotatedString()
		}

		assertThat(buildResult).isEqualTo(AnnotatedString("abc"))
	}

	@Test fun builderLambda() {
		val text1 = "Hello"
		val text2 = "World"
		val spanStyle1 = SpanStyle(color = Color.Red)
		val spanStyle2 = SpanStyle(background = Color.Green)
		val spanStyle3 = SpanStyle(color = Color.Blue)

		val buildResult = buildAnnotatedString {
			withStyle(spanStyle1) {
				withStyle(spanStyle2) {
					append(text1)
				}
			}
			append(" ")
			pushStyle(spanStyle3)
			append(text2)
			pop()
		}

		val expectedString = "$text1 $text2"
		val expectedSpanStyles = listOf(
			Range(spanStyle1, 0, text1.length),
			Range(spanStyle2, 0, text1.length),
			Range(spanStyle3, text1.length + 1, expectedString.length),
		)

		assertThat(buildResult.text).isEqualTo(expectedString)
		assertThat(buildResult.spanStyles).isEqualTo(expectedSpanStyles)
	}

	@Test fun toAnnotatedString_calling_twice_creates_equal_annotated_strings() {
		val builder = AnnotatedString.Builder().apply {
			// pushed styles not popped on purpose
			pushStyle(SpanStyle(color = Color.Red))
			append("Hello")
			pushStyle(SpanStyle(color = Color.Blue))
			append("World")
		}

		assertThat(builder.toAnnotatedString()).isEqualTo(builder.toAnnotatedString())
	}

	@Test fun can_call_other_functions_after_toAnnotatedString() {
		val builder = AnnotatedString.Builder().apply {
			// pushed styles not popped on purpose
			pushStyle(SpanStyle(color = Color.Red))
			append("Hello")
			pushStyle(SpanStyle(color = Color.Blue))
			append("World")
		}

		val buildResult1 = builder.toAnnotatedString()
		val buildResult2 = with(builder) {
			pop()
			pop()
			pushStyle(SpanStyle(color = Color.Green))
			append("!")
			toAnnotatedString()
		}

		// buildResult2 should be the same as creating a new AnnotatedString based on the first
		// result and appending the same values
		val expectedResult = with(AnnotatedString.Builder(buildResult1)) {
			withStyle(SpanStyle(color = Color.Green)) {
				append("!")
			}
			toAnnotatedString()
		}

		assertThat(buildResult2).isEqualTo(expectedResult)
	}

	private fun createAnnotatedString(
		text: String,
		color: Color = Color.Red,
	): AnnotatedString {
		return AnnotatedString(
			text = text,
			spanStyle = SpanStyle(color),
		)
	}

	/**
	 * Returns a [Range] from the index of [start] to the index of [end], both inclusive.
	 */
	private fun <T> String.inclusiveRangeOf(
		start: Char,
		end: Char,
		item: T,
	) = Range(
		item = item,
		start = indexOf(start),
		end = indexOf(end) + 1,
	)
}
