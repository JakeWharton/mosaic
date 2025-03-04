package com.jakewharton.mosaic.terminal

import assertk.assertThat
import assertk.assertions.isEqualTo
import com.jakewharton.mosaic.tty.TestTty
import kotlin.test.AfterTest
import kotlinx.coroutines.test.runTest

abstract class BaseTerminalParserTest {
	internal val testTty = TestTty.create()
	private val tty = testTty.tty
	internal val parser = TerminalParser(tty)

	@AfterTest fun after() = runTest {
		testTty.close()
		assertThat(parser.copyBuffer().toHexString()).isEqualTo("")
	}

	internal fun TestTty.writeHex(hex: String) {
		write(hex.hexToByteArray())
	}
}
