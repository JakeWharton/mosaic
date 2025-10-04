package com.jakewharton.mosaic.tty

// TODO rename this TestTerminal? TestHost? Something...
public expect class TestTty : AutoCloseable {
	public companion object {

		/**
		 * Initialize a [TestTty] instance. Only a single [TestTty] instance can be bound at a time,
		 * and only when a [Tty] is not also bound. Subsequent calls will throw an exception until
		 * [TestTty.close] is called.
		 *
		 * @param stdinIsTty Whether the standard input stream is the TTY.
		 * @param stdoutIsTty Whether the standard output stream is the TTY.
		 * @param stderrIsTty Whether the standard error stream is the TTY.
		 * @throws IOException If an error occurred creating the PTY.
		 * @throws IllegalStateException If another instance is already bound.
		 */
		public fun bind(
			stdinIsTty: Boolean = false,
			stdoutIsTty: Boolean = false,
			stderrIsTty: Boolean = false,
		): TestTty
	}

	public val streams: StandardStreams
	public val tty: Tty

	/**
	 * Write up to [count] bytes into [buffer] at [offset] to the PTY.
	 * The number of bytes written will be returned.
	 *
	 * @see Tty.read
	 * @see Tty.readWithTimeout
	 */
	public fun writeTty(buffer: ByteArray, offset: Int, count: Int): Int

	/**
	 * Read up to [count] bytes into [buffer] at [offset] from the PTY.
	 * The number of bytes read will be returned. 0 will be returned if [interruptTtyRead] is called
	 * while waiting for data.
	 *
	 * @see readTtyWithTimeout
	 * @see Tty.write
	 */
	public fun readTty(buffer: ByteArray, offset: Int, count: Int): Int

	/**
	 * Read up to [count] bytes into [buffer] at [offset] from the PTY.
	 * The number of bytes read will be returned. 0 will be returned if [interruptTtyRead] is called
	 * while waiting for data, or if at least [timeoutMillis] have passed without data.
	 *
	 * @param timeoutMillis A value of 0 will perform a non-blocking read. Otherwise, valid values
	 * are 1 to 999 which represent a maximum time (in milliseconds) to wait for data. Note: This
	 * value is not validated.
	 * @see readTty
	 * @see Tty.write
	 */
	public fun readTtyWithTimeout(buffer: ByteArray, offset: Int, count: Int, timeoutMillis: Int): Int

	/** Signal blocking calls to [readTty] or [readTtyWithTimeout] to wake up and return 0. */
	public fun interruptTtyRead()

	/**
	 * Write up to [count] bytes into [buffer] at [offset] to the standard input stream.
	 * If the standard input stream is set to the TTY, this is equivalent to [writeTty].
	 * The number of bytes written will be returned.
	 *
	 * @see StandardStreams.readInput
	 * @see StandardStreams.readInputWithTimeout
	 */
	public fun writeStandardInput(buffer: ByteArray, offset: Int, count: Int): Int

	/**
	 * Read up to [count] bytes into [buffer] at [offset] from the standard output stream.
	 * The number of bytes read will be returned. 0 will be returned if [interruptStandardOutputRead]
	 * is called while waiting for data.
	 *
	 * @see readStandardOutputWithTimeout
	 * @see StandardStreams.writeOutput
	 */
	public fun readStandardOutput(buffer: ByteArray, offset: Int, count: Int): Int

	/**
	 * Read up to [count] bytes into [buffer] at [offset] from the standard output stream.
	 * The number of bytes read will be returned. 0 will be returned if [interruptStandardOutputRead]
	 * is called while waiting for data, or if at least [timeoutMillis] have passed without data.
	 *
	 * @param timeoutMillis A value of 0 will perform a non-blocking read. Otherwise, valid values
	 * are 1 to 999 which represent a maximum time (in milliseconds) to wait for data. Note: This
	 * value is not validated.
	 * @see readStandardOutput
	 * @see StandardStreams.writeOutput
	 */
	public fun readStandardOutputWithTimeout(buffer: ByteArray, offset: Int, count: Int, timeoutMillis: Int): Int

	/**
	 * Signal blocking calls to [readStandardOutput] or [readStandardOutputWithTimeout] to wake up
	 * and return 0.
	 */
	public fun interruptStandardOutputRead()

	/**
	 * Read up to [count] bytes into [buffer] at [offset] from the standard error stream.
	 * The number of bytes read will be returned. 0 will be returned if [interruptStandardErrorRead]
	 * is called while waiting for data.
	 *
	 * @see readStandardErrorWithTimeout
	 * @see StandardStreams.writeError
	 */
	public fun readStandardError(buffer: ByteArray, offset: Int, count: Int): Int

	/**
	 * Read up to [count] bytes into [buffer] at [offset] from the standard error stream.
	 * The number of bytes read will be returned. 0 will be returned if [interruptStandardErrorRead]
	 * is called while waiting for data, or if at least [timeoutMillis] have passed without data.
	 *
	 * @param timeoutMillis A value of 0 will perform a non-blocking read. Otherwise, valid values
	 * are 1 to 999 which represent a maximum time (in milliseconds) to wait for data. Note: This
	 * value is not validated.
	 * @see readStandardError
	 * @see StandardStreams.writeError
	 */
	public fun readStandardErrorWithTimeout(buffer: ByteArray, offset: Int, count: Int, timeoutMillis: Int): Int

	/**
	 * Signal blocking calls to [readStandardError] or [readStandardErrorWithTimeout] to wake up
	 * and return 0.
	 */
	public fun interruptStandardErrorRead()

	/**
	 * Resize the TTY.
	 *
	 * This will change the value returned by [Tty.currentSize].
	 * If [Tty.enableWindowResizeEvents] was enabled, this will also cause [Tty.Callback.onResize]
	 * to be invoked.
	 */
	public fun resize(columns: Int, rows: Int, width: Int, height: Int)

	/**
	 * Send a focus event to [tty]'s callback.
	 *
	 * On Windows this event can only be observed by during calls to [Tty.read] or
	 * [Tty.readWithTimeout]. This event is not supported on other platforms.
	 */
	public fun sendFocusEvent(focused: Boolean)

	/**
	 * Send a key event to [tty]'s callback.
	 *
	 * Note: Currently this does not work.
	 *
	 * On Windows this event can only be observed by during calls to [Tty.read] or
	 * [Tty.readWithTimeout]. This event is not supported on other platforms.
	 */
	public fun sendKeyEvent()

	/**
	 * Send a mouse event to [tty]'s callback.
	 *
	 * Note: Currently this does not work.
	 *
	 * On Windows this event can only be observed by during calls to [Tty.read] or
	 * [Tty.readWithTimeout]. This event is not supported on other platforms.
	 */
	public fun sendMouseEvent()

	override fun close()
}
