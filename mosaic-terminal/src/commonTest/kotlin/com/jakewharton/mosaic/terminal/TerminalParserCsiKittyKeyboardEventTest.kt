package com.jakewharton.mosaic.terminal

import assertk.assertThat
import assertk.assertions.isEqualTo
import com.jakewharton.mosaic.terminal.event.KeyboardEvent
import com.jakewharton.mosaic.terminal.event.KeyboardEvent.Companion.ModifierShift
import kotlin.test.AfterTest
import kotlin.test.Test

@OptIn(ExperimentalStdlibApi::class)
class TerminalParserCsiKittyKeyboardEventTest {
	private val writer = Tty.stdinWriter()
	private val parser = TerminalParser(writer.reader)

	@AfterTest fun after() {
		writer.close()
	}

	@Test fun h() {
		writer.writeHex("1b5b31303475")
		assertThat(parser.next()).isEqualTo(
			KeyboardEvent(0x68),
		)
	}

	@Test fun shiftH() {
		writer.writeHex("1b5b3130343b3275")
		assertThat(parser.next()).isEqualTo(
			KeyboardEvent(0x68, modifiers = ModifierShift),
		)
	}

	@Test fun shiftHWithAlternate() {
		writer.writeHex("1b5b3130343a37323b3275")
		assertThat(parser.next()).isEqualTo(
			KeyboardEvent(0x68, 0x48, modifiers = ModifierShift),
		)
	}

	@Test fun shiftHWithReleaseEventType() {
		writer.writeHex("1b5b3130343b323a3375")
		assertThat(parser.next()).isEqualTo(
			KeyboardEvent(0x68, modifiers = ModifierShift, eventType = 3),
		)
	}

	@Test fun hWithAssociatedText() {
		writer.writeHex("1b5b3130343b3b31303475")
		assertThat(parser.next()).isEqualTo(
			KeyboardEvent(0x68, text = "h"),
		)
	}
}
