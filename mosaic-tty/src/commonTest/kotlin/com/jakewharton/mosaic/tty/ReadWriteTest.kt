package com.jakewharton.mosaic.tty

import app.cash.burst.Burst
import app.cash.burst.InterceptTest
import app.cash.burst.TestFunction
import app.cash.burst.TestInterceptor
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isGreaterThan
import assertk.assertions.isZero
import kotlin.test.Test
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.measureTime
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Burst
@OptIn(DelicateCoroutinesApi::class) // For simple fire-and-forget parallelism.
class ReadWriteTest(
	private val readerAndWriter: ReaderAndWriter,
) {
	@InterceptTest
	private val rw = readerAndWriter.interceptor

	@Test fun readWhatWasWritten() {
		val buffer = ByteArray(10) { 'x'.code.toByte() }

		rw.write("hello")
		val readA = rw.read(buffer, 0, 10)
		assertThat(readA, "readA").isEqualTo(5)
		assertThat(buffer.decodeToString()).isEqualTo("helloxxxxx")

		rw.write("world")
		val readB = rw.read(buffer, 0, 10)
		assertThat(readB, "readB").isEqualTo(5)
		assertThat(buffer.decodeToString()).isEqualTo("worldxxxxx")
	}

	@Test fun readOnlyUpToCount() {
		val buffer = ByteArray(10) { 'x'.code.toByte() }

		rw.write("abcdefghij")
		val read = rw.read(buffer, 0, 5)
		assertThat(read).isEqualTo(5)
		assertThat(buffer.decodeToString()).isEqualTo("abcdexxxxx")

		// Drain the buffer, otherwise resetting raw mode will hang on its flush.
		rw.read(buffer, 0, 5)
	}

	@Test fun readUnderflow() {
		val buffer = ByteArray(10) { 'x'.code.toByte() }

		rw.write("hello")
		val read = rw.read(buffer, 0, 10)
		assertThat(read).isEqualTo(5)
		assertThat(buffer.decodeToString()).isEqualTo("helloxxxxx")
	}

	@Test fun readAtOffset() {
		val buffer = ByteArray(10) { 'x'.code.toByte() }

		rw.write("hello")
		val read = rw.read(buffer, 5, 5)
		assertThat(read).isEqualTo(5)
		assertThat(buffer.decodeToString()).isEqualTo("xxxxxhello")
	}

	@Test fun readCanBeInterrupted() {
		GlobalScope.launch(Dispatchers.Default) {
			delay(150.milliseconds)
			rw.interruptRead()
		}
		val readA = rw.read(ByteArray(10), 0, 10)
		assertThat(readA).isZero()

		GlobalScope.launch(Dispatchers.Default) {
			delay(150.milliseconds)
			rw.interruptRead()
		}
		val readB = rw.read(ByteArray(10), 0, 10)
		assertThat(readB).isZero()
	}

	@Test fun readWithTimeoutReturnsZeroOnTimeout() {
		if (readerAndWriter == ReaderAndWriter.TestAndTty) return // Unsupported

		// Windows appears to be happy to return a few milliseconds early, so we just validate a
		// conservative lower bound which indicates that there was at least _some_ waiting.

		val readA: Int
		val tookA = measureTime {
			readA = rw.readWithTimeout(ByteArray(10), 0, 10, 100)
		}
		assertThat(readA).isZero()
		assertThat(tookA).isGreaterThan(50.milliseconds)

		val readB: Int
		val tookB = measureTime {
			readB = rw.readWithTimeout(ByteArray(10), 0, 10, 100)
		}
		assertThat(readB).isZero()
		assertThat(tookB).isGreaterThan(50.milliseconds)
	}

	@Test fun writeOnlyUpToCount() {
		val written = rw.write("abcdefghij".encodeToByteArray(), 0, 5)
		assertThat(written).isEqualTo(5)

		val buffer = ByteArray(10) { 'x'.code.toByte() }
		val read = rw.read(buffer, 0, 10)
		assertThat(read).isEqualTo(5)
		assertThat(buffer.decodeToString()).isEqualTo("abcdexxxxx")
	}

	@Test fun writeAtOffset() {
		val written = rw.write("abcdefghij".encodeToByteArray(), 5, 5)
		assertThat(written).isEqualTo(5)

		val buffer = ByteArray(10) { 'x'.code.toByte() }
		val read = rw.read(buffer, 0, 10)
		assertThat(read).isEqualTo(5)
		assertThat(buffer.decodeToString()).isEqualTo("fghijxxxxx")
	}

	interface ReadWrite : TestInterceptor {
		fun write(message: String)
		fun write(buffer: ByteArray, offset: Int, count: Int): Int
		fun read(buffer: ByteArray, offset: Int, count: Int): Int
		fun readWithTimeout(buffer: ByteArray, offset: Int, count: Int, timeoutMillis: Int): Int
		fun interruptRead()
	}

	enum class ReaderAndWriter {
		TtyAndTest {
			override val interceptor get() = object : ReadWrite {
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
					testTty.write(message)
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
					tty.interruptRead()
				}
			}
		},
		TestAndTty {
			override val interceptor get() = object : ReadWrite {
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
					tty.write(message)
				}

				override fun write(buffer: ByteArray, offset: Int, count: Int): Int {
					return tty.write(buffer, offset, count)
				}

				override fun read(buffer: ByteArray, offset: Int, count: Int): Int {
					return testTty.read(buffer, offset, count)
				}

				override fun readWithTimeout(buffer: ByteArray, offset: Int, count: Int, timeoutMillis: Int): Int {
					throw UnsupportedOperationException()
				}

				override fun interruptRead() {
					testTty.interruptRead()
				}
			}
		},
		;

		abstract val interceptor: ReadWrite
	}
}
