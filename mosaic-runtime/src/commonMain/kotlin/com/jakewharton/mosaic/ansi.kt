package com.jakewharton.mosaic

import com.github.ajalt.mordant.rendering.AnsiLevel as MordantAnsiLevel
import com.jakewharton.mosaic.ui.AnsiLevel
import com.jakewharton.mosaic.ui.Color
import kotlin.math.roundToInt

private const val ESC = "\u001B"
internal const val CSI = "$ESC["

internal const val ansiBeginSynchronizedUpdate = "$CSI?2026h"
internal const val ansiEndSynchronizedUpdate = "$CSI?2026l"

internal const val ansiReset = "${CSI}0"
internal const val clearLine = "${CSI}K"
internal const val cursorUp = "${CSI}F"

internal const val ansiSeparator = ";"
internal const val ansiClosingCharacter = "m"

internal const val ansiFgColorSelector = 38
internal const val ansiFgColorReset = 39
internal const val ansiFgColorOffset = 0

internal const val ansiBgColorSelector = 48
internal const val ansiBgColorReset = 49
internal const val ansiBgColorOffset = 10

internal const val ansiSelectorColor256 = 5
internal const val ansiSelectorColorRgb = 2

internal fun MordantAnsiLevel.toMosaicAnsiLevel(): AnsiLevel {
	return when (this) {
		MordantAnsiLevel.NONE -> AnsiLevel.NONE
		MordantAnsiLevel.ANSI16 -> AnsiLevel.ANSI16
		MordantAnsiLevel.ANSI256 -> AnsiLevel.ANSI256
		MordantAnsiLevel.TRUECOLOR -> AnsiLevel.TRUECOLOR
	}
}

// simpler version without full conversion to HSV
// https://github.com/ajalt/colormath/blob/4a0cc9796c743cb4965407204ee63b40aaf22fca/colormath/src/commonMain/kotlin/com/github/ajalt/colormath/model/RGB.kt#L301
internal fun Color.toAnsi16Code(): Int {
	val value = (maxOf(redFloat, greenFloat, blueFloat) * 100).roundToInt()
	if (value == 30) {
		return 30
	}
	val v = value / 50
	val ansiCode = 30 + (
		(blueFloat.roundToInt() * 4)
			or (greenFloat.roundToInt() * 2)
			or redFloat.roundToInt()
		)
	return if (v == 2) ansiCode + 60 else ansiCode
}

// https://github.com/ajalt/colormath/blob/4a0cc9796c743cb4965407204ee63b40aaf22fca/colormath/src/commonMain/kotlin/com/github/ajalt/colormath/model/RGB.kt#L310
internal fun Color.toAnsi256Code(): Int {
	val ri = redInt
	val gi = greenInt
	val bi = blueInt
	// grayscale
	return if (ri == gi && gi == bi) {
		when {
			ri < 8 -> 16
			ri > 248 -> 231
			else -> (((ri - 8) / 247.0) * 24.0).roundToInt() + 232
		}
	} else {
		16 + (36 * (redFloat * 5).roundToInt()) +
			(6 * (greenFloat * 5).roundToInt()) +
			(blueFloat * 5).roundToInt()
	}
}
