package com.jakewharton.mosaic.tty.terminal

import assertk.assertThat
import assertk.assertions.isEqualTo
import com.jakewharton.mosaic.terminal.OperatingStatusResponseEvent
import com.jakewharton.mosaic.terminal.UnknownEvent
import kotlin.test.Test

class EventParserCsiOperatingStatusResponseEventTest : BaseEventParserTest() {
	@Test fun ok() {
		testTty.write("${CSI}0n")
		assertThat(parser.next()).isEqualTo(OperatingStatusResponseEvent(ok = true))
	}

	@Test fun notOk() {
		testTty.write("${CSI}3n")
		assertThat(parser.next()).isEqualTo(OperatingStatusResponseEvent(ok = false))
	}

	@Test fun unknownP1() {
		testTty.write("${CSI}1n")
		assertThat(parser.next()).isEqualTo(UnknownEvent("1b5b316e".hexToByteArray()))
	}

	@Test fun nonDigitP1() {
		testTty.write("$CSI+n")
		assertThat(parser.next()).isEqualTo(UnknownEvent("1b5b2b6e".hexToByteArray()))
	}
}
