package com.jakewharton.mosaic.terminal

import com.jakewharton.mosaic.terminal.event.ResizeEvent

// TODO @JvmSynthetic https://youtrack.jetbrains.com/issue/KT-24981
internal actual class PlatformInput(
	private var inputPtr: Long,
	private val handlerPtr: Long,
) : AutoCloseable {
	actual fun read(buffer: ByteArray, offset: Int, count: Int): Int {
		return Jni.platformInputRead(inputPtr, buffer, offset, count)
	}

	actual fun readWithTimeout(buffer: ByteArray, offset: Int, count: Int, timeoutMillis: Int): Int {
		return Jni.platformInputReadWithTimeout(inputPtr, buffer, offset, count, timeoutMillis)
	}

	actual fun interrupt() {
		Jni.platformInputInterrupt(inputPtr)
	}

	actual fun enableRawMode() {
		Jni.platformInputEnableRawMode(inputPtr)
	}

	actual fun enableWindowResizeEvents() {
		Jni.platformInputEnableWindowResizeEvents(inputPtr)
	}

	actual fun currentSize(): ResizeEvent {
		val (columns, rows, width, height) = Jni.platformInputCurrentSize(inputPtr)
		return ResizeEvent(
			columns = columns,
			rows = rows,
			width = width,
			height = height,
		)
	}

	actual override fun close() {
		if (inputPtr != 0L) {
			Jni.platformInputFree(inputPtr)
			inputPtr = 0
			Jni.platformEventHandlerFree(handlerPtr)
		}
	}
}
