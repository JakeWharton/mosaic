package com.jakewharton.mosaic.terminal

import assertk.assertThat
import assertk.assertions.isEqualTo
import com.jakewharton.mosaic.terminal.event.PrimaryDeviceAttributesEvent
import com.jakewharton.mosaic.terminal.event.UnknownEvent
import kotlin.test.Test
import kotlinx.coroutines.test.runTest

class TerminalParserCsiPrimaryDeviceAttributesEventTest : BaseTerminalParserTest() {
	@Test fun noLeadingQuestionMarkIsUnknown() = runTest {
		testTty.writeHex("1b5b303063")
		assertThat(parser.next()).isEqualTo(
			UnknownEvent("1b5b303063".hexToByteArray()),
		)
	}

	@Test fun emptyDataFails() = runTest {
		testTty.writeHex("1b5b3f63")
		assertThat(parser.next()).isEqualTo(
			UnknownEvent("1b5b3f63".hexToByteArray()),
		)
	}

	@Test fun idNoData() = runTest {
		testTty.writeHex("1b5b3f3263")
		assertThat(parser.next()).isEqualTo(PrimaryDeviceAttributesEvent(id = 2, data = ""))
	}

	@Test fun idWithSemicolonNoData() = runTest {
		testTty.writeHex("1b5b3f323b63")
		assertThat(parser.next()).isEqualTo(PrimaryDeviceAttributesEvent(id = 2, data = ""))
	}

	@Test fun idAndData() = runTest {
		testTty.writeHex("1b5b3f323b3263")
		assertThat(parser.next()).isEqualTo(PrimaryDeviceAttributesEvent(id = 2, data = "2"))
	}
}
