package com.jakewharton.mosaic.tty

import app.cash.burst.TestInterceptor

interface DataInterceptor : TestInterceptor {
	fun write(message: String)
	fun write(buffer: ByteArray, offset: Int, count: Int): Int
	fun read(buffer: ByteArray, offset: Int, count: Int): Int
	fun readWithTimeout(buffer: ByteArray, offset: Int, count: Int, timeoutMillis: Int): Int
	fun interruptRead()
}
