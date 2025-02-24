package com.jakewharton.mosaic.tty

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isGreaterThan
import assertk.assertions.isZero
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.measureTime
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest

class TtyTest {
	private val testTty = TestTty.create(object : Tty.Callback {
		override fun onFocus(focused: Boolean) {}
		override fun onKey() {}
		override fun onMouse() {}
		override fun onResize(columns: Int, rows: Int, width: Int, height: Int) {}
	})
	private val tty = testTty.tty

	@AfterTest fun after() {
		tty.close()
		testTty.close()
	}

	@Test fun readWhatWasWritten() {
		val buffer = ByteArray(10) { 'x'.code.toByte() }

		testTty.write("hello".encodeToByteArray())
		val readA = tty.read(buffer, 0, 10)
		assertThat(readA, "readA").isEqualTo(5)
		assertThat(buffer.decodeToString()).isEqualTo("helloxxxxx")

		testTty.write("world".encodeToByteArray())
		val readB = tty.read(buffer, 0, 10)
		assertThat(readB, "readB").isEqualTo(5)
		assertThat(buffer.decodeToString()).isEqualTo("worldxxxxx")
	}

	@Test fun readOnlyUpToCount() {
		val buffer = ByteArray(10) { 'x'.code.toByte() }

		testTty.write("hello".encodeToByteArray())
		val read = tty.read(buffer, 0, 4)
		assertThat(read).isEqualTo(4)
		assertThat(buffer.decodeToString()).isEqualTo("hellxxxxxx")
	}

	@Test fun readUnderflow() {
		val buffer = ByteArray(10) { 'x'.code.toByte() }

		testTty.write("hello".encodeToByteArray())
		val read = tty.read(buffer, 0, 10)
		assertThat(read).isEqualTo(5)
		assertThat(buffer.decodeToString()).isEqualTo("helloxxxxx")
	}

	@Test fun readAtOffset() {
		val buffer = ByteArray(10) { 'x'.code.toByte() }

		testTty.write("hello".encodeToByteArray())
		val read = tty.read(buffer, 5, 5)
		assertThat(read).isEqualTo(5)
		assertThat(buffer.decodeToString()).isEqualTo("xxxxxhello")
	}

	@Test fun readCanBeInterrupted() = runTest {
		backgroundScope.launch(Dispatchers.Default) {
			delay(150.milliseconds)
			tty.interrupt()
		}
		val readA = tty.read(ByteArray(10), 0, 10)
		assertThat(readA).isZero()

		backgroundScope.launch(Dispatchers.Default) {
			delay(150.milliseconds)
			tty.interrupt()
		}
		val readB = tty.read(ByteArray(10), 0, 10)
		assertThat(readB).isZero()
	}

	@Test fun readWithTimeoutReturnsZeroOnTimeout() {
		// Windows appears to be happy to return a few milliseconds early, so we just validate a
		// conservative lower bound which indicates that there was at least _some_ waiting.

		val readA: Int
		val tookA = measureTime {
			readA = tty.readWithTimeout(ByteArray(10), 0, 10, 100)
		}
		assertThat(readA).isZero()
		assertThat(tookA).isGreaterThan(50.milliseconds)

		val readB: Int
		val tookB = measureTime {
			readB = tty.readWithTimeout(ByteArray(10), 0, 10, 100)
		}
		assertThat(readB).isZero()
		assertThat(tookB).isGreaterThan(50.milliseconds)
	}

	@Test fun callbackFocusWorks() {
		testTty.focusEvent(true)
		// TODO read event
	}

	@Test fun callbackKeyWorks() {
		testTty.keyEvent()
		// TODO read event
	}

	@Test fun callbackMouseWorks() {
		testTty.mouseEvent()
		// TODO read event
	}

	@Test fun callbackResizeEvent() {
		testTty.resizeEvent(1, 2, 3, 4)
		// TODO read event
	}
}
