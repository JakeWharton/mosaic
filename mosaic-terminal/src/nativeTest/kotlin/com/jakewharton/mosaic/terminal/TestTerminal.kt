package com.jakewharton.mosaic.terminal

import com.jakewharton.mosaic.terminal.RawTerminal.Companion.toAllocation
import com.jakewharton.mosaic.terminal.event.Event
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.StableRef
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.free
import kotlinx.cinterop.nativeHeap
import kotlinx.cinterop.useContents
import kotlinx.cinterop.usePinned
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.Channel.Factory.UNLIMITED

internal actual class TestTerminal private constructor(
	private var ptr: CPointer<MosaicTestTerminal>?,
	actual val reader: TerminalReader,
) : AutoCloseable {

	actual fun write(buffer: ByteArray) {
		buffer.usePinned {
			val error = MosaicTestTerminalWrite(ptr, it.addressOf(0), buffer.size)
			if (error != 0U) {
				Tty.throwError(error)
			}
		}
	}

	actual override fun close() {
		ptr?.let { ptr ->
			this.ptr = null

			val error = MosaicTestTerminalFree(ptr)
			if (error != 0U) {
				Tty.throwError(error)
			}
		}
	}

	actual companion object {
		actual fun create(): TestTerminal {
			val events = Channel<Event>(UNLIMITED)
			val callback = PlatformEventHandler(events, false)
			val callbackRef = StableRef.create(callback)
			val callbackPtr = callbackRef.toAllocation(nativeHeap)

			val error = MosaicTestTerminalInit(callbackPtr).useContents {
				testTerminal?.let { ptr ->
					val terminalPtr = MosaicTestTerminalGetTerminal(ptr)
					val terminal = RawTerminal(terminalPtr, callbackPtr, callbackRef)
					val reader = TerminalReader(terminal, events, false)
					return TestTerminal(ptr, reader)
				}
				error
			}

			nativeHeap.free(callbackPtr)
			callbackRef.dispose()

			if (error != 0U) {
				Tty.throwError(error)
			}
			throw OutOfMemoryError()
		}
	}
}
