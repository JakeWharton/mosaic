package com.jakewharton.mosaic.tty

import kotlin.time.Duration.Companion.seconds
import kotlin.time.TimeSource

internal expect fun gc()

internal fun WeakReference<*>.assertGc() {
	val timeout = 10.seconds
	val deadline = TimeSource.Monotonic.markNow() + timeout
	do {
		if (!hasValue()) {
			return
		}
		gc()
	} while (deadline.hasNotPassedNow())

	throw AssertionError("Reference was not cleared within $timeout")
}

@Keep // Ensure reference doesn't leak to a local.
private fun WeakReference<*>.hasValue(): Boolean {
	return get() != null
}
