package com.jakewharton.mosaic.tty.terminal

import assertk.assertThat
import assertk.assertions.isEqualTo
import com.jakewharton.mosaic.terminal.SystemThemeEvent
import com.jakewharton.mosaic.terminal.UnknownEvent
import kotlin.test.Test

class EventParserCsiSystemThemeEventTest : BaseEventParserTest() {
	@Test fun dark() {
		testTerminal.write("$CSI?997;1n")
		assertThat(parser.next()).isEqualTo(SystemThemeEvent(isDark = true))
	}

	@Test fun light() {
		testTerminal.write("$CSI?997;2n")
		assertThat(parser.next()).isEqualTo(SystemThemeEvent(isDark = false))
	}

	@Test fun missingP2() {
		testTerminal.write("$CSI?997;n")
		assertThat(parser.next()).isEqualTo(
			UnknownEvent("1b5b3f3939373b6e".hexToByteArray()),
		)
	}

	@Test fun unknownP2() {
		testTerminal.write("$CSI?997;4n")
		assertThat(parser.next()).isEqualTo(
			UnknownEvent("1b5b3f3939373b346e".hexToByteArray()),
		)
	}

	@Test fun nonDigitP2() {
		testTerminal.write("$CSI?997;+n")
		assertThat(parser.next()).isEqualTo(
			UnknownEvent("1b5b3f3939373b2b6e".hexToByteArray()),
		)
	}

	@Test fun tooLongP2() {
		testTerminal.write("$CSI?997;11n")
		assertThat(parser.next()).isEqualTo(
			UnknownEvent("1b5b3f3939373b31316e".hexToByteArray()),
		)
	}
}
