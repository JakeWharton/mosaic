package com.jakewharton.mosaic.terminal

import assertk.assertThat
import assertk.assertions.isEqualTo
import com.jakewharton.mosaic.terminal.event.BracketedPasteEvent
import kotlin.test.Test
import kotlinx.coroutines.test.runTest

class TerminalParserCsiBracketedPasteEventTest : BaseTerminalParserTest() {
	@Test fun pasteStart() = runTest {
		writer.writeHex("1b5b3230307e")
		assertThat(reader.next()).isEqualTo(BracketedPasteEvent(start = true))
	}

	@Test fun pasteEnd() = runTest {
		writer.writeHex("1b5b3230317e")
		assertThat(reader.next()).isEqualTo(BracketedPasteEvent(start = false))
	}
}
