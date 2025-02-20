package com.jakewharton.mosaic

import com.jakewharton.mosaic.ui.Color
import kotlin.math.roundToInt

internal const val ESC = "\u001B"
internal const val ST = "${ESC}\\"

internal const val APC = "${ESC}_"
internal const val CSI = "$ESC["
internal const val DCS = "${ESC}P"
internal const val OSC = "$ESC]"

internal const val cursorMode = 25
internal const val cursorEnable = "$CSI?${cursorMode}h"
internal const val cursorDisable = "$CSI?${cursorMode}l"

internal const val focusMode = 1004
internal const val focusEnable = "$CSI?${focusMode}h"
internal const val focusDisable = "$CSI?${focusMode}l"

internal const val synchronizedRenderingMode = 2026
internal const val synchronizedRenderingEnable = "$CSI?${synchronizedRenderingMode}h"
internal const val synchronizedRenderingDisable = "$CSI?${synchronizedRenderingMode}l"

internal const val systemThemeMode = 2031
internal const val systemThemeEnable = "$CSI?${systemThemeMode}h"
internal const val systemThemeDisable = "$CSI?${systemThemeMode}l"

internal const val inBandResizeMode = 2048
internal const val inBandResizeEnable = "$CSI?${inBandResizeMode}h"
internal const val inBandResizeDisable = "$CSI?${inBandResizeMode}l"

internal const val ansiReset = "${CSI}0"
internal const val clearLine = "${CSI}K"
internal const val clearDisplay = "${CSI}J"

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
