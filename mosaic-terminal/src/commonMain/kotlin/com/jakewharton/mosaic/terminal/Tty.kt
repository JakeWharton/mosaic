package com.jakewharton.mosaic.terminal

import com.jakewharton.mosaic.terminal.event.DebugEvent

public expect object Tty {
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
}
