package com.jakewharton.mosaic.terminal

import com.jakewharton.mosaic.terminal.Jni.platformEventHandlerFree
import com.jakewharton.mosaic.terminal.Jni.platformEventHandlerInit
import com.jakewharton.mosaic.terminal.Jni.platformInputInit
import com.jakewharton.mosaic.terminal.event.Event
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.Channel.Factory.UNLIMITED

public actual fun TerminalReader(emitDebugEvents: Boolean): TerminalReader {
	val events = Channel<Event>(UNLIMITED)
	val handlerPtr = platformEventHandlerInit(PlatformEventHandler(events, emitDebugEvents))
	if (handlerPtr != 0L) {
		val inputPtr = platformInputInit(handlerPtr)
		if (inputPtr != 0L) {
			val platformInput = PlatformInput(inputPtr, handlerPtr)
			return TerminalReader(platformInput, events, emitDebugEvents)
		}
		platformEventHandlerFree(handlerPtr)
	}
	throw OutOfMemoryError()
}
