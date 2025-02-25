package com.jakewharton.mosaic.tty

public expect class Tty : AutoCloseable {
	public companion object {
		public fun create(callback: Callback): Tty
	}

	/**
	 * Read up to [count] bytes into [buffer] at [offset] from the standard input stream.
	 * The number of bytes read will be returned. 0 will be returned if [interruptRead] is called
	 * while waiting for input. -1 will be returned if the input stream is closed.
	 *
	 * @see readInputWithTimeout
	 */
	public fun readInput(buffer: ByteArray, offset: Int, count: Int): Int

	/**
	 * Read up to [count] bytes into [buffer] at [offset] from the standard input stream.
	 * The number of bytes read will be returned. 0 will be returned if [interruptRead] is called
	 * while waiting for input, or if at least [timeoutMillis] have passed without data.
	 * -1 will be returned if the input stream is closed.
	 *
	 * @param timeoutMillis A value of 0 will perform a non-blocking read. Otherwise, valid values
	 * are 1 to 999 which represent a maximum time (in milliseconds) to wait for data. Note: This
	 * value is not validated.
	 * @see readInput
	 */
	public fun readInputWithTimeout(buffer: ByteArray, offset: Int, count: Int, timeoutMillis: Int): Int

	/** Signal blocking calls to [readInput] or [readInputWithTimeout] to wake up and return 0. */
	public fun interruptRead()

	/**
	 * Write up to [count] bytes from [buffer] at [offset] to the standard output stream.
	 * The number of bytes written will be returned.
	 */
	public fun writeOutput(buffer: ByteArray, offset: Int, count: Int): Int

	/**
	 * Write up to [count] bytes from [buffer] at [offset] to the standard error stream.
	 * The number of bytes written will be returned.
	 */
	public fun writeError(buffer: ByteArray, offset: Int, count: Int): Int

	public fun enableRawMode()

	public fun enableWindowResizeEvents()

	/** @return Array of `[columns, rows, width, height]` */
	public fun currentSize(): IntArray

	/**
	 * Free the resources associated with this reader.
	 *
	 * This call can be omitted if your process is exiting.
	 */
	override fun close()

	public interface Callback {
		public fun onFocus(focused: Boolean)
		public fun onKey()
		public fun onMouse()
		public fun onResize(columns: Int, rows: Int, width: Int, height: Int)
	}
}
