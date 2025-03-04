package com.jakewharton.mosaic.tty

import com.jakewharton.mosaic.tty.Jni.testTtyGetTty
import com.jakewharton.mosaic.tty.Jni.testTtyInit

public actual class TestTty private constructor(
	private var testTtyPtr: Long,
	public actual val tty: Tty,
) : AutoCloseable {
	public actual companion object {
		@JvmStatic
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

	public actual fun write(buffer: ByteArray) {
		Jni.testTtyWrite(testTtyPtr, buffer)
	}

	public actual fun focusEvent(focused: Boolean) {
		Jni.testTtyFocusEvent(testTtyPtr, focused)
	}

	public actual fun keyEvent() {
		Jni.testTtyKeyEvent(testTtyPtr)
	}

	public actual fun mouseEvent() {
		Jni.testTtyMouseEvent(testTtyPtr)
	}

	public actual fun resizeEvent(columns: Int, rows: Int, width: Int, height: Int) {
		Jni.testTtyResizeEvent(testTtyPtr, columns, rows, width, height)
	}

	actual override fun close() {
		if (testTtyPtr != 0L) {
			tty.close()
			Jni.testTtyFree(testTtyPtr)
			testTtyPtr = 0
		}
	}
}
