package com.jakewharton.mosaic.tty

public expect class StandardStreams : AutoCloseable {
	public companion object {
		public fun bind(): StandardStreams
	}

	public fun isInputTty(): Boolean
	public fun isOutputTty(): Boolean
	public fun isErrorTty(): Boolean

	public fun readInput(buffer: ByteArray, offset: Int, count: Int): Int
	public fun readInputWithTimeout(buffer: ByteArray, offset: Int, count: Int, timeoutMillis: Int): Int
	public fun interruptInputRead()

	public fun writeOutput(buffer: ByteArray, offset: Int, count: Int): Int
	public fun writeError(buffer: ByteArray, offset: Int, count: Int): Int

	override fun close()
}
