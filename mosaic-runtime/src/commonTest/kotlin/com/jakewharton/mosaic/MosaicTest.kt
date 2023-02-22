package com.jakewharton.mosaic

import kotlin.test.Test
import kotlin.test.assertEquals

class MosaicTest {
	@Test fun render() {
		val actual = renderMosaic {
			Column {
				Text("One")
				Text("Two")
				Text("Three")
			}
		}
		assertEquals("""
			|One  $clearLine
			|Two  $clearLine
			|Three$clearLine
			|""".trimMargin(), actual)
	}
}
