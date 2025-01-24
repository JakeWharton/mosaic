package com.jakewharton.mosaic.terminal

import com.jakewharton.mosaic.terminal.event.Event
import kotlinx.coroutines.channels.Channel

// TODO @JvmSynthetic https://youtrack.jetbrains.com/issue/KT-24981
internal actual class PlatformInputWriter internal constructor(
	private var writerPtr: Long,
	private val events: Channel<Event>,
	actual val input: PlatformInput,
) : AutoCloseable {
	actual fun terminalReader(emitDebugEvents: Boolean): TerminalReader {
		return TerminalReader(input, events, emitDebugEvents)
	}

	actual fun write(buffer: ByteArray) {
		Jni.stdinWriterWrite(writerPtr, buffer)
	}

	actual fun focusEvent(focused: Boolean) {
		Jni.stdinWriterFocusEvent(writerPtr, focused)
	}

	actual fun keyEvent() {
		Jni.stdinWriterKeyEvent(writerPtr)
	}

	actual fun mouseEvent() {
		Jni.stdinWriterMouseEvent(writerPtr)
	}

	actual fun resizeEvent(columns: Int, rows: Int, width: Int, height: Int) {
		Jni.stdinWriterResizeEvent(writerPtr, columns, rows, width, height)
	}

	actual override fun close() {
		if (writerPtr != 0L) {
			input.close()
			Jni.stdinWriterFree(writerPtr)
			writerPtr = 0
		}
	}
}
