package com.jakewharton.mosaic.tty

import com.jakewharton.mosaic.tty.Jni.testGetStreams
import com.jakewharton.mosaic.tty.Jni.testGetTty
import com.jakewharton.mosaic.tty.Jni.testInit

public actual class TestTerminal private constructor(
	private var ptr: Long,
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
		): TestTerminal {
			val testTtyPtr = testInit(stdinIsTty, stdoutIsTty, stderrIsTty)
			val streamsPtr = testGetStreams(testTtyPtr)
			val streams = StandardStreams(streamsPtr)
			val ttyPtr = testGetTty(testTtyPtr)
			val tty = Tty(ttyPtr)
			return TestTerminal(testTtyPtr, streams, tty)
		}
	}

	@Throws(IOException::class)
	public actual fun writeTty(buffer: ByteArray, offset: Int, count: Int): Int {
		return Jni.testWriteTty(ptr, buffer, offset, count)
	}

	@Throws(IOException::class)
	public actual fun readTty(buffer: ByteArray, offset: Int, count: Int): Int {
		return Jni.testReadTty(ptr, buffer, offset, count)
	}

	@Throws(IOException::class)
	public actual fun readTtyWithTimeout(buffer: ByteArray, offset: Int, count: Int, timeoutMillis: Int): Int {
		return Jni.testReadTtyWithTimeout(ptr, buffer, offset, count, timeoutMillis)
	}

	@Throws(IOException::class)
	public actual fun interruptTtyRead() {
		Jni.testInterruptTtyRead(ptr)
	}

	@Throws(IOException::class)
	public actual fun writeStandardInput(buffer: ByteArray, offset: Int, count: Int): Int {
		return Jni.testWriteInput(ptr, buffer, offset, count)
	}

	@Throws(IOException::class)
	public actual fun readStandardOutput(buffer: ByteArray, offset: Int, count: Int): Int {
		return Jni.testReadOutput(ptr, buffer, offset, count)
	}

	@Throws(IOException::class)
	public actual fun readStandardOutputWithTimeout(buffer: ByteArray, offset: Int, count: Int, timeoutMillis: Int): Int {
		return Jni.testReadOutputWithTimeout(ptr, buffer, offset, count, timeoutMillis)
	}

	@Throws(IOException::class)
	public actual fun interruptStandardOutputRead() {
		Jni.testInterruptOutputRead(ptr)
	}

	@Throws(IOException::class)
	public actual fun readStandardError(buffer: ByteArray, offset: Int, count: Int): Int {
		return Jni.testReadError(ptr, buffer, offset, count)
	}

	@Throws(IOException::class)
	public actual fun readStandardErrorWithTimeout(buffer: ByteArray, offset: Int, count: Int, timeoutMillis: Int): Int {
		return Jni.testReadErrorWithTimeout(ptr, buffer, offset, count, timeoutMillis)
	}

	@Throws(IOException::class)
	public actual fun interruptStandardErrorRead() {
		Jni.testInterruptErrorRead(ptr)
	}

	@Throws(IOException::class)
	public actual fun resize(columns: Int, rows: Int, width: Int, height: Int) {
		Jni.testResize(ptr, columns, rows, width, height)
	}

	@Throws(IOException::class)
	public actual fun sendFocusEvent(focused: Boolean) {
		Jni.testSendFocusEvent(ptr, focused)
	}

	@Throws(IOException::class)
	public actual fun sendKeyEvent() {
		Jni.testSendKeyEvent(ptr)
	}

	@Throws(IOException::class)
	public actual fun sendMouseEvent() {
		Jni.testSendMouseEvent(ptr)
	}

	@Throws(IOException::class)
	actual override fun close() {
		if (ptr != 0L) {
			tty.close()
			Jni.testFree(ptr)
			ptr = 0
		}
	}
}
