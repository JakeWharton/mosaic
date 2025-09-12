package com.jakewharton.mosaic.tty

public expect class StandardStreams : AutoCloseable {
	public companion object {
		public fun bind(): StandardStreams
	}

	public fun isInputTty(): Boolean
	public fun isOutputTty(): Boolean
	public fun isErrorTty(): Boolean

	override fun close()
}
