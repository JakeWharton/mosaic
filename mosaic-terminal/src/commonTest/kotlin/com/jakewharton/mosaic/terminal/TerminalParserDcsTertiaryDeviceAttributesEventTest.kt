package com.jakewharton.mosaic.terminal

import assertk.assertThat
import assertk.assertions.isEqualTo
import com.jakewharton.mosaic.terminal.event.TertiaryDeviceAttributesEvent
import com.jakewharton.mosaic.terminal.event.UnknownEvent
import kotlin.test.Test
import kotlinx.coroutines.test.runTest

class TerminalParserDcsTertiaryDeviceAttributesEventTest : BaseTerminalParserTest() {
	@Test fun zeroes() = runTest {
		testTty.writeHex("1b50217c30303030303030301b5c")
		assertThat(reader.next()).isEqualTo(TertiaryDeviceAttributesEvent(0, 0))
	}

	@Test fun values() = runTest {
		testTty.writeHex("1b50217c37423036463835351b5c")
		assertThat(reader.next()).isEqualTo(TertiaryDeviceAttributesEvent(123, 456789))
	}

	@Test fun tooShort() = runTest {
		testTty.writeHex("1b50217c303030303030301b5c")
		assertThat(reader.next()).isEqualTo(
			UnknownEvent("1b50217c303030303030301b5c".hexToByteArray()),
		)
	}

	@Test fun tooLong() = runTest {
		testTty.writeHex("1b50217c3030303030303030301b5c")
		assertThat(reader.next()).isEqualTo(
			UnknownEvent("1b50217c3030303030303030301b5c".hexToByteArray()),
		)
	}

	@Test fun idOddHex() = runTest {
		testTty.writeHex("1b50217c374230364638351b5c")
		assertThat(reader.next()).isEqualTo(
			UnknownEvent("1b50217c374230364638351b5c".hexToByteArray()),
		)
	}
}
