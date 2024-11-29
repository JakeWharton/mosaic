package com.jakewharton.mosaic.terminal

import assertk.assertThat
import assertk.assertions.isEqualTo
import com.jakewharton.mosaic.terminal.event.KeyboardEvent
import com.jakewharton.mosaic.terminal.event.KeyboardEvent.Companion.Down
import com.jakewharton.mosaic.terminal.event.KeyboardEvent.Companion.End
import com.jakewharton.mosaic.terminal.event.KeyboardEvent.Companion.Home
import com.jakewharton.mosaic.terminal.event.KeyboardEvent.Companion.KpBegin
import com.jakewharton.mosaic.terminal.event.KeyboardEvent.Companion.Left
import com.jakewharton.mosaic.terminal.event.KeyboardEvent.Companion.ModifierAlt
import com.jakewharton.mosaic.terminal.event.KeyboardEvent.Companion.ModifierCapsLock
import com.jakewharton.mosaic.terminal.event.KeyboardEvent.Companion.ModifierCtrl
import com.jakewharton.mosaic.terminal.event.KeyboardEvent.Companion.ModifierHyper
import com.jakewharton.mosaic.terminal.event.KeyboardEvent.Companion.ModifierMeta
import com.jakewharton.mosaic.terminal.event.KeyboardEvent.Companion.ModifierNumLock
import com.jakewharton.mosaic.terminal.event.KeyboardEvent.Companion.ModifierShift
import com.jakewharton.mosaic.terminal.event.KeyboardEvent.Companion.ModifierSuper
import com.jakewharton.mosaic.terminal.event.KeyboardEvent.Companion.Right
import com.jakewharton.mosaic.terminal.event.KeyboardEvent.Companion.Up
import com.jakewharton.mosaic.terminal.event.UnknownEvent
import kotlin.test.AfterTest
import kotlin.test.Test

@OptIn(ExperimentalStdlibApi::class)
class TerminalParserCsiLegacyKeyboardEventTest {
	private val writer = Tty.stdinWriter()
	private val parser = TerminalParser(writer.reader)

	@AfterTest fun after() {
		writer.close()
	}

	@Test fun up() {
		writer.writeHex("1b5b41")
		assertThat(parser.next()).isEqualTo(KeyboardEvent(Up))
	}

	@Test fun down() {
		writer.writeHex("1b5b42")
		assertThat(parser.next()).isEqualTo(KeyboardEvent(Down))
	}

	@Test fun right() {
		writer.writeHex("1b5b43")
		assertThat(parser.next()).isEqualTo(KeyboardEvent(Right))
	}

	@Test fun left() {
		writer.writeHex("1b5b44")
		assertThat(parser.next()).isEqualTo(KeyboardEvent(Left))
	}

	@Test fun begin() {
		writer.writeHex("1b5b45")
		assertThat(parser.next()).isEqualTo(KeyboardEvent(KpBegin))
	}

	@Test fun end() {
		writer.writeHex("1b5b46")
		assertThat(parser.next()).isEqualTo(KeyboardEvent(End))
	}

	@Test fun home() {
		writer.writeHex("1b5b48")
		assertThat(parser.next()).isEqualTo(KeyboardEvent(Home))
	}

	@Test fun modifierShiftUp() {
		writer.writeHex("1b5b313b3241")
		assertThat(parser.next()).isEqualTo(KeyboardEvent(Up, ModifierShift))
	}

	@Test fun modifierAltUp() {
		writer.writeHex("1b5b313b3341")
		assertThat(parser.next()).isEqualTo(KeyboardEvent(Up, ModifierAlt))
	}

	@Test fun modifierCtrlUp() {
		writer.writeHex("1b5b313b3541")
		assertThat(parser.next()).isEqualTo(KeyboardEvent(Up, ModifierCtrl))
	}

	@Test fun modifierSuperUp() {
		writer.writeHex("1b5b313b3941")
		assertThat(parser.next()).isEqualTo(KeyboardEvent(Up, ModifierSuper))
	}

	@Test fun modifierHyperUp() {
		writer.writeHex("1b5b313b313741")
		assertThat(parser.next()).isEqualTo(KeyboardEvent(Up, ModifierHyper))
	}

	@Test fun modifierMetaUp() {
		writer.writeHex("1b5b313b333341")
		assertThat(parser.next()).isEqualTo(KeyboardEvent(Up, ModifierMeta))
	}

	@Test fun modifierCapsLockUp() {
		writer.writeHex("1b5b313b363541")
		assertThat(parser.next()).isEqualTo(KeyboardEvent(Up, ModifierCapsLock))
	}

	@Test fun modifierNumLockUp() {
		writer.writeHex("1b5b313b31323941")
		assertThat(parser.next()).isEqualTo(KeyboardEvent(Up, ModifierNumLock))
	}

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
}
