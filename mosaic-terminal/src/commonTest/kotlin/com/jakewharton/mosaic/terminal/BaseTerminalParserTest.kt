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
	internal val writer = Tty.stdinWriter()
	internal val parser = writer.reader
	private val runLoop = GlobalScope.launch(Dispatchers.IO) {
		parser.runParseLoop()
	}

	@AfterTest fun after() = runTest {
		parser.interrupt()
		runLoop.join()
		writer.close()
		assertThat(parser.copyBuffer().toHexString()).isEqualTo("")
	}

	internal fun StdinWriter.writeHex(hex: String) {
		write(hex.hexToByteArray())
	}

	internal suspend fun TerminalReader.next(): Event {
		return events.receive()
	}
}
