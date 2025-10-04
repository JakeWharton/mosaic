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

	public fun interceptOtherWrites(): InterceptedStreams

	override fun close()

	public class InterceptedStreams : AutoCloseable {
		public fun readOutput(buffer: ByteArray, offset: Int, count: Int): Int
		public fun readOutputWithTimeout(buffer: ByteArray, offset: Int, count: Int, timeoutMillis: Int): Int
		public fun interruptOutputRead()

		public fun readError(buffer: ByteArray, offset: Int, count: Int): Int
		public fun readErrorWithTimeout(buffer: ByteArray, offset: Int, count: Int, timeoutMillis: Int): Int
		public fun interruptErrorRead()

		override fun close()
	}
}
