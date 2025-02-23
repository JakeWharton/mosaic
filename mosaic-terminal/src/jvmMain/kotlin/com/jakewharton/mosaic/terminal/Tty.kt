package com.jakewharton.mosaic.terminal

import com.jakewharton.mosaic.terminal.Jni.terminalEventCallbackFree
import com.jakewharton.mosaic.terminal.Jni.terminalEventCallbackInit
import com.jakewharton.mosaic.terminal.Jni.terminalInit
import com.jakewharton.mosaic.terminal.event.Event
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.Channel.Factory.UNLIMITED

public actual object Tty {
	@JvmStatic
	public actual fun terminalReader(emitDebugEvents: Boolean): TerminalReader {
		val events = Channel<Event>(UNLIMITED)
		val callbackPtr = terminalEventCallbackInit(ChannelSendingEventCallback(events, emitDebugEvents))
		if (callbackPtr != 0L) {
			val inputPtr = terminalInit(callbackPtr)
			if (inputPtr != 0L) {
				val platformInput = RawTerminal(inputPtr, callbackPtr,)
				return TerminalReader(platformInput, events, emitDebugEvents)
			}
			terminalEventCallbackFree(callbackPtr)
		}
		throw OutOfMemoryError()
	}
}
