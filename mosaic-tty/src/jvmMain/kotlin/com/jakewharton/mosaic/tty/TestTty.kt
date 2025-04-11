package com.jakewharton.mosaic.tty

import com.jakewharton.mosaic.tty.Jni.testTtyGetTty
import com.jakewharton.mosaic.tty.Jni.testTtyInit

public actual class TestTty private constructor(
	private var testTtyPtr: Long,
	public actual val tty: Tty,
) : AutoCloseable {
	public actual companion object {
		@JvmStatic
		@Throws(IOException::class)
		public actual fun create(): TestTty {
			val testTtyPtr = testTtyInit()
			if (testTtyPtr != 0L) {
				val ttyPtr = testTtyGetTty(testTtyPtr)
				val tty = Tty(ttyPtr)
				return TestTty(testTtyPtr, tty)
			}
			throw OutOfMemoryError()
		}
	}

	@Throws(IOException::class)
	public actual fun write(buffer: ByteArray, offset: Int, count: Int): Int {
		return Jni.testTtyWrite(testTtyPtr, buffer, offset, count)
	}

	@Throws(IOException::class)
	public actual fun read(buffer: ByteArray, offset: Int, count: Int): Int {
		return Jni.testTtyRead(testTtyPtr, buffer, offset, count)
	}

	@Throws(IOException::class)
	public actual fun interruptRead() {
		Jni.testTtyInterruptRead(testTtyPtr)
	}

	@Throws(IOException::class)
	public actual fun focusEvent(focused: Boolean) {
		Jni.testTtyFocusEvent(testTtyPtr, focused)
	}

	@Throws(IOException::class)
	public actual fun keyEvent() {
		Jni.testTtyKeyEvent(testTtyPtr)
	}

	@Throws(IOException::class)
	public actual fun mouseEvent() {
		Jni.testTtyMouseEvent(testTtyPtr)
	}

	@Throws(IOException::class)
	public actual fun resizeEvent(columns: Int, rows: Int, width: Int, height: Int) {
		Jni.testTtyResizeEvent(testTtyPtr, columns, rows, width, height)
	}

	@Throws(IOException::class)
	actual override fun close() {
		if (testTtyPtr != 0L) {
			tty.close()
			Jni.testTtyFree(testTtyPtr)
			testTtyPtr = 0
		}
	}
}
