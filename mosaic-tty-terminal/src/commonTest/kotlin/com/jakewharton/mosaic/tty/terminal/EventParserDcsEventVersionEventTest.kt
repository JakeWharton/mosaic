package com.jakewharton.mosaic.tty.terminal

import assertk.assertThat
import assertk.assertions.isEqualTo
import com.jakewharton.mosaic.terminal.TerminalVersionEvent
import kotlin.test.Test

class EventParserDcsEventVersionEventTest : BaseEventParserTest() {
	@Test fun empty() {
		testTty.write("$DCS>|$ST")
		assertThat(parser.next()).isEqualTo(TerminalVersionEvent(""))
	}

	@Test fun text() {
		testTty.write("$DCS>|hello$ST")
		assertThat(parser.next()).isEqualTo(TerminalVersionEvent("hello"))
	}
}
