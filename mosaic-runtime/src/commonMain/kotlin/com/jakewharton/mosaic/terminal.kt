package com.jakewharton.mosaic

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.compositionLocalOf
import com.jakewharton.mosaic.terminal.Terminal
import dev.drewhamilton.poko.Poko

public val LocalTerminalState: ProvidableCompositionLocal<TerminalState> = compositionLocalOf {
	error("No terminal info provided")
}

@[Immutable Poko]
public class TerminalState(
	public val focused: Boolean,
	public val theme: Terminal.Theme,
	public val size: Terminal.Size,
)

@Suppress("NOTHING_TO_INLINE")
internal inline fun TerminalState.copy(
	focused: Boolean = this.focused,
	theme: Terminal.Theme = this.theme,
	size: Terminal.Size = this.size,
) = TerminalState(focused, theme, size)
