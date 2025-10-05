package com.jakewharton.mosaic.tty.terminal

import assertk.assertThat
import assertk.assertions.isEqualTo
import com.jakewharton.mosaic.terminal.KeyboardEvent
import com.jakewharton.mosaic.terminal.KeyboardEvent.Companion.Down
import com.jakewharton.mosaic.terminal.KeyboardEvent.Companion.End
import com.jakewharton.mosaic.terminal.KeyboardEvent.Companion.Home
import com.jakewharton.mosaic.terminal.KeyboardEvent.Companion.KpBegin
import com.jakewharton.mosaic.terminal.KeyboardEvent.Companion.Left
import com.jakewharton.mosaic.terminal.KeyboardEvent.Companion.ModifierAlt
import com.jakewharton.mosaic.terminal.KeyboardEvent.Companion.ModifierCapsLock
import com.jakewharton.mosaic.terminal.KeyboardEvent.Companion.ModifierCtrl
import com.jakewharton.mosaic.terminal.KeyboardEvent.Companion.ModifierHyper
import com.jakewharton.mosaic.terminal.KeyboardEvent.Companion.ModifierMeta
import com.jakewharton.mosaic.terminal.KeyboardEvent.Companion.ModifierNumLock
import com.jakewharton.mosaic.terminal.KeyboardEvent.Companion.ModifierShift
import com.jakewharton.mosaic.terminal.KeyboardEvent.Companion.ModifierSuper
import com.jakewharton.mosaic.terminal.KeyboardEvent.Companion.Right
import com.jakewharton.mosaic.terminal.KeyboardEvent.Companion.Up
import com.jakewharton.mosaic.terminal.UnknownEvent
import kotlin.test.Test

class EventParserCsiKeyboardEventTest : BaseEventParserTest() {
	@Test fun up() {
		testTerminal.write("${CSI}A")
		assertThat(parser.next()).isEqualTo(KeyboardEvent(Up))
	}

	@Test fun down() {
		testTerminal.write("${CSI}B")
		assertThat(parser.next()).isEqualTo(KeyboardEvent(Down))
	}

	@Test fun right() {
		testTerminal.write("${CSI}C")
		assertThat(parser.next()).isEqualTo(KeyboardEvent(Right))
	}

	@Test fun left() {
		testTerminal.write("${CSI}D")
		assertThat(parser.next()).isEqualTo(KeyboardEvent(Left))
	}

	@Test fun begin() {
		testTerminal.write("${CSI}E")
		assertThat(parser.next()).isEqualTo(KeyboardEvent(KpBegin))
	}

	@Test fun end() {
		testTerminal.write("${CSI}F")
		assertThat(parser.next()).isEqualTo(KeyboardEvent(End))
	}

	@Test fun home() {
		testTerminal.write("${CSI}H")
		assertThat(parser.next()).isEqualTo(KeyboardEvent(Home))
	}

	@Test fun modifierShiftUp() {
		testTerminal.write("${CSI}1;2A")
		assertThat(parser.next()).isEqualTo(KeyboardEvent(Up, modifiers = ModifierShift))
	}

	@Test fun modifierAltUp() {
		testTerminal.write("${CSI}1;3A")
		assertThat(parser.next()).isEqualTo(KeyboardEvent(Up, modifiers = ModifierAlt))
	}

	@Test fun modifierCtrlUp() {
		testTerminal.write("${CSI}1;5A")
		assertThat(parser.next()).isEqualTo(KeyboardEvent(Up, modifiers = ModifierCtrl))
	}

	@Test fun modifierSuperUp() {
		testTerminal.write("${CSI}1;9A")
		assertThat(parser.next()).isEqualTo(KeyboardEvent(Up, modifiers = ModifierSuper))
	}

	@Test fun modifierHyperUp() {
		testTerminal.write("${CSI}1;17A")
		assertThat(parser.next()).isEqualTo(KeyboardEvent(Up, modifiers = ModifierHyper))
	}

	@Test fun modifierMetaUp() {
		testTerminal.write("${CSI}1;33A")
		assertThat(parser.next()).isEqualTo(KeyboardEvent(Up, modifiers = ModifierMeta))
	}

	@Test fun modifierCapsLockUp() {
		testTerminal.write("${CSI}1;65A")
		assertThat(parser.next()).isEqualTo(KeyboardEvent(Up, modifiers = ModifierCapsLock))
	}

	@Test fun modifierNumLockUp() {
		testTerminal.write("${CSI}1;129A")
		assertThat(parser.next()).isEqualTo(KeyboardEvent(Up, modifiers = ModifierNumLock))
	}

	@Test fun non1p0() {
		testTerminal.write("${CSI}2;2H")
		assertThat(parser.next()).isEqualTo(
			UnknownEvent("1b5b323b3248".hexToByteArray()),
		)
	}

	@Test fun emptyModifier() {
		testTerminal.write("${CSI}1;H")
		assertThat(parser.next()).isEqualTo(
			UnknownEvent("1b5b313b48".hexToByteArray()),
		)
	}

	@Test fun nonDigitModifier() {
		testTerminal.write("${CSI}1;/H")
		assertThat(parser.next()).isEqualTo(
			UnknownEvent("1b5b313b2f48".hexToByteArray()),
		)
	}
}
