package com.jakewharton.mosaic.terminal

import com.jakewharton.mosaic.terminal.RawTerminal.Companion.toAllocation
import com.jakewharton.mosaic.terminal.event.Event
import kotlinx.cinterop.StableRef
import kotlinx.cinterop.free
import kotlinx.cinterop.nativeHeap
import kotlinx.cinterop.useContents
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.Channel.Factory.UNLIMITED

public actual object Tty {
	public actual fun terminalReader(emitDebugEvents: Boolean): TerminalReader {
		val events = Channel<Event>(UNLIMITED)

		val callback = ChannelSendingEventCallback(events, emitDebugEvents)
		val callbackRef = StableRef.create(callback)
		val callbackPtr = callbackRef.toAllocation(nativeHeap)

		val ptr = MosaicTerminalInit(callbackPtr).useContents {
			terminal?.let { return@useContents it }

			nativeHeap.free(callbackPtr)
			callbackRef.dispose()

			check(error == 0U) { "Unable to create stdin reader: $error" }
			throw OutOfMemoryError()
		}

		val rawTerminal = RawTerminal(ptr, callbackPtr, callbackRef)
		return TerminalReader(rawTerminal, events, emitDebugEvents)
	}

	internal fun throwError(error: UInt): Nothing {
		throw RuntimeException(error.toString())
	}
}
