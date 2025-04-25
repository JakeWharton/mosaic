package com.jakewharton.mosaic.tty.terminal

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isTrue
import com.jakewharton.mosaic.terminal.FocusEvent
import kotlin.test.Test

class TtyTerminalFocusTest {
	@Test fun noReply() = terminalTest {
		expect("${CSI}0c", reply = "$CSI?62;22c")
		expect("${CSI}5n", reply = "${CSI}0n")

		val teardown = withTerminal { setup ->
			assertThat(capabilities.focusEvents).isFalse()
			assertThat(state.focused.value).isTrue()

			// No attempt to enable focus events.
			assertThat(setup).doesNotContain("$CSI?1004h")

			// Write an unsolicited focus=false event which should be ignored.
			ptyWrite("${CSI}O")
			assertThat(events.receive()).isEqualTo(FocusEvent(focused = false))
			assertThat(state.focused.value).isTrue()
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
			assertThat(state.focused.value).isTrue()

			// Focus events are not re-enabled.
			assertThat(setup).doesNotContain("$CSI?1004h")

			// Write focus events which should be honored.
			ptyWrite("${CSI}O")
			assertThat(events.receive()).isEqualTo(FocusEvent(focused = false))
			assertThat(state.focused.value).isFalse()
			ptyWrite("${CSI}I")
			assertThat(events.receive()).isEqualTo(FocusEvent(focused = true))
			assertThat(state.focused.value).isTrue()
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
			assertThat(state.focused.value).isTrue()

			// Enable focus events.
			assertThat(setup).contains("$CSI?1004h")

			// Write focus events which should be honored.
			ptyWrite("${CSI}O")
			assertThat(events.receive()).isEqualTo(FocusEvent(focused = false))
			assertThat(state.focused.value).isFalse()
			ptyWrite("${CSI}I")
			assertThat(events.receive()).isEqualTo(FocusEvent(focused = true))
			assertThat(state.focused.value).isTrue()
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
			assertThat(state.focused.value).isTrue()

			// No attempt to enable focus events.
			assertThat(setup).doesNotContain("$CSI?1004h")

			// Write focus events which should be honored.
			ptyWrite("${CSI}O")
			assertThat(events.receive()).isEqualTo(FocusEvent(focused = false))
			assertThat(state.focused.value).isFalse()
			ptyWrite("${CSI}I")
			assertThat(events.receive()).isEqualTo(FocusEvent(focused = true))
			assertThat(state.focused.value).isTrue()
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
			assertThat(state.focused.value).isTrue()

			// No attempt to enable focus events.
			assertThat(setup).doesNotContain("$CSI?1004h")

			// Write an unsolicited focus=false event which should be ignored.
			ptyWrite("${CSI}O")
			assertThat(events.receive()).isEqualTo(FocusEvent(focused = false))
			assertThat(state.focused.value).isTrue()
		}

		// No attempt to disable focus events.
		assertThat(teardown).doesNotContain("$CSI?1004l")
	}
}
