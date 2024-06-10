package com.jakewharton.mosaic

import com.jakewharton.mosaic.ui.unit.IntSize

private external val process: dynamic

internal actual fun getPlatformTerminalSize(): IntSize {
	val size = process.stdout.getWindowSize()
	return if (size == undefined) {
		IntSize.Zero
	} else {
		IntSize(width = size[0] as Int, height = size[1] as Int)
	}
}
