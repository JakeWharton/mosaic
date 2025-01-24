package com.jakewharton.mosaic.terminal

import kotlinx.cinterop.CPointer
import kotlinx.cinterop.StableRef
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.free
import kotlinx.cinterop.nativeHeap
import kotlinx.cinterop.useContents
import kotlinx.cinterop.usePinned

internal actual class PlatformInput internal constructor(
	ptr: CPointer<stdinReader>,
	private val handlerPtr: CPointer<platformEventHandler>?,
	private val handlerRef: StableRef<PlatformEventHandler>?,
) : AutoCloseable {
	private var ptr: CPointer<stdinReader>? = ptr

	actual fun read(buffer: ByteArray, offset: Int, count: Int): Int {
		buffer.usePinned {
			stdinReader_read(ptr, it.addressOf(offset), count).useContents {
				if (error == 0U) return this.count
				Tty.throwError(error)
			}
		}
	}

	actual fun readWithTimeout(buffer: ByteArray, offset: Int, count: Int, timeoutMillis: Int): Int {
		buffer.usePinned {
			stdinReader_readWithTimeout(ptr, it.addressOf(offset), count, timeoutMillis).useContents {
				if (error == 0U) return this.count
				Tty.throwError(error)
			}
		}
	}

	actual fun interrupt() {
		val error = stdinReader_interrupt(ptr)
		if (error == 0U) return
		Tty.throwError(error)
	}

	actual override fun close() {
		ptr?.let { ptr ->
			this.ptr = null

			val error = stdinReader_free(ptr)
			handlerPtr?.let(nativeHeap::free)
			handlerRef?.dispose()

			if (error == 0U) return
			Tty.throwError(error)
		}
	}
}
