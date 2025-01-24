package com.jakewharton.mosaic.terminal

import assertk.assertThat
import assertk.assertions.isEqualTo
import com.jakewharton.mosaic.terminal.event.KeyboardEvent
import com.jakewharton.mosaic.terminal.event.KeyboardEvent.Companion.ModifierShift
import kotlin.test.Test
import kotlinx.coroutines.test.runTest

class TerminalParserCsiKittyKeyboardEventTest : BaseTerminalParserTest() {
	@Test fun h() = runTest {
		writer.writeHex("1b5b31303475")
		assertThat(reader.next()).isEqualTo(
			KeyboardEvent(0x68),
		)
	}

	@Test fun shiftH() = runTest {
		writer.writeHex("1b5b3130343b3275")
		assertThat(reader.next()).isEqualTo(
			KeyboardEvent(0x68, modifiers = ModifierShift),
		)
	}

	@Test fun shiftHWithAlternate() = runTest {
		writer.writeHex("1b5b3130343a37323b3275")
		assertThat(reader.next()).isEqualTo(
			KeyboardEvent(0x68, 0x48, modifiers = ModifierShift),
		)
	}

	@Test fun shiftHWithReleaseEventType() = runTest {
		writer.writeHex("1b5b3130343b323a3375")
		assertThat(reader.next()).isEqualTo(
			KeyboardEvent(0x68, modifiers = ModifierShift, eventType = 3),
		)
	}

	@Test fun hWithAssociatedText() = runTest {
		writer.writeHex("1b5b3130343b3b31303475")
		assertThat(reader.next()).isEqualTo(
			KeyboardEvent(0x68, text = "h"),
		)
	}
}
