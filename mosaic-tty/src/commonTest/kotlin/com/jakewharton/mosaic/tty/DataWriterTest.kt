package com.jakewharton.mosaic.tty

import app.cash.burst.Burst
import app.cash.burst.InterceptTest
import assertk.assertThat
import assertk.assertions.isEqualTo
import kotlin.test.Test

@Burst
class DataWriterTest(dataPipe: DataPipe) {
	@InterceptTest
	private val rw = dataPipe.createInterceptor()

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
}
