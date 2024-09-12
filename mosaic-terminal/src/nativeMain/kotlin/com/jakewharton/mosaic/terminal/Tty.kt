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
}

@OptIn(ExperimentalForeignApi::class)
public actual class StdinReader internal constructor(
	private val ref: CPointer<stdinReader>,
) : AutoCloseable {
	public actual fun read(buffer: ByteArray, offset: Int, length: Int): Int {
		buffer.usePinned {
			stdinReader_read(ref, it.addressOf(offset), length).useContents {
				if (error != 0U) throw RuntimeException(error.toString())
				return count
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
