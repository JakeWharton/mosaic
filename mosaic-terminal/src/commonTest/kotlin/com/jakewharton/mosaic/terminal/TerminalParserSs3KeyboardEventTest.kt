package com.jakewharton.mosaic.terminal

import assertk.assertThat
import assertk.assertions.isEqualTo
import com.jakewharton.mosaic.terminal.event.KeyboardEvent
import com.jakewharton.mosaic.terminal.event.KeyboardEvent.Companion.Down
import com.jakewharton.mosaic.terminal.event.KeyboardEvent.Companion.End
import com.jakewharton.mosaic.terminal.event.KeyboardEvent.Companion.F1
import com.jakewharton.mosaic.terminal.event.KeyboardEvent.Companion.F2
import com.jakewharton.mosaic.terminal.event.KeyboardEvent.Companion.F3
import com.jakewharton.mosaic.terminal.event.KeyboardEvent.Companion.F4
import com.jakewharton.mosaic.terminal.event.KeyboardEvent.Companion.Home
import com.jakewharton.mosaic.terminal.event.KeyboardEvent.Companion.Left
import com.jakewharton.mosaic.terminal.event.KeyboardEvent.Companion.Right
import com.jakewharton.mosaic.terminal.event.KeyboardEvent.Companion.Up
import com.jakewharton.mosaic.terminal.event.OperatingStatusResponseEvent
import com.jakewharton.mosaic.terminal.event.UnknownEvent
import kotlin.test.Test

@OptIn(ExperimentalStdlibApi::class)
class TerminalParserSs3KeyboardEventTest : BaseTerminalParserTest() {
	@Test fun up() {
		writer.writeHex("1b4f41")
		assertThat(parser.next()).isEqualTo(KeyboardEvent(Up))
	}

	@Test fun down() {
		writer.writeHex("1b4f42")
		assertThat(parser.next()).isEqualTo(KeyboardEvent(Down))
	}

	@Test fun right() {
		writer.writeHex("1b4f43")
		assertThat(parser.next()).isEqualTo(KeyboardEvent(Right))
	}

	@Test fun left() {
		writer.writeHex("1b4f44")
		assertThat(parser.next()).isEqualTo(KeyboardEvent(Left))
	}

	@Test fun end() {
		writer.writeHex("1b4f46")
		assertThat(parser.next()).isEqualTo(KeyboardEvent(End))
	}

	@Test fun home() {
		writer.writeHex("1b4f48")
		assertThat(parser.next()).isEqualTo(KeyboardEvent(Home))
	}

	@Test fun f1() {
		writer.writeHex("1b4f50")
		assertThat(parser.next()).isEqualTo(KeyboardEvent(F1))
	}

	@Test fun f2() {
		writer.writeHex("1b4f51")
		assertThat(parser.next()).isEqualTo(KeyboardEvent(F2))
	}

	@Test fun f3() {
		writer.writeHex("1b4f52")
		assertThat(parser.next()).isEqualTo(KeyboardEvent(F3))
	}

	@Test fun f4() {
		writer.writeHex("1b4f53")
		assertThat(parser.next()).isEqualTo(KeyboardEvent(F4))
	}

	@Test fun invalidKey() {
		writer.writeHex("1b4f75")
		assertThat(parser.next()).isEqualTo(
			UnknownEvent("1b4f75".hexToByteArray()),
		)
	}

	@Test fun keyIsEscapeDoesNotConsumeEscape() {
		writer.writeHex("1b4f1b5b306e")
		assertThat(parser.next()).isEqualTo(
			UnknownEvent("1b4f".hexToByteArray()),
		)
		assertThat(parser.next()).isEqualTo(OperatingStatusResponseEvent(ok = true))
	}
}
