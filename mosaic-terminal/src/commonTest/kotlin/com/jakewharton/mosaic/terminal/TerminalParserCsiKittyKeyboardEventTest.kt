package com.jakewharton.mosaic.terminal

import assertk.assertThat
import assertk.assertions.isEqualTo
import com.jakewharton.mosaic.terminal.event.KeyboardEvent
import com.jakewharton.mosaic.terminal.event.KeyboardEvent.Companion.ModifierCtrl
import kotlin.test.AfterTest
import kotlin.test.Test

@OptIn(ExperimentalStdlibApi::class)
class TerminalParserCsiKittyKeyboardEventTest {
	private val writer = Tty.stdinWriter()
	private val parser = TerminalParser(writer.reader)

	@AfterTest fun after() {
		writer.close()
	}

	@Test fun ctrlC() {
		writer.writeHex("1b5b39393b3575")
		assertThat(parser.next()).isEqualTo(KeyboardEvent(0x63, ModifierCtrl))
	}
}
