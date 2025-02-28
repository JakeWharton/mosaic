package com.jakewharton.mosaic.terminal

import assertk.assertThat
import assertk.assertions.isEqualTo
import com.jakewharton.mosaic.terminal.event.UnknownEvent
import com.jakewharton.mosaic.terminal.event.XtermCharacterSizeEvent
import kotlin.test.Test
import kotlinx.coroutines.test.runTest

class TerminalParserCsiXtermCharacterSizeEventTest : BaseTerminalParserTest() {
	@Test fun basic() = runTest {
		testTty.writeHex("1b5b383b313b3274")
		assertThat(parser.next()).isEqualTo(XtermCharacterSizeEvent(1, 2))
	}

	@Test fun emptyParameterFails() = runTest {
		testTty.writeHex("1b5b383b3b3274")
		assertThat(parser.next()).isEqualTo(
			UnknownEvent("1b5b383b3b3274".hexToByteArray()),
		)
		testTty.writeHex("1b5b383b313b74")
		assertThat(parser.next()).isEqualTo(
			UnknownEvent("1b5b383b313b74".hexToByteArray()),
		)
	}

	@Test fun nonDigitParameterFails() = runTest {
		testTty.writeHex("1b5b383b223b3274")
		assertThat(parser.next()).isEqualTo(
			UnknownEvent("1b5b383b223b3274".hexToByteArray()),
		)
		testTty.writeHex("1b5b383b313b2274")
		assertThat(parser.next()).isEqualTo(
			UnknownEvent("1b5b383b313b2274".hexToByteArray()),
		)
	}
}
