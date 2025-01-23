package com.jakewharton.mosaic.terminal

import assertk.assertThat
import assertk.assertions.isEqualTo
import com.jakewharton.mosaic.terminal.event.TerminalVersionEvent
import kotlin.test.Test

class TerminalParserDcsTerminalVersionEventTest : BaseTerminalParserTest() {
	@Test fun empty() {
		writer.writeHex("1b503e7c1b5c")
		assertThat(parser.next()).isEqualTo(TerminalVersionEvent(""))
	}

	@Test fun text() {
		writer.writeHex("1b503e7c68656c6c6f1b5c")
		assertThat(parser.next()).isEqualTo(TerminalVersionEvent("hello"))
	}
}
