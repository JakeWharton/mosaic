package com.jakewharton.mosaic.terminal

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
	external fun terminalEventCallbackInit(callback: RawTerminal.EventCallback): Long

	@JvmStatic
	external fun terminalEventCallbackFree(callbackPtr: Long)

	@JvmStatic
	external fun terminalInit(callbackPtr: Long): Long

	@JvmStatic
	external fun terminalRead(
		terminalPtr: Long,
		buffer: ByteArray,
		offset: Int,
		count: Int,
	): Int

	@JvmStatic
	external fun terminalReadWithTimeout(
		terminalPtr: Long,
		buffer: ByteArray,
		offset: Int,
		count: Int,
		timeoutMillis: Int,
	): Int

	@JvmStatic
	external fun terminalInterruptRead(terminalPtr: Long)

	@JvmStatic
	external fun terminalEnableRawMode(terminalPtr: Long)

	@JvmStatic
	external fun terminalEnableWindowResizeEvents(terminalPtr: Long)

	/**
	 * @return Array of `[columns, rows, width, height]`. Using an array saves us from having to
	 * pass a complex object across the JNI boundary.
	 */
	@JvmStatic
	external fun terminalCurrentSize(terminalPtr: Long): IntArray

	@JvmStatic
	external fun terminalFree(terminalPtr: Long)

	@JvmStatic
	external fun testTerminalInit(callbackPtr: Long): Long

	@JvmStatic
	external fun testTerminalGetTerminal(testTerminalPtr: Long): Long

	@JvmStatic
	external fun testTerminalWrite(testTerminalPtr: Long, buffer: ByteArray)

	@JvmStatic
	external fun testTerminalFocusEvent(testTerminalPtr: Long, focused: Boolean)

	@JvmStatic
	external fun testTerminalKeyEvent(testTerminalPtr: Long)

	@JvmStatic
	external fun testTerminalMouseEvent(testTerminalPtr: Long)

	@JvmStatic
	external fun testTerminalResizeEvent(
		testTerminalPtr: Long,
		columns: Int,
		rows: Int,
		width: Int,
		height: Int,
	)

	@JvmStatic
	external fun testTerminalFree(testTerminalPtr: Long)

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
		val nativeLibraryUrl = Tty::class.java.getResource(nativeLibraryJarPath)
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
