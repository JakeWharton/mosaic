package com.jakewharton.mosaic.tty

public expect class TestTty : AutoCloseable {
	public companion object {

		/**
		 * Initialize a [TestTty] instance. Only a single [TestTty] instance can be bound at a time,
		 * and only when a [Tty] is not also bound. Subsequent calls will throw an exception until
		 * [TestTty.close] is called.
		 *
		 * @throws IOException If an error occurred creating the PTY.
		 * @throws IllegalStateException If another instance is already bound.
		 */
		public fun bind(): TestTty
	}

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
	 * Send a focus event to [tty]'s callback.
	 *
	 * On Windows this event can only be observed by during calls to [Tty.read] or
	 * [Tty.readWithTimeout]. This event is not supported on other platforms.
	 */
	public fun focusEvent(focused: Boolean)

	/**
	 * Send a key event to [tty]'s callback.
	 *
	 * Note: Currently this does not work.
	 *
	 * On Windows this event can only be observed by during calls to [Tty.read] or
	 * [Tty.readWithTimeout]. This event is not supported on other platforms.
	 */
	public fun keyEvent()

	/**
	 * Send a mouse event to [tty]'s callback.
	 *
	 * Note: Currently this does not work.
	 *
	 * On Windows this event can only be observed by during calls to [Tty.read] or
	 * [Tty.readWithTimeout]. This event is not supported on other platforms.
	 */
	public fun mouseEvent()

	/**
	 * Send a resize event to [tty]'s callback.
	 *
	 * On Windows this event can only be observed by during calls to [Tty.read] or
	 * [Tty.readWithTimeout]. On other platforms this is delivered to the callback synchronously.
	 */
	public fun resizeEvent(columns: Int, rows: Int, width: Int, height: Int)

	override fun close()
}
