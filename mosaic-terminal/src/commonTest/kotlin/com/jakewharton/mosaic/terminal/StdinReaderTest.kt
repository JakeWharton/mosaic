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
	private val writer = Tty.stdinWriter()
	private val reader = writer.reader

	@AfterTest fun after() {
		reader.close()
		writer.close()
	}

	@Test fun readWhatWasWritten() {
		val buffer = ByteArray(100)

		writer.write("hello".encodeToByteArray())
		val readA = reader.read(buffer, 0, buffer.size)
		assertThat(buffer.decodeToString(endIndex = readA)).isEqualTo("hello")

		writer.write("world".encodeToByteArray())
		val readB = reader.read(buffer, 0, buffer.size)
		assertThat(buffer.decodeToString(endIndex = readB)).isEqualTo("world")
	}

	@Test fun readCanBeInterrupted() {
		GlobalScope.launch(Dispatchers.Default) {
			delay(150.milliseconds)
			reader.interrupt()
		}
		val readA = reader.read(ByteArray(10), 0, 10)
		assertThat(readA).isZero()

		GlobalScope.launch(Dispatchers.Default) {
			delay(150.milliseconds)
			reader.interrupt()
		}
		val readB = reader.read(ByteArray(10), 0, 10)
		assertThat(readB).isZero()
	}

	@Test fun readWithTimeoutReturnsZeroOnTimeout() {
		// The timeouts passed are slightly higher than those validated thanks to Windows which
		// can return _slightly_ early. Usually it's around .1ms, but we go 10ms to be sure.

		val readA: Int
		val tookA = measureTime {
			readA = reader.readWithTimeout(ByteArray(10), 0, 10, 110)
		}
		assertThat(readA).isZero()
		assertThat(tookA).isGreaterThan(100.milliseconds)

		val readB: Int
		val tookB = measureTime {
			readB = reader.readWithTimeout(ByteArray(10), 0, 10, 110)
		}
		assertThat(readB).isZero()
		assertThat(tookB).isGreaterThan(100.milliseconds)
	}
}
