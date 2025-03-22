package com.jakewharton.mosaic.tty

import assertk.assertThat
import assertk.assertions.isEqualTo
import kotlin.test.Test

class TestTtyTest {
	@Test fun canCreateMultiple() {
		if (isWindows()) return // TODO Not currently supported.

		TestTty.create().use { testOne ->
			TestTty.create().use { testTwo ->
				testOne.writeInput("hey\n")
				testTwo.writeInput("bye\n")
				assertThat(testTwo.tty.readInput(4)).isEqualTo("bye\n")
				assertThat(testOne.tty.readInput(4)).isEqualTo("hey\n")
			}
		}
	}

	@Test fun multipleResets() {
		TestTty.create().use { testTty ->
			val tty = testTty.tty
			repeat(10) {
				tty.enableRawMode()
				tty.reset()
			}
		}
	}
}
