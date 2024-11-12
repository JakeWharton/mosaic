package com.jakewharton.mosaic.terminal

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isGreaterThan
import assertk.assertions.isZero
import kotlin.test.AfterTest
import kotlin.test.Ignore
import kotlin.test.Test
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.measureTime

class StdinReaderTest {
	private val writer = Tty.stdinWriter()
	private val reader = writer.reader

	@AfterTest fun after() {
		reader.close()
		writer.close()
	}

	@Test fun readWhatWasWritten() {
		writer.write("hello".encodeToByteArray())

		val buffer = ByteArray(100)
		val read = reader.read(buffer, 0, buffer.size)
		assertThat(buffer.decodeToString(endIndex = read)).isEqualTo("hello")
	}

	@Ignore // Pipe in the Windows StdinWriter doesn't work with StdinReader signal waiting.
	@Test fun readWithTimeoutReturnsZeroOnTimeout() {
		val read: Int
		val took = measureTime {
			read = reader.readWithTimeout(ByteArray(10), 0, 10, 100)
		}
		assertThat(read).isZero()
		assertThat(took).isGreaterThan(100.milliseconds)
	}
}
