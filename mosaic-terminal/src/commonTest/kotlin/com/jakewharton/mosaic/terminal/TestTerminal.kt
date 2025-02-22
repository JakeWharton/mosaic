package com.jakewharton.mosaic.terminal

internal expect class TestTerminal : AutoCloseable {
	val reader: TerminalReader

	fun write(buffer: ByteArray)
	override fun close()

	companion object {
		fun create(): TestTerminal
	}
}
