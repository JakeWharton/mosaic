package com.jakewharton.mosaic.tty.terminal

import assertk.assertThat
import assertk.assertions.isEqualTo
import com.jakewharton.mosaic.terminal.KittyPointerQueryNameEvent
import com.jakewharton.mosaic.terminal.KittyPointerQuerySupportEvent
import com.jakewharton.mosaic.terminal.UnknownEvent
import kotlin.test.Test

class EventParserOscKittyPointerQueryEventTest : BaseEventParserTest() {
	@Test fun emptyFails() {
		testTty.writeHex("1b5d32323b1b5c")
		assertThat(parser.next()).isEqualTo(
			UnknownEvent("1b5d32323b1b5c".hexToByteArray()),
		)
	}

	@Test fun valuesSingleFalse() {
		testTty.writeHex("1b5d32323b301b5c")
		assertThat(parser.next()).isEqualTo(
			KittyPointerQuerySupportEvent(booleanArrayOf(false)),
		)
	}

	@Test fun valuesSingleTrue() {
		testTty.writeHex("1b5d32323b311b5c")
		assertThat(parser.next()).isEqualTo(
			KittyPointerQuerySupportEvent(booleanArrayOf(true)),
		)
	}

	@Test fun valuesSingleValueTrailingCommaFails() {
		testTty.writeHex("1b5d32323b312c1b5c")
		assertThat(parser.next()).isEqualTo(
			UnknownEvent("1b5d32323b312c1b5c".hexToByteArray()),
		)
	}

	@Test fun valuesMultiple() {
		testTty.writeHex("1b5d32323b302c302c312c312c301b5c")
		assertThat(parser.next()).isEqualTo(
			KittyPointerQuerySupportEvent(booleanArrayOf(false, false, true, true, false)),
		)
	}

	@Test fun valuesTons() {
		testTty.writeHex("1b5d32323b302c302c312c312c302c302c312c312c302c302c312c312c302c302c312c312c302c302c312c312c302c302c312c312c302c302c312c312c301b5c")
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
		testTty.writeHex("1b5d32323b321b5c")
		assertThat(parser.next()).isEqualTo(
			KittyPointerQueryNameEvent("2"),
		)
	}

	@Test fun nameLeadingValueDigit() {
		testTty.writeHex("1b5d32323b30611b5c")
		assertThat(parser.next()).isEqualTo(
			KittyPointerQueryNameEvent("0a"),
		)
	}

	@Test fun nameValidRange() {
		testTty.writeHex("1b5d32323b6162636465666768696a6b6c6d6e6f707172737475767778797a303132333435363738392d5f1b5c")
		assertThat(parser.next()).isEqualTo(
			KittyPointerQueryNameEvent("abcdefghijklmnopqrstuvwxyz0123456789-_"),
		)
	}

	@Test fun nameInvalidRange() {
		testTty.writeHex("1b5d32323b6162633132334142431b5c")
		assertThat(parser.next()).isEqualTo(
			UnknownEvent("1b5d32323b6162633132334142431b5c".hexToByteArray()),
		)
	}

	@Test fun brokenOldKitty() {
		// Kitty 0.39.1 and older sent 'OSC 22 :' instead of 'OSC 22 ;'. We don't bother parsing it.
		testTty.writeHex("1b5d32323a311b5c")
		assertThat(parser.next()).isEqualTo(
			UnknownEvent("1b5d32323a311b5c".hexToByteArray()),
		)
	}
}
