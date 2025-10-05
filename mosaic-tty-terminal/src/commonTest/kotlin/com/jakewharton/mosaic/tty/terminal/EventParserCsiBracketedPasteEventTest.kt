package com.jakewharton.mosaic.tty.terminal

import assertk.assertThat
import assertk.assertions.isEqualTo
import com.jakewharton.mosaic.terminal.BracketedPasteEvent
import kotlin.test.Test

class EventParserCsiBracketedPasteEventTest : BaseEventParserTest() {
	@Test fun pasteStart() {
		testTerminal.write("${CSI}200~")
		assertThat(parser.next()).isEqualTo(BracketedPasteEvent(start = true))
	}

	@Test fun pasteEnd() {
		testTerminal.write("${CSI}201~")
		assertThat(parser.next()).isEqualTo(BracketedPasteEvent(start = false))
	}
}
