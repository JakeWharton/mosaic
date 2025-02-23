package com.jakewharton.mosaic.terminal

import com.jakewharton.mosaic.terminal.event.Event
import kotlinx.cinterop.StableRef
import kotlinx.cinterop.free
import kotlinx.cinterop.nativeHeap
import kotlinx.cinterop.ptr
import kotlinx.cinterop.useContents
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.Channel.Factory.UNLIMITED

public actual object Tty {
	public actual fun terminalReader(emitDebugEvents: Boolean): TerminalReader {
		val events = Channel<Event>(UNLIMITED)

		val handler = PlatformEventHandler(events, emitDebugEvents)
		val handlerRef = StableRef.create(handler)
		val handlerPtr = handlerRef.toNativeAllocationIn(nativeHeap).ptr

		val inputPtr = platformInput_init(handlerPtr).useContents {
			input?.let { return@useContents it }

			nativeHeap.free(handlerPtr)
			handlerRef.dispose()

			check(error == 0U) { "Unable to create stdin reader: $error" }
			throw OutOfMemoryError()
		}

		val input = PlatformInput(inputPtr, handlerPtr, handlerRef)
		return TerminalReader(input, events, emitDebugEvents)
	}

	internal fun throwError(error: UInt): Nothing {
		throw RuntimeException(error.toString())
	}
}
