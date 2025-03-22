package com.jakewharton.mosaic.tty.terminal

import assertk.assertThat
import assertk.assertions.isEqualTo
import com.jakewharton.mosaic.terminal.TertiaryDeviceAttributesEvent
import com.jakewharton.mosaic.terminal.UnknownEvent
import kotlin.test.Test

class EventParserDcsTertiaryDeviceAttributesEventTest : BaseEventParserTest() {
	@Test fun zeroes() {
		testTty.writeHex("1b50217c30303030303030301b5c")
		assertThat(parser.next()).isEqualTo(TertiaryDeviceAttributesEvent(0, 0))
	}

	@Test fun values() {
		testTty.writeHex("1b50217c37423036463835351b5c")
		assertThat(parser.next()).isEqualTo(TertiaryDeviceAttributesEvent(123, 456789))
	}

	@Test fun tooShort() {
		testTty.writeHex("1b50217c303030303030301b5c")
		assertThat(parser.next()).isEqualTo(
			UnknownEvent("1b50217c303030303030301b5c".hexToByteArray()),
		)
	}

	@Test fun tooLong() {
		testTty.writeHex("1b50217c3030303030303030301b5c")
		assertThat(parser.next()).isEqualTo(
			UnknownEvent("1b50217c3030303030303030301b5c".hexToByteArray()),
		)
	}

	@Test fun idOddHex() {
		testTty.writeHex("1b50217c374230364638351b5c")
		assertThat(parser.next()).isEqualTo(
			UnknownEvent("1b50217c374230364638351b5c".hexToByteArray()),
		)
	}
}
