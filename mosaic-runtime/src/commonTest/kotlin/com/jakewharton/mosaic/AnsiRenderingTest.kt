package com.jakewharton.mosaic

import assertk.assertThat
import assertk.assertions.isEqualTo
import com.jakewharton.mosaic.testing.runMosaicTest
import com.jakewharton.mosaic.ui.AnsiLevel
import com.jakewharton.mosaic.ui.Column
import com.jakewharton.mosaic.ui.Row
import com.jakewharton.mosaic.ui.Static
import com.jakewharton.mosaic.ui.Text
import kotlin.test.Test
import kotlinx.coroutines.test.runTest

class AnsiRenderingTest {
	private val rendering = AnsiRendering(
		ansiLevel = AnsiLevel.TRUECOLOR,
		synchronizedRendering = true,
		supportsKittyUnderlines = false,
	)

	@Test fun firstRender() = runTest {
		runMosaicTest(RenderingSnapshots(rendering)) {
			setContent {
				Column {
					Text("Hello")
					Text("World!")
				}
			}

			// TODO We should not draw trailing whitespace.
			assertThat(awaitSnapshot()).isEqualTo(
				"""
				|Hello$s
				|World!
				|
				""".trimMargin().wrapWithAnsiSynchronizedUpdate().replaceLineEndingsWithCRLF(),
			)
		}
	}

	@Test fun subsequentLongerRenderClearsRenderedLines() = runTest {
		runMosaicTest(RenderingSnapshots(rendering)) {
			setContent {
				Column {
					Text("Hello")
					Text("World!")
				}
			}

			assertThat(awaitSnapshot()).isEqualTo(
				"""
				|Hello$s
				|World!
				|
				""".trimMargin().wrapWithAnsiSynchronizedUpdate().replaceLineEndingsWithCRLF(),
			)

			setContent {
				Column {
					Text("Hel")
					Text("lo")
					Text("Wor")
					Text("ld!")
				}
			}

			assertThat(awaitSnapshot()).isEqualTo(
				"""
				|${cursorUp(2)}${clearLine}Hel
				|${clearLine}lo$s
				|Wor
				|ld!
				|
				""".trimMargin().wrapWithAnsiSynchronizedUpdate().replaceLineEndingsWithCRLF(),
			)
		}
	}

	@Test fun subsequentShorterRenderClearsRenderedLines() = runTest {
		runMosaicTest(RenderingSnapshots(rendering)) {
			setContent {
				Column {
					Text("Hel")
					Text("lo")
					Text("Wor")
					Text("ld!")
				}
			}

			assertThat(awaitSnapshot()).isEqualTo(
				"""
				|Hel
				|lo$s
				|Wor
				|ld!
				|
				""".trimMargin().wrapWithAnsiSynchronizedUpdate().replaceLineEndingsWithCRLF(),
			)

			setContent {
				Column {
					Text("Hello")
					Text("World!")
				}
			}

			assertThat(awaitSnapshot()).isEqualTo(
				"""
				|${cursorUp(4)}${clearLine}Hello$s
				|${clearLine}World!
				|$clearDisplay
				""".trimMargin().wrapWithAnsiSynchronizedUpdate().replaceLineEndingsWithCRLF(),
			)
		}
	}

	@Test fun staticRendersFirst() = runTest {
		runMosaicTest(RenderingSnapshots(rendering)) {
			setContent {
				Text("Hello")
				Static {
					Text("World!")
				}
			}

			assertThat(awaitSnapshot()).isEqualTo(
				"""
				|World!
				|Hello
				|
				""".trimMargin().wrapWithAnsiSynchronizedUpdate().replaceLineEndingsWithCRLF(),
			)
		}
	}

	@Test fun staticLinesNotErased() = runTest {
		runMosaicTest(RenderingSnapshots(rendering)) {
			setContent {
				Static {
					Text("One")
				}
				Text("Two")
			}

			assertThat(awaitSnapshot()).isEqualTo(
				"""
				|One
				|Two
				|
				""".trimMargin().wrapWithAnsiSynchronizedUpdate().replaceLineEndingsWithCRLF(),
			)

			setContent {
				Static {
					Text("Three")
				}
				Text("Four")
			}

			assertThat(awaitSnapshot()).isEqualTo(
				"""
				|${cursorUp(1)}${clearLine}Three
				|Four
				|
				""".trimMargin().wrapWithAnsiSynchronizedUpdate().replaceLineEndingsWithCRLF(),
			)
		}
	}

	@Test fun staticOrderingIsDfs() = runTest {
		runMosaicTest(RenderingSnapshots(rendering)) {
			setContent {
				Static {
					Text("One")
				}
				Column {
					Static {
						Text("Two")
					}
					Row {
						Static {
							Text("Three")
						}
						Text("Sup")
					}
					Static {
						Text("Four")
					}
				}
				Static {
					Text("Five")
				}
			}

			assertThat(awaitSnapshot()).isEqualTo(
				"""
				|One
				|Two
				|Three
				|Four
				|Five
				|Sup
				|
				""".trimMargin().wrapWithAnsiSynchronizedUpdate().replaceLineEndingsWithCRLF(),
			)
		}
	}

	@Test fun staticInPositionedElement() = runTest {
		runMosaicTest(RenderingSnapshots(rendering)) {
			setContent {
				Column {
					Text("TopTopTop")
					Row {
						Text("LeftLeft")
						Static {
							Text("Static")
						}
					}
				}
			}

			assertThat(awaitSnapshot()).isEqualTo(
				"""
				|Static
				|TopTopTop
				|LeftLeft$s
				|
				""".trimMargin().wrapWithAnsiSynchronizedUpdate().replaceLineEndingsWithCRLF(),
			)
		}
	}
}
