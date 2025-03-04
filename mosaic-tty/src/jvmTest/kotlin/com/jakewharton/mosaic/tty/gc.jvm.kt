package com.jakewharton.mosaic.tty

internal actual fun gc() {
	System.gc()
	Thread.sleep(100)
	@Suppress("DEPRECATION")
	System.runFinalization()
	System.gc()
}
