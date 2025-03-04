package com.jakewharton.mosaic.tty

public expect class TestTty : AutoCloseable {
	public companion object {
		public fun create(): TestTty
	}

	public val tty: Tty

	// TODO Take ByteString once it migrates to stdlib,
	//  or if Sink/RawSink migrates expose that as a val.
	//  https://github.com/Kotlin/kotlinx-io/issues/354
	public fun write(buffer: ByteArray)

	/**
	 * Send a focus event to [tty]'s callback.
	 *
	 * On Windows this event can only be observed by during calls to [Tty.readInput] or
	 * [Tty.readInputWithTimeout]. This event is not supported on other platforms.
	 */
	public fun focusEvent(focused: Boolean)

	/**
	 * Send a key event to [tty]'s callback.
	 *
	 * Note: Currently this does not work.
	 *
	 * On Windows this event can only be observed by during calls to [Tty.readInput] or
	 * [Tty.readInputWithTimeout]. This event is not supported on other platforms.
	 */
	public fun keyEvent()

	/**
	 * Send a mouse event to [tty]'s callback.
	 *
	 * Note: Currently this does not work.
	 *
	 * On Windows this event can only be observed by during calls to [Tty.readInput] or
	 * [Tty.readInputWithTimeout]. This event is not supported on other platforms.
	 */
	public fun mouseEvent()

	/**
	 * Send a resize event to [tty]'s callback.
	 *
	 * On Windows this event can only be observed by during calls to [Tty.readInput] or
	 * [Tty.readInputWithTimeout]. On other platforms this is delivered to the callback synchronously.
	 */
	public fun resizeEvent(columns: Int, rows: Int, width: Int, height: Int)

	override fun close()
}
