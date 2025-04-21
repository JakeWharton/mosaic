package com.jakewharton.mosaic.tty.terminal

import assertk.assertThat
import assertk.assertions.isFalse
import assertk.assertions.isTrue
import kotlin.test.Test

class TtyTerminalCursorTest {
	@Test fun noReply() = terminalTest {
		expect("${CSI}0c", reply = "$CSI?62;22c")
		expect("${CSI}5n", reply = "${CSI}0n")

		val teardown = withTerminal { setup ->
			assertThat(capabilities.cursorVisibility).isFalse()

			// No attempt to hide cursor.
			assertThat(setup).doesNotContain("$CSI?25l")
		}

		// No attempt to show cursor.
		assertThat(teardown).doesNotContain("$CSI?25h")
	}

	@Test fun replySet() = terminalTest {
		expect("${CSI}0c", reply = "$CSI?62;22c")
		// Cursor is set (i.e, visible).
		expect("$CSI?25\$p", reply = "$CSI?25;1\$y")
		expect("${CSI}5n", reply = "${CSI}0n")

		val teardown = withTerminal { setup ->
			assertThat(capabilities.cursorVisibility).isTrue()

			// Cursor hidden automatically.
			assertThat(setup).contains("$CSI?25l")
		}

		// Cursor visibility restored.
		assertThat(teardown).contains("$CSI?25h")
	}

	@Test fun replyReset() = terminalTest {
		expect("${CSI}0c", reply = "$CSI?62;22c")
		// Cursor is reset (i.e, already hidden).
		expect("$CSI?25\$p", reply = "$CSI?25;2\$y")
		expect("${CSI}5n", reply = "${CSI}0n")

		val teardown = withTerminal { setup ->
			assertThat(capabilities.cursorVisibility).isTrue()

			// Cursor is not re-hidden.
			assertThat(setup).doesNotContain("$CSI?25l")
		}

		// Cursor is left hidden.
		assertThat(teardown).doesNotContain("$CSI?25h")
	}

	@Test fun replyPermanentlySet() = terminalTest {
		expect("${CSI}0c", reply = "$CSI?62;22c")
		// Cursor is permanently set (i.e, always visible).
		expect("$CSI?25\$p", reply = "$CSI?25;3\$y")
		expect("${CSI}5n", reply = "${CSI}0n")

		val teardown = withTerminal { setup ->
			assertThat(capabilities.cursorVisibility).isFalse()

			// No attempt to hide cursor.
			assertThat(setup).doesNotContain("$CSI?25l")
		}

		// No attempt to show cursor.
		assertThat(teardown).doesNotContain("$CSI?25h")
	}

	@Test fun replyPermanentlyReset() = terminalTest {
		expect("${CSI}0c", reply = "$CSI?62;22c")
		// Cursor is permanently reset (i.e, always hidden).
		expect("$CSI?25\$p", reply = "$CSI?25;4\$y")
		expect("${CSI}5n", reply = "${CSI}0n")

		val teardown = withTerminal { setup ->
			assertThat(capabilities.cursorVisibility).isFalse()

			// No attempt to hide cursor.
			assertThat(setup).doesNotContain("$CSI?25l")
		}

		// No attempt to show cursor.
		assertThat(teardown).doesNotContain("$CSI?25h")
	}
}
