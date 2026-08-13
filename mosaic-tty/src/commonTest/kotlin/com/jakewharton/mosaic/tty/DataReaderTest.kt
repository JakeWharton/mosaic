package com.jakewharton.mosaic.tty

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isGreaterThan
import assertk.assertions.isZero
import de.infix.testBalloon.framework.core.testSuite
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.measureTime
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

val DataReaderTests by testSuite {
	val dataReaders = listOf(
		TtyToTestTerminal,
		TestTerminalToTty,
		TestTerminalToStandardInput,
		TestTerminalToStandardInputAsTty,
		StandardOutputToTestTerminal,
		StandardOutputAsTtyToTestTerminal,
		StandardErrorToTestTerminal,
		StandardErrorAsTtyToTestTerminal,
		PrintlnToInterceptedStdout,
		EprintlnToInterceptedStderr,
	)
	val functions = listOf(
		DataReaderTest::readWhatWasWritten,
		DataReaderTest::readOnlyUpToCount,
		DataReaderTest::readUnderflow,
		DataReaderTest::readAtOffset,
		DataReaderTest::readCanBeInterrupted,
		DataReaderTest::readWithTimeoutReturnsZeroOnTimeout,
	)

	for (dataReader in dataReaders) {
		testSuite(dataReader.toString()) {
			val subject = DataReaderTest(dataReader)
			for (function in functions) {
				test(function.name) {
					dataReader.intercept {
						function.invoke(subject)
					}
				}
			}
		}
	}
}

@OptIn(DelicateCoroutinesApi::class) // For simple fire-and-forget parallelism.
private class DataReaderTest(
	private val data: DataReader,
) {
	fun readWhatWasWritten() {
		val buffer = ByteArray(10) { 'x'.code.toByte() }

		data.writeFully("hello")
		val readA = data.read(buffer, 0, 10)
		assertThat(readA, "readA").isEqualTo(5)
		assertThat(buffer.decodeToString()).isEqualTo("helloxxxxx")

		data.writeFully("world")
		val readB = data.read(buffer, 0, 10)
		assertThat(readB, "readB").isEqualTo(5)
		assertThat(buffer.decodeToString()).isEqualTo("worldxxxxx")
	}

	fun readOnlyUpToCount() {
		val buffer = ByteArray(10) { 'x'.code.toByte() }

		data.writeFully("abcdefghij")
		val read = data.read(buffer, 0, 5)
		assertThat(read).isEqualTo(5)
		assertThat(buffer.decodeToString()).isEqualTo("abcdexxxxx")

		// Drain the buffer, otherwise resetting raw mode will hang on its flush.
		data.read(buffer, 0, 5)
	}

	fun readUnderflow() {
		val buffer = ByteArray(10) { 'x'.code.toByte() }

		data.writeFully("hello")
		val read = data.read(buffer, 0, 10)
		assertThat(read).isEqualTo(5)
		assertThat(buffer.decodeToString()).isEqualTo("helloxxxxx")
	}

	fun readAtOffset() {
		val buffer = ByteArray(10) { 'x'.code.toByte() }

		data.writeFully("hello")
		val read = data.read(buffer, 5, 5)
		assertThat(read).isEqualTo(5)
		assertThat(buffer.decodeToString()).isEqualTo("xxxxxhello")
	}

	fun readCanBeInterrupted() {
		GlobalScope.launch(Dispatchers.Default) {
			delay(150.milliseconds)
			data.interruptRead()
		}
		val readA = data.read(ByteArray(10), 0, 10)
		assertThat(readA).isZero()

		GlobalScope.launch(Dispatchers.Default) {
			delay(150.milliseconds)
			data.interruptRead()
		}
		val readB = data.read(ByteArray(10), 0, 10)
		assertThat(readB).isZero()
	}

	fun readWithTimeoutReturnsZeroOnTimeout() {
		// Windows appears to be happy to return a few milliseconds early, so we just validate a
		// conservative lower bound which indicates that there was at least _some_ waiting.

		val readA: Int
		val tookA = measureTime {
			readA = data.readWithTimeout(ByteArray(10), 0, 10, 100)
		}
		assertThat(readA).isZero()
		assertThat(tookA).isGreaterThan(50.milliseconds)

		val readB: Int
		val tookB = measureTime {
			readB = data.readWithTimeout(ByteArray(10), 0, 10, 100)
		}
		assertThat(readB).isZero()
		assertThat(tookB).isGreaterThan(50.milliseconds)
	}
}
