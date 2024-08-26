package com.jakewharton.mosaic

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.staticCFunction
import platform.windows.DWORD
import platform.windows.FALSE
import platform.windows.SetConsoleCtrlHandler
import platform.windows.TRUE
import platform.windows.WINBOOL

@OptIn(ExperimentalForeignApi::class)
internal actual fun installAllSignalHandlers() {
	val signalHandlerFunction = staticCFunction(::signalHandler)
	SetConsoleCtrlHandler(signalHandlerFunction, TRUE) != 0
}

@OptIn(ExperimentalForeignApi::class)
internal actual fun restoreSignalHandlerAndTerminate(signal: Int): Boolean {
	return SetConsoleCtrlHandler(null, FALSE) != 0
}

@OptIn(ExperimentalForeignApi::class)
internal actual fun clearAllSignalHandlers() {
	SetConsoleCtrlHandler(null, FALSE)
}

private fun signalHandler(value: DWORD): WINBOOL {
	val signalHandler = signalHandlerRef.value
	if (signalHandler != null) {
		signalHandler.invoke(value.toInt())
		// Prevent the default handler from being called.
		return TRUE
	}
	// It is not possible for the signal handler lambda to be null. Juuuuuust in case,
	// allow the default behavior run which should terminate the process.
	return FALSE
}
