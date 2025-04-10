package com.jakewharton.mosaic.tty.terminal

import assertk.assertThat
import assertk.assertions.isEqualTo
import com.jakewharton.mosaic.terminal.KittyKeyboardQueryEvent
import com.jakewharton.mosaic.terminal.UnknownEvent
import kotlin.test.Test

class EventParserCsiKittyKeyboardQueryEventTest : BaseEventParserTest() {
	@Test fun flagsNone() {
		testTty.write("$CSI?0u")
		assertThat(parser.next()).isEqualTo(KittyKeyboardQueryEvent(0))
	}

	@Test fun flagsAll() {
		testTty.write("$CSI?31u")
		assertThat(parser.next()).isEqualTo(KittyKeyboardQueryEvent(31))
	}

	@Test fun flagsUnknown() {
		testTty.write("$CSI?128u")
		assertThat(parser.next()).isEqualTo(KittyKeyboardQueryEvent(128))
	}

	@Test fun flagsMissing() {
		testTty.write("$CSI?u")
		assertThat(parser.next()).isEqualTo(
			UnknownEvent("1b5b3f75".hexToByteArray()),
		)
	}

	@Test fun flagsNonDigit() {
		testTty.write("$CSI?1+ u")
		assertThat(parser.next()).isEqualTo(
			UnknownEvent("1b5b3f312b2075".hexToByteArray()),
		)
	}
}
