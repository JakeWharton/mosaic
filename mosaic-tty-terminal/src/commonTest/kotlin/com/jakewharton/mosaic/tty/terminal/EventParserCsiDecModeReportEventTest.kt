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
		testTty.write("$CSI?1004;0\$y")
		assertThat(parser.next()).isEqualTo(
			DecModeReportEvent(
				mode = 1004,
				setting = NotRecognized,
			),
		)

		testTty.write("$CSI?1004;1\$y")
		assertThat(parser.next()).isEqualTo(
			DecModeReportEvent(
				mode = 1004,
				setting = Set,
			),
		)

		testTty.write("$CSI?1004;2\$y")
		assertThat(parser.next()).isEqualTo(
			DecModeReportEvent(
				mode = 1004,
				setting = Reset,
			),
		)

		testTty.write("$CSI?1004;3\$y")
		assertThat(parser.next()).isEqualTo(
			DecModeReportEvent(
				mode = 1004,
				setting = PermanentlySet,
			),
		)

		testTty.write("$CSI?1004;4\$y")
		assertThat(parser.next()).isEqualTo(
			DecModeReportEvent(
				mode = 1004,
				setting = PermanentlyReset,
			),
		)
	}

	@Test fun minimal() {
		testTty.write("$CSI?1;0\$y")
		assertThat(parser.next()).isEqualTo(
			DecModeReportEvent(
				mode = 1,
				setting = NotRecognized,
			),
		)
	}

	@Test fun unknownSetting() {
		testTty.write("${CSI}1004;5\$y")
		assertThat(parser.next()).isEqualTo(
			UnknownEvent("1b5b313030343b352479".hexToByteArray()),
		)
	}

	@Test fun noQuestion() {
		testTty.write("${CSI}1004;0\$y")
		assertThat(parser.next()).isEqualTo(
			UnknownEvent("1b5b313030343b302479".hexToByteArray()),
		)
	}

	@Test fun noDollar() {
		testTty.write("$CSI?1004;0y")
		assertThat(parser.next()).isEqualTo(
			UnknownEvent("1b5b3f313030343b3079".hexToByteArray()),
		)
	}

	@Test fun noMode() {
		testTty.write("$CSI?;100\$y")
		assertThat(parser.next()).isEqualTo(
			UnknownEvent("1b5b3f3b3130302479".hexToByteArray()),
		)
	}

	@Test fun nonDigitMode() {
		testTty.write("$CSI?10-24;0\$y")
		assertThat(parser.next()).isEqualTo(
			UnknownEvent("1b5b3f31302d32343b302479".hexToByteArray()),
		)
	}

	@Test fun noSetting() {
		testTty.write("$CSI?1004;\$y")
		assertThat(parser.next()).isEqualTo(
			UnknownEvent("1b5b3f313030343b2479".hexToByteArray()),
		)
	}

	@Test fun nonDigitSetting() {
		testTty.write("$CSI?1004;1-2\$y")
		assertThat(parser.next()).isEqualTo(
			UnknownEvent("1b5b3f313030343b312d322479".hexToByteArray()),
		)
	}

	@Test fun noSemicolon() {
		testTty.write("$CSI?1004\$y")
		assertThat(parser.next()).isEqualTo(
			UnknownEvent("1b5b3f313030342479".hexToByteArray()),
		)
	}
}
