package com.jakewharton.mosaic.terminal

import com.jakewharton.mosaic.terminal.Jni.terminalCurrentSize
import com.jakewharton.mosaic.terminal.Jni.terminalEnableRawMode
import com.jakewharton.mosaic.terminal.Jni.terminalEnableWindowResizeEvents
import com.jakewharton.mosaic.terminal.Jni.terminalEventCallbackFree
import com.jakewharton.mosaic.terminal.Jni.terminalEventCallbackInit
import com.jakewharton.mosaic.terminal.Jni.terminalFree
import com.jakewharton.mosaic.terminal.Jni.terminalInit
import com.jakewharton.mosaic.terminal.Jni.terminalInterruptRead
import com.jakewharton.mosaic.terminal.Jni.terminalRead
import com.jakewharton.mosaic.terminal.Jni.terminalReadWithTimeout

public actual class RawTerminal internal constructor(
	private var terminalPtr: Long,
	private val callbackPtr: Long,
) : AutoCloseable {
	public actual fun read(buffer: ByteArray, offset: Int, count: Int): Int {
		return terminalRead(terminalPtr, buffer, offset, count)
	}

	public actual fun read(buffer: ByteArray, offset: Int, count: Int, timeoutMillis: Int): Int {
		return terminalReadWithTimeout(terminalPtr, buffer, offset, count, timeoutMillis)
	}

	public actual fun interruptRead() {
		terminalInterruptRead(terminalPtr)
	}

// 	public actual fun writeOutput(bytes: ByteArray, offset: Int, count: Int): Int = TODO()
// 	public actual fun writeError(bytes: ByteArray, offset: Int, count: Int): Int = TODO()

	public actual fun enableRawMode() {
		terminalEnableRawMode(terminalPtr)
	}

	// public actual fun enableStandardStreamRedirection(): Unit = TODO()

	public actual fun enableNativeResizeEvents() {
		terminalEnableWindowResizeEvents(terminalPtr)
	}

	public actual fun currentSize(): IntArray {
		return terminalCurrentSize(terminalPtr)
	}

	actual override fun close() {
		val terminalPtr = terminalPtr
		if (terminalPtr != 0L) {
			this.terminalPtr = 0L
			terminalFree(terminalPtr)
			terminalEventCallbackFree(callbackPtr)
		}
	}

	public actual interface EventCallback {
		public actual fun onFocus(focused: Boolean)
		public actual fun onKey()
		public actual fun onMouse()
		public actual fun onResize(columns: Int, rows: Int, width: Int, height: Int)
		// public actual fun onStandardOutput(bytes: ByteArray)
		// public actual fun onStandardError(bytes: ByteArray)
	}

	public actual companion object {
		@JvmStatic
		public actual fun initialize(callback: EventCallback): RawTerminal {
			val callbackPtr = terminalEventCallbackInit(callback)
			try {
				val ptr = terminalInit(callbackPtr)
				return RawTerminal(ptr, callbackPtr)
			} catch (t: Throwable) {
				terminalEventCallbackFree(callbackPtr)
				throw t
			}
		}
	}
}
