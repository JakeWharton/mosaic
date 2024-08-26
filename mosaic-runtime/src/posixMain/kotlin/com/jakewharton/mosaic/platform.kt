package com.jakewharton.mosaic

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.staticCFunction
import platform.posix.SIGABRT
import platform.posix.SIGALRM
import platform.posix.SIGBUS
import platform.posix.SIGFPE
import platform.posix.SIGHUP
import platform.posix.SIGINT
import platform.posix.SIGQUIT
import platform.posix.SIGTERM
import platform.posix.SIG_DFL
import platform.posix.SIG_ERR
import platform.posix.kill
import platform.posix.signal

private val allSignals = arrayOf(SIGABRT, SIGALRM, SIGBUS, SIGFPE, SIGHUP, SIGINT, SIGTERM, SIGQUIT)

@OptIn(ExperimentalForeignApi::class)
internal actual fun installAllSignalHandlers() {
	val signalHandlerFunction = staticCFunction(::signalHandler)
	for (signal in allSignals) {
		// TODO Migrate to sigaction.
		signal(signal, signalHandlerFunction)
	}
}

@OptIn(ExperimentalForeignApi::class)
internal actual fun restoreSignalHandlerAndTerminate(signal: Int): Boolean {
	return signal(signal, SIG_DFL) != SIG_ERR && kill(0, signal) == 0
}

@OptIn(ExperimentalForeignApi::class)
internal actual fun clearAllSignalHandlers() {
	for (signal in allSignals) {
		signal(signal, SIG_DFL)
	}
}

@OptIn(ExperimentalForeignApi::class)
private fun signalHandler(value: Int) {
	val signalHandler = signalHandlerRef.value
	if (signalHandler != null) {
		signalHandler.invoke(value)
	} else {
		// It is not possible for the signal handler lambda to be null. Juuuuuust in case,
		// remove ourselves as the signal handler and re-send it to produce the default behavior.
		signal(value, SIG_DFL)
		kill(0, value)
	}
}
