package com.jakewharton.mosaic.terminal

import assertk.assertThat
import assertk.assertions.isEqualTo
import com.jakewharton.mosaic.terminal.event.KittyKeyboardQueryEvent
import com.jakewharton.mosaic.terminal.event.UnknownEvent
import kotlin.test.Test
import kotlinx.coroutines.test.runTest

class TerminalParserCsiKittyKeyboardQueryEventTest : BaseTerminalParserTest() {
	@Test fun flagsNone() = runTest {
		testTty.writeHex("1b5b3f3075")
		assertThat(parser.next()).isEqualTo(KittyKeyboardQueryEvent(0))
	}

	@Test fun flagsAll() = runTest {
		testTty.writeHex("1b5b3f333175")
		assertThat(parser.next()).isEqualTo(KittyKeyboardQueryEvent(31))
	}

	@Test fun flagsUnknown() = runTest {
		testTty.writeHex("1b5b3f31323875")
		assertThat(parser.next()).isEqualTo(KittyKeyboardQueryEvent(128))
	}

	@Test fun flagsMissing() = runTest {
		testTty.writeHex("1b5b3f75")
		assertThat(parser.next()).isEqualTo(
			UnknownEvent("1b5b3f75".hexToByteArray()),
		)
	}

	@Test fun flagsNonDigit() = runTest {
		testTty.writeHex("1b5b3f312b2075")
		assertThat(parser.next()).isEqualTo(
			UnknownEvent("1b5b3f312b2075".hexToByteArray()),
		)
	}
}
