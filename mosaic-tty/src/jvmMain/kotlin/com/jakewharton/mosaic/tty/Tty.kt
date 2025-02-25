package com.jakewharton.mosaic.tty

public actual class Tty internal constructor(
	private var ttyPtr: Long,
	private val callbackPtr: Long,
) : AutoCloseable {
	public actual companion object {
		@JvmStatic
		public actual fun create(callback: Callback): Tty {
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

	public actual fun readInput(buffer: ByteArray, offset: Int, count: Int): Int {
		return Jni.ttyReadInput(ttyPtr, buffer, offset, count)
	}

	public actual fun readInputWithTimeout(buffer: ByteArray, offset: Int, count: Int, timeoutMillis: Int): Int {
		return Jni.ttyReadInputWithTimeout(ttyPtr, buffer, offset, count, timeoutMillis)
	}

	public actual fun interruptRead() {
		Jni.ttyInterruptRead(ttyPtr)
	}

	public actual fun writeOutput(buffer: ByteArray, offset: Int, count: Int): Int {
		return Jni.ttyWriteOutput(ttyPtr, buffer, offset, count)
	}

	public actual fun writeError(buffer: ByteArray, offset: Int, count: Int): Int {
		return Jni.ttyWriteError(ttyPtr, buffer, offset, count)
	}

	public actual fun enableRawMode() {
		Jni.ttyEnableRawMode(ttyPtr)
	}

	public actual fun enableWindowResizeEvents() {
		Jni.ttyEnableWindowResizeEvents(ttyPtr)
	}

	public actual fun currentSize(): IntArray {
		return Jni.ttyCurrentSize(ttyPtr)
	}

	actual override fun close() {
		if (ttyPtr != 0L) {
			Jni.ttyFree(ttyPtr)
			ttyPtr = 0
			Jni.ttyCallbackFree(callbackPtr)
		}
	}

	public actual interface Callback {
		public actual fun onFocus(focused: Boolean)
		public actual fun onKey()
		public actual fun onMouse()
		public actual fun onResize(columns: Int, rows: Int, width: Int, height: Int)
	}
}
