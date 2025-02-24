package com.jakewharton.mosaic.tty

import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption.REPLACE_EXISTING
import java.util.Locale.US

// TODO @JvmSynthetic https://youtrack.jetbrains.com/issue/KT-24981
internal object Jni {
	init {
		loadNativeLibrary("mosaic")
	}

	@JvmStatic
	external fun ttyCallbackInit(callback: Tty.Callback): Long

	@JvmStatic
	external fun ttyCallbackFree(callbackPtr: Long)

	@JvmStatic
	external fun ttyInit(callbackPtr: Long): Long

	@JvmStatic
	external fun ttyRead(
		ttyPtr: Long,
		buffer: ByteArray,
		offset: Int,
		count: Int,
	): Int

	@JvmStatic
	external fun ttyReadWithTimeout(
		ttyPtr: Long,
		buffer: ByteArray,
		offset: Int,
		count: Int,
		timeoutMillis: Int,
	): Int

	@JvmStatic
	external fun ttyInterrupt(ttyPtr: Long)

	@JvmStatic
	external fun ttyEnableRawMode(ttyPtr: Long)

	@JvmStatic
	external fun ttyEnableWindowResizeEvents(ttyPtr: Long)

	/**
	 * @return Array of `[columns, rows, width, height]`. Using an array saves us from having to
	 * pass a complex object across the JNI boundary.
	 */
	@JvmStatic
	external fun ttyCurrentSize(ttyPtr: Long): IntArray

	@JvmStatic
	external fun ttyFree(ttyPtr: Long)

	@JvmStatic
	external fun testTtyInit(callbackPtr: Long): Long

	@JvmStatic
	external fun testTtyGetTty(testTtyPtr: Long): Long

	@JvmStatic
	external fun testTtyWrite(testTtyPtr: Long, buffer: ByteArray)

	@JvmStatic
	external fun testTtyFocusEvent(testTtyPtr: Long, focused: Boolean)

	@JvmStatic
	external fun testTtyKeyEvent(testTtyPtr: Long)

	@JvmStatic
	external fun testTtyMouseEvent(testTtyPtr: Long)

	@JvmStatic
	external fun testTtyResizeEvent(
		testTtyPtr: Long,
		columns: Int,
		rows: Int,
		width: Int,
		height: Int,
	)

	@JvmStatic
	external fun testTtyFree(testTtyPtr: Long)

	@Suppress(
		// Only loading from our own JAR contents.
		"UnsafeDynamicallyLoadedCode",
		// Preserving copy/paste!
		"SameParameterValue",
	)
	private fun loadNativeLibrary(name: String) {
		val osName = System.getProperty("os.name").lowercase(US)
		val osArch = System.getProperty("os.arch").lowercase(US)
		val nativeLibraryJarPath = "/jni/$osArch/" + when {
			"linux" in osName -> "lib$name.so"
			"mac" in osName -> "lib$name.dylib"
			"windows" in osName -> "$name.dll"
			else -> throw IllegalStateException("Unsupported OS: $osName $osArch")
		}
		val nativeLibraryUrl = Jni::class.java.getResource(nativeLibraryJarPath)
			?: throw IllegalStateException("Unable to read $nativeLibraryJarPath from JAR")
		val nativeLibraryFile: Path
		try {
			nativeLibraryFile = Files.createTempFile(name, null)

			// File-based deleteOnExit() uses a special internal shutdown hook that always runs last.
			nativeLibraryFile.toFile().deleteOnExit()
			nativeLibraryUrl.openStream().use { nativeLibrary ->
				Files.copy(nativeLibrary, nativeLibraryFile, REPLACE_EXISTING)
			}
		} catch (e: IOException) {
			throw RuntimeException("Unable to extract native library from JAR", e)
		}
		System.load(nativeLibraryFile.toAbsolutePath().toString())
	}
}
