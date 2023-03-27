package com.jakewharton.mosaic

import com.jakewharton.mosaic.ui.Column
import com.jakewharton.mosaic.ui.Row
import com.jakewharton.mosaic.ui.Static
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

	@Test fun staticRendersFirst() {
		val hello = mosaicNodes {
			Text("Hello")
			Static(snapshotStateListOf("World!")) {
				Text(it)
			}
		}

		assertEquals(
			"""
			|World!
			|Hello
			|""".trimMargin(),
			rendering.render(hello).toString(),
		)
	}

	@Test fun staticLinesNotErased() {
		val first = mosaicNodes {
			Static(snapshotStateListOf("One")) {
				Text(it)
			}
			Text("Two")
		}

		assertEquals(
			"""
			|One
			|Two
			|""".trimMargin(),
			rendering.render(first).toString(),
		)

		val second = mosaicNodes {
			Static(snapshotStateListOf("Three")) {
				Text(it)
			}
			Text("Four")
		}

		assertEquals(
			"""
			|${cursorUp}Three$clearLine
			|Four
			|""".trimMargin(),
			rendering.render(second).toString(),
		)
	}

	@Test fun staticOrderingIsDfs() {
		val hello = mosaicNodes {
			Static(snapshotStateListOf("One")) {
				Text(it)
			}
			Column {
				Static(snapshotStateListOf("Two")) {
					Text(it)
				}
				Row {
					Static(snapshotStateListOf("Three")) {
						Text(it)
					}
					Text("Sup")
				}
				Static(snapshotStateListOf("Four")) {
					Text(it)
				}
			}
			Static(snapshotStateListOf("Five")) {
				Text(it)
			}
		}

		assertEquals(
			"""
			|One
			|Two
			|Three
			|Four
			|Five
			|Sup
			|""".trimMargin(),
			rendering.render(hello).toString(),
		)
	}

	@Test fun staticInPositionedElement() {
		val hello = mosaicNodes {
			Column {
				Text("TopTopTop")
				Row {
					Text("LeftLeft")
					Static(snapshotStateListOf("Static")) {
						Text(it)
					}
				}
			}
		}

		assertEquals(
			"""
			|Static
			|TopTopTop
			|LeftLeft$s
			|""".trimMargin(),
			rendering.render(hello).toString(),
		)
	}
}
