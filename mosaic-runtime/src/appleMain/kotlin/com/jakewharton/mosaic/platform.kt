package com.jakewharton.mosaic

import com.jakewharton.mosaic.ui.unit.IntSize
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import platform.posix.STDIN_FILENO
import platform.posix.TIOCGWINSZ
import platform.posix.ioctl
import platform.posix.winsize

@OptIn(ExperimentalForeignApi::class)
internal actual fun getPlatformTerminalSize(): IntSize = memScoped {
	val size = alloc<winsize>()
	if (ioctl(STDIN_FILENO, TIOCGWINSZ, size) < 0) {
		IntSize.Zero
	} else {
		IntSize(width = size.ws_col.toInt(), height = size.ws_row.toInt())
	}
}
