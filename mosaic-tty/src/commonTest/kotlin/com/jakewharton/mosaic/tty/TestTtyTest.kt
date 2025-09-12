package com.jakewharton.mosaic.tty

import app.cash.burst.Burst
import assertk.assertFailure
import assertk.assertThat
import assertk.assertions.hasMessage
import assertk.assertions.isEqualTo
import assertk.assertions.isInstanceOf
import assertk.assertions.isZero
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest

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

	@Test fun readWhatWasWritten() {
		tty.write("hello")

		val bufferA = ByteArray(10) { 'x'.code.toByte() }
		val readA = testTty.read(bufferA, 0, 10)
		assertThat(readA, "readA").isEqualTo(5)
		assertThat(bufferA.decodeToString()).isEqualTo("helloxxxxx")

		tty.write("world")

		val bufferB = ByteArray(10) { 'x'.code.toByte() }
		val readB = testTty.read(bufferB, 0, 10)
		assertThat(readB, "readB").isEqualTo(5)
		assertThat(bufferB.decodeToString()).isEqualTo("worldxxxxx")
	}

	@Test fun readOnlyUpToCount() {
		tty.write("abcdefghij")

		val buffer = ByteArray(10) { 'x'.code.toByte() }
		val read = testTty.read(buffer, 0, 5)
		assertThat(read).isEqualTo(5)
		assertThat(buffer.decodeToString()).isEqualTo("abcdexxxxx")

		// Drain the TTY output buffer, otherwise resetting raw mode will hang on its flush.
		testTty.read(5)
	}

	@Test fun readUnderflow() {
		tty.write("hello")

		val buffer = ByteArray(10) { 'x'.code.toByte() }
		val read = testTty.read(buffer, 0, 10)
		assertThat(read).isEqualTo(5)
		assertThat(buffer.decodeToString()).isEqualTo("helloxxxxx")
	}

	@Test fun readAtOffset() {
		tty.write("hello")

		val buffer = ByteArray(10) { 'x'.code.toByte() }
		val read = testTty.read(buffer, 5, 5)
		assertThat(read).isEqualTo(5)
		assertThat(buffer.decodeToString()).isEqualTo("xxxxxhello")
	}

	@Test fun readCanBeInterrupted() = runTest {
		backgroundScope.launch(Dispatchers.Default) {
			delay(150.milliseconds)
			testTty.interruptRead()
		}
		val readA = testTty.read(ByteArray(10), 0, 10)
		assertThat(readA).isZero()

		backgroundScope.launch(Dispatchers.Default) {
			delay(150.milliseconds)
			testTty.interruptRead()
		}
		val readB = testTty.read(ByteArray(10), 0, 10)
		assertThat(readB).isZero()
	}

	@Test fun writeOnlyUpToCount() {
		val written = testTty.write("abcdefghij".encodeToByteArray(), 0, 5)
		assertThat(written).isEqualTo(5)

		val buffer = ByteArray(10) { 'x'.code.toByte() }
		val read = tty.read(buffer, 0, 10)
		assertThat(read).isEqualTo(5)
		assertThat(buffer.decodeToString()).isEqualTo("abcdexxxxx")
	}

	@Test fun writeAtOffset() {
		val written = testTty.write("abcdefghij".encodeToByteArray(), 5, 5)
		assertThat(written).isEqualTo(5)

		val buffer = ByteArray(10) { 'x'.code.toByte() }
		val read = tty.read(buffer, 0, 10)
		assertThat(read).isEqualTo(5)
		assertThat(buffer.decodeToString()).isEqualTo("fghijxxxxx")
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
