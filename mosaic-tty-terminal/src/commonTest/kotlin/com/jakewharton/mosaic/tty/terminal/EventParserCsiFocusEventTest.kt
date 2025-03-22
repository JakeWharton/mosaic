package com.jakewharton.mosaic.tty.terminal

import assertk.assertThat
import assertk.assertions.isEqualTo
import com.jakewharton.mosaic.terminal.FocusEvent
import kotlin.test.Test

class EventParserCsiFocusEventTest : BaseEventParserTest() {
	@Test fun focusedTrue() {
		testTty.writeHex("1b5b49")
		assertThat(parser.next()).isEqualTo(FocusEvent(focused = true))
	}

	@Test fun focusedFalse() {
		testTty.writeHex("1b5b4f")
		assertThat(parser.next()).isEqualTo(FocusEvent(focused = false))
	}
}
