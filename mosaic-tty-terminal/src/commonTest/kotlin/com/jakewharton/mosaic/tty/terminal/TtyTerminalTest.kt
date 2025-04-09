package com.jakewharton.mosaic.tty.terminal

import assertk.assertThat
import assertk.assertions.isFalse
import assertk.assertions.isTrue
import kotlin.test.Test

class TtyTerminalTest {
	@Test fun cursorVisibilityDetectionNone() = terminalTest {
		expect("${CSI}0c", reply = "$CSI?62;22c")
		expect("${CSI}5n", reply = "${CSI}0n")

		val teardown = withTerminal { setup ->
			assertThat(capabilities.cursorVisibility).isFalse()

			// Cursor is not hidden.
			assertThat(setup).doesNotContain("$CSI?25l")
		}

		// Cursor is left hidden.
		assertThat(teardown).doesNotContain("$CSI?25h")
	}

	@Test fun cursorVisibilityDetectionSet() = terminalTest {
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

	@Test fun cursorVisibilityDetectionReset() = terminalTest {
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
}
