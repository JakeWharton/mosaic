package com.jakewharton.mosaic.tty.terminal

import assertk.assertThat
import assertk.assertions.isEqualTo
import com.jakewharton.mosaic.terminal.CursorPositionEvent
import com.jakewharton.mosaic.terminal.UnknownEvent
import kotlin.test.Test

class EventParserCsiCursorPositionEventTest : BaseEventParserTest() {
	@Test fun emptyFails() {
		testTty.writeHex("1b5b52")
		assertThat(parser.next()).isEqualTo(
			UnknownEvent("1b5b52".hexToByteArray()),
		)
	}

	@Test fun missingColFails() {
		testTty.writeHex("1b5b3152")
		assertThat(parser.next()).isEqualTo(
			UnknownEvent("1b5b3152".hexToByteArray()),
		)
	}

	@Test fun emptyRowFails() {
		testTty.writeHex("1b5b3b3152")
		assertThat(parser.next()).isEqualTo(
			UnknownEvent("1b5b3b3152".hexToByteArray()),
		)
	}

	@Test fun emptyColFails() {
		testTty.writeHex("1b5b313b52")
		assertThat(parser.next()).isEqualTo(
			UnknownEvent("1b5b313b52".hexToByteArray()),
		)
	}

	@Test fun nonDigitRowFails() {
		testTty.writeHex("1b5b3a3b3152")
		assertThat(parser.next()).isEqualTo(
			UnknownEvent("1b5b3a3b3152".hexToByteArray()),
		)
	}

	@Test fun nonDigitColFails() {
		testTty.writeHex("1b5b313b3a52")
		assertThat(parser.next()).isEqualTo(
			UnknownEvent("1b5b313b3a52".hexToByteArray()),
		)
	}

	@Test fun works() {
		testTty.writeHex("1b5b313b3152")
		assertThat(parser.next()).isEqualTo(CursorPositionEvent(1, 1))
	}
}
