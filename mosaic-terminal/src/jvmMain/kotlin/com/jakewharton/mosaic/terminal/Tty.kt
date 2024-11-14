package com.jakewharton.mosaic.terminal

import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption.REPLACE_EXISTING
import java.util.Locale.US

public actual object Tty {
	init {
		loadNativeLibrary("mosaic")
	}

	public actual fun enableRawMode(): AutoCloseable {
		val savedConfig = enterRawMode()
		if (savedConfig == 0L) throw OutOfMemoryError()
		return RawMode(savedConfig)
	}

	private class RawMode(
		private val savedPtr: Long,
	) : AutoCloseable {
		override fun close() {
			val error = exitRawMode(savedPtr)
			check(error == 0) { "Unable to exit raw mode: $error" }
		}
	}

	public actual fun stdinReader(): StdinReader {
		val reader = stdinReaderInit()
		if (reader == 0L) throw OutOfMemoryError()
		return StdinReader(reader)
	}

	@JvmSynthetic // Hide from Java callers.
	internal actual fun stdinWriter(): StdinWriter {
		val writer = stdinWriterInit()
		if (writer == 0L) throw OutOfMemoryError()
		val reader = stdinWriterGetReader(writer)
		return StdinWriter(writer, reader)
	}

	@JvmStatic
	private external fun enterRawMode(): Long

	@JvmStatic
	private external fun exitRawMode(savedConfig: Long): Int

	@JvmStatic
	private external fun stdinReaderInit(): Long

	@JvmStatic
	@JvmSynthetic // Hide from Java callers.
	@JvmName("stdinReaderRead") // Avoid internal name mangling.
	internal external fun stdinReaderRead(
		reader: Long,
		buffer: ByteArray,
		offset: Int,
		length: Int,
	): Int

	@JvmStatic
	@JvmSynthetic // Hide from Java callers.
	@JvmName("stdinReaderReadWithTimeout") // Avoid internal name mangling.
	internal external fun stdinReaderReadWithTimeout(
		reader: Long,
		buffer: ByteArray,
		offset: Int,
		length: Int,
		timeoutMillis: Int,
	): Int

	@JvmStatic
	@JvmSynthetic // Hide from Java callers.
	@JvmName("stdinReaderInterrupt") // Avoid internal name mangling.
	internal external fun stdinReaderInterrupt(reader: Long): Int

	@JvmStatic
	@JvmSynthetic // Hide from Java callers.
	@JvmName("stdinReaderFree") // Avoid internal name mangling.
	internal external fun stdinReaderFree(reader: Long): Int

	@JvmStatic
	private external fun stdinWriterInit(): Long

	@JvmStatic
	private external fun stdinWriterGetReader(writer: Long): Long

	@JvmStatic
	@JvmSynthetic // Hide from Java callers.
	@JvmName("stdinWriterWrite") // Avoid internal name mangling.
	internal external fun stdinWriterWrite(writer: Long, buffer: ByteArray)

	@JvmStatic
	@JvmSynthetic // Hide from Java callers.
	@JvmName("stdinWriterFree") // Avoid internal name mangling.
	internal external fun stdinWriterFree(writer: Long)

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

public actual class StdinReader internal constructor(
	private val readerPtr: Long,
) : AutoCloseable {
	public actual fun read(buffer: ByteArray, offset: Int, length: Int): Int {
		return Tty.stdinReaderRead(readerPtr, buffer, offset, length)
	}

	public actual fun readWithTimeout(buffer: ByteArray, offset: Int, length: Int, timeoutMillis: Int): Int {
		return Tty.stdinReaderReadWithTimeout(readerPtr, buffer, offset, length, timeoutMillis)
	}

	public actual fun interrupt() {
		Tty.stdinReaderInterrupt(readerPtr)
	}

	public actual override fun close() {
		Tty.stdinReaderFree(readerPtr)
	}
}

// TODO @JvmSynthetic https://youtrack.jetbrains.com/issue/KT-24981
internal actual class StdinWriter internal constructor(
	private val writerPtr: Long,
	readerPtr: Long,
) : AutoCloseable {
	actual val reader: StdinReader = StdinReader(readerPtr)

	actual fun write(buffer: ByteArray) {
		Tty.stdinWriterWrite(writerPtr, buffer)
	}

	actual override fun close() {
		Tty.stdinWriterFree(writerPtr)
	}
}
