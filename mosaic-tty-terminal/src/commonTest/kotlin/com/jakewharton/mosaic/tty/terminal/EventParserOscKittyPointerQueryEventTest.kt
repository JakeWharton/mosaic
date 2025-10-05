package com.jakewharton.mosaic.tty.terminal

import assertk.assertThat
import assertk.assertions.isEqualTo
import com.jakewharton.mosaic.terminal.KittyPointerQueryNameEvent
import com.jakewharton.mosaic.terminal.KittyPointerQuerySupportEvent
import com.jakewharton.mosaic.terminal.UnknownEvent
import kotlin.test.Test

class EventParserOscKittyPointerQueryEventTest : BaseEventParserTest() {
	@Test fun emptyFails() {
		testTerminal.write("${OSC}22;$ST")
		assertThat(parser.next()).isEqualTo(
			UnknownEvent("1b5d32323b1b5c".hexToByteArray()),
		)
	}

	@Test fun valuesSingleFalse() {
		testTerminal.write("${OSC}22;0$ST")
		assertThat(parser.next()).isEqualTo(
			KittyPointerQuerySupportEvent(booleanArrayOf(false)),
		)
	}

	@Test fun valuesSingleTrue() {
		testTerminal.write("${OSC}22;1$ST")
		assertThat(parser.next()).isEqualTo(
			KittyPointerQuerySupportEvent(booleanArrayOf(true)),
		)
	}

	@Test fun valuesSingleValueTrailingCommaFails() {
		testTerminal.write("${OSC}22;1,$ST")
		assertThat(parser.next()).isEqualTo(
			UnknownEvent("1b5d32323b312c1b5c".hexToByteArray()),
		)
	}

	@Test fun valuesMultiple() {
		testTerminal.write("${OSC}22;0,0,1,1,0$ST")
		assertThat(parser.next()).isEqualTo(
			KittyPointerQuerySupportEvent(booleanArrayOf(false, false, true, true, false)),
		)
	}

	@Test fun valuesTons() {
		testTerminal.write("${OSC}22;0,0,1,1,0,0,1,1,0,0,1,1,0,0,1,1,0,0,1,1,0,0,1,1,0,0,1,1,0$ST")
		assertThat(parser.next()).isEqualTo(
			KittyPointerQuerySupportEvent(
				booleanArrayOf(
					false,
					false, true, true, false,
					false, true, true, false,
					false, true, true, false,
					false, true, true, false,
					false, true, true, false,
					false, true, true, false,
					false, true, true, false,
				),
			),
		)
	}

	@Test fun nameSingleDigit() {
		testTerminal.write("${OSC}22;2$ST")
		assertThat(parser.next()).isEqualTo(
			KittyPointerQueryNameEvent("2"),
		)
	}

	@Test fun nameLeadingValueDigit() {
		testTerminal.write("${OSC}22;0a$ST")
		assertThat(parser.next()).isEqualTo(
			KittyPointerQueryNameEvent("0a"),
		)
	}

	@Test fun nameValidRange() {
		testTerminal.write("${OSC}22;abcdefghijklmnopqrstuvwxyz0123456789-_$ST")
		assertThat(parser.next()).isEqualTo(
			KittyPointerQueryNameEvent("abcdefghijklmnopqrstuvwxyz0123456789-_"),
		)
	}

	@Test fun nameInvalidRange() {
		testTerminal.write("${OSC}22;abc123ABC$ST")
		assertThat(parser.next()).isEqualTo(
			UnknownEvent("1b5d32323b6162633132334142431b5c".hexToByteArray()),
		)
	}

	@Test fun brokenOldKitty() {
		// Kitty 0.39.1 and older sent 'OSC 22 :' instead of 'OSC 22 ;'. We don't bother parsing it.
		testTerminal.write("${OSC}22:1$ST")
		assertThat(parser.next()).isEqualTo(
			UnknownEvent("1b5d32323a311b5c".hexToByteArray()),
		)
	}
}
