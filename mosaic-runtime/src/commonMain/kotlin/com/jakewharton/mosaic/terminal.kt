package com.jakewharton.mosaic

import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.Stable
import androidx.compose.runtime.compositionLocalOf
import dev.drewhamilton.poko.Poko

public val Terminal: ProvidableCompositionLocal<TerminalInfo> = compositionLocalOf {
	error("No terminal info")
}

@[Stable Poko]
public class TerminalInfo(
	public val size: Size,
) {
	@[Stable Poko]
	public class Size(
		public val width: Int,
		public val height: Int,
	)
}
