package com.jakewharton.mosaic.tty.terminal

import assertk.assertThat
import assertk.assertions.isEqualTo
import com.jakewharton.mosaic.terminal.CursorPositionEvent
import com.jakewharton.mosaic.terminal.UnknownEvent
import kotlin.test.Test

class EventParserCsiCursorPositionEventTest : BaseEventParserTest() {
	@Test fun emptyFails() {
		testTty.write("${CSI}R")
		assertThat(parser.next()).isEqualTo(
			UnknownEvent("1b5b52".hexToByteArray()),
		)
	}

	@Test fun emptyQuestionFails() {
		testTty.write("$CSI?R")
		assertThat(parser.next()).isEqualTo(
			UnknownEvent("1b5b3f52".hexToByteArray()),
		)
	}

	@Test fun missingColFails() {
		testTty.write("${CSI}1R")
		assertThat(parser.next()).isEqualTo(
			UnknownEvent("1b5b3152".hexToByteArray()),
		)
	}

	@Test fun missingQuestionColFails() {
		testTty.write("$CSI?1R")
		assertThat(parser.next()).isEqualTo(
			UnknownEvent("1b5b3f3152".hexToByteArray()),
		)
	}

	@Test fun emptyRowFails() {
		testTty.write("$CSI;1R")
		assertThat(parser.next()).isEqualTo(
			UnknownEvent("1b5b3b3152".hexToByteArray()),
		)
	}

	@Test fun emptyQuestionRowFails() {
		testTty.write("$CSI?;1R")
		assertThat(parser.next()).isEqualTo(
			UnknownEvent("1b5b3f3b3152".hexToByteArray()),
		)
	}

	@Test fun emptyColFails() {
		testTty.write("${CSI}1;R")
		assertThat(parser.next()).isEqualTo(
			UnknownEvent("1b5b313b52".hexToByteArray()),
		)
	}

	@Test fun emptyQuestionColFails() {
		testTty.write("$CSI?1;R")
		assertThat(parser.next()).isEqualTo(
			UnknownEvent("1b5b3f313b52".hexToByteArray()),
		)
	}

	@Test fun nonDigitRowFails() {
		testTty.write("$CSI:;1R")
		assertThat(parser.next()).isEqualTo(
			UnknownEvent("1b5b3a3b3152".hexToByteArray()),
		)
	}

	@Test fun nonDigitQuestionRowFails() {
		testTty.write("$CSI?:;1R")
		assertThat(parser.next()).isEqualTo(
			UnknownEvent("1b5b3f3a3b3152".hexToByteArray()),
		)
	}

	@Test fun nonDigitColFails() {
		testTty.write("${CSI}1;:R")
		assertThat(parser.next()).isEqualTo(
			UnknownEvent("1b5b313b3a52".hexToByteArray()),
		)
	}

	@Test fun nonDigitQuestionColFails() {
		testTty.write("$CSI?1;:R")
		assertThat(parser.next()).isEqualTo(
			UnknownEvent("1b5b3f313b3a52".hexToByteArray()),
		)
	}

	@Test fun works() {
		testTty.write("${CSI}1;1R")
		assertThat(parser.next()).isEqualTo(CursorPositionEvent(1, 1))
	}

	@Test fun worksQuestion() {
		testTty.write("$CSI?1;1R")
		assertThat(parser.next()).isEqualTo(CursorPositionEvent(1, 1))
	}
}
