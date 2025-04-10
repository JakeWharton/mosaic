package com.jakewharton.mosaic.tty.terminal

import assertk.assertThat
import assertk.assertions.isEqualTo
import com.jakewharton.mosaic.terminal.PrimaryDeviceAttributesEvent
import com.jakewharton.mosaic.terminal.UnknownEvent
import kotlin.test.Test

class EventParserCsiPrimaryDeviceAttributesEventTest : BaseEventParserTest() {
	@Test fun noLeadingQuestionMarkIsUnknown() {
		testTty.write("${CSI}00c")
		assertThat(parser.next()).isEqualTo(
			UnknownEvent("1b5b303063".hexToByteArray()),
		)
	}

	@Test fun emptyDataFails() {
		testTty.write("$CSI?c")
		assertThat(parser.next()).isEqualTo(
			UnknownEvent("1b5b3f63".hexToByteArray()),
		)
	}

	@Test fun idNoData() {
		testTty.write("$CSI?2c")
		assertThat(parser.next()).isEqualTo(PrimaryDeviceAttributesEvent(id = 2, data = ""))
	}

	@Test fun idWithSemicolonNoData() {
		testTty.write("$CSI?2;c")
		assertThat(parser.next()).isEqualTo(PrimaryDeviceAttributesEvent(id = 2, data = ""))
	}

	@Test fun idAndData() {
		testTty.write("$CSI?2;2c")
		assertThat(parser.next()).isEqualTo(PrimaryDeviceAttributesEvent(id = 2, data = "2"))
	}
}
