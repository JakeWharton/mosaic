package com.jakewharton.mosaic.tty;

final class Jni {
	static {
		NativeLibrary.ensureLoaded();
	}

	static native long streamsInit();

	static native boolean streamsInputIsTty(long streamsPtr);

	static native boolean streamsOutputIsTty(long streamsPtr);

	static native boolean streamsErrorIsTty(long streamsPtr);

	static native int streamsReadInput(
		long streamsPtr,
		byte[] buffer,
		int offset,
		int count
	);

	static native int streamsReadInputWithTimeout(
		long streamsPtr,
		byte[] buffer,
		int offset,
		int count,
		int timeout
	);

	static native void streamsInterruptInputRead(long streamsPtr);

	static native int streamsWriteOutput(
		long streamsPtr,
		byte[] buffer,
		int offset,
		int count
	);

	static native int streamsWriteError(
		long streamsPtr,
		byte[] buffer,
		int offset,
		int count
	);

	static native void streamsFree(long streamsPtr);

	static native long ttyCallbackInit(Tty.Callback callback);

	static native void ttyCallbackFree(long callbackPtr);

	static native long ttyInit();

	static native void ttySetCallback(long ttyPtr, long callbackPtr);

	static native int ttyRead(
		long ttyPtr,
		byte[] buffer,
		int offset,
		int count
	);

	static native int ttyReadWithTimeout(
		long ttyPtr,
		byte[] buffer,
		int offset,
		int count,
		int timeoutMillis
	);

	static native void ttyInterruptRead(long ttyPtr);

	static native int ttyWrite(
		long ttyPtr,
		byte[] buffer,
		int offset,
		int count
	);

	static native void ttyEnableRawMode(long ttyPtr);

	static native void ttyEnableWindowResizeEvents(long ttyPtr);

	/**
	 * @return Array of `[columns, rows, width, height]`. Using an array saves us from having to
	 * pass a complex object across the JNI boundary.
	 */
	static native int[] ttyCurrentSize(long ttyPtr);

	static native void ttyReset(long ttyPtr);

	static native void ttyFree(long ttyPtr);

	static native long testInit(boolean stdinIsTty, boolean stdoutIsTty, boolean stderrIsTty);

	static native long testGetStreams(long testTtyPtr);

	static native long testGetTty(long testTtyPtr);

	static native int testWriteTty(long testTtyPtr, byte[] buffer, int offset, int count);

	static native int testReadTty(long testTtyPtr, byte[] buffer, int offset, int count);

	static native int testReadTtyWithTimeout(long testTtyPtr, byte[] buffer, int offset, int count, int timeoutMillis);

	static native void testInterruptTtyRead(long testTtyPtr);

	static native int testWriteInput(long testTtyPtr, byte[] buffer, int offset, int count);

	static native int testReadOutput(long testTtyPtr, byte[] buffer, int offset, int count);

	static native int testReadOutputWithTimeout(long testTtyPtr, byte[] buffer, int offset, int count, int timeoutMillis);

	static native void testInterruptOutputRead(long testTtyPtr);

	static native int testReadError(long testTtyPtr, byte[] buffer, int offset, int count);

	static native int testReadErrorWithTimeout(long testTtyPtr, byte[] buffer, int offset, int count, int timeoutMillis);

	static native void testInterruptErrorRead(long testTtyPtr);

	static native void testResize(
		long testTtyPtr,
		int columns,
		int rows,
		int width,
		int height
	);

	static native void testSendFocusEvent(long testTtyPtr, boolean focused);

	static native void testSendKeyEvent(long testTtyPtr);

	static native void testSendMouseEvent(long testTtyPtr);

	static native void testFree(long testTtyPtr);

	private Jni() {}
}
