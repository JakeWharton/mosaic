package com.jakewharton.mosaic.terminal

import assertk.assertThat
import assertk.assertions.isEqualTo
import com.jakewharton.mosaic.terminal.event.TerminalVersionEvent
import kotlin.test.Test
import kotlinx.coroutines.test.runTest

class TerminalParserDcsTerminalVersionEventTest : BaseTerminalParserTest() {
	@Test fun empty() = runTest {
		testTty.writeHex("1b503e7c1b5c")
		assertThat(reader.next()).isEqualTo(TerminalVersionEvent(""))
	}

	@Test fun text() = runTest {
		testTty.writeHex("1b503e7c68656c6c6f1b5c")
		assertThat(reader.next()).isEqualTo(TerminalVersionEvent("hello"))
	}
}
