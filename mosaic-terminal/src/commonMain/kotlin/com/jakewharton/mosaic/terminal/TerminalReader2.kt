package com.jakewharton.mosaic.terminal

import com.jakewharton.mosaic.terminal.event.DebugEvent
import com.jakewharton.mosaic.terminal.event.Event
import com.jakewharton.mosaic.terminal.event.ResizeEvent
import com.jakewharton.mosaic.tty.Tty
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.Channel.Factory.UNLIMITED
import kotlinx.coroutines.channels.ReceiveChannel

/**
 * Create a [TerminalReader] which will read from this process' stdin stream while also
 * supporting interruption.
 *
 * Use with [enableRawMode] to read input byte-by-byte.
 *
 * @param emitDebugEvents When true, each event sent to [TerminalReader.events] will be followed
 * by a [DebugEvent] that contains the original event and the bytes which produced it.
 */
public fun TerminalReader(emitDebugEvents: Boolean = false): TerminalReader {
	val events = Channel<Event>(UNLIMITED)
	val callback = EventChannelTtyCallback(events, emitDebugEvents)
	val tty = Tty.create(callback)
	val parser = TerminalParser(tty)
	return TerminalReader(tty, parser, events, emitDebugEvents)
}

public class TerminalReader internal constructor(
	public val tty: Tty,
	private val parser: TerminalParser,
	events: Channel<Event>,
	private val emitDebugEvents: Boolean,
) : AutoCloseable {
	private val _events = events

	/** Events read as a result of calls to [tryReadEvents]. */
	public val events: ReceiveChannel<Event> get() = _events

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
	 * than consuming CPU. Use [create] to read in a manner that can still be interrupted.
	 */
	public fun enableRawMode() {
		tty.enableRawMode()
	}

	/**
	 * Write [ResizeEvent]s into [events] using platform-specific window monitoring.
	 *
	 * Note: Before enabling this, consider querying the terminal for support of
	 * [mode 2048 in-band resize events](https://gist.github.com/rockorager/e695fb2924d36b2bcf1fff4a3704bd83)
	 * which are more reliable. Mode 2048 events are also parsed and sent as [ResizeEvent]s.
	 *
	 * On Windows this enables receiving
	 * [`WINDOW_BUFFER_SIZE_RECORD`](https://learn.microsoft.com/en-us/windows/console/window-buffer-size-record-str)
	 * records from the console. Only the row and column values of the [ResizeEvent] will be present.
	 * The width and height will always be 0.
	 *
	 * On Linux and macOS this installs a `SIGWINCH` signal handler which then queries `TIOCGWINSZ`
	 * using `ioctl`.
	 *
	 * Note: You can also respond to [ResizeEvent]s which lack necessary data by sending `XTWINOPS`
	 * to query row/col counts and/or window or cell size in pixels. More details
	 * [here](https://invisible-island.net/xterm/ctlseqs/ctlseqs.html#h4-Functions-using-CSI-_-ordered-by-the-final-character-lparen-s-rparen:CSI-Ps;Ps;Ps-t:Ps-=-1-4.2064).
	 */
	public fun enableWindowResizeEvents() {
		tty.enableWindowResizeEvents()
	}

	/** Synchronously query for the current terminal size. */
	public fun currentSize(): ResizeEvent {
		val (columns, rows, width, height) = tty.currentSize()
		return ResizeEvent(
			columns = columns,
			rows = rows,
			width = width,
			height = height,
		)
	}

	/**
	 * Perform a blocking read from stdin to try and parse events. Calls to this function are not
	 * guaranteed to read an event, nor are they guaranteed to read only one event. Events
	 * which are read will be placed into [events].
	 *
	 * It is expected that this function will be called repeatedly in a loop.
	 *
	 * @return False if returning due to [interrupt] being called. True means some data was read,
	 * but not necessarily that any events were put into the [events] channel. This could be because
	 * not enough bytes were available to parse the entire event, for example.
	 */
	public fun runParseLoop() {
		if (!emitDebugEvents) {
			while (true) {
				val event = parser.next() ?: break
				_events.trySend(event)
			}
		} else {
			while (true) {
				val (event, bytes) = parser.debugNext() ?: break
				_events.trySend(event)
				_events.trySend(DebugEvent(event, bytes))
			}
		}
		// TODO Hmmm....
		_events.close()
	}

	public fun interrupt() {
		tty.interruptRead()
	}

	/**
	 * Free the resources associated with this reader.
	 *
	 * This call can be omitted if your process is exiting.
	 */
	override fun close() {
		tty.close()
	}
}
