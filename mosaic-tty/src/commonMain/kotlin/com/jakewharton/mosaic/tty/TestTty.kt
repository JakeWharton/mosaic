package com.jakewharton.mosaic.tty

public expect class TestTty : AutoCloseable {
	public companion object {

		/**
		 * Initialize a [TestTty] instance. Only a single [TestTty] instance can be bound at a time,
		 * and only when a [Tty] is not also bound. Subsequent calls will throw an exception until
		 * [TestTty.close] is called.
		 *
		 * @param stdinIsTty The return value of [StandardStreams.isInputTty].
		 * @param stdoutIsTty The return value of [StandardStreams.isOutputTty].
		 * @param stderrIsTty The return value of [StandardStreams.isErrorTty].
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
	public fun write(buffer: ByteArray, offset: Int, count: Int): Int

	/**
	 * Read up to [count] bytes into [buffer] at [offset] from the PTY.
	 * The number of bytes read will be returned.
	 *
	 * @see Tty.write
	 */
	public fun read(buffer: ByteArray, offset: Int, count: Int): Int

	/** Signal blocking calls to [read] to wake up and return 0. */
	public fun interruptRead()

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
