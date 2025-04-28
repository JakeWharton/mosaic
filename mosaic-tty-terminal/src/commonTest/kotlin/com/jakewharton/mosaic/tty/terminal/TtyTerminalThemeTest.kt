package com.jakewharton.mosaic.tty.terminal

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isTrue
import com.jakewharton.mosaic.terminal.SystemThemeEvent
import com.jakewharton.mosaic.terminal.Terminal.Theme.Dark
import com.jakewharton.mosaic.terminal.Terminal.Theme.Light
import com.jakewharton.mosaic.terminal.Terminal.Theme.Unknown
import kotlin.test.Test

class TtyTerminalThemeTest {
	@Test fun noModeOrThemeReply() = terminalTest {
		expect("${CSI}0c", reply = "$CSI?62;22c")
		expect("${CSI}5n", reply = "${CSI}0n")

		val teardown = withTerminal { setup ->
			assertThat(capabilities.themeEvents).isFalse()
			assertThat(state.theme.value).isEqualTo(Unknown)

			// No attempt to enable theme events.
			assertThat(setup).doesNotContain("$CSI?2031h")

			// Write an unsolicited theme=dark event which should be ignored.
			ptyWrite("$CSI?997;1n")
			assertThat(events.receive()).isEqualTo(SystemThemeEvent(isDark = true))
			assertThat(state.theme.value).isEqualTo(Unknown)
		}

		// No attempt to disable theme events.
		assertThat(teardown).doesNotContain("$CSI?2031l")
	}

	@Test fun noModeReplyWithThemeReply() = terminalTest {
		expect("${CSI}0c", reply = "$CSI?62;22c")
		expect("$CSI?996n", reply = "$CSI?997;2n")
		expect("${CSI}5n", reply = "${CSI}0n")

		val teardown = withTerminal { setup ->
			assertThat(capabilities.themeEvents).isFalse()
			assertThat(state.theme.value).isEqualTo(Light)

			// No attempt to enable theme events.
			assertThat(setup).doesNotContain("$CSI?2031h")

			// Write an unsolicited theme=dark event which should be ignored.
			ptyWrite("$CSI?997;1n")
			assertThat(events.receive()).isEqualTo(SystemThemeEvent(isDark = true))
			assertThat(state.theme.value).isEqualTo(Light)
		}

		// No attempt to disable theme events.
		assertThat(teardown).doesNotContain("$CSI?2031l")
	}

	@Test fun replyModeSetWithoutThemeReply() = terminalTest {
		expect("${CSI}0c", reply = "$CSI?62;22c")
		// Theme events are set (i.e, enabled).
		expect("$CSI?2031\$p", reply = "$CSI?2031;1\$y")
		expect("${CSI}5n", reply = "${CSI}0n")

		val teardown = withTerminal { setup ->
			assertThat(capabilities.themeEvents).isTrue()
			assertThat(state.theme.value).isEqualTo(Unknown)

			// Theme events are not re-enabled.
			assertThat(setup).doesNotContain("$CSI?2031h")

			// Write theme events which should be honored.
			ptyWrite("$CSI?997;1n")
			assertThat(events.receive()).isEqualTo(SystemThemeEvent(isDark = true))
			assertThat(state.theme.value).isEqualTo(Dark)
			ptyWrite("$CSI?997;2n")
			assertThat(events.receive()).isEqualTo(SystemThemeEvent(isDark = false))
			assertThat(state.theme.value).isEqualTo(Light)
		}

		// Theme events are left enabled.
		assertThat(teardown).doesNotContain("$CSI?2031l")
	}

	@Test fun replyModeSetWithThemeReply() = terminalTest {
		expect("${CSI}0c", reply = "$CSI?62;22c")
		// Theme events are set (i.e, enabled).
		expect("$CSI?2031\$p", reply = "$CSI?2031;1\$y")
		expect("$CSI?996n", reply = "$CSI?997;2n")
		expect("${CSI}5n", reply = "${CSI}0n")

		val teardown = withTerminal { setup ->
			assertThat(capabilities.themeEvents).isTrue()
			assertThat(state.theme.value).isEqualTo(Light)

			// Theme events are not re-enabled.
			assertThat(setup).doesNotContain("$CSI?2031h")

			// Write theme events which should be honored.
			ptyWrite("$CSI?997;1n")
			assertThat(events.receive()).isEqualTo(SystemThemeEvent(isDark = true))
			assertThat(state.theme.value).isEqualTo(Dark)
			ptyWrite("$CSI?997;2n")
			assertThat(events.receive()).isEqualTo(SystemThemeEvent(isDark = false))
			assertThat(state.theme.value).isEqualTo(Light)
		}

		// Theme events are left enabled.
		assertThat(teardown).doesNotContain("$CSI?2031l")
	}

	@Test fun replyModeResetWithoutThemeReply() = terminalTest {
		expect("${CSI}0c", reply = "$CSI?62;22c")
		// Theme events are reset (i.e, not enabled).
		expect("$CSI?2031\$p", reply = "$CSI?2031;2\$y")
		expect("${CSI}5n", reply = "${CSI}0n")

		val teardown = withTerminal { setup ->
			assertThat(capabilities.themeEvents).isTrue()
			assertThat(state.theme.value).isEqualTo(Unknown)

			// Enable theme events.
			assertThat(setup).contains("$CSI?2031h")

			// Write theme events which should be honored.
			ptyWrite("$CSI?997;1n")
			assertThat(events.receive()).isEqualTo(SystemThemeEvent(isDark = true))
			assertThat(state.theme.value).isEqualTo(Dark)
			ptyWrite("$CSI?997;2n")
			assertThat(events.receive()).isEqualTo(SystemThemeEvent(isDark = false))
			assertThat(state.theme.value).isEqualTo(Light)
		}

		// Disable theme events.
		assertThat(teardown).contains("$CSI?2031l")
	}

