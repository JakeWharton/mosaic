package com.jakewharton.mosaic.tty.terminal

import assertk.assertThat
import assertk.assertions.isEqualTo
import com.jakewharton.mosaic.terminal.FocusEvent
import kotlin.test.Test

class EventParserCsiFocusEventTest : BaseEventParserTest() {
	@Test fun focusedTrue() {
		testTty.write("${CSI}I")
		assertThat(parser.next()).isEqualTo(FocusEvent(focused = true))
	}

	@Test fun focusedFalse() {
		testTty.write("${CSI}O")
		assertThat(parser.next()).isEqualTo(FocusEvent(focused = false))
	}
}
