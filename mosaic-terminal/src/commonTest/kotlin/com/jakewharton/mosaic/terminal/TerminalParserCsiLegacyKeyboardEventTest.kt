package com.jakewharton.mosaic.terminal

import assertk.assertThat
import assertk.assertions.isEqualTo
import com.jakewharton.mosaic.terminal.event.LegacyKeyboardEvent
import com.jakewharton.mosaic.terminal.event.LegacyKeyboardEvent.Companion.Down
import com.jakewharton.mosaic.terminal.event.LegacyKeyboardEvent.Companion.End
import com.jakewharton.mosaic.terminal.event.LegacyKeyboardEvent.Companion.Home
import com.jakewharton.mosaic.terminal.event.LegacyKeyboardEvent.Companion.KpBegin
import com.jakewharton.mosaic.terminal.event.LegacyKeyboardEvent.Companion.Left
import com.jakewharton.mosaic.terminal.event.LegacyKeyboardEvent.Companion.Right
import com.jakewharton.mosaic.terminal.event.LegacyKeyboardEvent.Companion.Up
import com.jakewharton.mosaic.terminal.event.UnknownEvent
import kotlin.test.AfterTest
import kotlin.test.Test

@OptIn(ExperimentalStdlibApi::class)
class TerminalParserCsiLegacyKeyboardEventTest {
	private val writer = Tty.stdinWriter()
	private val parser = TerminalParser(writer.reader, true)

	@AfterTest fun after() {
		writer.close()
	}

	@Test fun up() {
		writer.writeHex("1b5b41")
		assertThat(parser.next()).isEqualTo(LegacyKeyboardEvent(Up))
	}

	@Test fun down() {
		writer.writeHex("1b5b42")
		assertThat(parser.next()).isEqualTo(LegacyKeyboardEvent(Down))
	}

	@Test fun right() {
		writer.writeHex("1b5b43")
		assertThat(parser.next()).isEqualTo(LegacyKeyboardEvent(Right))
	}

	@Test fun left() {
		writer.writeHex("1b5b44")
		assertThat(parser.next()).isEqualTo(LegacyKeyboardEvent(Left))
	}

	@Test fun begin() {
		writer.writeHex("1b5b45")
		assertThat(parser.next()).isEqualTo(LegacyKeyboardEvent(KpBegin))
	}

	@Test fun end() {
		writer.writeHex("1b5b46")
		assertThat(parser.next()).isEqualTo(LegacyKeyboardEvent(End))
	}

	@Test fun home() {
		writer.writeHex("1b5b48")
		assertThat(parser.next()).isEqualTo(LegacyKeyboardEvent(Home))
	}

	// TODO with all modifier variations

	@Test fun non1p0() {
		writer.writeHex("1b5b323b3248")
		assertThat(parser.next()).isEqualTo(
			UnknownEvent("1b5b323b3248".hexToByteArray()),
		)
	}

	@Test fun emptyModifier() {
		writer.writeHex("1b5b313b48")
		assertThat(parser.next()).isEqualTo(
			UnknownEvent("1b5b313b48".hexToByteArray()),
		)
	}

	@Test fun nonDigitModifier() {
		writer.writeHex("1b5b313b2f48")
		assertThat(parser.next()).isEqualTo(
			UnknownEvent("1b5b313b2f48".hexToByteArray()),
		)
	}

	@Test fun multiDigitModifier() {
		writer.writeHex("1b5b313b323048")
		assertThat(parser.next()).isEqualTo(
			UnknownEvent("1b5b313b323048".hexToByteArray()),
		)
	}
}
