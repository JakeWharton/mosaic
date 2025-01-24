package com.jakewharton.mosaic.terminal

import com.jakewharton.mosaic.terminal.Jni.enterRawMode
import com.jakewharton.mosaic.terminal.Jni.exitRawMode
import com.jakewharton.mosaic.terminal.Jni.platformEventHandlerFree
import com.jakewharton.mosaic.terminal.Jni.platformEventHandlerInit
import com.jakewharton.mosaic.terminal.Jni.stdinReaderFree
import com.jakewharton.mosaic.terminal.Jni.stdinReaderInit
import com.jakewharton.mosaic.terminal.Jni.stdinReaderInterrupt
import com.jakewharton.mosaic.terminal.Jni.stdinReaderRead
import com.jakewharton.mosaic.terminal.Jni.stdinReaderReadWithTimeout
import com.jakewharton.mosaic.terminal.Jni.stdinWriterFocusEvent
import com.jakewharton.mosaic.terminal.Jni.stdinWriterFree
import com.jakewharton.mosaic.terminal.Jni.stdinWriterGetReader
import com.jakewharton.mosaic.terminal.Jni.stdinWriterInit
import com.jakewharton.mosaic.terminal.Jni.stdinWriterKeyEvent
import com.jakewharton.mosaic.terminal.Jni.stdinWriterMouseEvent
import com.jakewharton.mosaic.terminal.Jni.stdinWriterResizeEvent
import com.jakewharton.mosaic.terminal.Jni.stdinWriterWrite
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

	@JvmSynthetic // Hide from Java callers.
	internal actual fun stdinWriter(emitDebugEvents: Boolean): StdinWriter {
		val events = Channel<Event>(UNLIMITED)
		val handlerPtr = platformEventHandlerInit(PlatformEventHandler(events))
		if (handlerPtr != 0L) {
			val writerPtr = stdinWriterInit(handlerPtr)
			if (writerPtr != 0L) {
				val readerPtr = stdinWriterGetReader(writerPtr)
				val platformInput = PlatformInput(readerPtr, handlerPtr)
				val terminalReader = TerminalReader(platformInput, events, emitDebugEvents)
				return StdinWriter(writerPtr, terminalReader)
			}
			platformEventHandlerFree(handlerPtr)
		}
		throw OutOfMemoryError()
	}
}

// TODO @JvmSynthetic https://youtrack.jetbrains.com/issue/KT-24981
internal actual class PlatformInput internal constructor(
	private var readerPtr: Long,
	private val handlerPtr: Long,
) : AutoCloseable {
	actual fun read(buffer: ByteArray, offset: Int, count: Int): Int {
		return stdinReaderRead(readerPtr, buffer, offset, count)
	}

	actual fun readWithTimeout(buffer: ByteArray, offset: Int, count: Int, timeoutMillis: Int): Int {
		return stdinReaderReadWithTimeout(readerPtr, buffer, offset, count, timeoutMillis)
	}

	actual fun interrupt() {
		stdinReaderInterrupt(readerPtr)
	}

	actual override fun close() {
		if (readerPtr != 0L) {
			stdinReaderFree(readerPtr)
			readerPtr = 0
			platformEventHandlerFree(handlerPtr)
		}
	}
}

// TODO @JvmSynthetic https://youtrack.jetbrains.com/issue/KT-24981
internal actual class StdinWriter internal constructor(
	private var writerPtr: Long,
	actual val reader: TerminalReader,
) : AutoCloseable {
	actual fun write(buffer: ByteArray) {
		stdinWriterWrite(writerPtr, buffer)
	}

	actual fun focusEvent(focused: Boolean) {
		stdinWriterFocusEvent(writerPtr, focused)
	}

	actual fun keyEvent() {
		stdinWriterKeyEvent(writerPtr)
	}

	actual fun mouseEvent() {
		stdinWriterMouseEvent(writerPtr)
	}

	actual fun resizeEvent(columns: Int, rows: Int, width: Int, height: Int) {
		stdinWriterResizeEvent(writerPtr, columns, rows, width, height)
	}

	actual override fun close() {
		reader.close()
		if (writerPtr != 0L) {
			stdinWriterFree(writerPtr)
			writerPtr = 0
		}
	}
}
