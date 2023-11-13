package com.jakewharton.mosaic

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.compositionLocalOf
import dev.drewhamilton.poko.Poko

public val LocalTerminal: ProvidableCompositionLocal<Terminal> = compositionLocalOf {
	error("No terminal info provided")
}

@[Immutable Poko]
public class Terminal(
	public val size: Size,
) {
	@[Immutable Poko]
	public class Size(
		public val width: Int,
		public val height: Int,
	)
}
