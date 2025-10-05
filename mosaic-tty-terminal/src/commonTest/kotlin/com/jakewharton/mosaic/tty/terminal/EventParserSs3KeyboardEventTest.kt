package com.jakewharton.mosaic.tty.terminal

import assertk.assertThat
import assertk.assertions.isEqualTo
import com.jakewharton.mosaic.terminal.KeyboardEvent
import com.jakewharton.mosaic.terminal.KeyboardEvent.Companion.Down
import com.jakewharton.mosaic.terminal.KeyboardEvent.Companion.End
import com.jakewharton.mosaic.terminal.KeyboardEvent.Companion.F1
import com.jakewharton.mosaic.terminal.KeyboardEvent.Companion.F2
import com.jakewharton.mosaic.terminal.KeyboardEvent.Companion.F3
import com.jakewharton.mosaic.terminal.KeyboardEvent.Companion.F4
import com.jakewharton.mosaic.terminal.KeyboardEvent.Companion.Home
import com.jakewharton.mosaic.terminal.KeyboardEvent.Companion.Left
import com.jakewharton.mosaic.terminal.KeyboardEvent.Companion.Right
import com.jakewharton.mosaic.terminal.KeyboardEvent.Companion.Up
import com.jakewharton.mosaic.terminal.OperatingStatusResponseEvent
import com.jakewharton.mosaic.terminal.UnknownEvent
import kotlin.test.Test

class EventParserSs3KeyboardEventTest : BaseEventParserTest() {
	@Test fun up() {
		testTerminal.write("${SS3}A")
		assertThat(parser.next()).isEqualTo(KeyboardEvent(Up))
	}

	@Test fun down() {
		testTerminal.write("${SS3}B")
		assertThat(parser.next()).isEqualTo(KeyboardEvent(Down))
	}

	@Test fun right() {
		testTerminal.write("${SS3}C")
		assertThat(parser.next()).isEqualTo(KeyboardEvent(Right))
	}

	@Test fun left() {
		testTerminal.write("${SS3}D")
		assertThat(parser.next()).isEqualTo(KeyboardEvent(Left))
	}

	@Test fun end() {
		testTerminal.write("${SS3}F")
		assertThat(parser.next()).isEqualTo(KeyboardEvent(End))
	}

	@Test fun home() {
		testTerminal.write("${SS3}H")
		assertThat(parser.next()).isEqualTo(KeyboardEvent(Home))
	}

	@Test fun f1() {
		testTerminal.write("${SS3}P")
		assertThat(parser.next()).isEqualTo(KeyboardEvent(F1))
	}

	@Test fun f2() {
		testTerminal.write("${SS3}Q")
		assertThat(parser.next()).isEqualTo(KeyboardEvent(F2))
	}

	@Test fun f3() {
		testTerminal.write("${SS3}R")
		assertThat(parser.next()).isEqualTo(KeyboardEvent(F3))
	}

	@Test fun f4() {
		testTerminal.write("${SS3}S")
		assertThat(parser.next()).isEqualTo(KeyboardEvent(F4))
	}

	@Test fun invalidKey() {
		testTerminal.write("${SS3}u")
		assertThat(parser.next()).isEqualTo(
			UnknownEvent("1b4f75".hexToByteArray()),
		)
	}

	@Test fun keyIsEscapeDoesNotConsumeEscape() {
		testTerminal.write("${SS3}${CSI}0n")
		assertThat(parser.next()).isEqualTo(
			UnknownEvent("1b4f".hexToByteArray()),
		)
		assertThat(parser.next()).isEqualTo(OperatingStatusResponseEvent(ok = true))
	}
}
