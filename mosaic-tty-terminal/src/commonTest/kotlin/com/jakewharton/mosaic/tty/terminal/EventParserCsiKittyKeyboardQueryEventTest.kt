package com.jakewharton.mosaic.tty.terminal

import assertk.assertThat
import assertk.assertions.isEqualTo
import com.jakewharton.mosaic.terminal.KittyKeyboardQueryEvent
import com.jakewharton.mosaic.terminal.UnknownEvent
import kotlin.test.Test

class EventParserCsiKittyKeyboardQueryEventTest : BaseEventParserTest() {
	@Test fun flagsNone() {
		testTty.writeHex("1b5b3f3075")
		assertThat(parser.next()).isEqualTo(KittyKeyboardQueryEvent(0))
	}

	@Test fun flagsAll() {
		testTty.writeHex("1b5b3f333175")
		assertThat(parser.next()).isEqualTo(KittyKeyboardQueryEvent(31))
	}

	@Test fun flagsUnknown() {
		testTty.writeHex("1b5b3f31323875")
		assertThat(parser.next()).isEqualTo(KittyKeyboardQueryEvent(128))
	}

	@Test fun flagsMissing() {
		testTty.writeHex("1b5b3f75")
		assertThat(parser.next()).isEqualTo(
			UnknownEvent("1b5b3f75".hexToByteArray()),
		)
	}

	@Test fun flagsNonDigit() {
		testTty.writeHex("1b5b3f312b2075")
		assertThat(parser.next()).isEqualTo(
			UnknownEvent("1b5b3f312b2075".hexToByteArray()),
		)
	}
}
