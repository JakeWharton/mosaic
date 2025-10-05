package com.jakewharton.mosaic.tty

import app.cash.burst.Burst
import assertk.assertFailure
import assertk.assertThat
import assertk.assertions.hasMessage
import assertk.assertions.isEqualTo
import assertk.assertions.isInstanceOf
import kotlin.test.AfterTest
import kotlin.test.Test

@Burst
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

	@Test fun stdinIsTtySetting(value: Boolean) {
		testTerminal = TestTerminal.bind(stdinIsTty = value)
		assertThat(testTerminal.streams.isInputTty()).isEqualTo(value)
	}

	@Test fun stdOutIsTtySetting(value: Boolean) {
		testTerminal = TestTerminal.bind(stdoutIsTty = value)
		assertThat(testTerminal.streams.isOutputTty()).isEqualTo(value)
	}

	@Test fun stdinErrTtySetting(value: Boolean) {
		testTerminal = TestTerminal.bind(stderrIsTty = value)
		assertThat(testTerminal.streams.isErrorTty()).isEqualTo(value)
	}
}
