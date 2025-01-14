package com.jakewharton.mosaic.terminal

import com.jakewharton.mosaic.terminal.Jni.enterRawMode
import com.jakewharton.mosaic.terminal.Jni.exitRawMode
import com.jakewharton.mosaic.terminal.Jni.stdinReaderFree
import com.jakewharton.mosaic.terminal.Jni.stdinReaderInit
import com.jakewharton.mosaic.terminal.Jni.stdinReaderInterrupt
import com.jakewharton.mosaic.terminal.Jni.stdinReaderRead
import com.jakewharton.mosaic.terminal.Jni.stdinReaderReadWithTimeout
import com.jakewharton.mosaic.terminal.Jni.stdinWriterFree
import com.jakewharton.mosaic.terminal.Jni.stdinWriterGetReader
import com.jakewharton.mosaic.terminal.Jni.stdinWriterInit
import com.jakewharton.mosaic.terminal.Jni.stdinWriterWrite

public actual object Tty {
	@JvmStatic
	public actual fun enableRawMode(): AutoCloseable {
		val savedConfig = enterRawMode()
		if (savedConfig == 0L) throw OutOfMemoryError()
		return RawMode(savedConfig)
	}

	private class RawMode(
		private val savedPtr: Long,
	) : AutoCloseable {
		override fun close() {
			exitRawMode(savedPtr)
		}
	}

	@JvmStatic
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
}

public actual class StdinReader internal constructor(
	private val readerPtr: Long,
) : AutoCloseable {
	public actual fun read(buffer: ByteArray, offset: Int, count: Int): Int {
		return stdinReaderRead(readerPtr, buffer, offset, count)
	}

	public actual fun readWithTimeout(buffer: ByteArray, offset: Int, count: Int, timeoutMillis: Int): Int {
		return stdinReaderReadWithTimeout(readerPtr, buffer, offset, count, timeoutMillis)
	}

	public actual fun interrupt() {
		stdinReaderInterrupt(readerPtr)
	}

	public actual override fun close() {
		stdinReaderFree(readerPtr)
	}
}

// TODO @JvmSynthetic https://youtrack.jetbrains.com/issue/KT-24981
internal actual class StdinWriter internal constructor(
	private val writerPtr: Long,
	readerPtr: Long,
) : AutoCloseable {
	actual val reader: StdinReader = StdinReader(readerPtr)

	actual fun write(buffer: ByteArray) {
		stdinWriterWrite(writerPtr, buffer)
	}

	actual override fun close() {
		stdinWriterFree(writerPtr)
	}
}
