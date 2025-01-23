package com.jakewharton.mosaic.terminal

import assertk.assertThat
import assertk.assertions.isEqualTo
import com.jakewharton.mosaic.terminal.event.KeyboardEvent
import com.jakewharton.mosaic.terminal.event.KeyboardEvent.Companion.ModifierCtrl
import kotlin.test.Test

class TerminalParserGroundKeyboardEventTest : BaseTerminalParserTest() {
	@Test fun graphic() {
		for (codepoint in 0x20..0x7f) {
			val hex = codepoint.toString(16)
			writer.writeHex(hex)
			assertThat(parser.next(), hex).isEqualTo(KeyboardEvent(codepoint))
		}
	}

	@Test fun ctrlShiftAt() {
		writer.writeHex("00")
		assertThat(parser.next()).isEqualTo(KeyboardEvent('@'.code, modifiers = ModifierCtrl))
	}

	@Test fun ctrlA() {
		writer.writeHex("01")
		assertThat(parser.next()).isEqualTo(KeyboardEvent('a'.code, modifiers = ModifierCtrl))
	}

	@Test fun ctrlB() {
		writer.writeHex("02")
		assertThat(parser.next()).isEqualTo(KeyboardEvent('b'.code, modifiers = ModifierCtrl))
	}

	@Test fun ctrlC() {
		writer.writeHex("03")
		assertThat(parser.next()).isEqualTo(KeyboardEvent('c'.code, modifiers = ModifierCtrl))
	}

	@Test fun ctrlD() {
		writer.writeHex("04")
		assertThat(parser.next()).isEqualTo(KeyboardEvent('d'.code, modifiers = ModifierCtrl))
	}

	@Test fun ctrlE() {
		writer.writeHex("05")
		assertThat(parser.next()).isEqualTo(KeyboardEvent('e'.code, modifiers = ModifierCtrl))
	}

	@Test fun ctrlF() {
		writer.writeHex("06")
		assertThat(parser.next()).isEqualTo(KeyboardEvent('f'.code, modifiers = ModifierCtrl))
	}

	@Test fun ctrlG() {
		writer.writeHex("07")
		assertThat(parser.next()).isEqualTo(KeyboardEvent('g'.code, modifiers = ModifierCtrl))
	}

	@Test fun ctrlH() {
		writer.writeHex("08")
		assertThat(parser.next()).isEqualTo(KeyboardEvent(0x7f))
	}

	@Test fun ctrlI() {
		writer.writeHex("09")
		assertThat(parser.next()).isEqualTo(KeyboardEvent(0x09))
	}

	@Test fun ctrlJ() {
		writer.writeHex("0a")
		assertThat(parser.next()).isEqualTo(KeyboardEvent(0x0d))
	}

	@Test fun ctrlK() {
		writer.writeHex("0b")
		assertThat(parser.next()).isEqualTo(KeyboardEvent('k'.code, modifiers = ModifierCtrl))
	}

	@Test fun ctrlL() {
		writer.writeHex("0c")
		assertThat(parser.next()).isEqualTo(KeyboardEvent('l'.code, modifiers = ModifierCtrl))
	}

	@Test fun ctrlM() {
		writer.writeHex("0d")
		assertThat(parser.next()).isEqualTo(KeyboardEvent(0x0d))
	}

	@Test fun ctrlN() {
		writer.writeHex("0e")
		assertThat(parser.next()).isEqualTo(KeyboardEvent('n'.code, modifiers = ModifierCtrl))
	}

	@Test fun ctrlO() {
		writer.writeHex("0f")
		assertThat(parser.next()).isEqualTo(KeyboardEvent('o'.code, modifiers = ModifierCtrl))
	}

	@Test fun ctrlP() {
		writer.writeHex("10")
		assertThat(parser.next()).isEqualTo(KeyboardEvent('p'.code, modifiers = ModifierCtrl))
	}

	@Test fun ctrlQ() {
		writer.writeHex("11")
		assertThat(parser.next()).isEqualTo(KeyboardEvent('q'.code, modifiers = ModifierCtrl))
	}

	@Test fun ctrlR() {
		writer.writeHex("12")
		assertThat(parser.next()).isEqualTo(KeyboardEvent('r'.code, modifiers = ModifierCtrl))
	}

	@Test fun ctrlS() {
		writer.writeHex("13")
		assertThat(parser.next()).isEqualTo(KeyboardEvent('s'.code, modifiers = ModifierCtrl))
	}

	@Test fun ctrlT() {
		writer.writeHex("14")
		assertThat(parser.next()).isEqualTo(KeyboardEvent('t'.code, modifiers = ModifierCtrl))
	}

	@Test fun ctrlU() {
		writer.writeHex("15")
		assertThat(parser.next()).isEqualTo(KeyboardEvent('u'.code, modifiers = ModifierCtrl))
	}

	@Test fun ctrlV() {
		writer.writeHex("16")
		assertThat(parser.next()).isEqualTo(KeyboardEvent('v'.code, modifiers = ModifierCtrl))
	}

	@Test fun ctrlW() {
		writer.writeHex("17")
		assertThat(parser.next()).isEqualTo(KeyboardEvent('w'.code, modifiers = ModifierCtrl))
	}

	@Test fun ctrlX() {
		writer.writeHex("18")
		assertThat(parser.next()).isEqualTo(KeyboardEvent('x'.code, modifiers = ModifierCtrl))
	}

	@Test fun ctrlY() {
		writer.writeHex("19")
		assertThat(parser.next()).isEqualTo(KeyboardEvent('y'.code, modifiers = ModifierCtrl))
	}

	@Test fun ctrlZ() {
		writer.writeHex("1a")
		assertThat(parser.next()).isEqualTo(KeyboardEvent('z'.code, modifiers = ModifierCtrl))
	}

	@Test fun bareEscape() {
		writer.writeHex("1b")
		assertThat(parser.next()).isEqualTo(KeyboardEvent(0x1b))
	}

	@Test fun hex1c() {
		writer.writeHex("1c")
		assertThat(parser.next()).isEqualTo(KeyboardEvent(0x1c))
	}

	@Test fun hex1d() {
		writer.writeHex("1d")
		assertThat(parser.next()).isEqualTo(KeyboardEvent(0x1d))
	}

	@Test fun hex1e() {
		writer.writeHex("1e")
		assertThat(parser.next()).isEqualTo(KeyboardEvent(0x1e))
	}

	@Test fun hex1f() {
		writer.writeHex("1f")
		assertThat(parser.next()).isEqualTo(KeyboardEvent(0x1f))
	}

	@Test fun utf8TwoBytes() {
		writer.writeHex("cea9")
		assertThat(parser.next()).isEqualTo(KeyboardEvent('Ω'.code))
	}

	@Test fun utf8ThreeBytes() {
		writer.writeHex("e28988")
		assertThat(parser.next()).isEqualTo(KeyboardEvent('≈'.code))
	}
}
