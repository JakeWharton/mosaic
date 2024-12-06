package com.jakewharton.mosaic.terminal

import com.jakewharton.mosaic.terminal.Tty.enableRawMode
import com.jakewharton.mosaic.terminal.Tty.stdinReader
import com.jakewharton.mosaic.terminal.event.ResizeEvent

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
	 * than consuming CPU. Use [stdinReader] to read in a manner that can still be interrupted.
	 */
	public fun enableRawMode(): AutoCloseable

	/**
	 * Create a [StdinReader] which will read from this process' stdin stream while also
	 * supporting interruption.
	 *
	 * Use with [enableRawMode] to read input byte-by-byte.
	 */
	public fun stdinReader(): StdinReader

	internal fun stdinWriter(): StdinWriter
}

public expect class StdinReader : AutoCloseable {
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

	/**
	 * Enable standalone terminal resize reporting.
	 *
	 * In-band resize reporting is accomplished via [TerminalParser] and enabling
	 * [mode 2048](https://gist.github.com/rockorager/e695fb2924d36b2bcf1fff4a3704bd83) which will be
	 * delivered as [ResizeEvent]s. If that mode is not supported by the terminal, however,
	 * standalone reporting can be enabled by passing a non-`null` [ResizeListener] to this function.
	 * Pass `null` or call [close] to stop callbacks.
	 *
	 * Once enabled, the behavior of this function varies by operating system:
	 *
	 * - On Windows, resize callbacks may be delivered to the [listener] during calls to [read]
	 *   prior to it actually reading bytes. The [listener] will be invoked on the callers thread.
	 *
	 * - On Linux and macOS, resize events can occur at any time and will be delivered to [listener]
	 *   on an arbitrary thread.
	 *
	 * On every operating system it is crucial that calls to [listener] complete quickly and do not
	 * throw exceptions.
	 */
	public fun setResizeListener(listener: ResizeListener?)

	/** Signal an active, blocking call to [read] or [readWithTimeout] to wake up and return 0. */
	public fun interrupt()

	/**
	 * Free the resources associated with this reader.
	 *
	 * This call can be omitted if your process is exiting.
	 */
	override fun close()
}

/** Callback for [StdinReader.setResizeListener]. */
public fun interface ResizeListener {
	/**
	 * Invoked when the terminal size has changed.
	 *
	 * Implementations of this function should complete quickly and must not throw exceptions.
	 */
	public fun onResize(rows: Int, columns: Int, height: Int, width: Int)
}

internal expect class StdinWriter : AutoCloseable {
	val reader: StdinReader

	// TODO Take ByteString once it migrates to stdlib,
	//  or if Sink/RawSink migrates expose that as a val.
	//  https://github.com/Kotlin/kotlinx-io/issues/354
	fun write(buffer: ByteArray)

	override fun close()
}
