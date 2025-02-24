package com.jakewharton.mosaic.terminal

import assertk.assertThat
import assertk.assertions.isEqualTo
import com.jakewharton.mosaic.terminal.event.KittyPointerQueryNameEvent
import com.jakewharton.mosaic.terminal.event.KittyPointerQuerySupportEvent
import com.jakewharton.mosaic.terminal.event.UnknownEvent
import kotlin.test.Test
import kotlinx.coroutines.test.runTest

class TerminalParserOscKittyPointerQueryEventTest : BaseTerminalParserTest() {
	@Test fun emptyFails() = runTest {
		testTty.writeHex("1b5d32323b1b5c")
		assertThat(reader.next()).isEqualTo(
			UnknownEvent("1b5d32323b1b5c".hexToByteArray()),
		)
	}

	@Test fun valuesSingleFalse() = runTest {
		testTty.writeHex("1b5d32323b301b5c")
		assertThat(reader.next()).isEqualTo(
			KittyPointerQuerySupportEvent(booleanArrayOf(false)),
		)
	}

	@Test fun valuesSingleTrue() = runTest {
		testTty.writeHex("1b5d32323b311b5c")
		assertThat(reader.next()).isEqualTo(
			KittyPointerQuerySupportEvent(booleanArrayOf(true)),
		)
	}

	@Test fun valuesSingleValueTrailingCommaFails() = runTest {
		testTty.writeHex("1b5d32323b312c1b5c")
		assertThat(reader.next()).isEqualTo(
			UnknownEvent("1b5d32323b312c1b5c".hexToByteArray()),
		)
	}

	@Test fun valuesMultiple() = runTest {
		testTty.writeHex("1b5d32323b302c302c312c312c301b5c")
		assertThat(reader.next()).isEqualTo(
			KittyPointerQuerySupportEvent(booleanArrayOf(false, false, true, true, false)),
		)
	}

	@Test fun valuesTons() = runTest {
		testTty.writeHex("1b5d32323b302c302c312c312c302c302c312c312c302c302c312c312c302c302c312c312c302c302c312c312c302c302c312c312c302c302c312c312c301b5c")
		assertThat(reader.next()).isEqualTo(
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

	@Test fun nameSingleDigit() = runTest {
		testTty.writeHex("1b5d32323b321b5c")
		assertThat(reader.next()).isEqualTo(
			KittyPointerQueryNameEvent("2"),
		)
	}

	@Test fun nameLeadingValueDigit() = runTest {
		testTty.writeHex("1b5d32323b30611b5c")
		assertThat(reader.next()).isEqualTo(
			KittyPointerQueryNameEvent("0a"),
		)
	}

	@Test fun nameValidRange() = runTest {
		testTty.writeHex("1b5d32323b6162636465666768696a6b6c6d6e6f707172737475767778797a303132333435363738392d5f1b5c")
		assertThat(reader.next()).isEqualTo(
			KittyPointerQueryNameEvent("abcdefghijklmnopqrstuvwxyz0123456789-_"),
		)
	}

	@Test fun nameInvalidRange() = runTest {
		testTty.writeHex("1b5d32323b6162633132334142431b5c")
		assertThat(reader.next()).isEqualTo(
			UnknownEvent("1b5d32323b6162633132334142431b5c".hexToByteArray()),
		)
	}

	@Test fun brokenOldKitty() = runTest {
		// Kitty 0.39.1 and older sent 'OSC 22 :' instead of 'OSC 22 ;'. We don't bother parsing it.
		testTty.writeHex("1b5d32323a311b5c")
		assertThat(reader.next()).isEqualTo(
			UnknownEvent("1b5d32323a311b5c".hexToByteArray()),
		)
	}
}
