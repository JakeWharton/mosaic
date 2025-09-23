package com.jakewharton.mosaic.tty

import app.cash.burst.TestFunction

enum class DataPipe {
	TestToTty {
		override fun createInterceptor() = object : DataInterceptor {
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
				return testTty.write(message)
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
	},
	TtyToTest {
		override fun createInterceptor() = object : DataInterceptor {
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
	},
	;

	abstract fun createInterceptor(): DataInterceptor
}
