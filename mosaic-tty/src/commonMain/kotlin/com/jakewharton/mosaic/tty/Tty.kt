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

	/**
	 * Save the current terminal settings and enter "raw" mode.
	 *
	 * Raw mode is described as "input is available character by character, echoing is disabled,
	 * and all special processing of terminal input and output characters is disabled."
	 *
	 * The saved settings can be restored by calling [close][AutoCloseable.close] on
	 * the returned instance.
	 *
	 * See [`termios(3)`](https://linux.die.net/man/3/termios) for more information.
	 *
	 * In addition to the flags required for entering "raw" mode, on POSIX-compliant platforms,
	 * this function will change the standard input stream to block indefinitely until a minimum
	 * of 1 byte is available to read. This allows the reader thread to fully be suspended rather
	 * than consuming CPU. Use [readInput] or [readInputWithTimeout] to read in a manner that can
	 * still be interrupted by [interruptRead].
	 */
	public fun enableRawMode()

	/**
	 * Use platform-specific window monitoring to call [Callback.onResize] when the OS determines
	 * the terminal window size has changed.
	 *
	 * Note: Before enabling this, consider querying the terminal for support of
	 * [mode 2048 in-band resize events](https://gist.github.com/rockorager/e695fb2924d36b2bcf1fff4a3704bd83)
	 * which are more reliable. Mode 2048 events are also parsed and sent as [ResizeEvent]s.
	 *
	 * On Windows this enables receiving
	 * [`WINDOW_BUFFER_SIZE_RECORD`](https://learn.microsoft.com/en-us/windows/console/window-buffer-size-record-str)
	 * records from the console. Only the row and column values of the [ResizeEvent] will be present.
	 * The width and height will always be 0.
	 *
	 * On Linux and macOS this installs a `SIGWINCH` signal handler which then queries `TIOCGWINSZ`
	 * using `ioctl`.
	 *
	 * Note: You can also respond to resize events which lack necessary data by sending `XTWINOPS`
	 * to query row/col counts and/or window or cell size in pixels. More details
	 * [here](https://invisible-island.net/xterm/ctlseqs/ctlseqs.html#h4-Functions-using-CSI-_-ordered-by-the-final-character-lparen-s-rparen:CSI-Ps;Ps;Ps-t:Ps-=-1-4.2064).
	 */
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
