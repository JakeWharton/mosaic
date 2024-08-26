package com.jakewharton.mosaic

import kotlin.concurrent.AtomicInt
import kotlin.concurrent.AtomicReference
import kotlin.coroutines.coroutineContext
import kotlin.system.exitProcess
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.job
import kotlinx.coroutines.withContext
import platform.posix.sleep

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

internal val signalHandlerRef = AtomicReference<((Int) -> Unit)?>(null)

internal expect fun installAllSignalHandlers()
internal expect fun restoreSignalHandlerAndTerminate(signal: Int): Boolean
internal expect fun clearAllSignalHandlers()

internal actual suspend fun <R> withShutdownHook(
	hook: () -> Unit,
	body: suspend CoroutineScope.() -> R,
): R {
	val job = Job(coroutineContext.job)
	val signalRef = AtomicInt(0)
	val signalHandler: (Int) -> Unit = { signal ->
		// It's possible multiple signals are received before we can clear this callback.
		// The code below only does a single read of this value, so the last received will win.
		signalRef.value = signal
		// Force the code below to jump directly into the 'finally' block.
		job.cancel()
	}
	check(signalHandlerRef.compareAndSet(null, signalHandler)) {
		"Cannot nest multiple shutdown hooks"
	}

	installAllSignalHandlers()

	return try {
		withContext(job, body).also {
			// Required so that the caller's scope can exit normally.
			job.complete()
		}
	} finally {
		hook()

		val signalValue = signalRef.value
		if (signalValue != 0) {
			if (restoreSignalHandlerAndTerminate(signalValue)) {
				// Since signal handling is asynchronous, sleep in the hopes we are
				// killed before it returns.
				sleep(1U)
			}

			// If we fail to restore the default signal handler, fail to kill ourselves,
			// or fail to be terminated in a reasonable amount of time, fallback to an exit.
			exitProcess(1)
		}

		clearAllSignalHandlers()
		signalHandlerRef.value = null
	}
}
