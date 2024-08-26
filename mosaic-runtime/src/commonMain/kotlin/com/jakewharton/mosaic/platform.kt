package com.jakewharton.mosaic

import kotlinx.coroutines.CoroutineScope

internal expect fun platformDisplay(chars: CharSequence)

internal expect class AtomicBoolean

internal expect inline fun AtomicBoolean.set(value: Boolean)

internal expect inline fun AtomicBoolean.compareAndSet(expect: Boolean, update: Boolean): Boolean

internal expect inline fun atomicBooleanOf(initialValue: Boolean): AtomicBoolean

internal expect suspend fun <R> withShutdownHook(
	hook: () -> Unit,
	body: suspend CoroutineScope.() -> R,
): R
