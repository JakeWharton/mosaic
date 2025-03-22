package com.jakewharton.mosaic.tty.terminal

import assertk.assertThat
import assertk.assertions.isEqualTo
import com.jakewharton.mosaic.terminal.PrimaryDeviceAttributesEvent
import com.jakewharton.mosaic.terminal.UnknownEvent
import kotlin.test.Test

class EventParserCsiPrimaryDeviceAttributesEventTest : BaseEventParserTest() {
	@Test fun noLeadingQuestionMarkIsUnknown() {
		testTty.writeHex("1b5b303063")
		assertThat(parser.next()).isEqualTo(
			UnknownEvent("1b5b303063".hexToByteArray()),
		)
	}

	@Test fun emptyDataFails() {
		testTty.writeHex("1b5b3f63")
		assertThat(parser.next()).isEqualTo(
			UnknownEvent("1b5b3f63".hexToByteArray()),
		)
	}

	@Test fun idNoData() {
		testTty.writeHex("1b5b3f3263")
		assertThat(parser.next()).isEqualTo(PrimaryDeviceAttributesEvent(id = 2, data = ""))
	}

	@Test fun idWithSemicolonNoData() {
		testTty.writeHex("1b5b3f323b63")
		assertThat(parser.next()).isEqualTo(PrimaryDeviceAttributesEvent(id = 2, data = ""))
	}

	@Test fun idAndData() {
		testTty.writeHex("1b5b3f323b3263")
		assertThat(parser.next()).isEqualTo(PrimaryDeviceAttributesEvent(id = 2, data = "2"))
	}
}
