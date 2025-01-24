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

class StdinReaderTest {
	private val writer = PlatformInputWriter()
	private val input = writer.input

	@AfterTest fun after() {
		input.close()
		writer.close()
	}

	@Test fun readWhatWasWritten() {
		val buffer = ByteArray(100)

		writer.write("hello".encodeToByteArray())
		val readA = input.read(buffer, 0, buffer.size)
		assertThat(buffer.decodeToString(endIndex = readA)).isEqualTo("hello")

		writer.write("world".encodeToByteArray())
		val readB = input.read(buffer, 0, buffer.size)
		assertThat(buffer.decodeToString(endIndex = readB)).isEqualTo("world")
	}

	@Test fun readCanBeInterrupted() {
		GlobalScope.launch(Dispatchers.Default) {
			delay(150.milliseconds)
			input.interrupt()
		}
		val readA = input.read(ByteArray(10), 0, 10)
		assertThat(readA).isZero()

		GlobalScope.launch(Dispatchers.Default) {
			delay(150.milliseconds)
			input.interrupt()
		}
		val readB = input.read(ByteArray(10), 0, 10)
		assertThat(readB).isZero()
	}

	@Test fun readWithTimeoutReturnsZeroOnTimeout() {
		// The timeouts passed are slightly higher than those validated thanks to Windows which
		// can return _slightly_ early. Usually it's around .1ms, but we go 10ms to be sure.

		val readA: Int
		val tookA = measureTime {
			readA = input.readWithTimeout(ByteArray(10), 0, 10, 110)
		}
		assertThat(readA).isZero()
		assertThat(tookA).isGreaterThan(100.milliseconds)

		val readB: Int
		val tookB = measureTime {
			readB = input.readWithTimeout(ByteArray(10), 0, 10, 110)
		}
		assertThat(readB).isZero()
		assertThat(tookB).isGreaterThan(100.milliseconds)
	}
}
