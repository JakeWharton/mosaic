package com.jakewharton.mosaic.terminal

import assertk.assertThat
import assertk.assertions.isEqualTo
import com.jakewharton.mosaic.tty.TestTty
import com.jakewharton.mosaic.tty.Tty
import kotlin.test.AfterTest
import kotlinx.coroutines.test.runTest

abstract class BaseTerminalParserTest {
	internal val testTty = TestTty.create(object : Tty.Callback {
		override fun onFocus(focused: Boolean) {}
		override fun onKey() {}
		override fun onMouse() {}
		override fun onResize(columns: Int, rows: Int, width: Int, height: Int) {}
	})
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
