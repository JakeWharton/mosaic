package com.jakewharton.mosaic

import java.io.Writer
import java.nio.file.StandardOpenOption
import kotlin.io.path.Path
import kotlin.io.path.writer

internal actual fun createDefaultLogWriter(logPath: String): MosaicLogWriter? {
	return object : MosaicLogWriter {

		private val writer: Writer = Path(logPath).writer(
			options = arrayOf(StandardOpenOption.WRITE, StandardOpenOption.APPEND),
		)

		override fun writeLine(message: String) {
			writer.write(message)
			writer.write("\n")
			writer.flush()
		}

		override fun close() {
			writer.close()
		}
	}
}
