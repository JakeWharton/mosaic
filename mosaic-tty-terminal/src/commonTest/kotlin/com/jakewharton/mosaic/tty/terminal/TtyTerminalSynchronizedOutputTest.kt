package com.jakewharton.mosaic.tty.terminal

import assertk.assertThat
import assertk.assertions.isFalse
import assertk.assertions.isTrue
import kotlin.test.Test

class TtyTerminalSynchronizedOutputTest {
	@Test fun noReply() = terminalTest {
		expect("${CSI}0c", reply = "$CSI?62;22c")
		expect("${CSI}5n", reply = "${CSI}0n")

		val teardown = withTerminal { setup ->
			assertThat(capabilities.synchronizedOutput).isFalse()

			// Do not try to enable synchronized output.
			assertThat(setup).doesNotContain("$CSI?2026h")
		}

		// Do not try to disable synchronized output.
		assertThat(teardown).doesNotContain("$CSI?2026l")
	}

	@Test fun replySet() = terminalTest {
		expect("${CSI}0c", reply = "$CSI?62;22c")
		// Theme events are set (i.e, enabled).
		expect("$CSI?2026\$p", reply = "$CSI?2026;1\$y")
		expect("${CSI}5n", reply = "${CSI}0n")

		val teardown = withTerminal { setup ->
			assertThat(capabilities.synchronizedOutput).isTrue()

			// Do not try to enable synchronized output.
			assertThat(setup).doesNotContain("$CSI?2026h")
		}

		// Do not try to disable synchronized output.
		assertThat(teardown).doesNotContain("$CSI?2026l")
	}

	@Test fun replyReset() = terminalTest {
		expect("${CSI}0c", reply = "$CSI?62;22c")
		// Theme events are reset (i.e, not enabled).
		expect("$CSI?2026\$p", reply = "$CSI?2026;2\$y")
		expect("${CSI}5n", reply = "${CSI}0n")

		val teardown = withTerminal { setup ->
			assertThat(capabilities.synchronizedOutput).isTrue()

			// Do not try to enable synchronized output. Even though supported, it's used per frame.
			assertThat(setup).doesNotContain("$CSI?2026h")
		}

		// Do not try to disable synchronized output.
		assertThat(teardown).doesNotContain("$CSI?2026l")
	}
}
