package com.jakewharton.mosaic.tty

import com.jakewharton.mosaic.tty.Jni.testGetStreams
import com.jakewharton.mosaic.tty.Jni.testGetTty
import com.jakewharton.mosaic.tty.Jni.testInit

public actual class TestTty private constructor(
	private var testTtyPtr: Long,
	public actual val streams: StandardStreams,
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
			val testTtyPtr = testInit(stdinIsTty, stdoutIsTty, stderrIsTty)
			val streamsPtr = testGetStreams(testTtyPtr)
			val streams = StandardStreams(streamsPtr)
			val ttyPtr = testGetTty(testTtyPtr)
			val tty = Tty(ttyPtr)
			return TestTty(testTtyPtr, streams, tty)
		}
	}

	@Throws(IOException::class)
	public actual fun writeTty(buffer: ByteArray, offset: Int, count: Int): Int {
		return Jni.testWriteTty(testTtyPtr, buffer, offset, count)
	}

	@Throws(IOException::class)
	public actual fun readTty(buffer: ByteArray, offset: Int, count: Int): Int {
		return Jni.testReadTty(testTtyPtr, buffer, offset, count)
	}

	@Throws(IOException::class)
	public actual fun readTtyWithTimeout(buffer: ByteArray, offset: Int, count: Int, timeoutMillis: Int): Int {
		return Jni.testReadTtyWithTimeout(testTtyPtr, buffer, offset, count, timeoutMillis)
	}

	@Throws(IOException::class)
	public actual fun interruptTtyRead() {
		Jni.testInterruptTtyRead(testTtyPtr)
	}

	@Throws(IOException::class)
	public actual fun writeStandardInput(buffer: ByteArray, offset: Int, count: Int): Int {
		return Jni.testWriteInput(testTtyPtr, buffer, offset, count)
	}

	@Throws(IOException::class)
	public actual fun readStandardOutput(buffer: ByteArray, offset: Int, count: Int): Int {
		return Jni.testReadOutput(testTtyPtr, buffer, offset, count)
	}

	@Throws(IOException::class)
	public actual fun readStandardOutputWithTimeout(buffer: ByteArray, offset: Int, count: Int, timeoutMillis: Int): Int {
		return Jni.testReadOutputWithTimeout(testTtyPtr, buffer, offset, count, timeoutMillis)
	}

	@Throws(IOException::class)
	public actual fun interruptStandardOutputRead() {
		Jni.testInterruptOutputRead(testTtyPtr)
	}

	@Throws(IOException::class)
	public actual fun readStandardError(buffer: ByteArray, offset: Int, count: Int): Int {
		return Jni.testReadError(testTtyPtr, buffer, offset, count)
	}

	@Throws(IOException::class)
	public actual fun readStandardErrorWithTimeout(buffer: ByteArray, offset: Int, count: Int, timeoutMillis: Int): Int {
		return Jni.testReadErrorWithTimeout(testTtyPtr, buffer, offset, count, timeoutMillis)
	}

	@Throws(IOException::class)
	public actual fun interruptStandardErrorRead() {
		Jni.testInterruptErrorRead(testTtyPtr)
	}

	@Throws(IOException::class)
	public actual fun resize(columns: Int, rows: Int, width: Int, height: Int) {
		Jni.testResize(testTtyPtr, columns, rows, width, height)
	}

	@Throws(IOException::class)
	public actual fun sendFocusEvent(focused: Boolean) {
		Jni.testSendFocusEvent(testTtyPtr, focused)
	}

	@Throws(IOException::class)
	public actual fun sendKeyEvent() {
		Jni.testSendKeyEvent(testTtyPtr)
	}

	@Throws(IOException::class)
	public actual fun sendMouseEvent() {
		Jni.testSendMouseEvent(testTtyPtr)
	}

	@Throws(IOException::class)
	actual override fun close() {
		if (testTtyPtr != 0L) {
			tty.close()
			Jni.testFree(testTtyPtr)
			testTtyPtr = 0
		}
	}
}
