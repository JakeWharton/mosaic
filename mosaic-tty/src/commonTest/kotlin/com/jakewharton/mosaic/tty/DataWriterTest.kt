package com.jakewharton.mosaic.tty

import assertk.assertThat
import assertk.assertions.isEqualTo
import de.infix.testBalloon.framework.core.testSuite

val DataWriterTests by testSuite {
	val dataWriters = listOf(
		TtyToTestTerminal,
		TestTerminalToTty,
		TestTerminalToStandardInput,
		TestTerminalToStandardInputAsTty,
		StandardOutputToTestTerminal,
		StandardOutputAsTtyToTestTerminal,
		StandardErrorToTestTerminal,
		StandardErrorAsTtyToTestTerminal,
	)
	val functions = listOf(
		DataWriterTest::writeOnlyUpToCount,
		DataWriterTest::writeAtOffset,
	)

	for (dataWriter in dataWriters) {
		testSuite(dataWriter.toString()) {
			val subject = DataWriterTest(dataWriter)
			for (function in functions) {
				test(function.name) {
					dataWriter.intercept {
						function.invoke(subject)
					}
				}
			}
		}
	}
}

private class DataWriterTest(
	private val data: DataWriter,
) {
	fun writeOnlyUpToCount() {
		val written = data.write("abcdefghij".encodeToByteArray(), 0, 5)
		assertThat(written).isEqualTo(5)

		assertThat(data.readAtMost(10).decodeToString()).isEqualTo("abcde")
	}

	fun writeAtOffset() {
		val written = data.write("abcdefghij".encodeToByteArray(), 5, 5)
		assertThat(written).isEqualTo(5)

		assertThat(data.readAtMost(5).decodeToString()).isEqualTo("fghij")
	}
}
