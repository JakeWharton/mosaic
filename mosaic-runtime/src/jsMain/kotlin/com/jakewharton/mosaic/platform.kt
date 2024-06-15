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

internal actual fun getEnv(key: String): String? = process.env[key] as? String

internal actual fun runningInIdeaJavaAgent(): Boolean = false

internal actual fun stdoutInteractive(): Boolean = js("Boolean(process.stdout.isTTY)") as Boolean
