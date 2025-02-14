package com.jakewharton.mosaic

internal actual fun env(name: String): String? {
	return System.getenv(name)
}

internal actual typealias AtomicBoolean = java.util.concurrent.atomic.AtomicBoolean

@Suppress("NOTHING_TO_INLINE", "EXTENSION_SHADOWED_BY_MEMBER")
internal actual inline fun AtomicBoolean.set(value: Boolean) {
	set(value)
}

@Suppress("NOTHING_TO_INLINE", "EXTENSION_SHADOWED_BY_MEMBER")
internal actual inline fun AtomicBoolean.compareAndSet(expect: Boolean, update: Boolean): Boolean {
	return compareAndSet(expect, update)
}

@Suppress("NOTHING_TO_INLINE")
internal actual inline fun atomicBooleanOf(initialValue: Boolean): AtomicBoolean {
	return AtomicBoolean(initialValue)
}

@Suppress("NOTHING_TO_INLINE")
internal actual inline fun nanoTime(): Long = System.nanoTime()
