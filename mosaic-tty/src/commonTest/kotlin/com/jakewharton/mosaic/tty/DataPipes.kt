package com.jakewharton.mosaic.tty

import app.cash.burst.TestFunction
import app.cash.burst.TestInterceptor
import assertk.assertThat
import assertk.assertions.isEqualTo
import com.jakewharton.mosaic.tty.StandardStreams.InterceptedStreams

interface DataReader : TestInterceptor {
	fun writeFully(message: String)
	fun read(buffer: ByteArray, offset: Int, count: Int): Int
	fun readWithTimeout(buffer: ByteArray, offset: Int, count: Int, timeoutMillis: Int): Int
	fun interruptRead()
}

interface DataWriter : TestInterceptor {
	fun write(buffer: ByteArray, offset: Int, count: Int): Int
	fun read(buffer: ByteArray, offset: Int, count: Int): Int
	fun readAtMost(count: Int): ByteArray {
		val buffer = ByteArray(count)
		val read = read(buffer, 0, count)
		return buffer.copyOf(read)
	}
}

/** A data stream for which we're both the reader and writer. */
abstract class MosaicData :
	DataReader,
	DataWriter {
	final override fun writeFully(message: String) {
		message.encodeToByteArray().writeFullyTo(::write)
	}
}

data object TtyToTestTty : MosaicData() {
	private lateinit var testTerminal: TestTerminal
	private lateinit var tty: Tty

	override fun intercept(testFunction: TestFunction) {
		TestTerminal.bind().use { testTty ->
			this.testTerminal = testTty
			tty = testTty.tty
			tty.enableRawMode()

			// There's a race condition(?) during setup where the initial resize event can put a record
			// into the console input causing reads to be missing a single byte. In normal code this is
			// tolerable as these records can come at _any_ time. However, we have tests which assert
			// precise read counts, so do a write/read roundtrip to ensure that record is flushed out.
			"dummy".encodeToByteArray().writeFullyTo(testTty::writeTty)
			val dummy = ByteArray(5).readFully(tty::read).decodeToString()
			assertThat(dummy).isEqualTo("dummy")

			testFunction()
		}
	}

	override fun write(buffer: ByteArray, offset: Int, count: Int): Int {
		return testTerminal.writeTty(buffer, offset, count)
	}

	override fun read(buffer: ByteArray, offset: Int, count: Int): Int {
		return tty.read(buffer, offset, count)
	}

	override fun readWithTimeout(buffer: ByteArray, offset: Int, count: Int, timeoutMillis: Int): Int {
		return tty.readWithTimeout(buffer, offset, count, timeoutMillis)
	}

	override fun interruptRead() {
		return tty.interruptRead()
	}
}

data object TestTtyToTest : MosaicData() {
	private lateinit var testTerminal: TestTerminal
	private lateinit var tty: Tty

	override fun intercept(testFunction: TestFunction) {
		TestTerminal.bind().use { testTty ->
			this.testTerminal = testTty
			tty = testTty.tty
			tty.enableRawMode()

			testFunction()
		}
	}

	override fun write(buffer: ByteArray, offset: Int, count: Int): Int {
		return tty.write(buffer, offset, count)
	}

	override fun read(buffer: ByteArray, offset: Int, count: Int): Int {
		return testTerminal.readTty(buffer, offset, count)
	}

	override fun readWithTimeout(
		buffer: ByteArray,
		offset: Int,
		count: Int,
		timeoutMillis: Int,
	): Int {
		return testTerminal.readTtyWithTimeout(buffer, offset, count, timeoutMillis)
	}

	override fun interruptRead() {
		testTerminal.interruptTtyRead()
	}
}

data object TestTtyToStandardInput : MosaicData() {
	private lateinit var testTerminal: TestTerminal
	private lateinit var streams: StandardStreams

	override fun intercept(testFunction: TestFunction) {
		TestTerminal.bind().use { testTty ->
			this.testTerminal = testTty
			streams = testTty.streams

			testFunction()
		}
	}

	override fun write(buffer: ByteArray, offset: Int, count: Int): Int {
		return testTerminal.writeStandardInput(buffer, offset, count)
	}

