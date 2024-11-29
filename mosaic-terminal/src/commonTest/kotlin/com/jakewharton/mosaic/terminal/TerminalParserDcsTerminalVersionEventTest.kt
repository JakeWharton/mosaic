package com.jakewharton.mosaic.terminal

import assertk.assertThat
import assertk.assertions.isEqualTo
import com.jakewharton.mosaic.terminal.event.TerminalVersionEvent
import kotlin.test.AfterTest
import kotlin.test.Test

class TerminalParserDcsTerminalVersionEventTest {
	private val writer = Tty.stdinWriter()
	private val parser = TerminalParser(writer.reader)

	@AfterTest fun after() {
		writer.close()
	}

	@Test fun empty() {
		writer.writeHex("1b503e7c1b5c")
		assertThat(parser.next()).isEqualTo(TerminalVersionEvent(""))
	}

	@Test fun text() {
		writer.writeHex("1b503e7c68656c6c6f1b5c")
		assertThat(parser.next()).isEqualTo(TerminalVersionEvent("hello"))
	}
}
