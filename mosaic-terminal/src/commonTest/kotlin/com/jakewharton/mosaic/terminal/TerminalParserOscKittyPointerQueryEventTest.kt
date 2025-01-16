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
		writer.writeHex("1b5d32323b1b5c")
		assertThat(parser.next()).isEqualTo(
			UnknownEvent("1b5d32323b1b5c".hexToByteArray()),
		)
	}

	@Test fun valuesSingleFalse() = runTest {
		writer.writeHex("1b5d32323b301b5c")
		assertThat(parser.next()).isEqualTo(
			KittyPointerQuerySupportEvent(booleanArrayOf(false)),
		)
	}

	@Test fun valuesSingleTrue() = runTest {
		writer.writeHex("1b5d32323b311b5c")
		assertThat(parser.next()).isEqualTo(
			KittyPointerQuerySupportEvent(booleanArrayOf(true)),
		)
	}

	@Test fun valuesSingleValueTrailingCommaFails() = runTest {
		writer.writeHex("1b5d32323b312c1b5c")
		assertThat(parser.next()).isEqualTo(
			UnknownEvent("1b5d32323b312c1b5c".hexToByteArray()),
		)
	}

	@Test fun valuesMultiple() = runTest {
		writer.writeHex("1b5d32323b302c302c312c312c301b5c")
		assertThat(parser.next()).isEqualTo(
			KittyPointerQuerySupportEvent(booleanArrayOf(false, false, true, true, false)),
		)
	}

	@Test fun valuesTons() = runTest {
		writer.writeHex("1b5d32323b302c302c312c312c302c302c312c312c302c302c312c312c302c302c312c312c302c302c312c312c302c302c312c312c302c302c312c312c301b5c")
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

	@Test fun nameSingleDigit() = runTest {
		writer.writeHex("1b5d32323b321b5c")
		assertThat(parser.next()).isEqualTo(
			KittyPointerQueryNameEvent("2"),
		)
	}

	@Test fun nameLeadingValueDigit() = runTest {
		writer.writeHex("1b5d32323b30611b5c")
		assertThat(parser.next()).isEqualTo(
			KittyPointerQueryNameEvent("0a"),
		)
	}

	@Test fun nameValidRange() = runTest {
		writer.writeHex("1b5d32323b6162636465666768696a6b6c6d6e6f707172737475767778797a303132333435363738392d5f1b5c")
		assertThat(parser.next()).isEqualTo(
			KittyPointerQueryNameEvent("abcdefghijklmnopqrstuvwxyz0123456789-_"),
		)
	}

	@Test fun nameInvalidRange() = runTest {
		writer.writeHex("1b5d32323b6162633132334142431b5c")
		assertThat(parser.next()).isEqualTo(
			UnknownEvent("1b5d32323b6162633132334142431b5c".hexToByteArray()),
		)
	}
}
