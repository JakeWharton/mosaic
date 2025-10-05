package com.jakewharton.mosaic.tty.terminal

import assertk.assertThat
import assertk.assertions.isEqualTo
import com.jakewharton.mosaic.terminal.KeyboardEvent
import com.jakewharton.mosaic.terminal.KeyboardEvent.Companion.ModifierShift
import kotlin.test.Test

class EventParserCsiKittyKeyboardEventTest : BaseEventParserTest() {
	@Test fun h() {
		testTerminal.write("${CSI}104u")
		assertThat(parser.next()).isEqualTo(
			KeyboardEvent(0x68),
		)
	}

	@Test fun shiftH() {
		testTerminal.write("${CSI}104;2u")
		assertThat(parser.next()).isEqualTo(
			KeyboardEvent(0x68, modifiers = ModifierShift),
		)
	}

	@Test fun shiftHWithAlternate() {
		testTerminal.write("${CSI}104:72;2u")
		assertThat(parser.next()).isEqualTo(
			KeyboardEvent(0x68, 0x48, modifiers = ModifierShift),
		)
	}

	@Test fun shiftHWithReleaseEventType() {
		testTerminal.write("${CSI}104;2:3u")
		assertThat(parser.next()).isEqualTo(
			KeyboardEvent(0x68, modifiers = ModifierShift, eventType = 3),
		)
	}

	@Test fun hWithAssociatedText() {
		testTerminal.write("${CSI}104;;104u")
		assertThat(parser.next()).isEqualTo(
			KeyboardEvent(0x68, text = "h"),
		)
	}
}
