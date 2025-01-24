package com.jakewharton.mosaic.terminal

import com.jakewharton.mosaic.terminal.Jni.enterRawMode
import com.jakewharton.mosaic.terminal.Jni.exitRawMode
import com.jakewharton.mosaic.terminal.Jni.platformEventHandlerFree
import com.jakewharton.mosaic.terminal.Jni.platformEventHandlerInit
import com.jakewharton.mosaic.terminal.Jni.stdinReaderInit
import com.jakewharton.mosaic.terminal.event.Event
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.Channel.Factory.UNLIMITED

public actual object Tty {
	@JvmStatic
	public actual fun enableRawMode(): AutoCloseable {
		val savedConfig = enterRawMode()
		if (savedConfig == 0L) throw OutOfMemoryError()
		return RawMode(savedConfig)
	}

	private class RawMode(
		private val savedPtr: Long,
	) : AutoCloseable {
		override fun close() {
			exitRawMode(savedPtr)
		}
	}

	@JvmStatic
	public actual fun terminalReader(emitDebugEvents: Boolean): TerminalReader {
		val events = Channel<Event>(UNLIMITED)
		val handlerPtr = platformEventHandlerInit(PlatformEventHandler(events))
		if (handlerPtr != 0L) {
			val readerPtr = stdinReaderInit(handlerPtr)
			if (readerPtr != 0L) {
				val platformInput = PlatformInput(readerPtr, handlerPtr)
				return TerminalReader(platformInput, events, emitDebugEvents)
			}
			platformEventHandlerFree(handlerPtr)
		}
		throw OutOfMemoryError()
	}
}
