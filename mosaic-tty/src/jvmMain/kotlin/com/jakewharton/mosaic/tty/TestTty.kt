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
		public actual fun bind(
			stdinIsTty: Boolean,
			stdoutIsTty: Boolean,
			stderrIsTty: Boolean,
		): TestTty {
			val testTtyPtr = testTtyInit(stdinIsTty, stdoutIsTty, stderrIsTty)
			val ttyPtr = testTtyGetTty(testTtyPtr)
			val tty = Tty(ttyPtr)
			return TestTty(testTtyPtr, tty)
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
	public actual fun resize(columns: Int, rows: Int, width: Int, height: Int) {
		Jni.testTtyResize(testTtyPtr, columns, rows, width, height)
	}

	@Throws(IOException::class)
	public actual fun sendFocusEvent(focused: Boolean) {
		Jni.testTtySendFocusEvent(testTtyPtr, focused)
	}

	@Throws(IOException::class)
	public actual fun sendKeyEvent() {
		Jni.testTtySendKeyEvent(testTtyPtr)
	}

	@Throws(IOException::class)
	public actual fun sendMouseEvent() {
		Jni.testTtySendMouseEvent(testTtyPtr)
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
