package com.jakewharton.mosaic.terminal

import assertk.assertThat
import assertk.assertions.isEqualTo
import com.jakewharton.mosaic.terminal.event.KittyPointerQueryNameEvent
import com.jakewharton.mosaic.terminal.event.KittyPointerQuerySupportEvent
import com.jakewharton.mosaic.terminal.event.UnknownEvent
import kotlin.test.AfterTest
import kotlin.test.Test

@OptIn(ExperimentalStdlibApi::class)
class TerminalParserOscKittyPointerQueryEventTest {
	private val writer = Tty.stdinWriter()
	private val parser = TerminalParser(writer.reader)

	@AfterTest fun after() {
		writer.close()
	}

	@Test fun emptyFails() {
		writer.writeHex("1b5d32323b1b5c")
		assertThat(parser.next()).isEqualTo(
			UnknownEvent("1b5d32323b1b5c".hexToByteArray()),
		)
	}

	@Test fun valuesSingleFalse() {
		writer.writeHex("1b5d32323b301b5c")
		assertThat(parser.next()).isEqualTo(
			KittyPointerQuerySupportEvent(booleanArrayOf(false)),
		)
	}

	@Test fun valuesSingleTrue() {
		writer.writeHex("1b5d32323b311b5c")
		assertThat(parser.next()).isEqualTo(
			KittyPointerQuerySupportEvent(booleanArrayOf(true)),
		)
	}

	@Test fun valuesSingleValueTrailingCommaFails() {
		writer.writeHex("1b5d32323b312c1b5c")
		assertThat(parser.next()).isEqualTo(
			UnknownEvent("1b5d32323b312c1b5c".hexToByteArray()),
		)
	}

	@Test fun valuesMultiple() {
		writer.writeHex("1b5d32323b302c302c312c312c301b5c")
		assertThat(parser.next()).isEqualTo(
			KittyPointerQuerySupportEvent(booleanArrayOf(false, false, true, true, false)),
		)
	}

	@Test fun valuesTons() {
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

	@Test fun nameSingleDigit() {
		writer.writeHex("1b5d32323b321b5c")
		assertThat(parser.next()).isEqualTo(
			KittyPointerQueryNameEvent("2"),
		)
	}

	@Test fun nameLeadingValueDigit() {
		writer.writeHex("1b5d32323b30611b5c")
		assertThat(parser.next()).isEqualTo(
			KittyPointerQueryNameEvent("0a"),
		)
	}

	@Test fun nameValidRange() {
		writer.writeHex("1b5d32323b6162636465666768696a6b6c6d6e6f707172737475767778797a303132333435363738392d5f1b5c")
		assertThat(parser.next()).isEqualTo(
			KittyPointerQueryNameEvent("abcdefghijklmnopqrstuvwxyz0123456789-_"),
		)
	}

	@Test fun nameInvalidRange() {
		writer.writeHex("1b5d32323b6162633132334142431b5c")
		assertThat(parser.next()).isEqualTo(
			UnknownEvent("1b5d32323b6162633132334142431b5c".hexToByteArray()),
		)
	}
}
