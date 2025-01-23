package com.jakewharton.mosaic.terminal

import assertk.assertThat
import assertk.assertions.isEqualTo
import com.jakewharton.mosaic.terminal.event.OperatingStatusResponseEvent
import com.jakewharton.mosaic.terminal.event.UnknownEvent
import kotlin.test.Test

@OptIn(ExperimentalStdlibApi::class)
class TerminalParserCsiOperatingStatusResponseEventTest : BaseTerminalParserTest() {
	@Test fun ok() {
		writer.writeHex("1b5b306e")
		assertThat(parser.next()).isEqualTo(OperatingStatusResponseEvent(ok = true))
	}

	@Test fun notOk() {
		writer.writeHex("1b5b336e")
		assertThat(parser.next()).isEqualTo(OperatingStatusResponseEvent(ok = false))
	}

	@Test fun unknownP1() {
		writer.writeHex("1b5b316e")
		assertThat(parser.next()).isEqualTo(UnknownEvent("1b5b316e".hexToByteArray()))
	}

	@Test fun nonDigitP1() {
		writer.writeHex("1b5b2b6e")
		assertThat(parser.next()).isEqualTo(UnknownEvent("1b5b2b6e".hexToByteArray()))
	}
}
