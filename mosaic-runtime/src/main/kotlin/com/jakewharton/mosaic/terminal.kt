package com.jakewharton.mosaic

import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.compositionLocalOf

val Terminal: ProvidableCompositionLocal<TerminalInfo> = compositionLocalOf {
	error("No terminal info")
}

data class TerminalInfo(
	val size: Size,
) {
	data class Size(
		val width: Int,
		val height: Int,
	)
}
