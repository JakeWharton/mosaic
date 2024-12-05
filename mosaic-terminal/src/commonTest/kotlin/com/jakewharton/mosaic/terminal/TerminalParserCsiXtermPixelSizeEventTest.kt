package com.jakewharton.mosaic.terminal

import assertk.assertThat
import assertk.assertions.isEqualTo
import com.jakewharton.mosaic.terminal.event.UnknownEvent
import com.jakewharton.mosaic.terminal.event.XtermPixelSizeEvent
import kotlin.test.AfterTest
import kotlin.test.Test

@OptIn(ExperimentalStdlibApi::class)
class TerminalParserCsiXtermPixelSizeEventTest {
	private val writer = Tty.stdinWriter()
	private val parser = TerminalParser(writer.reader)

	@AfterTest fun after() {
		writer.close()
	}

	@Test fun basic() {
		writer.writeHex("1b5b343b313b3274")
		assertThat(parser.next()).isEqualTo(XtermPixelSizeEvent(1, 2))
	}

	@Test fun emptyParameterFails() {
		writer.writeHex("1b5b343b3b3274")
		assertThat(parser.next()).isEqualTo(
			UnknownEvent("1b5b343b3b3274".hexToByteArray()),
		)
		writer.writeHex("1b5b343b313b74")
		assertThat(parser.next()).isEqualTo(
			UnknownEvent("1b5b343b313b74".hexToByteArray()),
		)
	}

	@Test fun nonDigitParameterFails() {
		writer.writeHex("1b5b343b223b3274")
		assertThat(parser.next()).isEqualTo(
			UnknownEvent("1b5b343b223b3274".hexToByteArray()),
		)
		writer.writeHex("1b5b343b313b2274")
		assertThat(parser.next()).isEqualTo(
			UnknownEvent("1b5b343b313b2274".hexToByteArray()),
		)
	}
}
