package com.jakewharton.mosaic

import kotlin.system.exitProcess
import kotlinx.cinterop.UnsafeNumber
import kotlinx.cinterop.convert
import kotlinx.cinterop.cstr
import kotlinx.cinterop.toKString
import platform.posix.STDERR_FILENO
import platform.posix.getenv
import platform.posix.write

internal actual fun env(name: String): String? {
	return getenv(name)?.toKString()
}

@OptIn(UnsafeNumber::class)
internal actual fun nonInteractiveExit(): Nothing {
	val message = "$NonInteractiveMessage\n".cstr
	write(STDERR_FILENO, message, message.size.convert())
	exitProcess(1)
}
