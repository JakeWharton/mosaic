package com.jakewharton.mosaic.terminal

import kotlinx.cinterop.CPointer
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.useContents
import kotlinx.cinterop.usePinned

@OptIn(ExperimentalForeignApi::class)
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
			check(error == 0U) { "Unable to exit raw mode: $error" }
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
}

@OptIn(ExperimentalForeignApi::class)
public actual class StdinReader internal constructor(
	private val ref: CPointer<stdinReader>,
) : AutoCloseable {
	public actual fun read(buffer: ByteArray, offset: Int, length: Int): Int {
		buffer.usePinned {
			stdinReader_read(ref, it.addressOf(offset), length).useContents {
				if (error == 0U) return count
				throw RuntimeException(error.toString())
			}
		}
	}

	public actual fun readWithTimeout(buffer: ByteArray, offset: Int, length: Int, timeoutMillis: Int): Int {
		buffer.usePinned {
			stdinReader_readWithTimeout(ref, it.addressOf(offset), length, timeoutMillis).useContents {
				if (error == 0U) return count
				throw RuntimeException(error.toString())
			}
		}
	}

	public actual fun interrupt() {
		stdinReader_interrupt(ref)
	}

	public actual override fun close() {
		stdinReader_free(ref)
	}
}

@OptIn(ExperimentalForeignApi::class)
internal actual class StdinWriter internal constructor(
	private val ref: CPointer<stdinWriter>,
	readerRef: CPointer<stdinReader>,
) : AutoCloseable {
	actual val reader: StdinReader = StdinReader(readerRef)

	actual fun write(buffer: ByteArray) {
		buffer.usePinned {
			stdinWriter_write(ref, it.addressOf(0), buffer.size)
		}
	}

	actual override fun close() {
		stdinWriter_free(ref)
	}
}
