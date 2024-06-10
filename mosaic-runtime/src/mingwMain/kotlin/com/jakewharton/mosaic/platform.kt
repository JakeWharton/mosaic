package com.jakewharton.mosaic

import com.jakewharton.mosaic.ui.unit.IntSize
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import platform.windows.CONSOLE_SCREEN_BUFFER_INFO
import platform.windows.GetConsoleScreenBufferInfo
import platform.windows.GetStdHandle
import platform.windows.INVALID_HANDLE_VALUE
import platform.windows.STD_OUTPUT_HANDLE

@OptIn(ExperimentalForeignApi::class)
internal actual fun getPlatformTerminalSize(): IntSize = memScoped {
	val csbi = alloc<CONSOLE_SCREEN_BUFFER_INFO>()
	val stdoutHandle = GetStdHandle(STD_OUTPUT_HANDLE)
	if (stdoutHandle == INVALID_HANDLE_VALUE) {
		return@memScoped IntSize.Zero
	}
	if (GetConsoleScreenBufferInfo(stdoutHandle, csbi.ptr) == 0) {
		return@memScoped IntSize.Zero
	}
	csbi.srWindow.run { IntSize(width = Right - Left + 1, height = Bottom - Top + 1) }
}
