package com.jakewharton.mosaic.tty

import assertk.assertThat
import assertk.assertions.isEqualTo
import kotlin.test.Test

class TestTtyTest {
	@Test fun canCreateMultiple() {
		if (isWindows()) return // TODO Not currently supported.

		TestTty.create().use { one ->
			TestTty.create().use { two ->
				one.writeInput("hey")
				two.writeInput("bye")
				assertThat(two.tty.readInput(3)).isEqualTo("bye")
				assertThat(one.tty.readInput(3)).isEqualTo("hey")
			}
		}
	}
}
