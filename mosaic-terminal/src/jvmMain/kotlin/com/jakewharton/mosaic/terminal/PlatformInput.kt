package com.jakewharton.mosaic.terminal

// TODO @JvmSynthetic https://youtrack.jetbrains.com/issue/KT-24981
internal actual class PlatformInput(
	private var readerPtr: Long,
	private val handlerPtr: Long,
) : AutoCloseable {
	actual fun read(buffer: ByteArray, offset: Int, count: Int): Int {
		return Jni.platformInputRead(readerPtr, buffer, offset, count)
	}

	actual fun readWithTimeout(buffer: ByteArray, offset: Int, count: Int, timeoutMillis: Int): Int {
		return Jni.platformInputReadWithTimeout(readerPtr, buffer, offset, count, timeoutMillis)
	}

	actual fun interrupt() {
		Jni.platformInputInterrupt(readerPtr)
	}

	actual override fun close() {
		if (readerPtr != 0L) {
			Jni.platformInputFree(readerPtr)
			readerPtr = 0
			Jni.platformEventHandlerFree(handlerPtr)
		}
	}
}
