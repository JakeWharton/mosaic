package com.jakewharton.mosaic.tty

public expect class TestTty : AutoCloseable {
	public companion object {
		public fun create(): TestTty
	}

	public val tty: Tty

	/**
	 * Write up to [count] bytes into [buffer] at [offset] to the PTY.
	 * The number of bytes read will be returned.
	 *
	 * @see Tty.read
	 * @see Tty.readWithTimeout
	 */
	public fun write(buffer: ByteArray, offset: Int, count: Int): Int

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
