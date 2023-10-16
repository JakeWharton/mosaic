package com.jakewharton.mosaic

import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.compositionLocalOf

public val Terminal: ProvidableCompositionLocal<TerminalInfo> = compositionLocalOf {
	error("No terminal info")
}

public data class TerminalInfo(
	val size: Size,
) {
	public data class Size(
		val width: Int,
		val height: Int,
	)
}