	override fun read(buffer: ByteArray, offset: Int, count: Int): Int {
		return streams.readInput(buffer, offset, count)
	}

	override fun readWithTimeout(
		buffer: ByteArray,
		offset: Int,
		count: Int,
		timeoutMillis: Int,
	): Int {
		return streams.readInputWithTimeout(buffer, offset, count, timeoutMillis)
	}

	override fun interruptRead() {
		streams.interruptInputRead()
	}
}

data object StandardOutputToTestTty : MosaicData() {
	private lateinit var testTerminal: TestTerminal
	private lateinit var streams: StandardStreams

	override fun intercept(testFunction: TestFunction) {
		TestTerminal.bind().use { testTty ->
			this.testTerminal = testTty
			streams = testTty.streams

			testFunction()
		}
	}

	override fun write(buffer: ByteArray, offset: Int, count: Int): Int {
		return streams.writeOutput(buffer, offset, count)
	}

	override fun read(buffer: ByteArray, offset: Int, count: Int): Int {
		return testTerminal.readStandardOutput(buffer, offset, count)
	}

	override fun readWithTimeout(
		buffer: ByteArray,
		offset: Int,
		count: Int,
		timeoutMillis: Int,
	): Int {
		return testTerminal.readStandardOutputWithTimeout(buffer, offset, count, timeoutMillis)
	}

	override fun interruptRead() {
		testTerminal.interruptStandardOutputRead()
	}
}

data object StandardErrorToTestTty : MosaicData() {
	private lateinit var testTerminal: TestTerminal
	private lateinit var streams: StandardStreams

	override fun intercept(testFunction: TestFunction) {
		TestTerminal.bind().use { testTty ->
			this.testTerminal = testTty
			streams = testTty.streams

			testFunction()
		}
	}

	override fun write(buffer: ByteArray, offset: Int, count: Int): Int {
		return streams.writeError(buffer, offset, count)
	}

	override fun read(buffer: ByteArray, offset: Int, count: Int): Int {
		return testTerminal.readStandardError(buffer, offset, count)
	}

	override fun readWithTimeout(
		buffer: ByteArray,
		offset: Int,
		count: Int,
		timeoutMillis: Int,
	): Int {
		return testTerminal.readStandardErrorWithTimeout(buffer, offset, count, timeoutMillis)
	}

	override fun interruptRead() {
		testTerminal.interruptStandardErrorRead()
	}
}

data object PrintlnToInterceptedStdout : DataReader {
	private lateinit var intercepted: InterceptedStreams

	override fun intercept(testFunction: TestFunction) {
		StandardStreams.bind().use { streams ->
			streams.interceptOtherWrites().use { intercepted ->
				this.intercepted = intercepted
			}
		}
	}

	override fun writeFully(message: String) {
		println(message)
	}

	override fun read(buffer: ByteArray, offset: Int, count: Int): Int {
		return intercepted.readOutput(buffer, offset, count)
	}

	override fun readWithTimeout(buffer: ByteArray, offset: Int, count: Int, timeoutMillis: Int): Int {
		return intercepted.readOutputWithTimeout(buffer, offset, count, timeoutMillis)
	}

	override fun interruptRead() {
		intercepted.interruptOutputRead()
	}
}

data object EprintlnToInterceptedStderr : DataReader {
	private lateinit var intercepted: InterceptedStreams

	override fun intercept(testFunction: TestFunction) {
		StandardStreams.bind().use { streams ->
			streams.interceptOtherWrites().use { intercepted ->
				this.intercepted = intercepted
			}
		}
	}

	override fun writeFully(message: String) {
		eprintln(message)
	}

	override fun read(buffer: ByteArray, offset: Int, count: Int): Int {
		return intercepted.readError(buffer, offset, count)
	}

	override fun readWithTimeout(buffer: ByteArray, offset: Int, count: Int, timeoutMillis: Int): Int {
		return intercepted.readErrorWithTimeout(buffer, offset, count, timeoutMillis)
	}

	override fun interruptRead() {
		intercepted.interruptErrorRead()
	}
}
