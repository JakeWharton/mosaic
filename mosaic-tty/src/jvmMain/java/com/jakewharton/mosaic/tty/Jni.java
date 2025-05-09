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
		loadEmbeddedNativeLibrary("jni", "mosaic");
	}

	@SuppressWarnings("SameParameterValue") // Preserving copy/paste!
	private static void loadEmbeddedNativeLibrary(String relativePath, String library) {
		String osName = System.getProperty("os.name").toLowerCase(US);
		String osArch = System.getProperty("os.arch").toLowerCase(US);
		StringBuilder nativeLibraryJarPathBuilder = new StringBuilder(50)
			.append(relativePath)
			.append('/')
			.append(osArch)
			.append('/');
		if (osName.contains("linux")) {
			nativeLibraryJarPathBuilder.append("lib")
				.append(library)
				.append(".so");
		} else if (osName.contains("mac")) {
			nativeLibraryJarPathBuilder.append("lib")
				.append(library)
				.append(".dylib");
		} else if (osName.contains("windows")) {
			nativeLibraryJarPathBuilder.append(library)
				.append(".dll");
		} else {
			throw new IllegalStateException("Unsupported OS: " + osName + ' ' + osArch);
		}
		String nativeLibraryJarPath = nativeLibraryJarPathBuilder.toString();
		URL nativeLibraryUrl = Jni.class.getResource(nativeLibraryJarPath);
		if (nativeLibraryUrl == null) {
			throw new IllegalStateException("Unable to read ".concat(nativeLibraryJarPath));
		}

		Path nativeLibraryFile;
		try {
			nativeLibraryFile = Files.createTempFile(library, null);

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

	static native long testTtyInit();

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
