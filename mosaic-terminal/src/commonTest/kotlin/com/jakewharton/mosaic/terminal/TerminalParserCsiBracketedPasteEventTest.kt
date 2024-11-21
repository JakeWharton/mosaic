package com.jakewharton.mosaic.terminal

import assertk.assertThat
import assertk.assertions.isEqualTo
import com.jakewharton.mosaic.terminal.event.BracketedPasteEvent
import kotlin.test.AfterTest
import kotlin.test.Test

class TerminalParserCsiBracketedPasteEventTest {
	private val writer = Tty.stdinWriter()
	private val parser = TerminalParser(writer.reader, true)

	@AfterTest fun after() {
		writer.close()
	}

	@Test fun pasteStart() {
		writer.writeHex("1b5b3230307e")
		assertThat(parser.next()).isEqualTo(BracketedPasteEvent(start = true))
	}

	@Test fun pasteEnd() {
		writer.writeHex("1b5b3230317e")
		assertThat(parser.next()).isEqualTo(BracketedPasteEvent(start = false))
	}
}
