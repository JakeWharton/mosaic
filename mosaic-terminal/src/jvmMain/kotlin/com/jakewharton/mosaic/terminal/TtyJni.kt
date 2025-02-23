package com.jakewharton.mosaic.terminal

import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption.REPLACE_EXISTING
import java.util.Locale.US

// TODO @JvmSynthetic https://youtrack.jetbrains.com/issue/KT-24981
internal object TtyJni {
	init {
		loadNativeLibrary("mosaic-tty")
	}

	@JvmStatic
	external fun platformEventHandlerInit(handler: PlatformEventHandler): Long

	@JvmStatic
	external fun platformEventHandlerFree(handlerPtr: Long)

	@JvmStatic
	external fun platformInputInit(handlerPtr: Long): Long

	@JvmStatic
	external fun platformInputRead(
		inputPtr: Long,
		buffer: ByteArray,
		offset: Int,
		count: Int,
	): Int

	@JvmStatic
	external fun platformInputReadWithTimeout(
		inputPtr: Long,
		buffer: ByteArray,
		offset: Int,
		count: Int,
		timeoutMillis: Int,
	): Int

	@JvmStatic
	external fun platformInputInterrupt(inputPtr: Long)

	@JvmStatic
	external fun platformInputEnableRawMode(inputPtr: Long)

	@JvmStatic
	external fun platformInputEnableWindowResizeEvents(inputPtr: Long)

	/**
	 * @return Array of `[columns, rows, width, height]`. Using an array saves us from having to
	 * pass a complex object across the JNI boundary.
	 */
	@JvmStatic
	external fun platformInputCurrentSize(inputPtr: Long): IntArray

	@JvmStatic
	external fun platformInputFree(inputPtr: Long)

	@Suppress(
		// Only loading from our own JAR contents.
		"UnsafeDynamicallyLoadedCode",
	)
	internal fun loadNativeLibrary(name: String) {
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
