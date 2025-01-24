package com.jakewharton.mosaic.terminal

import com.jakewharton.mosaic.terminal.event.DebugEvent

public expect object Tty {
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
	 * than consuming CPU. Use [terminalReader] to read in a manner that can still be interrupted.
	 */
	public fun enableRawMode(): AutoCloseable

	/**
	 * Create a [TerminalReader] which will read from this process' stdin stream while also
	 * supporting interruption.
	 *
	 * Use with [enableRawMode] to read input byte-by-byte.
	 *
	 * @param emitDebugEvents When true, each event sent to [TerminalReader.events] will be followed
	 * by a [DebugEvent] that contains the original event and the bytes which produced it.
	 */
	public fun terminalReader(emitDebugEvents: Boolean = false): TerminalReader

	@TestApi
	internal fun stdinWriter(emitDebugEvents: Boolean = false): StdinWriter
}

internal expect class PlatformInput : AutoCloseable {
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

	/**
	 * Free the resources associated with this reader.
	 *
	 * This call can be omitted if your process is exiting.
	 */
	override fun close()
}

@TestApi
internal expect class StdinWriter : AutoCloseable {
	val reader: TerminalReader

	// TODO Take ByteString once it migrates to stdlib,
	//  or if Sink/RawSink migrates expose that as a val.
	//  https://github.com/Kotlin/kotlinx-io/issues/354
	fun write(buffer: ByteArray)

	fun focusEvent(focused: Boolean)
	fun keyEvent()
	fun mouseEvent()
	fun resizeEvent(columns: Int, rows: Int, width: Int, height: Int)

	override fun close()
}
