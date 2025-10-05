package com.jakewharton.mosaic.tty.terminal

import assertk.assertThat
import assertk.assertions.isEqualTo
import com.jakewharton.mosaic.terminal.CursorPositionEvent
import com.jakewharton.mosaic.terminal.UnknownEvent
import kotlin.test.Test

class EventParserCsiCursorPositionEventTest : BaseEventParserTest() {
	@Test fun emptyFails() {
		testTerminal.write("${CSI}R")
		assertThat(parser.next()).isEqualTo(
			UnknownEvent("1b5b52".hexToByteArray()),
		)
	}

	@Test fun emptyQuestionFails() {
		testTerminal.write("$CSI?R")
		assertThat(parser.next()).isEqualTo(
			UnknownEvent("1b5b3f52".hexToByteArray()),
		)
	}

	@Test fun missingColFails() {
		testTerminal.write("${CSI}1R")
		assertThat(parser.next()).isEqualTo(
			UnknownEvent("1b5b3152".hexToByteArray()),
		)
	}

	@Test fun missingQuestionColFails() {
		testTerminal.write("$CSI?1R")
		assertThat(parser.next()).isEqualTo(
			UnknownEvent("1b5b3f3152".hexToByteArray()),
		)
	}

	@Test fun emptyRowFails() {
		testTerminal.write("$CSI;1R")
		assertThat(parser.next()).isEqualTo(
			UnknownEvent("1b5b3b3152".hexToByteArray()),
		)
	}

	@Test fun emptyQuestionRowFails() {
		testTerminal.write("$CSI?;1R")
		assertThat(parser.next()).isEqualTo(
			UnknownEvent("1b5b3f3b3152".hexToByteArray()),
		)
	}

	@Test fun emptyColFails() {
		testTerminal.write("${CSI}1;R")
		assertThat(parser.next()).isEqualTo(
			UnknownEvent("1b5b313b52".hexToByteArray()),
		)
	}

	@Test fun emptyQuestionColFails() {
		testTerminal.write("$CSI?1;R")
		assertThat(parser.next()).isEqualTo(
			UnknownEvent("1b5b3f313b52".hexToByteArray()),
		)
	}

	@Test fun nonDigitRowFails() {
		testTerminal.write("$CSI:;1R")
		assertThat(parser.next()).isEqualTo(
			UnknownEvent("1b5b3a3b3152".hexToByteArray()),
		)
	}

	@Test fun nonDigitQuestionRowFails() {
		testTerminal.write("$CSI?:;1R")
		assertThat(parser.next()).isEqualTo(
			UnknownEvent("1b5b3f3a3b3152".hexToByteArray()),
		)
	}

	@Test fun nonDigitColFails() {
		testTerminal.write("${CSI}1;:R")
		assertThat(parser.next()).isEqualTo(
			UnknownEvent("1b5b313b3a52".hexToByteArray()),
		)
	}

	@Test fun nonDigitQuestionColFails() {
		testTerminal.write("$CSI?1;:R")
		assertThat(parser.next()).isEqualTo(
			UnknownEvent("1b5b3f313b3a52".hexToByteArray()),
		)
	}

	@Test fun works() {
		testTerminal.write("${CSI}1;1R")
		assertThat(parser.next()).isEqualTo(CursorPositionEvent(1, 1))
	}

	@Test fun worksQuestion() {
		testTerminal.write("$CSI?1;1R")
		assertThat(parser.next()).isEqualTo(CursorPositionEvent(1, 1))
	}
}
