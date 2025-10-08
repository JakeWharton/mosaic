package com.jakewharton.mosaic.tty

import java.io.InputStream
import java.io.OutputStream

public actual class Tty internal constructor(
	private var ptr: Long,
) : AutoCloseable {
	public actual companion object {
		@JvmStatic
		@Throws(IOException::class)
		public actual fun tryBind(): Tty? {
			val ttyPtr = Jni.ttyInit()
			if (ttyPtr != 0L) {
				return Tty(ttyPtr)
			}
			return null
		}
	}

	/** Read from the TTY using a regular [InputStream]. */
	public fun asInputStream(): InputStream = TtyInputStream(this)

	/** Write to the TTY using a regular [OutputStream]. */
	public fun asOutputStream(): OutputStream = TtyOutputStream(this)

	private var callbackPtr = 0L

	public actual fun setCallback(callback: Callback?) {
		val oldCallbackPtr = callbackPtr
		if (oldCallbackPtr != 0L) {
			Jni.ttyCallbackFree(oldCallbackPtr)
		}

		val newCallbackPtr = if (callback != null) {
			Jni.ttyCallbackInit(callback).also { ptr ->
				if (ptr == 0L) {
					throw OutOfMemoryError()
				}
			}
		} else {
			0L
		}

		callbackPtr = newCallbackPtr
		Jni.ttySetCallback(ptr, newCallbackPtr)
	}

	@Throws(IOException::class)
	public actual fun read(buffer: ByteArray, offset: Int, count: Int): Int {
		return Jni.ttyRead(ptr, buffer, offset, count)
	}

	@Throws(IOException::class)
	public actual fun readWithTimeout(buffer: ByteArray, offset: Int, count: Int, timeoutMillis: Int): Int {
		return Jni.ttyReadWithTimeout(ptr, buffer, offset, count, timeoutMillis)
	}

	@Throws(IOException::class)
	public actual fun interruptRead() {
		Jni.ttyInterruptRead(ptr)
	}

	@Throws(IOException::class)
	public actual fun write(buffer: ByteArray, offset: Int, count: Int): Int {
		return Jni.ttyWrite(ptr, buffer, offset, count)
	}

	@Throws(IOException::class)
	public actual fun enableRawMode() {
		Jni.ttyEnableRawMode(ptr)
	}

	@Throws(IOException::class)
	public actual fun enableWindowResizeEvents() {
		Jni.ttyEnableWindowResizeEvents(ptr)
	}

	@Throws(IOException::class)
	public actual fun currentSize(): IntArray {
		return Jni.ttyCurrentSize(ptr)
	}

	@Throws(IOException::class)
	public actual fun reset() {
		Jni.ttyReset(ptr)
	}

	@Throws(IOException::class)
	actual override fun close() {
		if (ptr != 0L) {
			Jni.ttyFree(ptr)
			ptr = 0

			if (callbackPtr != 0L) {
				Jni.ttyCallbackFree(callbackPtr)
				callbackPtr = 0
			}
		}
	}

	public actual interface Callback {
		public actual fun onFocus(focused: Boolean)
		public actual fun onKey()
		public actual fun onMouse()
		public actual fun onResize(columns: Int, rows: Int, width: Int, height: Int)
	}
}
