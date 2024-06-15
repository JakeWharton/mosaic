package com.jakewharton.mosaic

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

// region ansi level detection
// https://github.com/ajalt/mordant/blob/adad637a133a5221823fba9068b2f7ad8965d115/mordant/src/commonMain/kotlin/com/github/ajalt/mordant/terminal/TerminalDetection.kt
internal fun getAnsiLevel(): AnsiLevel {
	return ansiLevel(isIntellijRunActionConsole() || stdoutInteractive())
}

private fun ansiLevel(interactive: Boolean): AnsiLevel {
	forcedColor()?.let { return it }

	// Terminals embedded in some IDEs support color even though stdout isn't interactive. Check
	// those terminals before checking stdout.
	if (isIntellijRunActionConsole() || isVsCodeTerminal()) return AnsiLevel.TRUECOLOR

	// If output isn't interactive, never output colors, since we might be redirected to a file etc.
	if (!interactive) return AnsiLevel.NONE

	// Otherwise check the large variety of environment variables set by various terminal
	// emulators to detect color support

	if (isWindowsTerminal() || isDomTerm()) return AnsiLevel.TRUECOLOR

	if (isJediTerm()) return AnsiLevel.TRUECOLOR

	when (getColorTerm()) {
		"24bit", "24bits", "truecolor" -> return AnsiLevel.TRUECOLOR
	}

	if (isCI()) {
		return if (ciSupportsColor()) AnsiLevel.ANSI256 else AnsiLevel.NONE
	}

	when (getTermProgram()) {
		"hyper" -> return AnsiLevel.TRUECOLOR
		"apple_terminal" -> return AnsiLevel.ANSI256
		"iterm.app" -> return if (iTermVersionSupportsTruecolor()) AnsiLevel.TRUECOLOR else AnsiLevel.ANSI256
		"wezterm" -> return AnsiLevel.TRUECOLOR
		"mintty" -> return AnsiLevel.TRUECOLOR
	}

	val (term, level) = getTerm()?.split("-")
		?.let { it.firstOrNull() to it.lastOrNull() }
		?: (null to null)

	when (level) {
		"256", "256color", "256colors" -> return AnsiLevel.ANSI256
		"24bit", "24bits", "direct", "truecolor" -> return AnsiLevel.TRUECOLOR
	}

	// If there's no explicit level (like "xterm") or the level is ansi16 (like "rxvt-16color"),
	// guess the level based on the terminal.

	if (term == "xterm") {
		// Xterm sets an envvar with a version string that's "normally an identifier for the X
		// Window libraries used to build xterm, followed by xterm's patch number in
		// parentheses" https://linux.die.net/man/1/xterm
		//
		// 331 added truecolor support, 122 added 256 color support
		// https://invisible-island.net/xterm/xterm.log.html
		val xtermVersion = getEnv("XTERM_VERSION") ?: return AnsiLevel.ANSI16
		val m = Regex("""\((\d+)\)""").find(xtermVersion) ?: return AnsiLevel.ANSI16
		val v = m.groupValues[1].toInt()
		if (v >= 331) return AnsiLevel.TRUECOLOR
		if (v >= 122) return AnsiLevel.ANSI256
		return AnsiLevel.ANSI16
	}

	return when (term) {
		// New versions of Windows 10 cmd.exe supports truecolor, and most other terminal emulators
		// like ConEmu and mintty support truecolor, although they might downsample it.
		"cygwin" -> AnsiLevel.TRUECOLOR
		"vt100", "vt220", "screen", "tmux", "color", "linux", "ansi", "rxvt", "konsole" -> AnsiLevel.ANSI16
		else -> AnsiLevel.NONE
	}
}

private fun getTerm(): String? = getEnv("TERM")?.lowercase()

// https://github.com/termstandard/colors/
private fun getColorTerm(): String? = getEnv("COLORTERM")?.lowercase()

private fun forcedColor(): AnsiLevel? = when {
	getTerm() == "dumb" -> AnsiLevel.NONE
	// https://no-color.org/
	getEnv("NO_COLOR") != null -> AnsiLevel.NONE
	// A lot of npm packages support the FORCE_COLOR envvar, although they all look for
	// different values. We try to support them all.
	else -> when (getEnv("FORCE_COLOR")?.lowercase()) {
		"0", "false", "none" -> AnsiLevel.NONE
		"1", "", "true", "16color" -> AnsiLevel.ANSI16
		"2", "256color" -> AnsiLevel.ANSI256
		"3", "truecolor" -> AnsiLevel.TRUECOLOR
		else -> null
	}
}

private fun getTermProgram(): String? = getEnv("TERM_PROGRAM")?.lowercase()

// https://github.com/Microsoft/vscode/pull/30346
private fun isVsCodeTerminal(): Boolean = getTermProgram() == "vscode"

// https://github.com/microsoft/terminal/issues/1040#issuecomment-496691842
private fun isWindowsTerminal(): Boolean = !getEnv("WT_SESSION").isNullOrEmpty()

// https://domterm.org/Detecting-domterm-terminal.html
private fun isDomTerm(): Boolean = !getEnv("DOMTERM").isNullOrEmpty()

// https://github.com/JetBrains/intellij-community/blob/master/plugins/terminal/src/org/jetbrains/plugins/terminal/LocalTerminalDirectRunner.java#L141
private fun isJediTerm(): Boolean = getEnv("TERMINAL_EMULATOR") == "JetBrains-JediTerm"

private fun iTermVersionSupportsTruecolor(): Boolean {
	val ver = getEnv("TERM_PROGRAM_VERSION")?.split(".")?.firstOrNull()?.toIntOrNull()
	return ver != null && ver >= 3
}

private fun isCI(): Boolean {
	return getEnv("CI") != null
}

private fun ciSupportsColor(): Boolean {
	return listOf(
		"APPVEYOR",
		"BUILDKITE",
		"CIRCLECI",
		"DRONE",
		"GITHUB_ACTIONS",
		"GITLAB_CI",
		"TRAVIS",
	).any { getEnv(it) != null }
}

private fun isIntellijRunActionConsole(): Boolean {
	// For some reason, IntelliJ's terminal behaves differently when running from an IDE run action vs running from
	// their terminal tab. In the latter case, the JediTerm envvar is set, in the former it's missing.
	return !isJediTerm() && runningInIdeaJavaAgent()
}

// endregion
