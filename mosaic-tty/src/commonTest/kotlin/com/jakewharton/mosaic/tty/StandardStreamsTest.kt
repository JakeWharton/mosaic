package com.jakewharton.mosaic.tty

import assertk.assertFailure
import assertk.assertThat
import assertk.assertions.hasMessage
import assertk.assertions.isInstanceOf
import assertk.assertions.isZero
import kotlin.test.Test

class StandardStreamsTest {
	@Test fun interceptOnlyOnce() {
		StandardStreams.bind().use { one ->
			StandardStreams.bind().use { two ->
				one.interceptOtherWrites().use {
					assertFailure { two.interceptOtherWrites() }
						.isInstanceOf<IllegalStateException>()
						.hasMessage("Standard streams already intercepted")
				}
			}
		}
	}

	@Test fun interceptCloseIsIdempotent() {
		StandardStreams.bind().use { streams ->
			streams.close()
		}
	}

	@Test fun testStreamsCannotBeIntercepted() {
		TestTty.bind().use {
			val streams = it.streams
			assertFailure { streams.interceptOtherWrites() }
				.isInstanceOf<IllegalStateException>()
				.hasMessage("Cannot intercept test streams")
		}
	}

	@Test fun interceptedStdoutDoesNotSeeWrite() {
		StandardStreams.bind().use { streams ->
			streams.interceptOtherWrites().use { intercepted ->
				"hello".encodeToByteArray().writeFullyTo(streams::writeOutput)

				val read = ByteArray(10) { 'x'.code.toByte() }
				val count = intercepted.readOutputWithTimeout(read, 0, 10, 100)
				assertThat(count).isZero()
			}
		}
	}

	@Test fun interceptedStderrDoesNotSeeWrite() {
		StandardStreams.bind().use { streams ->
			streams.interceptOtherWrites().use { intercepted ->
				"hello".encodeToByteArray().writeFullyTo(streams::writeError)

				val read = ByteArray(10) { 'x'.code.toByte() }
				val count = intercepted.readErrorWithTimeout(read, 0, 10, 100)
				assertThat(count).isZero()
			}
		}
	}
}
