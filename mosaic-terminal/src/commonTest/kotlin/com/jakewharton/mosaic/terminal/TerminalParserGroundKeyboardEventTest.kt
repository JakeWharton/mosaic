package com.jakewharton.mosaic.terminal

import assertk.assertThat
import assertk.assertions.isEqualTo
import com.jakewharton.mosaic.terminal.event.KeyboardEvent
import kotlin.test.AfterTest
import kotlin.test.Test

class TerminalParserGroundKeyboardEventTest {
	private val writer = Tty.stdinWriter()
	private val parser = TerminalParser(writer.reader)

	@AfterTest fun after() {
		writer.close()
	}

	@Test fun bareEscape() {
		writer.writeHex("1b")
		assertThat(parser.next()).isEqualTo(KeyboardEvent(0x1b))
	}
}
