package com.jakewharton.mosaic.tty.terminal

import assertk.assertFailure
import assertk.assertions.isSameInstanceAs
import kotlin.test.Test

class TerminalTesterTest {
	/** Setup and teardown of the terminal in the presence of exceptions is _very_ fragile. */
	@Test fun failuresWork() = terminalTest {
		// Make test faster by reporting no capabilities.
		expect("${CSI}0c", reply = "$CSI?1c")
		// Subtype ensures coroutines do not break referential equality.
		val expected = object : IllegalStateException("sup") {}

		assertFailure {
			withTerminal {
				throw expected
			}
		}.isSameInstanceAs(expected)
	}
}
