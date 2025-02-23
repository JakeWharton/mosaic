package com.jakewharton.mosaic.terminal

import com.jakewharton.mosaic.terminal.Tty.terminalReader

public expect class RawTerminal : AutoCloseable {
	public fun read(buffer: ByteArray, offset: Int, count: Int): Int
	public fun read(buffer: ByteArray, offset: Int, count: Int, timeoutMillis: Int): Int
	public fun interruptRead()

// 	public fun writeOutput(bytes: ByteArray, offset: Int, count: Int): Int
// 	public fun writeError(bytes: ByteArray, offset: Int, count: Int): Int

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
	public fun enableRawMode()

	// public fun enableStandardStreamRedirection()

	public fun enableNativeResizeEvents()
	public fun currentSize(): IntArray

	override fun close()

	public interface EventCallback {
		public fun onFocus(focused: Boolean)
		public fun onKey() // TODO
		public fun onMouse() // TODO
		public fun onResize(columns: Int, rows: Int, width: Int, height: Int)
		// public actual fun onStandardOutput(bytes: ByteArray)
		// public actual fun onStandardError(bytes: ByteArray)
	}

	public companion object {
		public fun initialize(callback: EventCallback): RawTerminal
	}
}
