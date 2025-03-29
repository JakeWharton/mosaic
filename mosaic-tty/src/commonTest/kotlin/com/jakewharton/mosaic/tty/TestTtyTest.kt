package com.jakewharton.mosaic.tty

import assertk.assertThat
import assertk.assertions.isEqualTo
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test

class TestTtyTest {
	private val testTty = TestTty.create()
	private val tty = testTty.tty

	@BeforeTest fun before() {
		tty.enableRawMode()
	}

	@AfterTest fun after() {
		// TestTty.close() will call Tty.close(), but we get a free idempotency test here.
		tty.close()
		testTty.close()
	}

	@Test fun canCreateMultiple() {
		if (isWindows()) return // TODO Not currently supported.

		TestTty.create().use { testTty2 ->
			testTty.write("hey\n")
			testTty2.write("bye\n")
			assertThat(testTty2.tty.read(4)).isEqualTo("bye\n")
			assertThat(testTty.tty.read(4)).isEqualTo("hey\n")
		}
	}

	@Test fun multipleRawModeResetCycles() {
		repeat(10) {
			tty.reset()
			tty.enableRawMode()
		}
	}

	@Test fun writeOnlyUpToCount() {
		val written = testTty.write("abcdefghij".encodeToByteArray(), 0, 5)
		assertThat(written).isEqualTo(5)

		val buffer = ByteArray(10) { 'x'.code.toByte() }
		val read = tty.read(buffer, 0, 10)
		assertThat(read).isEqualTo(5)
		assertThat(buffer.decodeToString()).isEqualTo("abcdexxxxx")
	}

	@Test fun writeAtOffset() {
		val written = testTty.write("abcdefghij".encodeToByteArray(), 5, 5)
		assertThat(written).isEqualTo(5)

		val buffer = ByteArray(10) { 'x'.code.toByte() }
		val read = tty.read(buffer, 0, 10)
		assertThat(read).isEqualTo(5)
		assertThat(buffer.decodeToString()).isEqualTo("fghijxxxxx")
	}
}
