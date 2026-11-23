package com.jakewharton.mosaic.text

/**
 * Returns the number of terminal columns a Unicode code point occupies.
 * This follows the wcwidth() convention: 2 for wide characters, 0 for combining/zero-width, 1 otherwise.
 */
internal fun charWidth(codepoint: Int): Int {
	if (codepoint in zeroWidthSorted) return 0
	if (codepoint in wideSorted) return 2
	return 1
}

private val zeroWidthRanges = listOf(
	0x0300..0x036F, // Combining Diacritical Marks
	0x0483..0x0489, // Cyrillic combining marks
	0x0591..0x05BD, // Hebrew combining marks
	0x05BF..0x05BF,
	0x05C1..0x05C2,
	0x05C4..0x05C5,
	0x05C7..0x05C7,
	0x0610..0x061A, // Arabic combining marks
	0x064B..0x065F,
	0x0670..0x0670,
	0x06D6..0x06DC,
	0x06DF..0x06E4,
	0x06E7..0x06E8,
	0x06EA..0x06ED,
	0x0711..0x0711, // Syriac combining mark
	0x0730..0x074A, // Syriac combining marks
	0x07A6..0x07B0, // Thaana combining marks
	0x0901..0x0903, // Devanagari combining marks
	0x093C..0x093C,
	0x093E..0x094D,
	0x0951..0x0954,
	0x0E31..0x0E3A, // Thai combining marks
	0x0E47..0x0E4E,
	0x0F18..0x0F19, // Tibetan combining marks
	0x0F35..0x0F35,
	0x0F37..0x0F37,
	0x0F39..0x0F39,
	0x0F71..0x0F84,
	0x0F86..0x0F87,
	0x0F8D..0x0FBC,
	0x0FC6..0x0FC6,
	0x17B4..0x17B5, // Khmer combining marks
	0x17B7..0x17BD,
	0x17C6..0x17C6,
	0x17C9..0x17D3,
	0x17DD..0x17DD,
	0x180B..0x180D, // Mongolian free variation selectors
	0x200B..0x200F, // Zero-width space, ZWNJ, ZWJ, LRM, RLM
	0x2028..0x202E, // Line/paragraph separator, directional overrides
	0x2060..0x2069, // Word joiner, etc.
	0xFE00..0xFE0F, // Variation Selectors
	0xFE20..0xFE2F, // Combining Half Marks
	0xE0100..0xE01EF, // Variation Selectors Supplement
)

private val wideRanges = listOf(
	0x1100..0x115F, // Hangul Jamo
	0x2E80..0x303E, // CJK Radicals, Kangxi, Symbols
	0x3040..0x309F, // Hiragana
	0x30A0..0x30FF, // Katakana
	0x3100..0x312F, // Bopomofo
	0x3130..0x318F, // Hangul Compatibility Jamo
	0x3190..0x33FF, // CJK Strokes, Enclosed CJK
	0x3400..0x4DBF, // CJK Unified Ext A
	0x4E00..0x9FFF, // CJK Unified Ideographs
	0xA000..0xA4CF, // Yi
	0xAC00..0xD7AF, // Hangul Syllables
	0xF900..0xFAFF, // CJK Compatibility Ideographs
	0xFE10..0xFE1F, // Vertical Forms
	0xFE30..0xFE6F, // CJK Compatibility Forms
	0xFF01..0xFF60, // Fullwidth Forms
	0xFFE0..0xFFE6, // Fullwidth Signs
	0x1B000..0x1B12F, // Kana Supplement/Extended-A
	0x1F1E6..0x1F1FF, // Regional Indicators (flags)
	0x1F200..0x1F2FF, // Enclosed Ideographic Supplement
	0x20000..0x3FFFF, // CJK Ext B–I

	// Emoji ranges (consistently wide in modern terminals)
	0x1F300..0x1F5FF, // Misc Symbols and Pictographs
	0x1F600..0x1F64F, // Emoticons
	0x1F680..0x1F6FF, // Transport and Map Symbols
	0x1F900..0x1F9FF, // Supplemental Symbols and Pictographs
	0x1FA00..0x1FAFF, // Chess + Symbols Extended-A
)

private val zeroWidthSorted = zeroWidthRanges.sortedBy { it.first }
private val wideSorted = wideRanges.sortedBy { it.first }

private operator fun List<IntRange>.contains(codepoint: Int): Boolean {
	var low = 0
	var high = size - 1
	while (low <= high) {
		val mid = (low + high) ushr 1
		val range = get(mid)
		if (codepoint < range.first) {
			high = mid - 1
		} else if (codepoint > range.last) {
			low = mid + 1
		} else {
			return true
		}
	}
	return false
}
