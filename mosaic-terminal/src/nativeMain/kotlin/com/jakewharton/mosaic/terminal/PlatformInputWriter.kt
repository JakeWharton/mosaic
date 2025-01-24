package com.jakewharton.mosaic.terminal

import com.jakewharton.mosaic.terminal.event.Event
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import kotlinx.coroutines.channels.Channel

internal actual class PlatformInputWriter internal constructor(
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
