package com.jakewharton.mosaic

import platform.posix.O_APPEND
import platform.posix.O_WRONLY
import platform.posix.close
import platform.posix.open

internal actual fun createDefaultLogWriter(logPath: String): MosaicLogWriter? {
	val fileDescriptor = open(logPath, O_WRONLY or O_APPEND)
	if (fileDescriptor < 0) {
		return null
	}
	return object : MosaicLogWriter {

		override fun writeLine(message: String) {
			write(fileDescriptor, message)
			write(fileDescriptor, "\n")
		}

		override fun close() {
			close(fileDescriptor)
		}
	}
}

internal expect inline fun write(fileDescriptor: Int, str: String)
