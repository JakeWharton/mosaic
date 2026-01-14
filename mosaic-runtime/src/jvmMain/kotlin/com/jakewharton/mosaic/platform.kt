package com.jakewharton.mosaic

import kotlin.system.exitProcess

internal actual fun env(name: String): String? {
	return System.getenv(name)
}

@Suppress("NOTHING_TO_INLINE")
internal actual inline fun nanoTime(): Long = System.nanoTime()

internal actual fun nonInteractiveExit(): Nothing {
	System.err.apply {
		println(NonInteractiveMessage)
		flush()
	}
	exitProcess(1)
}
