package com.jakewharton.mosaic

import assertk.assertThat
import assertk.assertions.isEqualTo
import com.jakewharton.mosaic.ui.Column
import com.jakewharton.mosaic.ui.Row
import com.jakewharton.mosaic.ui.Static
import com.jakewharton.mosaic.ui.Text
import kotlin.test.Test

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
		assertThat(rendering.render(hello).toString()).isEqualTo(
			"""
			|Hello$s
			|World!
			|
			""".trimMargin().replaceLineEndingsWithCRLF(),
		)
	}

	@Test fun subsequentLongerRenderClearsRenderedLines() {
		val first = mosaicNodes {
			Column {
				Text("Hello")
				Text("World!")
			}
		}

		assertThat(rendering.render(first).toString()).isEqualTo(
			"""
			|Hello$s
			|World!
			|
			""".trimMargin().replaceLineEndingsWithCRLF(),
		)

		val second = mosaicNodes {
			Column {
				Text("Hel")
				Text("lo")
				Text("Wor")
				Text("ld!")
			}
		}

		assertThat(rendering.render(second).toString()).isEqualTo(
			"""
			|$cursorUp${cursorUp}Hel$clearLine
			|lo $clearLine
			|Wor
			|ld!
			|
			""".trimMargin().replaceLineEndingsWithCRLF(),
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

		assertThat(rendering.render(first).toString()).isEqualTo(
			"""
			|Hel
			|lo$s
			|Wor
			|ld!
			|
			""".trimMargin().replaceLineEndingsWithCRLF(),
		)

		val second = mosaicNodes {
			Column {
				Text("Hello")
				Text("World!")
			}
		}

		assertThat(rendering.render(second).toString()).isEqualTo(
			"""
			|$cursorUp$cursorUp$cursorUp${cursorUp}Hello $clearLine
			|World!$clearLine
			|$clearLine
			|$clearLine$cursorUp
			""".trimMargin().replaceLineEndingsWithCRLF(),
		)
	}

	@Test fun staticRendersFirst() {
		val hello = mosaicNodes {
			Text("Hello")
			Static(snapshotStateListOf("World!")) {
				Text(it)
			}
		}

		assertThat(rendering.render(hello).toString()).isEqualTo(
			"""
			|World!
			|Hello
			|
			""".trimMargin().replaceLineEndingsWithCRLF(),
		)
	}

	@Test fun staticLinesNotErased() {
		val first = mosaicNodes {
			Static(snapshotStateListOf("One")) {
				Text(it)
			}
			Text("Two")
		}

		assertThat(rendering.render(first).toString()).isEqualTo(
			"""
			|One
			|Two
			|
			""".trimMargin().replaceLineEndingsWithCRLF(),
		)

		val second = mosaicNodes {
			Static(snapshotStateListOf("Three")) {
				Text(it)
			}
			Text("Four")
		}

		assertThat(rendering.render(second).toString()).isEqualTo(
			"""
			|${cursorUp}Three$clearLine
			|Four
			|
			""".trimMargin().replaceLineEndingsWithCRLF(),
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

		assertThat(rendering.render(hello).toString()).isEqualTo(
			"""
			|One
			|Two
			|Three
			|Four
			|Five
			|Sup
			|
			""".trimMargin().replaceLineEndingsWithCRLF(),
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

		assertThat(rendering.render(hello).toString()).isEqualTo(
			"""
			|Static
			|TopTopTop
			|LeftLeft$s
			|
			""".trimMargin().replaceLineEndingsWithCRLF(),
		)
	}
}
