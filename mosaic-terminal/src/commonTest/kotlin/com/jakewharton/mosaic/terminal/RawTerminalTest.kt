package com.jakewharton.mosaic.terminal

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isGreaterThan
import assertk.assertions.isZero
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.measureTime
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class RawTerminalTest {
	private val testTerminal = TestTerminal.create()
	private val rawTerminal = testTerminal.rawTerminal

	@AfterTest fun after() {
		testTerminal.close()
	}

	@Test fun readWhatWasWritten() {
		val buffer = ByteArray(10) { 'x'.code.toByte() }

		testTerminal.write("hello".encodeToByteArray())
		val readA = rawTerminal.read(buffer, 0, 10)
		assertThat(readA, "readA").isEqualTo(5)
		assertThat(buffer.decodeToString()).isEqualTo("helloxxxxx")

		testTerminal.write("world".encodeToByteArray())
		val readB = rawTerminal.read(buffer, 0, 10)
		assertThat(readB, "readB").isEqualTo(5)
		assertThat(buffer.decodeToString()).isEqualTo("worldxxxxx")
	}

	@Test fun readOnlyUpToCount() {
		val buffer = ByteArray(10) { 'x'.code.toByte() }

		testTerminal.write("hello".encodeToByteArray())
		val read = rawTerminal.read(buffer, 0, 4)
		assertThat(read).isEqualTo(4)
		assertThat(buffer.decodeToString()).isEqualTo("hellxxxxxx")
	}

	@Test fun readUnderflow() {
		val buffer = ByteArray(10) { 'x'.code.toByte() }

		testTerminal.write("hello".encodeToByteArray())
		val read = rawTerminal.read(buffer, 0, 10)
		assertThat(read).isEqualTo(5)
		assertThat(buffer.decodeToString()).isEqualTo("helloxxxxx")
	}

	@Test fun readAtOffset() {
		val buffer = ByteArray(10) { 'x'.code.toByte() }

		testTerminal.write("hello".encodeToByteArray())
		val read = rawTerminal.read(buffer, 5, 5)
		assertThat(read).isEqualTo(5)
		assertThat(buffer.decodeToString()).isEqualTo("xxxxxhello")
	}

	@Test fun readCanBeInterrupted() {
		GlobalScope.launch(Dispatchers.Default) {
			delay(150.milliseconds)
			rawTerminal.interruptRead()
		}
		val readA = rawTerminal.read(ByteArray(10), 0, 10)
		assertThat(readA).isZero()

		GlobalScope.launch(Dispatchers.Default) {
			delay(150.milliseconds)
			rawTerminal.interruptRead()
		}
		val readB = rawTerminal.read(ByteArray(10), 0, 10)
		assertThat(readB).isZero()
	}

	@Test fun readWithTimeoutReturnsZeroOnTimeout() {
		// Windows appears to be happy to return a few milliseconds early, so we just validate a
		// conservative lower bound which indicates that there was at least _some_ waiting.

		val readA: Int
		val tookA = measureTime {
			readA = rawTerminal.read(ByteArray(10), 0, 10, 100)
		}
		assertThat(readA).isZero()
		assertThat(tookA).isGreaterThan(50.milliseconds)

		val readB: Int
		val tookB = measureTime {
			readB = rawTerminal.read(ByteArray(10), 0, 10, 100)
		}
		assertThat(readB).isZero()
		assertThat(tookB).isGreaterThan(50.milliseconds)
	}
}
