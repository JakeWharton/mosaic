package com.jakewharton.mosaic.terminal

internal actual fun env(name: String): String? {
	return System.getenv(name)
}
