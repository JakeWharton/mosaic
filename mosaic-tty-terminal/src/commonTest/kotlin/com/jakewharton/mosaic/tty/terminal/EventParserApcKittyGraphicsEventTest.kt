package com.jakewharton.mosaic.tty.terminal

import assertk.assertThat
import assertk.assertions.isEqualTo
import com.jakewharton.mosaic.terminal.KittyGraphicsEvent
import kotlin.test.Test

class EventParserApcKittyGraphicsEventTest : BaseEventParserTest() {
	@Test fun pasteStart() {
		testTty.write("${APC}Gi=31;OK$ST")
		assertThat(parser.next()).isEqualTo(KittyGraphicsEvent(31, "OK"))
	}
}
