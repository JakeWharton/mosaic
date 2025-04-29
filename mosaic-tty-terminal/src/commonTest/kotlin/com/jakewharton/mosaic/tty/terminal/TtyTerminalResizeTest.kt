package com.jakewharton.mosaic.tty.terminal

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isTrue
import com.jakewharton.mosaic.terminal.ResizeEvent
import com.jakewharton.mosaic.terminal.Terminal
import kotlin.test.Test

class TtyTerminalResizeTest {
	@Test fun noReply() = terminalTest {
		expect("${CSI}0c", reply = "$CSI?62;22c")
		expect("${CSI}5n", reply = "${CSI}0n")

		val teardown = withTerminal { setup ->
			assertThat(capabilities.inBandResizeEvents).isFalse()
			assertThat(state.size.value).isEqualTo(Terminal.Size(80, 24, 0, 0))

			// No attempt to enable in-band resize events.
			assertThat(setup).doesNotContain("$CSI?2048h")

			// Write an unsolicited resize event which should be ignored.
			ptyWrite("${CSI}48;40;100;400;800t")
			assertThat(events.receive()).isEqualTo(ResizeEvent(100, 40, 800, 400))
			assertThat(state.size.value).isEqualTo(Terminal.Size(80, 24, 0, 0))

			// Platform-specific resize event should be honored.
			testTty.resize(100, 40, 0, 0)
			assertThat(events.receive()).isEqualTo(ResizeEvent(100, 40, 0, 0))
			assertThat(state.size.value).isEqualTo(Terminal.Size(100, 40, 0, 0))
		}

		// No attempt to disable in-band resize events.
		assertThat(teardown).doesNotContain("$CSI?2048l")
	}

	@Test fun replySetNoInitialValue() = terminalTest {
		expect("${CSI}0c", reply = "$CSI?62;22c")
		// Resize events are set (i.e, enabled).
		expect("$CSI?2048\$p", reply = "$CSI?2048;1\$y")
		expect("${CSI}5n", reply = "${CSI}0n")
		// Second DSR is used to check for initial size.
		expect("${CSI}5n", reply = "${CSI}0n")

		val teardown = withTerminal { setup ->
			assertThat(capabilities.inBandResizeEvents).isTrue()
			assertThat(state.size.value).isEqualTo(Terminal.Size(80, 24, 0, 0))

			// Resize events re-enabled to trigger initial value reply.
			assertThat(setup).contains("$CSI?2048h")

			// Write resize events which should be honored.
			ptyWrite("${CSI}48;40;100;400;800t")
			assertThat(events.receive()).isEqualTo(ResizeEvent(100, 40, 800, 400))
			assertThat(state.size.value).isEqualTo(Terminal.Size(100, 40, 800, 400))
		}

		// Resize events are left enabled.
		assertThat(teardown).doesNotContain("$CSI?2048l")
	}

	@Test fun replySetWithInitialValue() = terminalTest {
		expect("${CSI}0c", reply = "$CSI?62;22c")
		// Resize events are set (i.e, enabled).
		expect("$CSI?2048\$p", reply = "$CSI?2048;1\$y")
		expect("${CSI}5n", reply = "${CSI}0n")
		expect("$CSI?2048h", reply = "${CSI}48;30;90;300;700t")
		expect("${CSI}5n", reply = "${CSI}0n")

		withTerminal {
			assertThat(state.size.value).isEqualTo(Terminal.Size(90, 30, 700, 300))
		}
	}

	@Test fun replyResetNoInitialValue() = terminalTest {
		expect("${CSI}0c", reply = "$CSI?62;22c")
		// Resize events are reset (i.e, not enabled).
		expect("$CSI?2048\$p", reply = "$CSI?2048;2\$y")
		expect("${CSI}5n", reply = "${CSI}0n")
		// Second DSR is used to check for initial size.
		expect("${CSI}5n", reply = "${CSI}0n")

		val teardown = withTerminal { setup ->
			assertThat(capabilities.inBandResizeEvents).isTrue()
			assertThat(state.size.value).isEqualTo(Terminal.Size(80, 24, 0, 0))

			// Enable resize events.
			assertThat(setup).contains("$CSI?2048h")

			// Write resize events which should be honored.
			ptyWrite("${CSI}48;40;100;400;800t")
			assertThat(events.receive()).isEqualTo(ResizeEvent(100, 40, 800, 400))
			assertThat(state.size.value).isEqualTo(Terminal.Size(100, 40, 800, 400))
		}

		// Disable resize events.
		assertThat(teardown).contains("$CSI?2048l")
	}

