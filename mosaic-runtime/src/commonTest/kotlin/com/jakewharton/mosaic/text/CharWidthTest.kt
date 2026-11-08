package com.jakewharton.mosaic.text

import kotlin.test.Test
import kotlin.test.assertEquals

class CharWidthTest {

	@Test fun asciiCharacters() {
		assertEquals(1, charWidth('A'.code))
		assertEquals(1, charWidth('z'.code))
		assertEquals(1, charWidth('0'.code))
		assertEquals(1, charWidth(' '.code))
		assertEquals(1, charWidth('~'.code))
	}

	@Test fun latinSupplement() {
		assertEquals(1, charWidth('é'.code))
		assertEquals(1, charWidth('ü'.code))
		assertEquals(1, charWidth('ñ'.code))
	}

	@Test fun cjkIdeographs() {
		assertEquals(2, charWidth('你'.code))
		assertEquals(2, charWidth('好'.code))
		assertEquals(2, charWidth(0x4E2D)) // 中
		assertEquals(2, charWidth(0x56FD)) // 国
		assertEquals(2, charWidth(0x4E00)) // First CJK
		assertEquals(2, charWidth(0x9FFF)) // Last CJK
	}

	@Test fun cjkExtensionA() {
		assertEquals(2, charWidth(0x3400))
		assertEquals(2, charWidth(0x4DBF))
	}

	@Test fun cjkExtensionB() {
		assertEquals(2, charWidth(0x20000))
		assertEquals(2, charWidth(0x2FFFF))
	}

	@Test fun fullwidthForms() {
		assertEquals(2, charWidth(0xFF01)) // ！
		assertEquals(2, charWidth(0xFF60))
		assertEquals(2, charWidth(0xFFE0)) // ¢ (fullwidth)
		assertEquals(2, charWidth(0xFFE6))
	}

	@Test fun hiragana() {
		assertEquals(2, charWidth(0x3041)) // あ
		assertEquals(2, charWidth(0x309F))
	}

	@Test fun katakana() {
		assertEquals(2, charWidth(0x30A1)) // ア
		assertEquals(2, charWidth(0x30FF))
	}

	@Test fun hangul() {
		assertEquals(2, charWidth(0xAC00)) // 가
		assertEquals(2, charWidth(0xD7AF))
	}

	@Test fun combiningMarksAreZeroWidth() {
		assertEquals(0, charWidth(0x0300)) // Combining grave accent
		assertEquals(0, charWidth(0x0301)) // Combining acute accent
		assertEquals(0, charWidth(0x036F)) // Last combining diacritical
	}

	@Test fun variationSelectorsAreZeroWidth() {
		assertEquals(0, charWidth(0xFE00))
		assertEquals(0, charWidth(0xFE0F))
		assertEquals(0, charWidth(0xE0100))
		assertEquals(0, charWidth(0xE01EF))
	}

	@Test fun zeroWidthSpace() {
		assertEquals(0, charWidth(0x200B)) // ZWSP
		assertEquals(0, charWidth(0x200C)) // ZWNJ
		assertEquals(0, charWidth(0x200D)) // ZWJ
	}

	@Test fun cjkRadicalsAndKangxi() {
		assertEquals(2, charWidth(0x2E80))
		assertEquals(2, charWidth(0x2F00))
		assertEquals(2, charWidth(0x303E))
	}

	@Test fun emoticonsAreWide() {
		assertEquals(2, charWidth(0x1F600)) // 😀
		assertEquals(2, charWidth(0x1F603)) // 😃
		assertEquals(2, charWidth(0x1F615)) // 😕
		assertEquals(2, charWidth(0x1F64F)) // 🙏
	}

	@Test fun miscSymbolsAndPictographsAreWide() {
		assertEquals(2, charWidth(0x1F300)) // 🌀
		assertEquals(2, charWidth(0x1F4A9)) // 💩
		assertEquals(2, charWidth(0x1F5FF)) // 🗿
	}

	@Test fun transportSymbolsAreWide() {
		assertEquals(2, charWidth(0x1F680)) // 🚀
		assertEquals(2, charWidth(0x1F697)) // 🚗
		assertEquals(2, charWidth(0x1F6FF))
	}

	@Test fun supplementalSymbolsAreWide() {
		assertEquals(2, charWidth(0x1F900)) // 🤀
		assertEquals(2, charWidth(0x1F9E6)) // 🧦
		assertEquals(2, charWidth(0x1F9FF)) // 🧿
	}

	@Test fun extendedSymbolsAreWide() {
		assertEquals(2, charWidth(0x1FA70)) // 🩰
		assertEquals(2, charWidth(0x1FA80)) // 🪀
		assertEquals(2, charWidth(0x1FAFF))
	}

	@Test fun chessSymbolsAreWide() {
		assertEquals(2, charWidth(0x1FA00))
		assertEquals(2, charWidth(0x1FA60))
		assertEquals(2, charWidth(0x1FA6F))
	}

	@Test fun regionalIndicatorsAreWide() {
		assertEquals(2, charWidth(0x1F1E6)) // 🇦
		assertEquals(2, charWidth(0x1F1E8)) // 🇨
		assertEquals(2, charWidth(0x1F1FF)) // 🇿
	}

	@Test fun enclosedIdeographicSupplement() {
		assertEquals(2, charWidth(0x1F200)) // 🈀
		assertEquals(2, charWidth(0x1F2FF))
	}

	@Test fun bopomofo() {
		assertEquals(2, charWidth(0x3100))
		assertEquals(2, charWidth(0x312F))
	}

	@Test fun enclosedCjk() {
		assertEquals(2, charWidth(0x3200))
		assertEquals(2, charWidth(0x33FF))
	}

	@Test fun cjkCompatibilityIdeographs() {
		assertEquals(2, charWidth(0xF900))
		assertEquals(2, charWidth(0xFAFF))
	}

	@Test fun cjkCompatibilityForms() {
		assertEquals(2, charWidth(0xFE30))
		assertEquals(2, charWidth(0xFE6F))
	}

	@Test fun kanaSupplement() {
		assertEquals(2, charWidth(0x1B000))
		assertEquals(2, charWidth(0x1B12F))
	}

	@Test fun yiSyllables() {
		assertEquals(2, charWidth(0xA000))
		assertEquals(2, charWidth(0xA4CF))
	}

	@Test fun hangulJamo() {
		assertEquals(2, charWidth(0x1100))
		assertEquals(2, charWidth(0x115F))
	}

}
