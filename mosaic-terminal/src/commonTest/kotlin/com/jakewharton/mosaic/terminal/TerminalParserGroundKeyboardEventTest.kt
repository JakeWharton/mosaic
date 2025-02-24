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
			assertThat(reader.next(), hex).isEqualTo(KeyboardEvent(codepoint))
		}
	}

	@Test fun ctrlShiftAt() = runTest {
		testTty.writeHex("00")
		assertThat(reader.next()).isEqualTo(KeyboardEvent('@'.code, modifiers = ModifierCtrl))
	}

	@Test fun ctrlA() = runTest {
		testTty.writeHex("01")
		assertThat(reader.next()).isEqualTo(KeyboardEvent('a'.code, modifiers = ModifierCtrl))
	}

	@Test fun ctrlB() = runTest {
		testTty.writeHex("02")
		assertThat(reader.next()).isEqualTo(KeyboardEvent('b'.code, modifiers = ModifierCtrl))
	}

	@Test fun ctrlC() = runTest {
		testTty.writeHex("03")
		assertThat(reader.next()).isEqualTo(KeyboardEvent('c'.code, modifiers = ModifierCtrl))
	}

	@Test fun ctrlD() = runTest {
		testTty.writeHex("04")
		assertThat(reader.next()).isEqualTo(KeyboardEvent('d'.code, modifiers = ModifierCtrl))
	}

	@Test fun ctrlE() = runTest {
		testTty.writeHex("05")
		assertThat(reader.next()).isEqualTo(KeyboardEvent('e'.code, modifiers = ModifierCtrl))
	}

	@Test fun ctrlF() = runTest {
		testTty.writeHex("06")
		assertThat(reader.next()).isEqualTo(KeyboardEvent('f'.code, modifiers = ModifierCtrl))
	}

	@Test fun ctrlG() = runTest {
		testTty.writeHex("07")
		assertThat(reader.next()).isEqualTo(KeyboardEvent('g'.code, modifiers = ModifierCtrl))
	}

	@Test fun ctrlH() = runTest {
		testTty.writeHex("08")
		assertThat(reader.next()).isEqualTo(KeyboardEvent(0x7f))
	}

	@Test fun ctrlI() = runTest {
		testTty.writeHex("09")
		assertThat(reader.next()).isEqualTo(KeyboardEvent(0x09))
	}

	@Test fun ctrlJ() = runTest {
		testTty.writeHex("0a")
		assertThat(reader.next()).isEqualTo(KeyboardEvent(0x0d))
	}

	@Test fun ctrlK() = runTest {
		testTty.writeHex("0b")
		assertThat(reader.next()).isEqualTo(KeyboardEvent('k'.code, modifiers = ModifierCtrl))
	}

	@Test fun ctrlL() = runTest {
		testTty.writeHex("0c")
		assertThat(reader.next()).isEqualTo(KeyboardEvent('l'.code, modifiers = ModifierCtrl))
	}

	@Test fun ctrlM() = runTest {
		testTty.writeHex("0d")
		assertThat(reader.next()).isEqualTo(KeyboardEvent(0x0d))
	}

	@Test fun ctrlN() = runTest {
		testTty.writeHex("0e")
		assertThat(reader.next()).isEqualTo(KeyboardEvent('n'.code, modifiers = ModifierCtrl))
	}

	@Test fun ctrlO() = runTest {
		testTty.writeHex("0f")
		assertThat(reader.next()).isEqualTo(KeyboardEvent('o'.code, modifiers = ModifierCtrl))
	}

	@Test fun ctrlP() = runTest {
		testTty.writeHex("10")
		assertThat(reader.next()).isEqualTo(KeyboardEvent('p'.code, modifiers = ModifierCtrl))
	}

	@Test fun ctrlQ() = runTest {
		testTty.writeHex("11")
		assertThat(reader.next()).isEqualTo(KeyboardEvent('q'.code, modifiers = ModifierCtrl))
	}

	@Test fun ctrlR() = runTest {
		testTty.writeHex("12")
		assertThat(reader.next()).isEqualTo(KeyboardEvent('r'.code, modifiers = ModifierCtrl))
	}

	@Test fun ctrlS() = runTest {
		testTty.writeHex("13")
		assertThat(reader.next()).isEqualTo(KeyboardEvent('s'.code, modifiers = ModifierCtrl))
	}

	@Test fun ctrlT() = runTest {
		testTty.writeHex("14")
		assertThat(reader.next()).isEqualTo(KeyboardEvent('t'.code, modifiers = ModifierCtrl))
	}

	@Test fun ctrlU() = runTest {
		testTty.writeHex("15")
		assertThat(reader.next()).isEqualTo(KeyboardEvent('u'.code, modifiers = ModifierCtrl))
	}

	@Test fun ctrlV() = runTest {
		testTty.writeHex("16")
		assertThat(reader.next()).isEqualTo(KeyboardEvent('v'.code, modifiers = ModifierCtrl))
	}

	@Test fun ctrlW() = runTest {
		testTty.writeHex("17")
		assertThat(reader.next()).isEqualTo(KeyboardEvent('w'.code, modifiers = ModifierCtrl))
	}

	@Test fun ctrlX() = runTest {
		testTty.writeHex("18")
		assertThat(reader.next()).isEqualTo(KeyboardEvent('x'.code, modifiers = ModifierCtrl))
	}

	@Test fun ctrlY() = runTest {
		testTty.writeHex("19")
		assertThat(reader.next()).isEqualTo(KeyboardEvent('y'.code, modifiers = ModifierCtrl))
	}

	@Test fun ctrlZ() = runTest {
		testTty.writeHex("1a")
		assertThat(reader.next()).isEqualTo(KeyboardEvent('z'.code, modifiers = ModifierCtrl))
	}

	@Test fun bareEscape() = runTest {
		testTty.writeHex("1b")
		assertThat(reader.next()).isEqualTo(KeyboardEvent(0x1b))
	}

	@Test fun hex1c() = runTest {
		testTty.writeHex("1c")
		assertThat(reader.next()).isEqualTo(KeyboardEvent(0x1c))
	}

	@Test fun hex1d() = runTest {
		testTty.writeHex("1d")
		assertThat(reader.next()).isEqualTo(KeyboardEvent(0x1d))
	}

	@Test fun hex1e() = runTest {
		testTty.writeHex("1e")
		assertThat(reader.next()).isEqualTo(KeyboardEvent(0x1e))
	}

	@Test fun hex1f() = runTest {
		testTty.writeHex("1f")
		assertThat(reader.next()).isEqualTo(KeyboardEvent(0x1f))
	}

	@Test fun utf8TwoBytes() = runTest {
		testTty.writeHex("cea9")
		assertThat(reader.next()).isEqualTo(KeyboardEvent('Ω'.code))
	}

	@Test fun utf8ThreeBytes() = runTest {
		testTty.writeHex("e28988")
		assertThat(reader.next()).isEqualTo(KeyboardEvent('≈'.code))
	}
}
