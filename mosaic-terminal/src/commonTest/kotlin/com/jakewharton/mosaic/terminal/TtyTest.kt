package com.jakewharton.mosaic.terminal

import assertk.assertThat
import assertk.assertions.isGreaterThan
import assertk.assertions.isZero
import kotlin.test.Test
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.measureTime
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid
import kotlinx.io.files.Path
import kotlinx.io.files.SystemTemporaryDirectory

@OptIn(ExperimentalUuidApi::class)
class TtyTest {
	private val stdinReader = Tty.stdinReader(
		path = Path(SystemTemporaryDirectory, Uuid.random().toString()).toString(),
	)

	@Test fun readWithTimeoutReturnsZeroOnTimeout() {
		val read: Int
		val took = measureTime {
			read = stdinReader.readWithTimeout(ByteArray(10), 0, 10, 100)
		}
		assertThat(read).isZero()
		assertThat(took).isGreaterThan(100.milliseconds)
	}
}
