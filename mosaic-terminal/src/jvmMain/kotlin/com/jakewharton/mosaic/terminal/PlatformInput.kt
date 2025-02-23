package com.jakewharton.mosaic.terminal

// TODO @JvmSynthetic https://youtrack.jetbrains.com/issue/KT-24981
internal actual class PlatformInput(
	private var inputPtr: Long,
	private val handlerPtr: Long,
) : AutoCloseable {
	actual fun read(buffer: ByteArray, offset: Int, count: Int): Int {
		return TtyJni.platformInputRead(inputPtr, buffer, offset, count)
	}

	actual fun readWithTimeout(buffer: ByteArray, offset: Int, count: Int, timeoutMillis: Int): Int {
		return TtyJni.platformInputReadWithTimeout(inputPtr, buffer, offset, count, timeoutMillis)
	}

	actual fun interrupt() {
		TtyJni.platformInputInterrupt(inputPtr)
	}

	actual fun enableRawMode() {
		TtyJni.platformInputEnableRawMode(inputPtr)
	}

	actual fun enableWindowResizeEvents() {
		TtyJni.platformInputEnableWindowResizeEvents(inputPtr)
	}

<<<<<<< Updated upstream
	actual fun currentSize(): IntArray {
		return Jni.platformInputCurrentSize(inputPtr)
=======
	actual fun currentSize(): ResizeEvent {
		val (columns, rows, width, height) = TtyJni.platformInputCurrentSize(inputPtr)
		return ResizeEvent(
			columns = columns,
			rows = rows,
			width = width,
			height = height,
		)
>>>>>>> Stashed changes
	}

	actual override fun close() {
		if (inputPtr != 0L) {
			TtyJni.platformInputFree(inputPtr)
			inputPtr = 0
			TtyJni.platformEventHandlerFree(handlerPtr)
		}
	}
}
