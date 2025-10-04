package com.jakewharton.mosaic.tty

import kotlin.experimental.ExperimentalNativeApi
import platform.posix.fflush
import platform.posix.fputs
import platform.posix.stderr

@OptIn(ExperimentalNativeApi::class)
internal actual fun isWindows() = Platform.osFamily == OsFamily.WINDOWS

internal actual fun eprintln(message: String) {
	fputs(message, stderr)
	fputs("\n", stderr)
	fflush(stderr)
}
