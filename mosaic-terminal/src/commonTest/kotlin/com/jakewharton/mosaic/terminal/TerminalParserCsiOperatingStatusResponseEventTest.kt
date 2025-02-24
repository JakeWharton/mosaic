package com.jakewharton.mosaic.terminal

import assertk.assertThat
import assertk.assertions.isEqualTo
import com.jakewharton.mosaic.terminal.event.OperatingStatusResponseEvent
import com.jakewharton.mosaic.terminal.event.UnknownEvent
import kotlin.test.Test
import kotlinx.coroutines.test.runTest

class TerminalParserCsiOperatingStatusResponseEventTest : BaseTerminalParserTest() {
	@Test fun ok() = runTest {
		testTty.writeHex("1b5b306e")
		assertThat(reader.next()).isEqualTo(OperatingStatusResponseEvent(ok = true))
	}

	@Test fun notOk() = runTest {
		testTty.writeHex("1b5b336e")
		assertThat(reader.next()).isEqualTo(OperatingStatusResponseEvent(ok = false))
	}

	@Test fun unknownP1() = runTest {
		testTty.writeHex("1b5b316e")
		assertThat(reader.next()).isEqualTo(UnknownEvent("1b5b316e".hexToByteArray()))
	}

	@Test fun nonDigitP1() = runTest {
		testTty.writeHex("1b5b2b6e")
		assertThat(reader.next()).isEqualTo(UnknownEvent("1b5b2b6e".hexToByteArray()))
	}
}
