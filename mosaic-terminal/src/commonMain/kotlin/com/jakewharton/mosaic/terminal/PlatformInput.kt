package com.jakewharton.mosaic.terminal

internal expect class PlatformInput : AutoCloseable {
	companion object {
		fun create(callback: Callback): PlatformInput
	}

	/**
	 * Read up to [count] bytes into [buffer] at [offset]. The number of bytes read will be returned.
	 * 0 will be returned if [interrupt] is called while waiting for input. -1 will be returned if
	 * the input stream is closed.
	 *
	 * @see readWithTimeout
	 */
	fun read(buffer: ByteArray, offset: Int, count: Int): Int

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
	fun readWithTimeout(buffer: ByteArray, offset: Int, count: Int, timeoutMillis: Int): Int

	/** Signal blocking calls to [read] to wake up and return 0. */
	fun interrupt()

	fun enableRawMode()

	fun enableWindowResizeEvents()

	/** @return Array of `[columns, rows, width, height]` */
	fun currentSize(): IntArray

	/**
	 * Free the resources associated with this reader.
	 *
	 * This call can be omitted if your process is exiting.
	 */
	override fun close()

	interface Callback {
		fun onFocus(focused: Boolean)
		fun onKey()
		fun onMouse()
		fun onResize(columns: Int, rows: Int, width: Int, height: Int)
	}
}
