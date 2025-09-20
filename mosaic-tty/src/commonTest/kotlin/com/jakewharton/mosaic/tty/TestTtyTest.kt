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
	private var rawTestTty: TestTty? = null
	private var testTty: TestTty
		get() {
			return rawTestTty ?: TestTty.bind().also {
				it.tty.enableRawMode()
				rawTestTty = it
			}
		}
		set(value) {
			check(rawTestTty == null) { "TestTty already created" }
			rawTestTty = value
			value.tty.enableRawMode()
		}

	private val tty: Tty get() = testTty.tty

	@AfterTest fun after() {
		testTty.close()
	}

	@Test fun onlyOne() {
		// Force read to trigger initialization.
		testTty

		assertFailure {
			TestTty.bind()
		}.isInstanceOf<IllegalStateException>()
			.hasMessage("TestTty or Tty already bound")
	}

	@Test fun multipleRawModeResetCycles() {
		repeat(10) {
			tty.reset()
			tty.enableRawMode()
		}
	}

	@Test fun stdinIsTtySetting(value: Boolean) {
		testTty = TestTty.bind(stdinIsTty = value)
		assertThat(testTty.streams.isInputTty()).isEqualTo(value)
	}

	@Test fun stdOutIsTtySetting(value: Boolean) {
		testTty = TestTty.bind(stdoutIsTty = value)
		assertThat(testTty.streams.isOutputTty()).isEqualTo(value)
	}

	@Test fun stdinErrTtySetting(value: Boolean) {
		testTty = TestTty.bind(stderrIsTty = value)
		assertThat(testTty.streams.isErrorTty()).isEqualTo(value)
	}
}
