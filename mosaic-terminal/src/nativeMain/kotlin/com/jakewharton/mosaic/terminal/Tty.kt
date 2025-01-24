package com.jakewharton.mosaic.terminal

import com.jakewharton.mosaic.terminal.event.Event
import kotlinx.cinterop.COpaquePointer
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.StableRef
import kotlinx.cinterop.alloc
import kotlinx.cinterop.asStableRef
import kotlinx.cinterop.free
import kotlinx.cinterop.nativeHeap
import kotlinx.cinterop.ptr
import kotlinx.cinterop.staticCFunction
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

		val handler = PlatformEventHandler(events)
		val handlerRef = StableRef.create(handler)
		val handlerPtr = nativeHeap.alloc<platformEventHandler> {
			opaque = handlerRef.asCPointer()
			onFocus = staticCFunction(::onFocusCallback)
			onKey = staticCFunction(::onKeyCallback)
			onMouse = staticCFunction(::onMouseCallback)
			onResize = staticCFunction(::onResizeCallback)
		}.ptr

		val readerPtr = stdinReader_init(handlerPtr).useContents {
			reader?.let { return@useContents it }

			nativeHeap.free(handlerPtr)
			handlerRef.dispose()

			check(error == 0U) { "Unable to create stdin reader: $error" }
			throw OutOfMemoryError()
		}

		val reader = PlatformInput(readerPtr, handlerPtr, handlerRef)
		return TerminalReader(reader, events, emitDebugEvents)
	}

	internal actual fun platformInputWriter(): PlatformInputWriter {
		val events = Channel<Event>(UNLIMITED)

		// TODO Fix all this duplication, ownership
		val handler = PlatformEventHandler(events)
		val handlerRef = StableRef.create(handler)
		val handlerPtr = nativeHeap.alloc<platformEventHandler> {
			opaque = handlerRef.asCPointer()
			onFocus = staticCFunction(::onFocusCallback)
			onKey = staticCFunction(::onKeyCallback)
			onMouse = staticCFunction(::onMouseCallback)
			onResize = staticCFunction(::onResizeCallback)
		}.ptr

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

	internal fun throwError(error: UInt): Nothing {
		throw RuntimeException(error.toString())
	}
}

private fun onFocusCallback(opaque: COpaquePointer?, focused: Boolean) {
	val handler = opaque!!.asStableRef<PlatformEventHandler>().get()
	handler.onFocus(focused)
}

private fun onKeyCallback(opaque: COpaquePointer?) {
	val handler = opaque!!.asStableRef<PlatformEventHandler>().get()
	handler.onKey()
}

private fun onMouseCallback(opaque: COpaquePointer?) {
	val handler = opaque!!.asStableRef<PlatformEventHandler>().get()
	handler.onMouse()
}

private fun onResizeCallback(opaque: COpaquePointer?, columns: Int, rows: Int, width: Int, height: Int) {
	val handler = opaque!!.asStableRef<PlatformEventHandler>().get()
	handler.onResize(columns, rows, width, height)
}
