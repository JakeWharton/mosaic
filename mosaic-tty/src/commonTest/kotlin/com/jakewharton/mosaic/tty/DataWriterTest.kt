package com.jakewharton.mosaic.tty

import app.cash.burst.Burst
import app.cash.burst.InterceptTest
import app.cash.burst.burstValues
import assertk.assertThat
import assertk.assertions.isEqualTo
import kotlin.test.Test

@Burst
class DataWriterTest(
	@InterceptTest
	private val data: DataWriter = burstValues(
		TtyToTestTty,
		TestTtyToTest,
		TestTtyToStandardInput,
		StandardOutputToTestTty,
		StandardErrorToTestTty,
	),
) {
	@Test fun writeOnlyUpToCount() {
		val written = data.write("abcdefghij".encodeToByteArray(), 0, 5)
		assertThat(written).isEqualTo(5)

		assertThat(data.readAtMost(10).decodeToString()).isEqualTo("abcde")
	}

	@Test fun writeAtOffset() {
		val written = data.write("abcdefghij".encodeToByteArray(), 5, 5)
		assertThat(written).isEqualTo(5)

		assertThat(data.readAtMost(5).decodeToString()).isEqualTo("fghij")
	}
}
