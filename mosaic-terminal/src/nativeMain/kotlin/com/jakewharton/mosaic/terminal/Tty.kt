package com.jakewharton.mosaic.terminal

import com.jakewharton.mosaic.terminal.event.Event
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.StableRef
import kotlinx.cinterop.free
import kotlinx.cinterop.nativeHeap
import kotlinx.cinterop.ptr
import kotlinx.cinterop.useContents
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.Channel.Factory.UNLIMITED

public actual object Tty {
	public actual fun enableRawMode(): AutoCloseable {
		val savedConfig = enterRawMode().useContents {
			check(error == 0U) { "Unable to enable raw mode: $error" }
			saved ?: throw OutOfMemoryError()
		}
		return RawMode(savedConfig)
	}

	private class RawMode(
		private val savedConfig: CPointer<rawModeConfig>,
	) : AutoCloseable {
		override fun close() {
			val error = exitRawMode(savedConfig)
			if (error == 0U) return
			throwError(error)
		}
	}

	public actual fun terminalReader(emitDebugEvents: Boolean): TerminalReader {
		val events = Channel<Event>(UNLIMITED)

		val handler = PlatformEventHandler(events, emitDebugEvents)
		val handlerRef = StableRef.create(handler)
		val handlerPtr = handlerRef.toNativeAllocationIn(nativeHeap).ptr

		val readerPtr = platformInput_init(handlerPtr).useContents {
			reader?.let { return@useContents it }

			nativeHeap.free(handlerPtr)
			handlerRef.dispose()

			check(error == 0U) { "Unable to create stdin reader: $error" }
			throw OutOfMemoryError()
		}

		val reader = PlatformInput(readerPtr, handlerPtr, handlerRef)
		return TerminalReader(reader, events, emitDebugEvents)
	}

	internal fun throwError(error: UInt): Nothing {
		throw RuntimeException(error.toString())
	}
}
