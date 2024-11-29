package com.jakewharton.mosaic.terminal

import assertk.assertThat
import assertk.assertions.isEqualTo
import com.jakewharton.mosaic.terminal.event.FocusEvent
import kotlin.test.AfterTest
import kotlin.test.Test

class TerminalParserCsiFocusEventTest {
	private val writer = Tty.stdinWriter()
	private val parser = TerminalParser(writer.reader)

	@AfterTest fun after() {
		writer.close()
	}

	@Test fun focusedTrue() {
		writer.writeHex("1b5b49")
		assertThat(parser.next()).isEqualTo(FocusEvent(focused = true))
	}

	@Test fun focusedFalse() {
		writer.writeHex("1b5b4f")
		assertThat(parser.next()).isEqualTo(FocusEvent(focused = false))
	}
}
