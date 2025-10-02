package com.jakewharton.mosaic.tty

import app.cash.burst.TestFunction
import app.cash.burst.TestInterceptor
import assertk.assertThat
import assertk.assertions.isEqualTo

interface DataReader : TestInterceptor {
	fun write(message: String)
	fun read(buffer: ByteArray, offset: Int, count: Int): Int
	fun readWithTimeout(buffer: ByteArray, offset: Int, count: Int, timeoutMillis: Int): Int
	fun interruptRead()
}

interface DataWriter : TestInterceptor {
	fun write(buffer: ByteArray, offset: Int, count: Int): Int
	fun read(buffer: ByteArray, offset: Int, count: Int): Int
	fun read(count: Int): String {
		val buffer = ByteArray(count)
		val read = read(buffer, 0, count)
		if (read == -1) throw AssertionError("Got -1")
		return buffer.decodeToString(endIndex = read)
	}
}

data object TtyToTestTty : DataReader, DataWriter {
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
			testTty.writeFully("dummy")
			assertThat(tty.readExactly(5)).isEqualTo("dummy")

			testFunction()
		}
	}

	override fun write(message: String) {
		return testTty.writeFully(message)
	}

	override fun write(buffer: ByteArray, offset: Int, count: Int): Int {
		return testTty.write(buffer, offset, count)
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

data object TestTtyToTest : DataReader, DataWriter {
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

	override fun write(message: String) {
		tty.writeFully(message)
	}

	override fun write(buffer: ByteArray, offset: Int, count: Int): Int {
		return tty.write(buffer, offset, count)
	}

	override fun read(buffer: ByteArray, offset: Int, count: Int): Int {
		return testTty.read(buffer, offset, count)
	}

	override fun readWithTimeout(
		buffer: ByteArray,
		offset: Int,
		count: Int,
		timeoutMillis: Int,
	): Int {
		return testTty.readWithTimeout(buffer, offset, count, timeoutMillis)
	}

	override fun interruptRead() {
		testTty.interruptRead()
	}
}
