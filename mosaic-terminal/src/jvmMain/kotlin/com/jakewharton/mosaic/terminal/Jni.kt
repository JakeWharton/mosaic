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
	external fun enterRawMode(): Long

	@JvmStatic
	external fun exitRawMode(savedConfig: Long)

	@JvmStatic
	external fun platformEventHandlerInit(handler: PlatformEventHandler): Long

	@JvmStatic
	external fun platformEventHandlerFree(handler: Long)

	@JvmStatic
	external fun platformInputInit(handler: Long): Long

	@JvmStatic
	external fun platformInputRead(
		reader: Long,
		buffer: ByteArray,
		offset: Int,
		count: Int,
	): Int

	@JvmStatic
	external fun platformInputReadWithTimeout(
		reader: Long,
		buffer: ByteArray,
		offset: Int,
		count: Int,
		timeoutMillis: Int,
	): Int

	@JvmStatic
	external fun platformInputInterrupt(reader: Long)

	@JvmStatic
	external fun platformInputFree(reader: Long)

	@JvmStatic
	external fun platformInputWriterInit(handler: Long): Long

	@JvmStatic
	external fun platformInputWriterGetReader(writer: Long): Long

	@JvmStatic
	external fun platformInputWriterWrite(writer: Long, buffer: ByteArray)

	@JvmStatic
	external fun platformInputWriterFocusEvent(writer: Long, focused: Boolean)

	@JvmStatic
	external fun platformInputWriterKeyEvent(writer: Long)

	@JvmStatic
	external fun platformInputWriterMouseEvent(writer: Long)

	@JvmStatic
	external fun platformInputWriterResizeEvent(
		writer: Long,
		columns: Int,
		rows: Int,
		width: Int,
		height: Int,
	)

	@JvmStatic
	external fun platformInputWriterFree(writer: Long)

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
