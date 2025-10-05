package com.jakewharton.mosaic.tty.terminal

import assertk.assertThat
import assertk.assertions.isEqualTo
import com.jakewharton.mosaic.terminal.SecondaryDeviceAttributesEvent
import com.jakewharton.mosaic.terminal.UnknownEvent
import kotlin.test.Test

class EventParserCsiSecondaryDeviceAttributesEventTest : BaseEventParserTest() {
	@Test fun emptyFails() {
		testTerminal.write("$CSI>c")
		assertThat(parser.next()).isEqualTo(
			UnknownEvent("1b5b3e63".hexToByteArray()),
		)
	}

	@Test fun missingFirmwareVersionAndRegistrationNumberFails() {
		testTerminal.write("$CSI>1c")
		assertThat(parser.next()).isEqualTo(
			UnknownEvent("1b5b3e3163".hexToByteArray()),
		)
	}

	@Test fun missingRegistrationNumberFails() {
		testTerminal.write("$CSI>1;10c")
		assertThat(parser.next()).isEqualTo(
			UnknownEvent("1b5b3e313b313063".hexToByteArray()),
		)
	}

	@Test fun emptyTypeFails() {
		testTerminal.write("$CSI>;10;0c")
		assertThat(parser.next()).isEqualTo(
			UnknownEvent("1b5b3e3b31303b3063".hexToByteArray()),
		)
	}

	@Test fun emptyFirmwareVersionFails() {
		testTerminal.write("$CSI>1;;0c")
		assertThat(parser.next()).isEqualTo(
			UnknownEvent("1b5b3e313b3b3063".hexToByteArray()),
		)
	}

	@Test fun emptyRegistrationNumberFails() {
		testTerminal.write("$CSI>1;10;c")
		assertThat(parser.next()).isEqualTo(
			UnknownEvent("1b5b3e313b31303b63".hexToByteArray()),
		)
	}

	@Test fun nonDigitTypeFails() {
		testTerminal.write("$CSI>:;10;0c")
		assertThat(parser.next()).isEqualTo(
			UnknownEvent("1b5b3e3a3b31303b3063".hexToByteArray()),
		)
	}

	@Test fun nonDigitFirmwareVersionFails() {
		testTerminal.write("$CSI>1;:;0c")
		assertThat(parser.next()).isEqualTo(
			UnknownEvent("1b5b3e313b3a3b3063".hexToByteArray()),
		)
	}

	@Test fun nonDigitRegistrationNumberFails() {
		testTerminal.write("$CSI>1;01;:c")
		assertThat(parser.next()).isEqualTo(
			UnknownEvent("1b5b3e313b30313b3a63".hexToByteArray()),
		)
	}

	@Test fun valid() {
		testTerminal.write("$CSI>1;10;0c")
		assertThat(parser.next()).isEqualTo(
			SecondaryDeviceAttributesEvent(1, 10, 0),
		)
	}
}
