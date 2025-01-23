package com.jakewharton.mosaic.terminal

import assertk.assertThat
import assertk.assertions.isEqualTo
import com.jakewharton.mosaic.terminal.event.UnknownEvent
import com.jakewharton.mosaic.terminal.event.XtermCharacterSizeEvent
import kotlin.test.Test

@OptIn(ExperimentalStdlibApi::class)
class TerminalParserCsiXtermCharacterSizeEventTest : BaseTerminalParserTest() {
	@Test fun basic() {
		writer.writeHex("1b5b383b313b3274")
		assertThat(parser.next()).isEqualTo(XtermCharacterSizeEvent(1, 2))
	}

	@Test fun emptyParameterFails() {
		writer.writeHex("1b5b383b3b3274")
		assertThat(parser.next()).isEqualTo(
			UnknownEvent("1b5b383b3b3274".hexToByteArray()),
		)
		writer.writeHex("1b5b383b313b74")
		assertThat(parser.next()).isEqualTo(
			UnknownEvent("1b5b383b313b74".hexToByteArray()),
		)
	}

	@Test fun nonDigitParameterFails() {
		writer.writeHex("1b5b383b223b3274")
		assertThat(parser.next()).isEqualTo(
			UnknownEvent("1b5b383b223b3274".hexToByteArray()),
		)
		writer.writeHex("1b5b383b313b2274")
		assertThat(parser.next()).isEqualTo(
			UnknownEvent("1b5b383b313b2274".hexToByteArray()),
		)
	}
}
