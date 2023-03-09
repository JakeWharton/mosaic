package com.jakewharton.mosaic

import com.jakewharton.mosaic.ui.Column
import com.jakewharton.mosaic.ui.Text
import kotlin.test.Test
import kotlin.test.assertEquals

class AnsiRenderingTest {
	private val rendering = AnsiRendering()

	@Test fun firstRender() {
		val hello = mosaicNodes {
			Column {
				Text("Hello")
				Text("World!")
			}
		}

		// TODO We should not draw trailing whitespace.
		assertEquals(
			"""
			|Hello$s
			|World!
			|""".trimMargin(),
			rendering.render(hello).toString(),
		)
	}

	@Test fun subsequentLongerRenderClearsRenderedLines() {
		val first = mosaicNodes {
			Column {
				Text("Hello")
				Text("World!")
			}
		}

		assertEquals(
			"""
			|Hello$s
			|World!
			|""".trimMargin(),
			rendering.render(first).toString(),
		)

		val second = mosaicNodes {
			Column {
				Text("Hel")
				Text("lo")
				Text("Wor")
				Text("ld!")
			}
		}

		assertEquals(
			"""
			|$cursorUp${cursorUp}Hel$clearLine
			|lo $clearLine
			|Wor
			|ld!
			|""".trimMargin(),
			rendering.render(second).toString(),
		)
	}

	@Test fun subsequentShorterRenderClearsRenderedLines() {
		val first = mosaicNodes {
			Column {
				Text("Hel")
				Text("lo")
				Text("Wor")
				Text("ld!")
			}
		}

		assertEquals(
			"""
			|Hel
			|lo$s
			|Wor
			|ld!
			|""".trimMargin(),
			rendering.render(first).toString(),
		)

		val second = mosaicNodes {
			Column {
				Text("Hello")
				Text("World!")
			}
		}

		assertEquals(
			"""
			|$cursorUp$cursorUp$cursorUp${cursorUp}Hello $clearLine
			|World!$clearLine
			|$clearLine
			|$clearLine$cursorUp
			""".trimMargin(),
			rendering.render(second).toString(),
		)
	}
}
