package com.jakewharton.mosaic.terminal

import com.jakewharton.mosaic.terminal.event.Event
import kotlinx.cinterop.COpaquePointer
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.StableRef
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.alloc
import kotlinx.cinterop.asStableRef
import kotlinx.cinterop.free
import kotlinx.cinterop.nativeHeap
import kotlinx.cinterop.ptr
import kotlinx.cinterop.staticCFunction
import kotlinx.cinterop.useContents
import kotlinx.cinterop.usePinned
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

	internal actual fun stdinWriter(emitDebugEvents: Boolean): StdinWriter {
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
		val terminalReader = TerminalReader(platformInput, events, emitDebugEvents)
		return StdinWriter(writerPtr, terminalReader)
	}

	internal fun throwError(error: UInt): Nothing {
		throw RuntimeException(error.toString())
	}
}

internal actual class PlatformInput internal constructor(
	ptr: CPointer<stdinReader>,
	private val handlerPtr: CPointer<platformEventHandler>?,
	private val handlerRef: StableRef<PlatformEventHandler>?,
) : AutoCloseable {
	private var ptr: CPointer<stdinReader>? = ptr

	actual fun read(buffer: ByteArray, offset: Int, count: Int): Int {
		buffer.usePinned {
			stdinReader_read(ptr, it.addressOf(offset), count).useContents {
				if (error == 0U) return this.count
				Tty.throwError(error)
			}
		}
	}

	actual fun readWithTimeout(buffer: ByteArray, offset: Int, count: Int, timeoutMillis: Int): Int {
		buffer.usePinned {
			stdinReader_readWithTimeout(ptr, it.addressOf(offset), count, timeoutMillis).useContents {
				if (error == 0U) return this.count
				Tty.throwError(error)
			}
		}
	}

	actual fun interrupt() {
		val error = stdinReader_interrupt(ptr)
		if (error == 0U) return
		Tty.throwError(error)
	}

	actual override fun close() {
		ptr?.let { ptr ->
			this.ptr = null

			val error = stdinReader_free(ptr)
			handlerPtr?.let(nativeHeap::free)
			handlerRef?.dispose()

			if (error == 0U) return
			Tty.throwError(error)
		}
	}
}

internal actual class StdinWriter internal constructor(
	private var ptr: CPointer<stdinWriter>?,
	actual val reader: TerminalReader,
) : AutoCloseable {
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

			reader.close()

			val error = stdinWriter_free(ref)

			if (error == 0U) return
			Tty.throwError(error)
		}
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
