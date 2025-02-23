package com.jakewharton.mosaic.terminal

import com.jakewharton.mosaic.terminal.Jni.terminalEventCallbackFree
import com.jakewharton.mosaic.terminal.Jni.terminalEventCallbackInit
import com.jakewharton.mosaic.terminal.Jni.testTerminalFree
import com.jakewharton.mosaic.terminal.Jni.testTerminalGetTerminal
import com.jakewharton.mosaic.terminal.Jni.testTerminalInit
import com.jakewharton.mosaic.terminal.Jni.testTerminalWrite
import com.jakewharton.mosaic.terminal.event.Event
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.Channel.Factory.UNLIMITED

internal actual class TestTerminal private constructor(
	private var testTerminalPtr: Long,
	actual val rawTerminal: RawTerminal,
	actual val reader: TerminalReader,
) : AutoCloseable {
	actual fun write(buffer: ByteArray) {
		testTerminalWrite(testTerminalPtr, buffer)
	}

	actual override fun close() {
		val testTerminalPtr = testTerminalPtr
		if (testTerminalPtr != 0L) {
			this.testTerminalPtr = 0L
			testTerminalFree(testTerminalPtr)
			reader.close()
		}
	}

	actual companion object {
		actual fun create(): TestTerminal {
			val events = Channel<Event>(UNLIMITED)
			val callback = ChannelSendingEventCallback(events, emitDebugEvents = false)
			val callbackPtr = terminalEventCallbackInit(callback)
			try {
				val ptr = testTerminalInit(callbackPtr)
				val terminalPtr = testTerminalGetTerminal(ptr)
				val rawTerminal = RawTerminal(ptr, callbackPtr)
				val reader = TerminalReader(rawTerminal, events, emitDebugEvents = false)
				return TestTerminal(terminalPtr, rawTerminal, reader)
			} catch (t: Throwable) {
				terminalEventCallbackFree(callbackPtr)
				throw t
			}
		}
	}
}
