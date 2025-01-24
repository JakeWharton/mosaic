package com.jakewharton.mosaic.terminal

import com.jakewharton.mosaic.terminal.event.Event
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.StableRef
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.free
import kotlinx.cinterop.nativeHeap
import kotlinx.cinterop.ptr
import kotlinx.cinterop.useContents
import kotlinx.cinterop.usePinned
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.Channel.Factory.UNLIMITED

internal actual fun PlatformInputWriter(): PlatformInputWriter {
	val events = Channel<Event>(UNLIMITED)

	val handler = PlatformEventHandler(events)
	val handlerRef = StableRef.create(handler)
	val handlerPtr = handlerRef.toNativeAllocationIn(nativeHeap).ptr

	val writerPtr = stdinWriter_init(handlerPtr).useContents {
		writer?.let { return@useContents it }

		nativeHeap.free(handlerPtr)
		handlerRef.dispose()

		check(error == 0U) { "Unable to create stdin writer: $error" }
		throw OutOfMemoryError()
	}

	val readerPtr = stdinWriter_getReader(writerPtr)!!
	val platformInput = PlatformInput(readerPtr, handlerPtr, handlerRef)
	return PlatformInputWriter(writerPtr, events, platformInput)
}

internal actual class PlatformInputWriter(
	private var ptr: CPointer<stdinWriter>?,
	private val events: Channel<Event>,
	actual val input: PlatformInput,
) : AutoCloseable {
	actual fun terminalReader(emitDebugEvents: Boolean): TerminalReader {
		return TerminalReader(input, events, emitDebugEvents)
	}

	actual fun write(buffer: ByteArray) {
		val error = buffer.usePinned {
			stdinWriter_write(ptr, it.addressOf(0), buffer.size)
		}
		if (error == 0U) return
		Tty.throwError(error)
	}

	actual fun focusEvent(focused: Boolean) {
		stdinWriter_focusEvent(ptr, focused)
	}

	actual fun keyEvent() {
		stdinWriter_keyEvent(ptr)
	}

	actual fun mouseEvent() {
		stdinWriter_mouseEvent(ptr)
	}

	actual fun resizeEvent(columns: Int, rows: Int, width: Int, height: Int) {
		stdinWriter_resizeEvent(ptr, columns, rows, width, height)
	}

	actual override fun close() {
		ptr?.let { ref ->
			this.ptr = null

			input.close()

			val error = stdinWriter_free(ref)

			if (error == 0U) return
			Tty.throwError(error)
		}
	}
}
