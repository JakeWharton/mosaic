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
			testTty.writeHex(hex)
			assertThat(parser.next(), hex).isEqualTo(KeyboardEvent(codepoint))
		}
	}

	@Test fun ctrlShiftAt() {
		testTty.writeHex("00")
		assertThat(parser.next()).isEqualTo(KeyboardEvent('@'.code, modifiers = ModifierCtrl))
	}

	@Test fun ctrlA() {
		testTty.writeHex("01")
		assertThat(parser.next()).isEqualTo(KeyboardEvent('a'.code, modifiers = ModifierCtrl))
	}

	@Test fun ctrlB() {
		testTty.writeHex("02")
		assertThat(parser.next()).isEqualTo(KeyboardEvent('b'.code, modifiers = ModifierCtrl))
	}

	@Test fun ctrlC() {
		testTty.writeHex("03")
		assertThat(parser.next()).isEqualTo(KeyboardEvent('c'.code, modifiers = ModifierCtrl))
	}

	@Test fun ctrlD() {
		testTty.writeHex("04")
		assertThat(parser.next()).isEqualTo(KeyboardEvent('d'.code, modifiers = ModifierCtrl))
	}

	@Test fun ctrlE() {
		testTty.writeHex("05")
		assertThat(parser.next()).isEqualTo(KeyboardEvent('e'.code, modifiers = ModifierCtrl))
	}

	@Test fun ctrlF() {
		testTty.writeHex("06")
		assertThat(parser.next()).isEqualTo(KeyboardEvent('f'.code, modifiers = ModifierCtrl))
	}

	@Test fun ctrlG() {
		testTty.writeHex("07")
		assertThat(parser.next()).isEqualTo(KeyboardEvent('g'.code, modifiers = ModifierCtrl))
	}

	@Test fun ctrlH() {
		testTty.writeHex("08")
		assertThat(parser.next()).isEqualTo(KeyboardEvent(0x7f))
	}

	@Test fun ctrlI() {
		testTty.writeHex("09")
		assertThat(parser.next()).isEqualTo(KeyboardEvent(0x09))
	}

	@Test fun ctrlJ() {
		testTty.writeHex("0a")
		assertThat(parser.next()).isEqualTo(KeyboardEvent(0x0d))
	}

	@Test fun ctrlK() {
		testTty.writeHex("0b")
		assertThat(parser.next()).isEqualTo(KeyboardEvent('k'.code, modifiers = ModifierCtrl))
	}

	@Test fun ctrlL() {
		testTty.writeHex("0c")
		assertThat(parser.next()).isEqualTo(KeyboardEvent('l'.code, modifiers = ModifierCtrl))
	}

	@Test fun ctrlM() {
		testTty.writeHex("0d")
		assertThat(parser.next()).isEqualTo(KeyboardEvent(0x0d))
	}

	@Test fun ctrlN() {
		testTty.writeHex("0e")
		assertThat(parser.next()).isEqualTo(KeyboardEvent('n'.code, modifiers = ModifierCtrl))
	}

	@Test fun ctrlO() {
		testTty.writeHex("0f")
		assertThat(parser.next()).isEqualTo(KeyboardEvent('o'.code, modifiers = ModifierCtrl))
	}

	@Test fun ctrlP() {
		testTty.writeHex("10")
		assertThat(parser.next()).isEqualTo(KeyboardEvent('p'.code, modifiers = ModifierCtrl))
	}

	@Test fun ctrlQ() {
		testTty.writeHex("11")
		assertThat(parser.next()).isEqualTo(KeyboardEvent('q'.code, modifiers = ModifierCtrl))
	}

	@Test fun ctrlR() {
		testTty.writeHex("12")
		assertThat(parser.next()).isEqualTo(KeyboardEvent('r'.code, modifiers = ModifierCtrl))
	}

	@Test fun ctrlS() {
		testTty.writeHex("13")
		assertThat(parser.next()).isEqualTo(KeyboardEvent('s'.code, modifiers = ModifierCtrl))
	}

	@Test fun ctrlT() {
		testTty.writeHex("14")
		assertThat(parser.next()).isEqualTo(KeyboardEvent('t'.code, modifiers = ModifierCtrl))
	}

	@Test fun ctrlU() {
		testTty.writeHex("15")
		assertThat(parser.next()).isEqualTo(KeyboardEvent('u'.code, modifiers = ModifierCtrl))
	}

	@Test fun ctrlV() {
		testTty.writeHex("16")
		assertThat(parser.next()).isEqualTo(KeyboardEvent('v'.code, modifiers = ModifierCtrl))
	}

	@Test fun ctrlW() {
		testTty.writeHex("17")
		assertThat(parser.next()).isEqualTo(KeyboardEvent('w'.code, modifiers = ModifierCtrl))
	}

	@Test fun ctrlX() {
		testTty.writeHex("18")
		assertThat(parser.next()).isEqualTo(KeyboardEvent('x'.code, modifiers = ModifierCtrl))
	}

	@Test fun ctrlY() {
		testTty.writeHex("19")
		assertThat(parser.next()).isEqualTo(KeyboardEvent('y'.code, modifiers = ModifierCtrl))
	}

	@Test fun ctrlZ() {
		testTty.writeHex("1a")
		assertThat(parser.next()).isEqualTo(KeyboardEvent('z'.code, modifiers = ModifierCtrl))
	}

	@Test fun bareEscape() {
		testTty.writeHex("1b")
		assertThat(parser.next()).isEqualTo(KeyboardEvent(0x1b))
	}

	@Test fun hex1c() {
		testTty.writeHex("1c")
		assertThat(parser.next()).isEqualTo(KeyboardEvent(0x1c))
	}

	@Test fun hex1d() {
		testTty.writeHex("1d")
		assertThat(parser.next()).isEqualTo(KeyboardEvent(0x1d))
	}

	@Test fun hex1e() {
		testTty.writeHex("1e")
		assertThat(parser.next()).isEqualTo(KeyboardEvent(0x1e))
	}

	@Test fun hex1f() {
		testTty.writeHex("1f")
		assertThat(parser.next()).isEqualTo(KeyboardEvent(0x1f))
	}

	@Test fun utf8TwoBytes() {
		testTty.writeHex("cea9")
		assertThat(parser.next()).isEqualTo(KeyboardEvent('Ω'.code))
	}

	@Test fun utf8ThreeBytes() {
		testTty.writeHex("e28988")
		assertThat(parser.next()).isEqualTo(KeyboardEvent('≈'.code))
	}
}
