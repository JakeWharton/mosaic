package com.jakewharton.mosaic.tty

public expect class Tty : AutoCloseable {
	public companion object {
		public fun create(callback: Callback): Tty
	}

	/**
	 * Read up to [count] bytes into [buffer] at [offset]. The number of bytes read will be returned.
	 * 0 will be returned if [interrupt] is called while waiting for input. -1 will be returned if
	 * the input stream is closed.
	 *
	 * @see readWithTimeout
	 */
	public fun read(buffer: ByteArray, offset: Int, count: Int): Int

	/**
	 * Read up to [count] bytes into [buffer] at [offset]. The number of bytes read will be returned.
	 * 0 will be returned if [interrupt] is called while waiting for input, or if at least
	 * [timeoutMillis] have passed without data. -1 will be returned if the input stream is closed.
	 *
	 * @param timeoutMillis A value of 0 will perform a non-blocking read. Otherwise, valid values
	 * are 1 to 999 which represent a maximum time (in milliseconds) to wait for data. Note: This
	 * value is not validated.
	 * @see read
	 */
	public fun readWithTimeout(buffer: ByteArray, offset: Int, count: Int, timeoutMillis: Int): Int

	/** Signal blocking calls to [read] to wake up and return 0. */
	public fun interrupt()

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
