package com.jakewharton.mosaic.tty.terminal

import assertk.assertThat
import assertk.assertions.isEqualTo
import com.jakewharton.mosaic.terminal.KeyboardEvent
import com.jakewharton.mosaic.terminal.KeyboardEvent.Companion.ModifierCtrl
import kotlin.test.Test

class EventParserGroundKeyboardEventTest : BaseEventParserTest() {
	@Test fun graphic() {
		for (codepoint in 0x20..0x7f) {
			val hex = codepoint.toString(16)
			testTerminal.writeHex(hex)
			assertThat(parser.next(), hex).isEqualTo(KeyboardEvent(codepoint))
		}
	}

	@Test fun ctrlShiftAt() {
		testTerminal.writeHex("00")
		assertThat(parser.next()).isEqualTo(KeyboardEvent('@'.code, modifiers = ModifierCtrl))
	}

	@Test fun ctrlA() {
		testTerminal.writeHex("01")
		assertThat(parser.next()).isEqualTo(KeyboardEvent('a'.code, modifiers = ModifierCtrl))
	}

	@Test fun ctrlB() {
		testTerminal.writeHex("02")
		assertThat(parser.next()).isEqualTo(KeyboardEvent('b'.code, modifiers = ModifierCtrl))
	}

	@Test fun ctrlC() {
		testTerminal.writeHex("03")
		assertThat(parser.next()).isEqualTo(KeyboardEvent('c'.code, modifiers = ModifierCtrl))
	}

	@Test fun ctrlD() {
		testTerminal.writeHex("04")
		assertThat(parser.next()).isEqualTo(KeyboardEvent('d'.code, modifiers = ModifierCtrl))
	}

	@Test fun ctrlE() {
		testTerminal.writeHex("05")
		assertThat(parser.next()).isEqualTo(KeyboardEvent('e'.code, modifiers = ModifierCtrl))
	}

	@Test fun ctrlF() {
		testTerminal.writeHex("06")
		assertThat(parser.next()).isEqualTo(KeyboardEvent('f'.code, modifiers = ModifierCtrl))
	}

	@Test fun ctrlG() {
		testTerminal.writeHex("07")
		assertThat(parser.next()).isEqualTo(KeyboardEvent('g'.code, modifiers = ModifierCtrl))
	}

	@Test fun ctrlH() {
		testTerminal.writeHex("08")
		assertThat(parser.next()).isEqualTo(KeyboardEvent(0x7f))
	}

	@Test fun ctrlI() {
		testTerminal.writeHex("09")
		assertThat(parser.next()).isEqualTo(KeyboardEvent(0x09))
	}

	@Test fun ctrlJ() {
		testTerminal.writeHex("0a")
		assertThat(parser.next()).isEqualTo(KeyboardEvent(0x0d))
	}

	@Test fun ctrlK() {
		testTerminal.writeHex("0b")
		assertThat(parser.next()).isEqualTo(KeyboardEvent('k'.code, modifiers = ModifierCtrl))
	}

	@Test fun ctrlL() {
		testTerminal.writeHex("0c")
		assertThat(parser.next()).isEqualTo(KeyboardEvent('l'.code, modifiers = ModifierCtrl))
	}

	@Test fun ctrlM() {
		testTerminal.writeHex("0d")
		assertThat(parser.next()).isEqualTo(KeyboardEvent(0x0d))
	}

	@Test fun ctrlN() {
		testTerminal.writeHex("0e")
		assertThat(parser.next()).isEqualTo(KeyboardEvent('n'.code, modifiers = ModifierCtrl))
	}

	@Test fun ctrlO() {
		testTerminal.writeHex("0f")
		assertThat(parser.next()).isEqualTo(KeyboardEvent('o'.code, modifiers = ModifierCtrl))
	}

	@Test fun ctrlP() {
		testTerminal.writeHex("10")
		assertThat(parser.next()).isEqualTo(KeyboardEvent('p'.code, modifiers = ModifierCtrl))
	}

	@Test fun ctrlQ() {
		testTerminal.writeHex("11")
		assertThat(parser.next()).isEqualTo(KeyboardEvent('q'.code, modifiers = ModifierCtrl))
	}

	@Test fun ctrlR() {
		testTerminal.writeHex("12")
		assertThat(parser.next()).isEqualTo(KeyboardEvent('r'.code, modifiers = ModifierCtrl))
	}

	@Test fun ctrlS() {
		testTerminal.writeHex("13")
		assertThat(parser.next()).isEqualTo(KeyboardEvent('s'.code, modifiers = ModifierCtrl))
	}

	@Test fun ctrlT() {
		testTerminal.writeHex("14")
		assertThat(parser.next()).isEqualTo(KeyboardEvent('t'.code, modifiers = ModifierCtrl))
	}

	@Test fun ctrlU() {
		testTerminal.writeHex("15")
		assertThat(parser.next()).isEqualTo(KeyboardEvent('u'.code, modifiers = ModifierCtrl))
	}

	@Test fun ctrlV() {
		testTerminal.writeHex("16")
		assertThat(parser.next()).isEqualTo(KeyboardEvent('v'.code, modifiers = ModifierCtrl))
	}

	@Test fun ctrlW() {
		testTerminal.writeHex("17")
		assertThat(parser.next()).isEqualTo(KeyboardEvent('w'.code, modifiers = ModifierCtrl))
	}

	@Test fun ctrlX() {
		testTerminal.writeHex("18")
		assertThat(parser.next()).isEqualTo(KeyboardEvent('x'.code, modifiers = ModifierCtrl))
	}

	@Test fun ctrlY() {
		testTerminal.writeHex("19")
		assertThat(parser.next()).isEqualTo(KeyboardEvent('y'.code, modifiers = ModifierCtrl))
	}

	@Test fun ctrlZ() {
		testTerminal.writeHex("1a")
		assertThat(parser.next()).isEqualTo(KeyboardEvent('z'.code, modifiers = ModifierCtrl))
	}

	@Test fun bareEscape() {
		testTerminal.writeHex("1b")
		assertThat(parser.next()).isEqualTo(KeyboardEvent(0x1b))
	}

	@Test fun hex1c() {
		testTerminal.writeHex("1c")
		assertThat(parser.next()).isEqualTo(KeyboardEvent(0x1c))
	}

	@Test fun hex1d() {
		testTerminal.writeHex("1d")
		assertThat(parser.next()).isEqualTo(KeyboardEvent(0x1d))
	}

	@Test fun hex1e() {
		testTerminal.writeHex("1e")
		assertThat(parser.next()).isEqualTo(KeyboardEvent(0x1e))
	}

	@Test fun hex1f() {
		testTerminal.writeHex("1f")
		assertThat(parser.next()).isEqualTo(KeyboardEvent(0x1f))
	}

	@Test fun utf8TwoBytes() {
		testTerminal.writeHex("cea9")
		assertThat(parser.next()).isEqualTo(KeyboardEvent('Ω'.code))
	}

	@Test fun utf8ThreeBytes() {
		testTerminal.writeHex("e28988")
		assertThat(parser.next()).isEqualTo(KeyboardEvent('≈'.code))
	}
}
