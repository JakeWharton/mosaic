package com.jakewharton.mosaic.terminal

import assertk.assertThat
import assertk.assertions.isEqualTo
import com.jakewharton.mosaic.terminal.event.KeyboardEvent
import com.jakewharton.mosaic.terminal.event.KeyboardEvent.Companion.ModifierCtrl
import kotlin.test.Test
import kotlinx.coroutines.test.runTest

class TerminalParserGroundKeyboardEventTest : BaseTerminalParserTest() {
	@Test fun graphic() = runTest {
		for (codepoint in 0x20..0x7f) {
			val hex = codepoint.toString(16)
			testTty.writeHex(hex)
			assertThat(parser.next(), hex).isEqualTo(KeyboardEvent(codepoint))
		}
	}

	@Test fun ctrlShiftAt() = runTest {
		testTty.writeHex("00")
		assertThat(parser.next()).isEqualTo(KeyboardEvent('@'.code, modifiers = ModifierCtrl))
	}

	@Test fun ctrlA() = runTest {
		testTty.writeHex("01")
		assertThat(parser.next()).isEqualTo(KeyboardEvent('a'.code, modifiers = ModifierCtrl))
	}

	@Test fun ctrlB() = runTest {
		testTty.writeHex("02")
		assertThat(parser.next()).isEqualTo(KeyboardEvent('b'.code, modifiers = ModifierCtrl))
	}

	@Test fun ctrlC() = runTest {
		testTty.writeHex("03")
		assertThat(parser.next()).isEqualTo(KeyboardEvent('c'.code, modifiers = ModifierCtrl))
	}

	@Test fun ctrlD() = runTest {
		testTty.writeHex("04")
		assertThat(parser.next()).isEqualTo(KeyboardEvent('d'.code, modifiers = ModifierCtrl))
	}

	@Test fun ctrlE() = runTest {
		testTty.writeHex("05")
		assertThat(parser.next()).isEqualTo(KeyboardEvent('e'.code, modifiers = ModifierCtrl))
	}

	@Test fun ctrlF() = runTest {
		testTty.writeHex("06")
		assertThat(parser.next()).isEqualTo(KeyboardEvent('f'.code, modifiers = ModifierCtrl))
	}

	@Test fun ctrlG() = runTest {
		testTty.writeHex("07")
		assertThat(parser.next()).isEqualTo(KeyboardEvent('g'.code, modifiers = ModifierCtrl))
	}

	@Test fun ctrlH() = runTest {
		testTty.writeHex("08")
		assertThat(parser.next()).isEqualTo(KeyboardEvent(0x7f))
	}

	@Test fun ctrlI() = runTest {
		testTty.writeHex("09")
		assertThat(parser.next()).isEqualTo(KeyboardEvent(0x09))
	}

	@Test fun ctrlJ() = runTest {
		testTty.writeHex("0a")
		assertThat(parser.next()).isEqualTo(KeyboardEvent(0x0d))
	}

	@Test fun ctrlK() = runTest {
		testTty.writeHex("0b")
		assertThat(parser.next()).isEqualTo(KeyboardEvent('k'.code, modifiers = ModifierCtrl))
	}

	@Test fun ctrlL() = runTest {
		testTty.writeHex("0c")
		assertThat(parser.next()).isEqualTo(KeyboardEvent('l'.code, modifiers = ModifierCtrl))
	}

	@Test fun ctrlM() = runTest {
		testTty.writeHex("0d")
		assertThat(parser.next()).isEqualTo(KeyboardEvent(0x0d))
	}

	@Test fun ctrlN() = runTest {
		testTty.writeHex("0e")
		assertThat(parser.next()).isEqualTo(KeyboardEvent('n'.code, modifiers = ModifierCtrl))
	}

	@Test fun ctrlO() = runTest {
		testTty.writeHex("0f")
		assertThat(parser.next()).isEqualTo(KeyboardEvent('o'.code, modifiers = ModifierCtrl))
	}

	@Test fun ctrlP() = runTest {
		testTty.writeHex("10")
		assertThat(parser.next()).isEqualTo(KeyboardEvent('p'.code, modifiers = ModifierCtrl))
	}

	@Test fun ctrlQ() = runTest {
		testTty.writeHex("11")
		assertThat(parser.next()).isEqualTo(KeyboardEvent('q'.code, modifiers = ModifierCtrl))
	}

	@Test fun ctrlR() = runTest {
		testTty.writeHex("12")
		assertThat(parser.next()).isEqualTo(KeyboardEvent('r'.code, modifiers = ModifierCtrl))
	}

	@Test fun ctrlS() = runTest {
		testTty.writeHex("13")
		assertThat(parser.next()).isEqualTo(KeyboardEvent('s'.code, modifiers = ModifierCtrl))
	}

	@Test fun ctrlT() = runTest {
		testTty.writeHex("14")
		assertThat(parser.next()).isEqualTo(KeyboardEvent('t'.code, modifiers = ModifierCtrl))
	}

	@Test fun ctrlU() = runTest {
		testTty.writeHex("15")
		assertThat(parser.next()).isEqualTo(KeyboardEvent('u'.code, modifiers = ModifierCtrl))
	}

	@Test fun ctrlV() = runTest {
		testTty.writeHex("16")
		assertThat(parser.next()).isEqualTo(KeyboardEvent('v'.code, modifiers = ModifierCtrl))
	}

	@Test fun ctrlW() = runTest {
		testTty.writeHex("17")
		assertThat(parser.next()).isEqualTo(KeyboardEvent('w'.code, modifiers = ModifierCtrl))
	}

	@Test fun ctrlX() = runTest {
		testTty.writeHex("18")
		assertThat(parser.next()).isEqualTo(KeyboardEvent('x'.code, modifiers = ModifierCtrl))
	}

	@Test fun ctrlY() = runTest {
		testTty.writeHex("19")
		assertThat(parser.next()).isEqualTo(KeyboardEvent('y'.code, modifiers = ModifierCtrl))
	}

	@Test fun ctrlZ() = runTest {
		testTty.writeHex("1a")
		assertThat(parser.next()).isEqualTo(KeyboardEvent('z'.code, modifiers = ModifierCtrl))
	}

	@Test fun bareEscape() = runTest {
		testTty.writeHex("1b")
		assertThat(parser.next()).isEqualTo(KeyboardEvent(0x1b))
	}

	@Test fun hex1c() = runTest {
		testTty.writeHex("1c")
		assertThat(parser.next()).isEqualTo(KeyboardEvent(0x1c))
	}

	@Test fun hex1d() = runTest {
		testTty.writeHex("1d")
		assertThat(parser.next()).isEqualTo(KeyboardEvent(0x1d))
	}

	@Test fun hex1e() = runTest {
		testTty.writeHex("1e")
		assertThat(parser.next()).isEqualTo(KeyboardEvent(0x1e))
	}

	@Test fun hex1f() = runTest {
		testTty.writeHex("1f")
		assertThat(parser.next()).isEqualTo(KeyboardEvent(0x1f))
	}

	@Test fun utf8TwoBytes() = runTest {
		testTty.writeHex("cea9")
		assertThat(parser.next()).isEqualTo(KeyboardEvent('Ω'.code))
	}

	@Test fun utf8ThreeBytes() = runTest {
		testTty.writeHex("e28988")
		assertThat(parser.next()).isEqualTo(KeyboardEvent('≈'.code))
	}
}
