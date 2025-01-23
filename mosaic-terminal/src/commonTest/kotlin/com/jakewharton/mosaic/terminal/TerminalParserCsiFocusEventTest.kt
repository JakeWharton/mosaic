package com.jakewharton.mosaic.terminal

import assertk.assertThat
import assertk.assertions.isEqualTo
import com.jakewharton.mosaic.terminal.event.FocusEvent
import kotlin.test.Test

class TerminalParserCsiFocusEventTest : BaseTerminalParserTest() {
	@Test fun focusedTrue() {
		writer.writeHex("1b5b49")
		assertThat(parser.next()).isEqualTo(FocusEvent(focused = true))
	}

	@Test fun focusedFalse() {
		writer.writeHex("1b5b4f")
		assertThat(parser.next()).isEqualTo(FocusEvent(focused = false))
	}
}
