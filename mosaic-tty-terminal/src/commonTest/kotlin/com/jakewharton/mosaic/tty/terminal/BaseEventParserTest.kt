package com.jakewharton.mosaic.tty.terminal

import assertk.assertThat
import assertk.assertions.isEqualTo
import com.jakewharton.mosaic.tty.TestTty
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlinx.coroutines.test.runTest

abstract class BaseEventParserTest {
	internal val testTty = TestTty.create()
	private val tty = testTty.tty
	internal val parser = EventParser(tty)

	@BeforeTest fun before() {
		tty.enableRawMode()
	}

	@AfterTest fun after() = runTest {
		testTty.close()
		assertThat(parser.copyBuffer().toHexString()).isEqualTo("")
	}

	internal fun TestTty.writeHex(hex: String) {
		val buffer = hex.hexToByteArray()
		val written = writeInput(buffer, 0, buffer.size)
		assertThat(written).isEqualTo(buffer.size)
	}
}
