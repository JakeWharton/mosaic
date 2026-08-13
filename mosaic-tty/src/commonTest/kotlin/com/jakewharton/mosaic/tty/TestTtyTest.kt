package com.jakewharton.mosaic.tty

import assertk.assertFailure
import assertk.assertThat
import assertk.assertions.hasMessage
import assertk.assertions.isFalse
import assertk.assertions.isInstanceOf
import assertk.assertions.isTrue
import kotlin.test.AfterTest
import kotlin.test.Test

class TestTtyTest {
	private var rawTestTerminal: TestTerminal? = null
	private var testTerminal: TestTerminal
		get() {
			return rawTestTerminal ?: TestTerminal.bind().also {
				it.tty.enableRawMode()
				rawTestTerminal = it
			}
		}
		set(value) {
			check(rawTestTerminal == null) { "TestTty already created" }
			rawTestTerminal = value
			value.tty.enableRawMode()
		}

	private val tty: Tty get() = testTerminal.tty

	@AfterTest fun after() {
		testTerminal.close()
	}

	@Test fun onlyOne() {
		// Force read to trigger initialization.
		testTerminal

		assertFailure {
			TestTerminal.bind()
		}.isInstanceOf<IllegalStateException>()
			.hasMessage("TestTerminal or Tty already bound")
	}

	@Test fun multipleRawModeResetCycles() {
		repeat(10) {
			tty.reset()
			tty.enableRawMode()
		}
	}

	@Test fun stdinIsTtySettingTrue() {
		testTerminal = TestTerminal.bind(stdinIsTty = true)
		assertThat(testTerminal.streams.isInputTty()).isTrue()
	}

	@Test fun stdinIsTtySettingFalse() {
		testTerminal = TestTerminal.bind(stdinIsTty = false)
		assertThat(testTerminal.streams.isInputTty()).isFalse()
	}

	@Test fun stdOutIsTtySettingTrue() {
		testTerminal = TestTerminal.bind(stdoutIsTty = true)
		assertThat(testTerminal.streams.isOutputTty()).isTrue()
	}

	@Test fun stdOutIsTtySettingFalse() {
		testTerminal = TestTerminal.bind(stdoutIsTty = false)
		assertThat(testTerminal.streams.isOutputTty()).isFalse()
	}

	@Test fun stdinErrTtySettingTrue() {
		testTerminal = TestTerminal.bind(stderrIsTty = true)
		assertThat(testTerminal.streams.isErrorTty()).isTrue()
	}

	@Test fun stdinErrTtySettingFalse() {
		testTerminal = TestTerminal.bind(stderrIsTty = true)
		assertThat(testTerminal.streams.isErrorTty()).isTrue()
	}
}
