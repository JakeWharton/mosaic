package com.jakewharton.mosaic.terminal

import kotlin.test.AfterTest

abstract class BaseTerminalParserTest {
	internal val writer = Tty.stdinWriter()
	internal val parser = TerminalParser(writer.reader)

	@AfterTest fun after() {
		writer.close()
	}

	@OptIn(ExperimentalStdlibApi::class)
	internal fun StdinWriter.writeHex(hex: String) {
		write(hex.hexToByteArray())
	}
}
