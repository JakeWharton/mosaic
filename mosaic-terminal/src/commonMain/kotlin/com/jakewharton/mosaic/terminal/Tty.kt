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
	internal fun platformInputWriter(): PlatformInputWriter
}
