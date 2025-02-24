package com.jakewharton.mosaic.terminal

// TODO @JvmSynthetic https://youtrack.jetbrains.com/issue/KT-24981
internal actual class Tty(
	private var ttyPtr: Long,
	private val callbackPtr: Long,
) : AutoCloseable {
	actual companion object {
		actual fun create(callback: Callback): Tty {
			val callbackPtr = Jni.ttyCallbackInit(callback)
			if (callbackPtr != 0L) {
				val ttyPtr = Jni.ttyInit(callbackPtr)
				if (ttyPtr != 0L) {
					return Tty(ttyPtr, callbackPtr)
				}
				Jni.ttyCallbackFree(callbackPtr)
			}
			throw OutOfMemoryError()
		}
	}

	actual fun read(buffer: ByteArray, offset: Int, count: Int): Int {
		return Jni.ttyRead(ttyPtr, buffer, offset, count)
	}

	actual fun readWithTimeout(buffer: ByteArray, offset: Int, count: Int, timeoutMillis: Int): Int {
		return Jni.ttyReadWithTimeout(ttyPtr, buffer, offset, count, timeoutMillis)
	}

	actual fun interrupt() {
		Jni.ttyInterrupt(ttyPtr)
	}

	actual fun enableRawMode() {
		Jni.ttyEnableRawMode(ttyPtr)
	}

	actual fun enableWindowResizeEvents() {
		Jni.ttyEnableWindowResizeEvents(ttyPtr)
	}

	actual fun currentSize(): IntArray {
		return Jni.ttyCurrentSize(ttyPtr)
	}

	actual override fun close() {
		if (ttyPtr != 0L) {
			Jni.ttyFree(ttyPtr)
			ttyPtr = 0
			Jni.ttyCallbackFree(callbackPtr)
		}
	}

	actual interface Callback {
		actual fun onFocus(focused: Boolean)
		actual fun onKey()
		actual fun onMouse()
		actual fun onResize(columns: Int, rows: Int, width: Int, height: Int)
	}
}
