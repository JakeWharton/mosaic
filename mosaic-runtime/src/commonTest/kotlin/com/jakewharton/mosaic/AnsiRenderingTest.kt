package com.jakewharton.mosaic

import assertk.assertThat
import assertk.assertions.isEqualTo
import com.jakewharton.mosaic.testing.runMosaicTest
import com.jakewharton.mosaic.ui.Column
import com.jakewharton.mosaic.ui.Row
import com.jakewharton.mosaic.ui.Static
import com.jakewharton.mosaic.ui.Text
import kotlin.test.Test
import kotlinx.coroutines.test.runTest

class AnsiRenderingTest {
	private val rendering = AnsiRendering()

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
				|$cursorUp${cursorUp}Hel$clearLine
				|lo $clearLine
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
				|$cursorUp$cursorUp$cursorUp${cursorUp}Hello $clearLine
				|World!$clearLine
				|$clearLine
				|$clearLine$cursorUp
				""".trimMargin().wrapWithAnsiSynchronizedUpdate().replaceLineEndingsWithCRLF(),
			)
		}
	}

	@Test fun staticRendersFirst() = runTest {
		runMosaicTest(RenderingSnapshots(rendering)) {
			setContent {
				Text("Hello")
				Static(snapshotStateListOf("World!")) {
					Text(it)
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
				Static(snapshotStateListOf("One")) {
					Text(it)
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
				Static(snapshotStateListOf("Three")) {
					Text(it)
				}
				Text("Four")
			}

			assertThat(awaitSnapshot()).isEqualTo(
				"""
				|${cursorUp}Three$clearLine
				|Four
				|
				""".trimMargin().wrapWithAnsiSynchronizedUpdate().replaceLineEndingsWithCRLF(),
			)
		}
	}

	@Test fun staticOrderingIsDfs() = runTest {
		runMosaicTest(RenderingSnapshots(rendering)) {
			setContent {
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
						Static(snapshotStateListOf("Static")) {
							Text(it)
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
