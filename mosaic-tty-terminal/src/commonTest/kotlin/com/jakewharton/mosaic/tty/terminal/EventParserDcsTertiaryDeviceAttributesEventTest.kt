package com.jakewharton.mosaic.tty.terminal

import assertk.assertThat
import assertk.assertions.isEqualTo
import com.jakewharton.mosaic.terminal.TertiaryDeviceAttributesEvent
import com.jakewharton.mosaic.terminal.UnknownEvent
import kotlin.test.Test

class EventParserDcsTertiaryDeviceAttributesEventTest : BaseEventParserTest() {
	@Test fun zeroes() {
		testTty.write("$DCS!|00000000$ST")
		assertThat(parser.next()).isEqualTo(TertiaryDeviceAttributesEvent(0, 0))
	}

	@Test fun values() {
		testTty.write("$DCS!|7B06F855$ST")
		assertThat(parser.next()).isEqualTo(TertiaryDeviceAttributesEvent(123, 456789))
	}

	@Test fun tooShort() {
		testTty.write("$DCS!|0000000$ST")
		assertThat(parser.next()).isEqualTo(
			UnknownEvent("1b50217c303030303030301b5c".hexToByteArray()),
		)
	}

	@Test fun tooLong() {
		testTty.write("$DCS!|000000000$ST")
		assertThat(parser.next()).isEqualTo(
			UnknownEvent("1b50217c3030303030303030301b5c".hexToByteArray()),
		)
	}

	@Test fun idOddHex() {
		testTty.write("$DCS!|7B06F85$ST")
		assertThat(parser.next()).isEqualTo(
			UnknownEvent("1b50217c374230364638351b5c".hexToByteArray()),
		)
	}
}
