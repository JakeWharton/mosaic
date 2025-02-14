package com.jakewharton.mosaic

internal expect fun env(name: String): String?

internal expect class AtomicBoolean

internal expect inline fun AtomicBoolean.set(value: Boolean)

internal expect inline fun AtomicBoolean.compareAndSet(expect: Boolean, update: Boolean): Boolean

internal expect inline fun atomicBooleanOf(initialValue: Boolean): AtomicBoolean

internal expect inline fun nanoTime(): Long
