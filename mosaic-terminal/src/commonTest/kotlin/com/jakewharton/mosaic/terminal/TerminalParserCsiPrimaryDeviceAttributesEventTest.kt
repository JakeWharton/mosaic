package com.jakewharton.mosaic.terminal

import assertk.assertThat
import assertk.assertions.isEqualTo
import com.jakewharton.mosaic.terminal.event.PrimaryDeviceAttributesEvent
import com.jakewharton.mosaic.terminal.event.UnknownEvent
import kotlin.test.AfterTest
import kotlin.test.Test

@OptIn(ExperimentalStdlibApi::class)
class TerminalParserCsiPrimaryDeviceAttributesEventTest {
	private val writer = Tty.stdinWriter()
	private val parser = TerminalParser(writer.reader, true)

	@AfterTest fun after() {
		writer.close()
	}

	@Test fun noLeadingQuestionMarkIsUnknown() {
		writer.writeHex("1b5b303063")
		assertThat(parser.next()).isEqualTo(
			UnknownEvent("1b5b303063".hexToByteArray()),
		)
	}

	@Test fun emptyData() {
		writer.writeHex("1b5b3f63")
		assertThat(parser.next()).isEqualTo(PrimaryDeviceAttributesEvent(data = ""))
	}

	@Test fun data() {
		writer.writeHex("1b5b3f323b3263")
		assertThat(parser.next()).isEqualTo(PrimaryDeviceAttributesEvent(data = "2;2"))
	}
}
