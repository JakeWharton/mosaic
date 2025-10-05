package com.jakewharton.mosaic.tty.terminal

import assertk.assertThat
import assertk.assertions.isEqualTo
import com.jakewharton.mosaic.terminal.ResizeEvent
import com.jakewharton.mosaic.terminal.UnknownEvent
import kotlin.test.Test

class EventParserCsiResizeEventTest : BaseEventParserTest() {
	@Test fun basic() {
		testTerminal.write("${CSI}48;1;2;3;4t")
		assertThat(parser.next()).isEqualTo(ResizeEvent(2, 1, 4, 3))
	}

	@Test fun pixelSizeAsZero() {
		testTerminal.write("${CSI}48;1;2;0;0t")
		assertThat(parser.next()).isEqualTo(ResizeEvent(2, 1, 0, 0))
	}

	@Test fun subparametersIgnored() {
		testTerminal.write("${CSI}48;1:99;2:98:97;3:99::97;4:99:t")
		assertThat(parser.next()).isEqualTo(ResizeEvent(2, 1, 4, 3))
	}

	@Test fun emptySubparametersIgnored() {
		testTerminal.write("${CSI}48;1:;2:;3:;4:t")
		assertThat(parser.next()).isEqualTo(ResizeEvent(2, 1, 4, 3))
	}

	@Test fun emptyModeFails() {
		testTerminal.write("$CSI;1;2;3;4t")
		assertThat(parser.next()).isEqualTo(
			UnknownEvent("1b5b3b313b323b333b3474".hexToByteArray()),
		)
	}

	@Test fun emptyParameterFails() {
		testTerminal.write("${CSI}48;;2;3;4t")
		assertThat(parser.next()).isEqualTo(
			UnknownEvent("1b5b34383b3b323b333b3474".hexToByteArray()),
		)
		testTerminal.write("${CSI}48;1;;3;4t")
		assertThat(parser.next()).isEqualTo(
			UnknownEvent("1b5b34383b313b3b333b3474".hexToByteArray()),
		)
		testTerminal.write("${CSI}48;1;2;;4t")
		assertThat(parser.next()).isEqualTo(
			UnknownEvent("1b5b34383b313b323b3b3474".hexToByteArray()),
		)
		testTerminal.write("${CSI}48;1;2;3;t")
		assertThat(parser.next()).isEqualTo(
			UnknownEvent("1b5b34383b313b323b333b74".hexToByteArray()),
		)
	}

	@Test fun nonDigitParameterFails() {
		testTerminal.write("${CSI}48;1.0;2;3;4t")
		assertThat(parser.next()).isEqualTo(
			UnknownEvent("1b5b34383b312e303b323b333b3474".hexToByteArray()),
		)
		testTerminal.write("${CSI}48;1;2.0;3;4t")
		assertThat(parser.next()).isEqualTo(
			UnknownEvent("1b5b34383b313b322e303b333b3474".hexToByteArray()),
		)
		testTerminal.write("${CSI}48;1;2;3.0;4t")
		assertThat(parser.next()).isEqualTo(
			UnknownEvent("1b5b34383b313b323b332e303b3474".hexToByteArray()),
		)
		testTerminal.write("${CSI}48;1;2;3;4.0t")
		assertThat(parser.next()).isEqualTo(
			UnknownEvent("1b5b34383b313b323b333b342e3074".hexToByteArray()),
		)
	}
}
