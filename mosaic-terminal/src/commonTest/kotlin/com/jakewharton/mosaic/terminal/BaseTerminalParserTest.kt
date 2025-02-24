package com.jakewharton.mosaic.terminal

import assertk.assertThat
import assertk.assertions.isEqualTo
import com.jakewharton.mosaic.terminal.event.Event
import kotlin.test.AfterTest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.IO
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest

abstract class BaseTerminalParserTest {
	internal val testTty = TestTty()
	internal val reader = testTty.terminalReader()
	private val runLoop = GlobalScope.launch(Dispatchers.IO) {
		reader.runParseLoop()
	}

	@AfterTest fun after() = runTest {
		reader.interrupt()
		runLoop.join()
		testTty.close()
		assertThat(reader.copyBuffer().toHexString()).isEqualTo("")
	}

	internal fun TestTty.writeHex(hex: String) {
		write(hex.hexToByteArray())
	}

	internal suspend fun TerminalReader.next(): Event {
		return events.receive()
	}
}
