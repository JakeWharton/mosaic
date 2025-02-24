package com.jakewharton.mosaic.terminal

// TODO @JvmSynthetic https://youtrack.jetbrains.com/issue/KT-24981
internal actual class PlatformInput(
	private var inputPtr: Long,
	private val handlerPtr: Long,
) : AutoCloseable {
	actual companion object {
		actual fun create(callback: Callback): PlatformInput {
			val handlerPtr = Jni.platformInputCallbackInit(callback)
			if (handlerPtr != 0L) {
				val inputPtr = Jni.platformInputInit(handlerPtr)
				if (inputPtr != 0L) {
					return PlatformInput(inputPtr, handlerPtr)
				}
				Jni.platformInputCallbackFree(handlerPtr)
			}
			throw OutOfMemoryError()
		}
	}

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

	actual fun currentSize(): IntArray {
		return Jni.platformInputCurrentSize(inputPtr)
	}

	actual override fun close() {
		if (inputPtr != 0L) {
			Jni.platformInputFree(inputPtr)
			inputPtr = 0
			Jni.platformInputCallbackFree(handlerPtr)
		}
	}

	actual interface Callback {
		actual fun onFocus(focused: Boolean)
		actual fun onKey()
		actual fun onMouse()
		actual fun onResize(columns: Int, rows: Int, width: Int, height: Int)
	}
}