	@Test fun replyModeResetWithThemeReply() = terminalTest {
		expect("${CSI}0c", reply = "$CSI?62;22c")
		// Theme events are reset (i.e, not enabled).
		expect("$CSI?2031\$p", reply = "$CSI?2031;2\$y")
		expect("$CSI?996n", reply = "$CSI?997;2n")
		expect("${CSI}5n", reply = "${CSI}0n")

		val teardown = withTerminal { setup ->
			assertThat(capabilities.themeEvents).isTrue()
			assertThat(state.theme.value).isEqualTo(Light)

			// Enable theme events.
			assertThat(setup).contains("$CSI?2031h")

			// Write theme events which should be honored.
			ptyWrite("$CSI?997;1n")
			assertThat(events.receive()).isEqualTo(SystemThemeEvent(isDark = true))
			assertThat(state.theme.value).isEqualTo(Dark)
			ptyWrite("$CSI?997;2n")
			assertThat(events.receive()).isEqualTo(SystemThemeEvent(isDark = false))
			assertThat(state.theme.value).isEqualTo(Light)
		}

		// Disable theme events.
		assertThat(teardown).contains("$CSI?2031l")
	}

	@Test fun replyModePermanentlySetWithoutThemeReply() = terminalTest {
		expect("${CSI}0c", reply = "$CSI?62;22c")
		// Theme events are permanently set (i.e, always enabled).
		expect("$CSI?2031\$p", reply = "$CSI?2031;3\$y")
		expect("${CSI}5n", reply = "${CSI}0n")

		val teardown = withTerminal { setup ->
			assertThat(capabilities.themeEvents).isTrue()
			assertThat(state.theme.value).isEqualTo(Unknown)

			// No attempt to enable theme events.
			assertThat(setup).doesNotContain("$CSI?2031h")

			// Write theme events which should be honored.
			ptyWrite("$CSI?997;1n")
			assertThat(events.receive()).isEqualTo(SystemThemeEvent(isDark = true))
			assertThat(state.theme.value).isEqualTo(Dark)
			ptyWrite("$CSI?997;2n")
			assertThat(events.receive()).isEqualTo(SystemThemeEvent(isDark = false))
			assertThat(state.theme.value).isEqualTo(Light)
		}

		// No attempt to disable theme events.
		assertThat(teardown).doesNotContain("$CSI?2031l")
	}

	@Test fun replyModePermanentlySetWithThemeReply() = terminalTest {
		expect("${CSI}0c", reply = "$CSI?62;22c")
		// Theme events are permanently set (i.e, always enabled).
		expect("$CSI?2031\$p", reply = "$CSI?2031;3\$y")
		expect("$CSI?996n", reply = "$CSI?997;2n")
		expect("${CSI}5n", reply = "${CSI}0n")

		val teardown = withTerminal { setup ->
			assertThat(capabilities.themeEvents).isTrue()
			assertThat(state.theme.value).isEqualTo(Light)

			// No attempt to enable theme events.
			assertThat(setup).doesNotContain("$CSI?2031h")

			// Write theme events which should be honored.
			ptyWrite("$CSI?997;1n")
			assertThat(events.receive()).isEqualTo(SystemThemeEvent(isDark = true))
			assertThat(state.theme.value).isEqualTo(Dark)
			ptyWrite("$CSI?997;2n")
			assertThat(events.receive()).isEqualTo(SystemThemeEvent(isDark = false))
			assertThat(state.theme.value).isEqualTo(Light)
		}

		// No attempt to disable theme events.
		assertThat(teardown).doesNotContain("$CSI?2031l")
	}

	@Test fun replyModePermanentlyResetWithoutThemeReply() = terminalTest {
		expect("${CSI}0c", reply = "$CSI?62;22c")
		// Theme events are permanently reset (i.e, not supported).
		expect("$CSI?2031\$p", reply = "$CSI?2031;4\$y")
		expect("${CSI}5n", reply = "${CSI}0n")

		val teardown = withTerminal { setup ->
			assertThat(capabilities.themeEvents).isFalse()
			assertThat(state.theme.value).isEqualTo(Unknown)

			// No attempt to enable theme events.
			assertThat(setup).doesNotContain("$CSI?2031h")

			// Write an unsolicited theme=dark event which should be ignored.
			ptyWrite("$CSI?997;1n")
			assertThat(events.receive()).isEqualTo(SystemThemeEvent(isDark = true))
			assertThat(state.theme.value).isEqualTo(Unknown)
		}

		// No attempt to disable theme events.
		assertThat(teardown).doesNotContain("$CSI?2031l")
	}

	@Test fun replyModePermanentlyResetWithThemeReply() = terminalTest {
		expect("${CSI}0c", reply = "$CSI?62;22c")
		// Theme events are permanently reset (i.e, not supported).
		expect("$CSI?2031\$p", reply = "$CSI?2031;4\$y")
		expect("$CSI?996n", reply = "$CSI?997;2n")
		expect("${CSI}5n", reply = "${CSI}0n")

		val teardown = withTerminal { setup ->
			assertThat(capabilities.themeEvents).isFalse()
			assertThat(state.theme.value).isEqualTo(Light)

			// No attempt to enable theme events.
			assertThat(setup).doesNotContain("$CSI?2031h")

			// Write an unsolicited theme=dark event which should be ignored.
			ptyWrite("$CSI?997;1n")
			assertThat(events.receive()).isEqualTo(SystemThemeEvent(isDark = true))
			assertThat(state.theme.value).isEqualTo(Light)
		}

		// No attempt to disable theme events.
		assertThat(teardown).doesNotContain("$CSI?2031l")
	}
}
