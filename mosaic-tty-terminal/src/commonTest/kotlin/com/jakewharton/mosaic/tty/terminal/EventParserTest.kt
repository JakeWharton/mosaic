package com.jakewharton.mosaic.tty.terminal

import assertk.assertThat
import assertk.assertions.isEqualTo
import com.jakewharton.mosaic.terminal.OperatingStatusResponseEvent
import kotlin.test.Test

class EventParserTest : BaseEventParserTest() {
	@Test fun copyBuffer() {
		assertThat(parser.copyBuffer().toHexString()).isEqualTo("")
		testTerminal.write("${CSI}0n${CSI}0n")
		assertThat(parser.next()).isEqualTo(OperatingStatusResponseEvent(ok = true))
		assertThat(parser.copyBuffer().toHexString()).isEqualTo("1b5b306e")
		assertThat(parser.next()).isEqualTo(OperatingStatusResponseEvent(ok = true))
		assertThat(parser.copyBuffer().toHexString()).isEqualTo("")
	}
}
