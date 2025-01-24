package com.jakewharton.mosaic.terminal

// TODO @JvmSynthetic https://youtrack.jetbrains.com/issue/KT-24981
internal actual class PlatformInput internal constructor(
	private var readerPtr: Long,
	private val handlerPtr: Long,
) : AutoCloseable {
	actual fun read(buffer: ByteArray, offset: Int, count: Int): Int {
		return Jni.stdinReaderRead(readerPtr, buffer, offset, count)
	}

	actual fun readWithTimeout(buffer: ByteArray, offset: Int, count: Int, timeoutMillis: Int): Int {
		return Jni.stdinReaderReadWithTimeout(readerPtr, buffer, offset, count, timeoutMillis)
	}

	actual fun interrupt() {
		Jni.stdinReaderInterrupt(readerPtr)
	}

	actual override fun close() {
		if (readerPtr != 0L) {
			Jni.stdinReaderFree(readerPtr)
			readerPtr = 0
			Jni.platformEventHandlerFree(handlerPtr)
		}
	}
}
