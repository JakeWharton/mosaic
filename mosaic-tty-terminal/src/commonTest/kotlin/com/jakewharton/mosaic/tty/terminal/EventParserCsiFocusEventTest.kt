package com.jakewharton.mosaic.tty.terminal

import assertk.assertThat
import assertk.assertions.isEqualTo
import com.jakewharton.mosaic.terminal.FocusEvent
import kotlin.test.Test

class EventParserCsiFocusEventTest : BaseEventParserTest() {
	@Test fun focusedTrue() {
		testTerminal.write("${CSI}I")
		assertThat(parser.next()).isEqualTo(FocusEvent(focused = true))
	}

	@Test fun focusedFalse() {
		testTerminal.write("${CSI}O")
		assertThat(parser.next()).isEqualTo(FocusEvent(focused = false))
	}
}
