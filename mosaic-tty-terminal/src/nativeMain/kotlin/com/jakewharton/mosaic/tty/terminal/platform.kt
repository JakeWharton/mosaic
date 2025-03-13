package com.jakewharton.mosaic.tty.terminal

import kotlinx.cinterop.toKString
import platform.posix.getenv

internal actual fun env(name: String): String? {
	return getenv(name)?.toKString()
}
