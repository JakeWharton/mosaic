package com.jakewharton.mosaic.tty.terminal

import assertk.assertThat
import assertk.assertions.isEqualTo
import com.jakewharton.mosaic.tty.TestTerminal
import kotlin.test.AfterTest
import kotlin.test.BeforeTest

abstract class BaseEventParserTest {
	internal val testTerminal = TestTerminal.bind()
	private val tty = testTerminal.tty
	internal val parser = EventParser(tty)

	@BeforeTest fun before() {
		tty.enableRawMode()
	}

	@AfterTest fun after() {
		testTerminal.close()
		assertThat(parser.copyBuffer().toHexString()).isEqualTo("")
	}
}

fun TestTerminal.writeHex(hex: String) {
	val buffer = hex.hexToByteArray()
	val written = writeTty(buffer, 0, buffer.size)
	assertThat(written).isEqualTo(buffer.size)
}

fun TestTerminal.write(s: String) {
	val bytes = s.encodeToByteArray()
	val written = writeTty(bytes, 0, bytes.size)
	assertThat(written).isEqualTo(bytes.size)
}
