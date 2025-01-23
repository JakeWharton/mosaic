package com.jakewharton.mosaic.terminal

import kotlinx.cinterop.CPointer
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.useContents
import kotlinx.cinterop.usePinned

public actual object Tty {
	public actual fun enableRawMode(): AutoCloseable {
		val savedConfig = enterRawMode().useContents {
			check(error == 0U) { "Unable to enable raw mode: $error" }
			saved ?: throw OutOfMemoryError()
		}
		return RawMode(savedConfig)
	}

	private class RawMode(
		private val savedConfig: CPointer<rawModeConfig>,
	) : AutoCloseable {
		override fun close() {
			val error = exitRawMode(savedConfig)
			if (error == 0U) return
			throwError(error)
		}
	}

	public actual fun stdinReader(): StdinReader {
		val reader = stdinReader_init().useContents {
			check(error == 0U) { "Unable to create stdin reader: $error" }
			reader ?: throw OutOfMemoryError()
		}
		return StdinReader(reader)
	}

	internal actual fun stdinWriter(): StdinWriter {
		val writer = stdinWriter_init().useContents {
			check(error == 0U) { "Unable to create stdin writer: $error" }
			writer ?: throw OutOfMemoryError()
		}
		val reader = stdinWriter_getReader(writer)!!
		return StdinWriter(writer, reader)
	}

	internal fun throwError(error: UInt): Nothing {
		throw RuntimeException(error.toString())
	}
}

public actual class StdinReader internal constructor(
	private var ref: CPointer<stdinReader>?,
) : AutoCloseable {
	public actual fun read(buffer: ByteArray, offset: Int, count: Int): Int {
		buffer.usePinned {
			stdinReader_read(ref, it.addressOf(offset), count).useContents {
				if (error == 0U) return this.count
				Tty.throwError(error)
			}
		}
	}

	public actual fun readWithTimeout(buffer: ByteArray, offset: Int, count: Int, timeoutMillis: Int): Int {
		buffer.usePinned {
			stdinReader_readWithTimeout(ref, it.addressOf(offset), count, timeoutMillis).useContents {
				if (error == 0U) return this.count
				Tty.throwError(error)
			}
		}
	}

	public actual fun interrupt() {
		val error = stdinReader_interrupt(ref)
		if (error == 0U) return
		Tty.throwError(error)
	}

	public actual override fun close() {
		ref?.let { ref ->
			this.ref = null

			val error = stdinReader_free(ref)
			if (error == 0U) return
			Tty.throwError(error)
		}
	}
}

internal actual class StdinWriter internal constructor(
	private var ref: CPointer<stdinWriter>?,
	readerRef: CPointer<stdinReader>,
) : AutoCloseable {
	actual val reader: StdinReader = StdinReader(readerRef)

	actual fun write(buffer: ByteArray) {
		val error = buffer.usePinned {
			stdinWriter_write(ref, it.addressOf(0), buffer.size)
		}
		if (error == 0U) return
		Tty.throwError(error)
	}

	actual override fun close() {
		ref?.let { ref ->
			this.ref = null

			reader.close()

			val error = stdinWriter_free(ref)

			if (error == 0U) return
			Tty.throwError(error)
		}
	}
}
