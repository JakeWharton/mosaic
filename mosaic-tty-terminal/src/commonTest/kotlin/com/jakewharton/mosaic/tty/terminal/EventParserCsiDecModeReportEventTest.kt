package com.jakewharton.mosaic.tty.terminal

import assertk.assertThat
import assertk.assertions.isEqualTo
import com.jakewharton.mosaic.terminal.DecModeReportEvent
import com.jakewharton.mosaic.terminal.DecModeReportEvent.Setting.NotRecognized
import com.jakewharton.mosaic.terminal.DecModeReportEvent.Setting.PermanentlyReset
import com.jakewharton.mosaic.terminal.DecModeReportEvent.Setting.PermanentlySet
import com.jakewharton.mosaic.terminal.DecModeReportEvent.Setting.Reset
import com.jakewharton.mosaic.terminal.DecModeReportEvent.Setting.Set
import com.jakewharton.mosaic.terminal.UnknownEvent
import kotlin.test.Test

class EventParserCsiDecModeReportEventTest : BaseEventParserTest() {
	@Test fun settings() {
		testTty.writeHex("1b5b3f313030343b302479")
		assertThat(parser.next()).isEqualTo(
			DecModeReportEvent(
				mode = 1004,
				setting = NotRecognized,
			),
		)

		testTty.writeHex("1b5b3f313030343b312479")
		assertThat(parser.next()).isEqualTo(
			DecModeReportEvent(
				mode = 1004,
				setting = Set,
			),
		)

		testTty.writeHex("1b5b3f313030343b322479")
		assertThat(parser.next()).isEqualTo(
			DecModeReportEvent(
				mode = 1004,
				setting = Reset,
			),
		)

		testTty.writeHex("1b5b3f313030343b332479")
		assertThat(parser.next()).isEqualTo(
			DecModeReportEvent(
				mode = 1004,
				setting = PermanentlySet,
			),
		)

		testTty.writeHex("1b5b3f313030343b342479")
		assertThat(parser.next()).isEqualTo(
			DecModeReportEvent(
				mode = 1004,
				setting = PermanentlyReset,
			),
		)
	}

	@Test fun minimal() {
		testTty.writeHex("1b5b3f313b302479")
		assertThat(parser.next()).isEqualTo(
			DecModeReportEvent(
				mode = 1,
				setting = NotRecognized,
			),
		)
	}

	@Test fun unknownSetting() {
		testTty.writeHex("1b5b313030343b352479")
		assertThat(parser.next()).isEqualTo(
			UnknownEvent("1b5b313030343b352479".hexToByteArray()),
		)
	}

	@Test fun noQuestion() {
		testTty.writeHex("1b5b313030343b302479")
		assertThat(parser.next()).isEqualTo(
			UnknownEvent("1b5b313030343b302479".hexToByteArray()),
		)
	}

	@Test fun noDollar() {
		testTty.writeHex("1b5b3f313030343b3079")
		assertThat(parser.next()).isEqualTo(
			UnknownEvent("1b5b3f313030343b3079".hexToByteArray()),
		)
	}

	@Test fun noMode() {
		testTty.writeHex("1b5b3f3b3130302479")
		assertThat(parser.next()).isEqualTo(
			UnknownEvent("1b5b3f3b3130302479".hexToByteArray()),
		)
	}

	@Test fun nonDigitMode() {
		testTty.writeHex("1b5b3f31302d32343b302479")
		assertThat(parser.next()).isEqualTo(
			UnknownEvent("1b5b3f31302d32343b302479".hexToByteArray()),
		)
	}

	@Test fun noSetting() {
		testTty.writeHex("1b5b3f313030343b2479")
		assertThat(parser.next()).isEqualTo(
			UnknownEvent("1b5b3f313030343b2479".hexToByteArray()),
		)
	}

	@Test fun nonDigitSetting() {
		testTty.writeHex("1b5b3f313030343b312d322479")
		assertThat(parser.next()).isEqualTo(
			UnknownEvent("1b5b3f313030343b312d322479".hexToByteArray()),
		)
	}

	@Test fun noSemicolon() {
		testTty.writeHex("1b5b3f313030342479")
		assertThat(parser.next()).isEqualTo(
			UnknownEvent("1b5b3f313030342479".hexToByteArray()),
		)
	}
}
