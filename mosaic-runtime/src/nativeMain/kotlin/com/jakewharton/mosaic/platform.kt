package com.jakewharton.mosaic

import kotlin.concurrent.AtomicInt
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

internal actual typealias AtomicBoolean = AtomicInt

@Suppress("NOTHING_TO_INLINE")
internal actual inline fun AtomicBoolean.set(value: Boolean) {
	this.value = value.toInt()
}

@Suppress("NOTHING_TO_INLINE")
internal actual inline fun AtomicBoolean.compareAndSet(expect: Boolean, update: Boolean): Boolean {
	return compareAndSet(expect.toInt(), update.toInt())
}

@Suppress("NOTHING_TO_INLINE")
internal actual inline fun atomicBooleanOf(initialValue: Boolean): AtomicBoolean {
	return AtomicInt(initialValue.toInt())
}

@Suppress("NOTHING_TO_INLINE")
private inline fun Boolean.toInt() = if (this) 1 else 0

@OptIn(UnsafeNumber::class)
internal actual fun nonInteractiveExit(): Nothing {
	val message = "$NonInteractiveMessage\n".cstr
	write(STDERR_FILENO, message, message.size.convert())
	exitProcess(1)
}
