package com.jakewharton.mosaic.tty;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static java.util.Locale.US;

final class Jni {
	static {
		loadEmbeddedNativeLibrary("mosaic");
	}

	@SuppressWarnings("SameParameterValue") // Preserving copy/paste!
	private static void loadEmbeddedNativeLibrary(String name) {
		String osName = System.getProperty("os.name").toLowerCase(US);
		String osArch = System.getProperty("os.arch").toLowerCase(US);
		StringBuilder nativeLibraryJarPath = new StringBuilder(30)
			.append("/jni/")
			.append(osArch)
			.append('/');
		if (osName.contains("linux")) {
			nativeLibraryJarPath.append("lib")
				.append(name)
				.append(".so");
		} else if (osName.contains("mac")) {
			nativeLibraryJarPath.append("lib")
				.append(name)
				.append(".dylib");
		} else if (osName.contains("windows")) {
			nativeLibraryJarPath.append(name)
				.append(".dll");
		} else {
			throw new IllegalStateException("Unsupported OS: " + osName + ' ' + osArch);
		}
		URL nativeLibraryUrl = Jni.class.getResource(nativeLibraryJarPath.toString());
		if (nativeLibraryUrl == null) {
			throw new IllegalStateException("Unable to read $nativeLibraryJarPath from JAR");
		}

		Path nativeLibraryFile;
		try {
			nativeLibraryFile = Files.createTempFile(name, null);

			// File-based deleteOnExit() uses a special internal shutdown hook that always runs last.
			nativeLibraryFile.toFile().deleteOnExit();
			try (InputStream nativeLibrary = nativeLibraryUrl.openStream()) {
				Files.copy(nativeLibrary, nativeLibraryFile, REPLACE_EXISTING);
			}
		} catch (IOException e) {
			throw new UncheckedIOException("Unable to extract native library from JAR", e);
		}
		System.load(nativeLibraryFile.toAbsolutePath().toString());
	}

	static native long ttyCallbackInit(Tty.Callback callback);

	static native void ttyCallbackFree(long callbackPtr);

	static native long ttyInit();

	static native long ttySetCallback(long ttyPtr, long callbackPtr);

	static native int ttyReadInput(
		long ttyPtr,
		byte[] buffer,
		int offset,
		int count
	);

	static native int ttyReadInputWithTimeout(
		long ttyPtr,
		byte[] buffer,
		int offset,
		int count,
		int timeoutMillis
	);

	static native void ttyInterruptRead(long ttyPtr);

	static native int ttyWriteOutput(
		long ttyPtr,
		byte[] buffer,
		int offset,
		int count
	);

	static native int ttyWriteError(
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

	static native void ttyFree(long ttyPtr);

	static native long testTtyInit();

	static native long testTtyGetTty(long testTtyPtr);

	static native void testTtyWrite(long testTtyPtr, byte[] buffer);

	static native void testTtyFocusEvent(long testTtyPtr, boolean focused);

	static native void testTtyKeyEvent(long testTtyPtr);

	static native void testTtyMouseEvent(long testTtyPtr);

	static native void testTtyResizeEvent(
		long testTtyPtr,
		int columns,
		int rows,
		int width,
		int height
	);

	static native void testTtyFree(long testTtyPtr);

	private Jni() {}
}
