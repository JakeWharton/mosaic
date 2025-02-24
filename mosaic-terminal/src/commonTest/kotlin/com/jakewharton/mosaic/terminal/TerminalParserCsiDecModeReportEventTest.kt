package com.jakewharton.mosaic.terminal

import assertk.assertThat
import assertk.assertions.isEqualTo
import com.jakewharton.mosaic.terminal.event.DecModeReportEvent
import com.jakewharton.mosaic.terminal.event.DecModeReportEvent.Setting.NotRecognized
import com.jakewharton.mosaic.terminal.event.DecModeReportEvent.Setting.PermanentlyReset
import com.jakewharton.mosaic.terminal.event.DecModeReportEvent.Setting.PermanentlySet
import com.jakewharton.mosaic.terminal.event.DecModeReportEvent.Setting.Reset
import com.jakewharton.mosaic.terminal.event.DecModeReportEvent.Setting.Set
import com.jakewharton.mosaic.terminal.event.UnknownEvent
import kotlin.test.Test
import kotlinx.coroutines.test.runTest

class TerminalParserCsiDecModeReportEventTest : BaseTerminalParserTest() {
	@Test fun settings() = runTest {
		testTty.writeHex("1b5b3f313030343b302479")
		assertThat(reader.next()).isEqualTo(
			DecModeReportEvent(
				mode = 1004,
				setting = NotRecognized,
			),
		)

		testTty.writeHex("1b5b3f313030343b312479")
		assertThat(reader.next()).isEqualTo(
			DecModeReportEvent(
				mode = 1004,
				setting = Set,
			),
		)

		testTty.writeHex("1b5b3f313030343b322479")
		assertThat(reader.next()).isEqualTo(
			DecModeReportEvent(
				mode = 1004,
				setting = Reset,
			),
		)

		testTty.writeHex("1b5b3f313030343b332479")
		assertThat(reader.next()).isEqualTo(
			DecModeReportEvent(
				mode = 1004,
				setting = PermanentlySet,
			),
		)

		testTty.writeHex("1b5b3f313030343b342479")
		assertThat(reader.next()).isEqualTo(
			DecModeReportEvent(
				mode = 1004,
				setting = PermanentlyReset,
			),
		)
	}

	@Test fun minimal() = runTest {
		testTty.writeHex("1b5b3f313b302479")
		assertThat(reader.next()).isEqualTo(
			DecModeReportEvent(
				mode = 1,
				setting = NotRecognized,
			),
		)
	}

	@Test fun unknownSetting() = runTest {
		testTty.writeHex("1b5b313030343b352479")
		assertThat(reader.next()).isEqualTo(
			UnknownEvent("1b5b313030343b352479".hexToByteArray()),
		)
	}

	@Test fun noQuestion() = runTest {
		testTty.writeHex("1b5b313030343b302479")
		assertThat(reader.next()).isEqualTo(
			UnknownEvent("1b5b313030343b302479".hexToByteArray()),
		)
	}

	@Test fun noDollar() = runTest {
		testTty.writeHex("1b5b3f313030343b3079")
		assertThat(reader.next()).isEqualTo(
			UnknownEvent("1b5b3f313030343b3079".hexToByteArray()),
		)
	}

	@Test fun noMode() = runTest {
		testTty.writeHex("1b5b3f3b3130302479")
		assertThat(reader.next()).isEqualTo(
			UnknownEvent("1b5b3f3b3130302479".hexToByteArray()),
		)
	}

	@Test fun nonDigitMode() = runTest {
		testTty.writeHex("1b5b3f31302d32343b302479")
		assertThat(reader.next()).isEqualTo(
			UnknownEvent("1b5b3f31302d32343b302479".hexToByteArray()),
		)
	}

	@Test fun noSetting() = runTest {
		testTty.writeHex("1b5b3f313030343b2479")
		assertThat(reader.next()).isEqualTo(
			UnknownEvent("1b5b3f313030343b2479".hexToByteArray()),
		)
	}

	@Test fun nonDigitSetting() = runTest {
		testTty.writeHex("1b5b3f313030343b312d322479")
		assertThat(reader.next()).isEqualTo(
			UnknownEvent("1b5b3f313030343b312d322479".hexToByteArray()),
		)
	}

	@Test fun noSemicolon() = runTest {
		testTty.writeHex("1b5b3f313030342479")
		assertThat(reader.next()).isEqualTo(
			UnknownEvent("1b5b3f313030342479".hexToByteArray()),
		)
	}
}
