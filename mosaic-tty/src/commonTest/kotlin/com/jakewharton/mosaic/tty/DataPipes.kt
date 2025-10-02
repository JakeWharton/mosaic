package com.jakewharton.mosaic.tty

import app.cash.burst.TestFunction
import app.cash.burst.TestInterceptor
import assertk.assertThat
import assertk.assertions.isEqualTo

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
	private lateinit var testTty: TestTty
	private lateinit var tty: Tty

	override fun intercept(testFunction: TestFunction) {
		TestTty.bind().use { testTty ->
			this.testTty = testTty
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
		return testTty.writeTty(buffer, offset, count)
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
	private lateinit var testTty: TestTty
	private lateinit var tty: Tty

	override fun intercept(testFunction: TestFunction) {
		TestTty.bind().use { testTty ->
			this.testTty = testTty
			tty = testTty.tty
			tty.enableRawMode()

			testFunction()
		}
	}

	override fun write(buffer: ByteArray, offset: Int, count: Int): Int {
		return tty.write(buffer, offset, count)
	}

	override fun read(buffer: ByteArray, offset: Int, count: Int): Int {
		return testTty.readTty(buffer, offset, count)
	}

	override fun readWithTimeout(
		buffer: ByteArray,
		offset: Int,
		count: Int,
		timeoutMillis: Int,
	): Int {
		return testTty.readTtyWithTimeout(buffer, offset, count, timeoutMillis)
	}

	override fun interruptRead() {
		testTty.interruptTtyRead()
	}
}

data object TestTtyToStandardInput : MosaicData() {
	private lateinit var testTty: TestTty
	private lateinit var streams: StandardStreams

	override fun intercept(testFunction: TestFunction) {
		TestTty.bind().use { testTty ->
			this.testTty = testTty
			streams = testTty.streams

			testFunction()
		}
	}

	override fun write(buffer: ByteArray, offset: Int, count: Int): Int {
		return testTty.writeStandardInput(buffer, offset, count)
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
	private lateinit var testTty: TestTty
	private lateinit var streams: StandardStreams

	override fun intercept(testFunction: TestFunction) {
		TestTty.bind().use { testTty ->
			this.testTty = testTty
			streams = testTty.streams

			testFunction()
		}
	}

	override fun write(buffer: ByteArray, offset: Int, count: Int): Int {
		return streams.writeOutput(buffer, offset, count)
	}

	override fun read(buffer: ByteArray, offset: Int, count: Int): Int {
		return testTty.readStandardOutput(buffer, offset, count)
	}

	override fun readWithTimeout(
		buffer: ByteArray,
		offset: Int,
		count: Int,
		timeoutMillis: Int,
	): Int {
		return testTty.readStandardOutputWithTimeout(buffer, offset, count, timeoutMillis)
	}

	override fun interruptRead() {
		testTty.interruptStandardOutputRead()
	}
}

data object StandardErrorToTestTty : MosaicData() {
	private lateinit var testTty: TestTty
	private lateinit var streams: StandardStreams

	override fun intercept(testFunction: TestFunction) {
		TestTty.bind().use { testTty ->
			this.testTty = testTty
			streams = testTty.streams

			testFunction()
		}
	}

	override fun write(buffer: ByteArray, offset: Int, count: Int): Int {
		return streams.writeError(buffer, offset, count)
	}

	override fun read(buffer: ByteArray, offset: Int, count: Int): Int {
		return testTty.readStandardError(buffer, offset, count)
	}

	override fun readWithTimeout(
		buffer: ByteArray,
		offset: Int,
		count: Int,
		timeoutMillis: Int,
	): Int {
		return testTty.readStandardErrorWithTimeout(buffer, offset, count, timeoutMillis)
	}

	override fun interruptRead() {
		testTty.interruptStandardErrorRead()
	}
}
