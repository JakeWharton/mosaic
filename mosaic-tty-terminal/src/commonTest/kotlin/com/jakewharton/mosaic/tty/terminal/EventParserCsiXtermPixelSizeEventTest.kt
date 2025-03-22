package com.jakewharton.mosaic.tty.terminal

import assertk.assertThat
import assertk.assertions.isEqualTo
import com.jakewharton.mosaic.terminal.UnknownEvent
import com.jakewharton.mosaic.terminal.XtermPixelSizeEvent
import kotlin.test.Test

class EventParserCsiXtermPixelSizeEventTest : BaseEventParserTest() {
	@Test fun basic() {
		testTty.writeHex("1b5b343b313b3274")
		assertThat(parser.next()).isEqualTo(XtermPixelSizeEvent(1, 2))
	}

	@Test fun emptyParameterFails() {
		testTty.writeHex("1b5b343b3b3274")
		assertThat(parser.next()).isEqualTo(
			UnknownEvent("1b5b343b3b3274".hexToByteArray()),
		)
		testTty.writeHex("1b5b343b313b74")
		assertThat(parser.next()).isEqualTo(
			UnknownEvent("1b5b343b313b74".hexToByteArray()),
		)
	}

	@Test fun nonDigitParameterFails() {
		testTty.writeHex("1b5b343b223b3274")
		assertThat(parser.next()).isEqualTo(
			UnknownEvent("1b5b343b223b3274".hexToByteArray()),
		)
		testTty.writeHex("1b5b343b313b2274")
		assertThat(parser.next()).isEqualTo(
			UnknownEvent("1b5b343b313b2274".hexToByteArray()),
		)
	}
}
