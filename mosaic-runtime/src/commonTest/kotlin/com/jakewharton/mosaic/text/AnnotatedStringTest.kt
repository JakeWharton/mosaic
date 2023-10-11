package com.jakewharton.mosaic.text

import com.jakewharton.mosaic.text.AnnotatedString.Range
import com.jakewharton.mosaic.ui.Color
import com.varabyte.truthish.assertThat
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class AnnotatedStringTest {

	@Test fun length_returns_text_length() {
		val text = "abc"
		val annotatedString = AnnotatedString(text)
		assertThat(annotatedString.length).isEqualTo(text.length)
	}

	@Test fun plus_operator_creates_a_new_annotated_string() {
		val text1 = "Hello"
		val spanStyles1 = listOf(
			Range(SpanStyle(color = Color.Red), 0, 3),
			Range(SpanStyle(color = Color.Blue), 2, 4)
		)
		val annotatedString1 = AnnotatedString(
			text = text1,
			spanStylesOrNull = spanStyles1,
		)

		val text2 = "World"
		val spanStyle = SpanStyle(color = Color.Cyan)
		val annotatedString2 = AnnotatedString(
			text = text2,
			spanStylesOrNull = listOf(Range(spanStyle, 0, text2.length)),
		)

		assertThat(annotatedString1 + annotatedString2).isEqualTo(
			AnnotatedString(
				"$text1$text2",
				spanStyles1 + listOf(
					Range(spanStyle, text1.length, text1.length + text2.length)
				),
			)
		)
	}

	@Test fun subSequence_returns_the_correct_string() {
		val annotatedString = AnnotatedString.Builder("abcd").toAnnotatedString()

		assertThat(annotatedString.subSequence(1, 3).text).isEqualTo("bc")
	}

	@Test fun subSequence_returns_empty_text_for_start_equals_end() {
		val annotatedString = with(AnnotatedString.Builder()) {
			withStyle(SpanStyle(color = Color.Red)) {
				append("a")
			}
			withStyle(SpanStyle(color = Color.Red)) {
				append("b")
			}
			append("c")
			toAnnotatedString()
		}.subSequence(1, 1)

		assertThat(annotatedString).isEqualTo(
			AnnotatedString("", listOf(Range(SpanStyle(color = Color.Red), 0, 0)))
		)
	}

	@Test fun subSequence_returns_original_text_for_text_range_is_full_range() {
		val annotatedString = with(AnnotatedString.Builder()) {
			withStyle(SpanStyle(color = Color.Red)) {
				append("a")
			}
			withStyle(SpanStyle(color = Color.Blue)) {
				append("b")
			}
			append("c")
			toAnnotatedString()
		}

		assertThat(annotatedString.subSequence(0, 3)).isSameAs(annotatedString)
	}

	@Test fun subSequence_doesNot_include_styles_before_the_start() {
		val annotatedString = with(AnnotatedString.Builder()) {
			withStyle(SpanStyle(color = Color.Red)) {
				append("a")
			}
			append("b")
			append("c")
			toAnnotatedString()
		}

		assertThat(annotatedString.subSequence("ab".length, annotatedString.length)).isEqualTo(
			AnnotatedString("c")
		)
	}

	@Test fun subSequence_doesNot_include_styles_after_the_end() {
		val annotatedString = with(AnnotatedString.Builder()) {
			append("a")
			withStyle(SpanStyle(color = Color.Red)) {
				append("b")
			}
			append("c")
			toAnnotatedString()
		}

		assertThat(annotatedString.subSequence(0, "a".length)).isEqualTo(
			AnnotatedString("a")
		)
	}

	@Test fun subSequence_collapsed_item_with_itemStart_equalTo_rangeStart() {
		val style = SpanStyle(color = Color.Red)
		val annotatedString = with(AnnotatedString.Builder()) {
			append("abc")
			// add collapsed item at the beginning of b
			addStyle(style, 1, 1)
			toAnnotatedString()
		}

		assertThat(annotatedString.subSequence(1, 2)).isEqualTo(
			AnnotatedString("b", listOf(Range(style, 0, 0)))
		)
	}

	@Test fun subSequence_collapses_included_item() {
		val style = SpanStyle(color = Color.Red)
		val annotatedString = with(AnnotatedString.Builder()) {
			append("a")
			// will collapse this style in subsequence
			withStyle(style) {
				append("b")
			}
			append("c")
			toAnnotatedString()
		}

		// subsequence with 1,1 will remove text, but include the style
		assertThat(annotatedString.subSequence(1, 1)).isEqualTo(
			AnnotatedString("", listOf(Range(style, 0, 0)))
		)
	}

	@Test fun subSequence_collapses_covering_item() {
		val style = SpanStyle(color = Color.Red)
		val annotatedString = with(AnnotatedString.Builder()) {
			withStyle(style) {
				append("abc")
			}
			toAnnotatedString()
		}

		assertThat(annotatedString.subSequence(1, 1)).isEqualTo(
			AnnotatedString("", listOf(Range(style, 0, 0)))
		)
	}

	@Test fun subSequence_with_collapsed_range_with_collapsed_item() {
		val style = SpanStyle(color = Color.Red)
		val annotatedString = with(AnnotatedString.Builder()) {
			append("abc")
			// add collapsed item at the beginning of b
			addStyle(style, 1, 1)
			toAnnotatedString()
		}

		assertThat(annotatedString.subSequence(1, 1)).isEqualTo(
			AnnotatedString("", listOf(Range(style, 0, 0)))
		)
	}

	@Test fun subSequence_includes_partial_matches() {
		val annotatedString = with(AnnotatedString.Builder()) {
			withStyle(SpanStyle(color = Color.Red)) {
				append("ab")
			}
			withStyle(SpanStyle(color = Color.Blue)) {
				append("c")
			}
			append("de")
			toAnnotatedString()
		}

		val expectedString = with(AnnotatedString.Builder()) {
			withStyle(SpanStyle(color = Color.Red)) {
				append("b")
			}
			withStyle(SpanStyle(color = Color.Blue)) {
				append("c")
			}
			append("d")
			toAnnotatedString()
		}

		val subSequence = annotatedString.subSequence("a".length, "abcd".length)

		assertThat(subSequence).isEqualTo(expectedString)
	}

	@Test fun subSequence_throws_exception_for_start_greater_than_end() {
		assertFailsWith<IllegalArgumentException> {
			AnnotatedString("ab").subSequence(1, 0)
		}
	}

	@Test fun creating_item_with_start_greater_than_end_throws_exception() {
		assertFailsWith<IllegalArgumentException> {
			Range(SpanStyle(color = Color.Red), 1, 0)
		}
	}

	@Test fun creating_item_with_start_equal_to_end_does_not_throw_exception() {
		Range(SpanStyle(color = Color.Red), 1, 1)
	}

	@Test fun constructor_function_with_single_spanStyle() {
		val text = "a"
		val spanStyle = SpanStyle(color = Color.Red)

		assertThat(
			AnnotatedString(text, spanStyle)
		).isEqualTo(
			AnnotatedString(text, listOf(Range(spanStyle, 0, text.length)))
		)
	}

	@Test fun toString_returns_the_plain_string() {
		val text = "abc"
		assertEquals(text, AnnotatedString(text).toString())
	}
}
