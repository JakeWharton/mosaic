package com.jakewharton.mosaic.terminal

import assertk.assertThat
import assertk.assertions.isEqualTo
import com.jakewharton.mosaic.terminal.event.OperatingStatusResponseEvent
import com.jakewharton.mosaic.terminal.event.UnknownEvent
import kotlin.test.Test
import kotlinx.coroutines.test.runTest

class TerminalParserCsiOperatingStatusResponseEventTest : BaseTerminalParserTest() {
	@Test fun ok() = runTest {
		writer.writeHex("1b5b306e")
		assertThat(parser.next()).isEqualTo(OperatingStatusResponseEvent(ok = true))
	}

	@Test fun notOk() = runTest {
		writer.writeHex("1b5b336e")
		assertThat(parser.next()).isEqualTo(OperatingStatusResponseEvent(ok = false))
	}

	@Test fun unknownP1() = runTest {
		writer.writeHex("1b5b316e")
		assertThat(parser.next()).isEqualTo(UnknownEvent("1b5b316e".hexToByteArray()))
	}

	@Test fun nonDigitP1() = runTest {
		writer.writeHex("1b5b2b6e")
		assertThat(parser.next()).isEqualTo(UnknownEvent("1b5b2b6e".hexToByteArray()))
	}
}
