package com.jakewharton.mosaic.tty.terminal

import assertk.assertThat
import assertk.assertions.isFalse
import assertk.assertions.isTrue
import kotlin.test.Test

class TtyTerminalFocusTest {
	@Test fun noReply() = terminalTest {
		expect("${CSI}0c", reply = "$CSI?62;22c")
		expect("${CSI}5n", reply = "${CSI}0n")

		val teardown = withTerminal { setup ->
			assertThat(capabilities.focusEvents).isFalse()

			// No attempt to enable focus events.
			assertThat(setup).doesNotContain("$CSI?1004h")
		}

		// No attempt to disable focus events.
		assertThat(teardown).doesNotContain("$CSI?1004l")
	}

	@Test fun replySet() = terminalTest {
		expect("${CSI}0c", reply = "$CSI?62;22c")
		// Focus events are set (i.e, enabled).
		expect("$CSI?1004\$p", reply = "$CSI?1004;1\$y")
		expect("${CSI}5n", reply = "${CSI}0n")

		val teardown = withTerminal { setup ->
			assertThat(capabilities.focusEvents).isTrue()

			// Focus events are not re-enabled.
			assertThat(setup).doesNotContain("$CSI?1004h")
		}

		// Focus events are left enabled.
		assertThat(teardown).doesNotContain("$CSI?1004l")
	}

	@Test fun replyReset() = terminalTest {
		expect("${CSI}0c", reply = "$CSI?62;22c")
		// Focus events are reset (i.e, not enabled).
		expect("$CSI?1004\$p", reply = "$CSI?1004;2\$y")
		expect("${CSI}5n", reply = "${CSI}0n")

		val teardown = withTerminal { setup ->
			assertThat(capabilities.focusEvents).isTrue()

			// Enable focus events.
			assertThat(setup).contains("$CSI?1004h")
		}

		// Disable focus events.
		assertThat(teardown).contains("$CSI?1004l")
	}

	@Test fun replyPermanentlySet() = terminalTest {
		expect("${CSI}0c", reply = "$CSI?62;22c")
		// Focus events are permanently set (i.e, always enabled).
		expect("$CSI?1004\$p", reply = "$CSI?1004;3\$y")
		expect("${CSI}5n", reply = "${CSI}0n")

		val teardown = withTerminal { setup ->
			assertThat(capabilities.focusEvents).isTrue()

			// No attempt to enable focus events.
			assertThat(setup).doesNotContain("$CSI?1004h")
		}

		// No attempt to disable focus events.
		assertThat(teardown).doesNotContain("$CSI?1004l")
	}

	@Test fun replyPermanentlyReset() = terminalTest {
		expect("${CSI}0c", reply = "$CSI?62;22c")
		// Focus events are permanently reset (i.e, not supported).
		expect("$CSI?1004\$p", reply = "$CSI?1004;4\$y")
		expect("${CSI}5n", reply = "${CSI}0n")

		val teardown = withTerminal { setup ->
			assertThat(capabilities.focusEvents).isFalse()

			// No attempt to enable focus events.
			assertThat(setup).doesNotContain("$CSI?1004h")
		}

		// No attempt to disable focus events.
		assertThat(teardown).doesNotContain("$CSI?1004l")
	}
}