	@Test fun replyResetWithInitialValue() = terminalTest {
		expect("${CSI}0c", reply = "$CSI?62;22c")
		// Resize events are reset (i.e, not enabled).
		expect("$CSI?2048\$p", reply = "$CSI?2048;2\$y")
		expect("${CSI}5n", reply = "${CSI}0n")
		expect("$CSI?2048h", reply = "${CSI}48;30;90;300;700t")
		expect("${CSI}5n", reply = "${CSI}0n")

		withTerminal {
			assertThat(state.size.value).isEqualTo(Terminal.Size(90, 30, 700, 300))
		}
	}

	@Test fun replyPermanentlySetNoInitialValue() = terminalTest {
		expect("${CSI}0c", reply = "$CSI?62;22c")
		// Resize events are permanently set (i.e, always enabled).
		expect("$CSI?2048\$p", reply = "$CSI?2048;3\$y")
		expect("${CSI}5n", reply = "${CSI}0n")
		// Second DSR is used to check for initial size.
		expect("${CSI}5n", reply = "${CSI}0n")

		val teardown = withTerminal { setup ->
			assertThat(capabilities.inBandResizeEvents).isTrue()
			assertThat(state.size.value).isEqualTo(Terminal.Size(80, 24, 0, 0))

			// Focus events re-enabled to possibly trigger initial value reply.
			assertThat(setup).contains("$CSI?2048h")

			// Write resize events which should be honored.
			ptyWrite("${CSI}48;40;100;400;800t")
			assertThat(events.receive()).isEqualTo(ResizeEvent(100, 40, 800, 400))
			assertThat(state.size.value).isEqualTo(Terminal.Size(100, 40, 800, 400))
		}

		// No attempt to disable focus events.
		assertThat(teardown).doesNotContain("$CSI?2048l")
	}

	@Test fun replyPermanentlySetWithInitialValue() = terminalTest {
		expect("${CSI}0c", reply = "$CSI?62;22c")
		// Resize events are permanently set (i.e, always enabled).
		expect("$CSI?2048\$p", reply = "$CSI?2048;3\$y")
		expect("${CSI}5n", reply = "${CSI}0n")
		expect("$CSI?2048h", reply = "${CSI}48;30;90;300;700t")
		expect("${CSI}5n", reply = "${CSI}0n")

		withTerminal {
			assertThat(state.size.value).isEqualTo(Terminal.Size(90, 30, 700, 300))
		}
	}

	@Test fun replyPermanentlyReset() = terminalTest {
		expect("${CSI}0c", reply = "$CSI?62;22c")
		// Resize events are permanently reset (i.e, not supported).
		expect("$CSI?2048\$p", reply = "$CSI?2048;4\$y")
		expect("${CSI}5n", reply = "${CSI}0n")

		val teardown = withTerminal { setup ->
			assertThat(capabilities.inBandResizeEvents).isFalse()
			assertThat(state.size.value).isEqualTo(Terminal.Size(80, 24, 0, 0))

			// No attempt to enable focus events.
			assertThat(setup).doesNotContain("$CSI?2048h")

			// Write an unsolicited resize event which should be ignored.
			ptyWrite("${CSI}48;40;100;400;800t")
			assertThat(events.receive()).isEqualTo(ResizeEvent(100, 40, 800, 400))
			assertThat(state.size.value).isEqualTo(Terminal.Size(80, 24, 0, 0))
		}

		// No attempt to disable focus events.
		assertThat(teardown).doesNotContain("$CSI?2048l")
	}
}
