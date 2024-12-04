package com.jakewharton.mosaic.terminal

import assertk.assertThat
import assertk.assertions.isEqualTo
import com.jakewharton.mosaic.terminal.event.KeyboardEvent
import kotlin.test.AfterTest
import kotlin.test.Test

class TerminalParserTest {
	private val writer = Tty.stdinWriter()
	private val parser = TerminalParser(writer.reader)

	@AfterTest fun after() {
		writer.close()
	}

	@Test fun fullBufferResets() {
		writer.writeHex("61".repeat(BufferSize + 1))
		repeat(BufferSize) {
			assertThat(parser.next()).isEqualTo(KeyboardEvent(0x61))
		}
		// The following read should cause underflow because both offset and limit are BufferSize.
		// This will produce a new read of the underlying data and its additional event.
		assertThat(parser.next()).isEqualTo(KeyboardEvent(0x61))
	}
}
