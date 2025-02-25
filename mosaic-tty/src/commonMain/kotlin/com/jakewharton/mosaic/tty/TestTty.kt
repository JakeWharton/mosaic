package com.jakewharton.mosaic.tty

public expect class TestTty : AutoCloseable {
	public companion object {
		public fun create(callback: Tty.Callback): TestTty
	}

	public val tty: Tty

	public fun writeInput(buffer: ByteArray, offset: Int, count: Int): Int

	/**
	 * Read up to [count] bytes into [buffer] at [offset] from this instance's faked standard output
	 * stream.
	 *
	 * @throws IllegalStateException if this instance was not created with faked streams (currently
	 * always the case).
	 */
	public fun readOutput(buffer: ByteArray, offset: Int, count: Int): Int

	/**
	 * Read up to [count] bytes into [buffer] at [offset] from this instance's faked standard error
	 * stream.
	 *
	 * @throws IllegalStateException if this instance was not created with faked streams (currently
	 * always the case).
	 */
	public fun readError(buffer: ByteArray, offset: Int, count: Int): Int

	public fun focusEvent(focused: Boolean)
	public fun keyEvent()
	public fun mouseEvent()
	public fun resizeEvent(columns: Int, rows: Int, width: Int, height: Int)

	override fun close()
}
