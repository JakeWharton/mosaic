package com.jakewharton.mosaic.tty.terminal

import assertk.assertThat
import assertk.assertions.isFalse
import assertk.assertions.isTrue
import kotlin.test.Test

class TtyTerminalThemeTest {
	@Test fun noReply() = terminalTest {
		expect("${CSI}0c", reply = "$CSI?62;22c")
		expect("${CSI}5n", reply = "${CSI}0n")

		val teardown = withTerminal { setup ->
			assertThat(capabilities.themeEvents).isFalse()

			// Do not try to enable theme events.
			assertThat(setup).doesNotContain("$CSI?2031h")
		}

		// Do not try to disable theme events.
		assertThat(teardown).doesNotContain("$CSI?2031l")
	}

	@Test fun replySet() = terminalTest {
		expect("${CSI}0c", reply = "$CSI?62;22c")
		// Theme events are set (i.e, enabled).
		expect("$CSI?2031\$p", reply = "$CSI?2031;1\$y")
		expect("${CSI}5n", reply = "${CSI}0n")

		val teardown = withTerminal { setup ->
			assertThat(capabilities.themeEvents).isTrue()

			// Do not try to enable theme events.
			assertThat(setup).doesNotContain("$CSI?2031h")
		}

		// Do not try to disable theme events.
		assertThat(teardown).doesNotContain("$CSI?2031l")
	}

	@Test fun replyReset() = terminalTest {
		expect("${CSI}0c", reply = "$CSI?62;22c")
		// Theme events are reset (i.e, not enabled).
		expect("$CSI?2031\$p", reply = "$CSI?2031;2\$y")
		expect("${CSI}5n", reply = "${CSI}0n")

		val teardown = withTerminal { setup ->
			assertThat(capabilities.themeEvents).isTrue()

			// Enable theme events.
			assertThat(setup).contains("$CSI?2031h")
		}

		// Disable theme events.
		assertThat(teardown).contains("$CSI?2031l")
	}
}
