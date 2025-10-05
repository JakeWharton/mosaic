package com.jakewharton.mosaic.tty.terminal

import assertk.assertThat
import assertk.assertions.isEqualTo
import com.jakewharton.mosaic.terminal.UnknownEvent
import com.jakewharton.mosaic.terminal.XtermCharacterSizeEvent
import kotlin.test.Test

class EventParserCsiXtermCharacterSizeEventTest : BaseEventParserTest() {
	@Test fun basic() {
		testTerminal.write("${CSI}8;1;2t")
		assertThat(parser.next()).isEqualTo(XtermCharacterSizeEvent(1, 2))
	}

	@Test fun emptyParameterFails() {
		testTerminal.write("${CSI}8;;2t")
		assertThat(parser.next()).isEqualTo(
			UnknownEvent("1b5b383b3b3274".hexToByteArray()),
		)
		testTerminal.write("${CSI}8;1;t")
		assertThat(parser.next()).isEqualTo(
			UnknownEvent("1b5b383b313b74".hexToByteArray()),
		)
	}

	@Test fun nonDigitParameterFails() {
		testTerminal.write("${CSI}8;\";2t")
		assertThat(parser.next()).isEqualTo(
			UnknownEvent("1b5b383b223b3274".hexToByteArray()),
		)
		testTerminal.write("${CSI}8;1;\"t")
		assertThat(parser.next()).isEqualTo(
			UnknownEvent("1b5b383b313b2274".hexToByteArray()),
		)
	}
}
