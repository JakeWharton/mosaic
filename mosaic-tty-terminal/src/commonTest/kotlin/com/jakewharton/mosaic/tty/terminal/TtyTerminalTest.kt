package com.jakewharton.mosaic.tty.terminal

import assertk.assertThat
import assertk.assertions.isTrue
import kotlin.test.Test

class TtyTerminalTest {
	@Test fun cursorVisibilitySetRegardlessOfCapability() = terminalTest {
		expect("${CSI}0c", andReply = "$CSI?62;22c")
		// Cursor is set (i.e, visible).
		expect("${CSI}?25\$p", andReply = "${CSI}25;1\$y")

		withTerminal {
			assertThat(capabilities.cursorVisibility).isTrue()

			// Cursor hidden automatically.
			expect("${CSI}?25l")
		}

		// Cursor visibility restored.
		expect("${CSI}?25h")
	}
}
