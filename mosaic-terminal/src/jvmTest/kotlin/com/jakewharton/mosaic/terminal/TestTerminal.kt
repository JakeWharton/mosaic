package com.jakewharton.mosaic.terminal

internal actual class TestTerminal : AutoCloseable {
	actual val reader: TerminalReader get() = TODO()

	actual fun write(buffer: ByteArray): Unit = TODO()
	actual override fun close(): Unit = TODO()

	actual companion object {
		actual fun create(): TestTerminal = TODO()
	}
}
