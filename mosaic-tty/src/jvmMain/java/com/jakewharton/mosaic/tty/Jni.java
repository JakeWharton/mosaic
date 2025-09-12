package com.jakewharton.mosaic.tty;

final class Jni {
	static {
		NativeLibrary.ensureLoaded();
	}

	static native long streamsInit();

	static native boolean streamsInputIsTty(long streamsPtr);

	static native boolean streamsOutputIsTty(long streamsPtr);

	static native boolean streamsErrorIsTty(long streamsPtr);

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

	static native long testTtyInit(boolean stdinIsTty, boolean stdoutIsTty, boolean stderrIsTty);

	static native long testTtyGetStreams(long testTtyPtr);

	static native long testTtyGetTty(long testTtyPtr);

	static native int testTtyWrite(long testTtyPtr, byte[] buffer, int offset, int count);

	static native int testTtyRead(long testTtyPtr, byte[] buffer, int offset, int count);

	static native void testTtyInterruptRead(long testTtyPtr);

	static native void testTtyResize(
		long testTtyPtr,
		int columns,
		int rows,
		int width,
		int height
	);

	static native void testTtySendFocusEvent(long testTtyPtr, boolean focused);

	static native void testTtySendKeyEvent(long testTtyPtr);

	static native void testTtySendMouseEvent(long testTtyPtr);

	static native void testTtyFree(long testTtyPtr);

	private Jni() {}
}
