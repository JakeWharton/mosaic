package com.jakewharton.mosaic

import com.jakewharton.mosaic.ui.Column
import com.jakewharton.mosaic.ui.Text
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
			|One $s
			|Two $s
			|Three
			|""".trimMargin(), actual)
	}
}
