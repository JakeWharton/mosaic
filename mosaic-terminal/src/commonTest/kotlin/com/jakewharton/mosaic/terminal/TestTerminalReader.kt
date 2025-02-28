package com.jakewharton.mosaic.terminal

import com.jakewharton.mosaic.terminal.event.Event
import com.jakewharton.mosaic.tty.TestTty
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.Channel.Factory.UNLIMITED

internal fun TestTerminalReader(): TestTerminalReader {
	val events = Channel<Event>(UNLIMITED)
	val callback = EventChannelTtyCallback(events, emitDebugEvents = false)
	val testTty = TestTty.create(callback)
	val reader = TerminalReader(testTty.tty, events, emitDebugEvents = false)
	return TestTerminalReader(testTty, reader)
}

internal class TestTerminalReader(
	val testTty: TestTty,
	val terminalReader: TerminalReader,
)
