package com.jakewharton.mosaic.terminal

import assertk.assertThat
import assertk.assertions.isEqualTo
import com.jakewharton.mosaic.terminal.event.BracketedPasteEvent
import kotlin.test.Test
import kotlinx.coroutines.test.runTest

class TerminalParserCsiBracketedPasteEventTest : BaseTerminalParserTest() {
	@Test fun pasteStart() = runTest {
		testTty.writeHex("1b5b3230307e")
		assertThat(parser.next()).isEqualTo(BracketedPasteEvent(start = true))
	}

	@Test fun pasteEnd() = runTest {
		testTty.writeHex("1b5b3230317e")
		assertThat(parser.next()).isEqualTo(BracketedPasteEvent(start = false))
	}
}
