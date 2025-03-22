package com.jakewharton.mosaic.tty.terminal

import assertk.assertThat
import assertk.assertions.isEqualTo
import com.jakewharton.mosaic.terminal.KittyGraphicsEvent
import kotlin.test.Test

class EventParserApcKittyGraphicsEventTest : BaseEventParserTest() {
	@Test fun pasteStart() {
		testTty.writeHex("1b5f47693d33313b4f4b1b5c")
		assertThat(parser.next()).isEqualTo(KittyGraphicsEvent(31, "OK"))
	}
}
