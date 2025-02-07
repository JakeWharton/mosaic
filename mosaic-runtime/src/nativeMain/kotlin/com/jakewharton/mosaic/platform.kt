package com.jakewharton.mosaic

import kotlin.concurrent.AtomicInt
import kotlinx.cinterop.toKString
import platform.posix.getenv

internal actual fun env(name: String): String? {
	return getenv(name)?.toKString()
}

internal actual fun platformDisplay(chars: CharSequence) {
	print(chars.toString())
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
