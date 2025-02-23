package com.jakewharton.mosaic.terminal

import com.jakewharton.mosaic.terminal.TtyJni.platformEventHandlerFree
import com.jakewharton.mosaic.terminal.TtyJni.platformEventHandlerInit
import com.jakewharton.mosaic.terminal.TtyJni.platformInputWriterGetPlatformInput
import com.jakewharton.mosaic.terminal.TtyJni.platformInputWriterInit
import com.jakewharton.mosaic.terminal.event.Event
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.Channel.Factory.UNLIMITED

internal actual fun PlatformInputWriter(): PlatformInputWriter {
	val events = Channel<Event>(UNLIMITED)
	val handlerPtr = platformEventHandlerInit(PlatformEventHandler(events, emitDebugEvents = false))
	if (handlerPtr != 0L) {
		val writerPtr = platformInputWriterInit(handlerPtr)
		if (writerPtr != 0L) {
			val inputPtr = platformInputWriterGetPlatformInput(writerPtr)
			val platformInput = PlatformInput(inputPtr, handlerPtr)
			return PlatformInputWriter(writerPtr, events, platformInput)
		}
		platformEventHandlerFree(handlerPtr)
	}
	throw OutOfMemoryError()
}

internal actual class PlatformInputWriter(
	private var writerPtr: Long,
	private val events: Channel<Event>,
	actual val input: PlatformInput,
) : AutoCloseable {
	actual fun terminalReader(emitDebugEvents: Boolean): TerminalReader {
		return TerminalReader(input, events, emitDebugEvents)
	}

	actual fun write(buffer: ByteArray) {
		TtyJni.platformInputWriterWrite(writerPtr, buffer)
	}

	actual fun focusEvent(focused: Boolean) {
		TtyJni.platformInputWriterFocusEvent(writerPtr, focused)
	}

	actual fun keyEvent() {
		TtyJni.platformInputWriterKeyEvent(writerPtr)
	}

	actual fun mouseEvent() {
		TtyJni.platformInputWriterMouseEvent(writerPtr)
	}

	actual fun resizeEvent(columns: Int, rows: Int, width: Int, height: Int) {
		TtyJni.platformInputWriterResizeEvent(writerPtr, columns, rows, width, height)
	}

	actual override fun close() {
		if (writerPtr != 0L) {
			input.close()
			TtyJni.platformInputWriterFree(writerPtr)
			writerPtr = 0
		}
	}
}
