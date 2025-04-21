package com.jakewharton.mosaic.tty.terminal

actual fun isWindows(): Boolean {
	return System.getProperty("os.name").contains("windows", ignoreCase = true)
}
