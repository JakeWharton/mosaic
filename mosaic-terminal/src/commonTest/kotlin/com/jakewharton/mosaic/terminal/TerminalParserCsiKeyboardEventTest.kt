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
import kotlin.test.Test
import kotlinx.coroutines.test.runTest

class TerminalParserCsiKeyboardEventTest : BaseTerminalParserTest() {
	@Test fun up() = runTest {
		writer.writeHex("1b5b41")
		assertThat(reader.next()).isEqualTo(KeyboardEvent(Up))
	}

	@Test fun down() = runTest {
		writer.writeHex("1b5b42")
		assertThat(reader.next()).isEqualTo(KeyboardEvent(Down))
	}

	@Test fun right() = runTest {
		writer.writeHex("1b5b43")
		assertThat(reader.next()).isEqualTo(KeyboardEvent(Right))
	}

	@Test fun left() = runTest {
		writer.writeHex("1b5b44")
		assertThat(reader.next()).isEqualTo(KeyboardEvent(Left))
	}

	@Test fun begin() = runTest {
		writer.writeHex("1b5b45")
		assertThat(reader.next()).isEqualTo(KeyboardEvent(KpBegin))
	}

	@Test fun end() = runTest {
		writer.writeHex("1b5b46")
		assertThat(reader.next()).isEqualTo(KeyboardEvent(End))
	}

	@Test fun home() = runTest {
		writer.writeHex("1b5b48")
		assertThat(reader.next()).isEqualTo(KeyboardEvent(Home))
	}

	@Test fun modifierShiftUp() = runTest {
		writer.writeHex("1b5b313b3241")
		assertThat(reader.next()).isEqualTo(KeyboardEvent(Up, modifiers = ModifierShift))
	}

	@Test fun modifierAltUp() = runTest {
		writer.writeHex("1b5b313b3341")
		assertThat(reader.next()).isEqualTo(KeyboardEvent(Up, modifiers = ModifierAlt))
	}

	@Test fun modifierCtrlUp() = runTest {
		writer.writeHex("1b5b313b3541")
		assertThat(reader.next()).isEqualTo(KeyboardEvent(Up, modifiers = ModifierCtrl))
	}

	@Test fun modifierSuperUp() = runTest {
		writer.writeHex("1b5b313b3941")
		assertThat(reader.next()).isEqualTo(KeyboardEvent(Up, modifiers = ModifierSuper))
	}

	@Test fun modifierHyperUp() = runTest {
		writer.writeHex("1b5b313b313741")
		assertThat(reader.next()).isEqualTo(KeyboardEvent(Up, modifiers = ModifierHyper))
	}

	@Test fun modifierMetaUp() = runTest {
		writer.writeHex("1b5b313b333341")
		assertThat(reader.next()).isEqualTo(KeyboardEvent(Up, modifiers = ModifierMeta))
	}

	@Test fun modifierCapsLockUp() = runTest {
		writer.writeHex("1b5b313b363541")
		assertThat(reader.next()).isEqualTo(KeyboardEvent(Up, modifiers = ModifierCapsLock))
	}

	@Test fun modifierNumLockUp() = runTest {
		writer.writeHex("1b5b313b31323941")
		assertThat(reader.next()).isEqualTo(KeyboardEvent(Up, modifiers = ModifierNumLock))
	}

	@Test fun non1p0() = runTest {
		writer.writeHex("1b5b323b3248")
		assertThat(reader.next()).isEqualTo(
			UnknownEvent("1b5b323b3248".hexToByteArray()),
		)
	}

	@Test fun emptyModifier() = runTest {
		writer.writeHex("1b5b313b48")
		assertThat(reader.next()).isEqualTo(
			UnknownEvent("1b5b313b48".hexToByteArray()),
		)
	}

	@Test fun nonDigitModifier() = runTest {
		writer.writeHex("1b5b313b2f48")
		assertThat(reader.next()).isEqualTo(
			UnknownEvent("1b5b313b2f48".hexToByteArray()),
		)
	}
}
