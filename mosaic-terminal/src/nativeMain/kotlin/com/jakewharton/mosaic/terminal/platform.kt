package com.jakewharton.mosaic.terminal

import kotlinx.cinterop.toKString
import platform.posix.getenv

internal actual fun env(name: String): String? {
	return getenv(name)?.toKString()
}
