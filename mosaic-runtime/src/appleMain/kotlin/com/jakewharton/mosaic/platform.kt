package com.jakewharton.mosaic

import com.jakewharton.mosaic.ui.unit.IntSize
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.toKStringFromUtf8
import platform.posix.STDIN_FILENO
import platform.posix.STDOUT_FILENO
import platform.posix.TIOCGWINSZ
import platform.posix.getenv
import platform.posix.ioctl
import platform.posix.isatty
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

@OptIn(ExperimentalForeignApi::class)
internal actual fun getEnv(key: String): String? = getenv(key)?.toKStringFromUtf8()

internal actual fun runningInIdeaJavaAgent(): Boolean = false

internal actual fun stdoutInteractive(): Boolean = isatty(STDOUT_FILENO) != 0
