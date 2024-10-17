package com.jakewharton.mosaic.terminal

import assertk.assertThat
import assertk.assertions.isGreaterThan
import assertk.assertions.isZero
import kotlin.test.Test
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.measureTime

class TtyTest {
	@Test fun readWithTimeoutReturnsZeroOnTimeout() {
		val read: Int
		val took = measureTime {
			Tty.stdinReader().use { stdinReader ->
				read = stdinReader.readWithTimeout(ByteArray(10), 0, 10, 100);
			}
		}
		assertThat(read).isZero()
		assertThat(took).isGreaterThan(100.milliseconds)
	}
}
