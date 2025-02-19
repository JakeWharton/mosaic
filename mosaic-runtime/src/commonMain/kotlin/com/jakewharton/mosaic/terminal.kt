package com.jakewharton.mosaic

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.compositionLocalOf
import com.jakewharton.mosaic.ui.AnsiLevel
import com.jakewharton.mosaic.ui.unit.IntSize
import dev.drewhamilton.poko.Poko

public val LocalTerminal: ProvidableCompositionLocal<Terminal> = compositionLocalOf {
	error("No terminal info provided")
}

@[Immutable Poko]
public class Terminal(
	public val focused: Boolean,
	public val darkTheme: Boolean,
	public val size: IntSize,
) {
	public companion object {
		public val Default: Terminal = Terminal(
			focused = true,
			darkTheme = false,
			// https://en.wikipedia.org/wiki/VT52
			size = IntSize(width = 80, height = 24),
		)
	}
}

@Suppress("NOTHING_TO_INLINE")
internal inline fun Terminal.copy(
	focused: Boolean = this.focused,
	darkTheme: Boolean = this.darkTheme,
	size: IntSize = this.size,
) = Terminal(focused, darkTheme, size)

internal fun detectAnsiLevel(): AnsiLevel {
	if (env("NO_COLOR").orEmpty().isNotEmpty()) {
		return AnsiLevel.NONE
	}
	val term = env("COLORTERM") ?: env("TERM") ?: "dumb"
	if (term.contains("24bit", ignoreCase = true) || term.contains("truecolor", ignoreCase = true)) {
		return AnsiLevel.TRUECOLOR
	}
	if (term.contains("256")) {
		return AnsiLevel.ANSI256
	}
	if (term != "dumb") {
		return AnsiLevel.ANSI16
	}
	return AnsiLevel.NONE
}
