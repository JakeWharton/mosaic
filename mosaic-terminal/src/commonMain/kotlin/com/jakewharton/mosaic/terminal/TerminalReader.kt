package com.jakewharton.mosaic.terminal

import com.jakewharton.mosaic.terminal.event.DebugEvent
import com.jakewharton.mosaic.terminal.event.Event
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
	val tty = Tty.bind()
	val events = Channel<Event>(UNLIMITED)
	val callback = EventChannelTtyCallback(events, emitDebugEvents)
	tty.setCallback(callback)
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

	/** Events read as a result of calls to [runParseLoop]. */
	public val events: ReceiveChannel<Event> get() = _events

	/**
	 * Perform blocking reads from stdin to parse and emit to [events].
	 *
	 * This function will not return unless [interrupt] is called.
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
		// TODO Hmmm catch EOF?
		//  _events.close()
	}

	/** Cause [runParseLoop] to return. */
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
