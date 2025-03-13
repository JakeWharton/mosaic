package com.jakewharton.mosaic

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.compositionLocalOf
import com.jakewharton.mosaic.ui.unit.IntSize
import dev.drewhamilton.poko.Poko

public val LocalTerminalState: ProvidableCompositionLocal<TerminalState> = compositionLocalOf {
	error("No terminal info provided")
}

@[Immutable Poko]
public class TerminalState(
	public val focused: Boolean,
	public val darkTheme: Boolean,
	public val size: IntSize,
)

@Suppress("NOTHING_TO_INLINE")
internal inline fun TerminalState.copy(
	focused: Boolean = this.focused,
	darkTheme: Boolean = this.darkTheme,
	size: IntSize = this.size,
) = TerminalState(focused, darkTheme, size)
