package com.jakewharton.mosaic

import assertk.assertThat
import assertk.assertions.isEqualTo
import com.jakewharton.mosaic.layout.background
import com.jakewharton.mosaic.modifier.Modifier
import com.jakewharton.mosaic.terminal.AnsiLevel
import com.jakewharton.mosaic.testing.TestTerminal
import com.jakewharton.mosaic.testing.runMosaicTest
import com.jakewharton.mosaic.ui.Color
import com.jakewharton.mosaic.ui.Column
import com.jakewharton.mosaic.ui.Row
import com.jakewharton.mosaic.ui.Text
import kotlin.test.Test
import kotlinx.coroutines.test.runTest

class AnsiRenderingTest {
	private val rendering = AnsiRendering(TestTerminal.Capabilities())

	@Test fun firstRender() = runTest {
		runMosaicTest(RenderingSnapshots(rendering)) {
			setContent {
				Column {
					Text("Hello")
					Text("World!")
				}
			}

			assertThat(awaitSnapshot()).isEqualTo(
				"""
				|Hello
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
				|Hello
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
				|${clearLine}lo
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
				|lo
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
				|${cursorUp(4)}${clearLine}Hello
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
				StaticEffect {
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
				StaticEffect {
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
				StaticEffect {
					Text("Three")
				}
				Text("Four")
			}

			assertThat(awaitSnapshot()).isEqualTo(
				"""
				|${cursorUp(1)}${clearDisplay}Three
				|Four
				|
				""".trimMargin().wrapWithAnsiSynchronizedUpdate().replaceLineEndingsWithCRLF(),
			)
		}
	}

	@Test fun staticOrderingIsDfs() = runTest {
		runMosaicTest(RenderingSnapshots(rendering)) {
			setContent {
				StaticEffect {
					Text("One")
				}
				Column {
					StaticEffect {
						Text("Two")
					}
					Row {
						StaticEffect {
							Text("Three")
						}
						Text("Sup")
					}
					StaticEffect {
						Text("Four")
					}
				}
				StaticEffect {
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
						StaticEffect {
							Text("Static")
						}
					}
				}
			}

			assertThat(awaitSnapshot()).isEqualTo(
				"""
				|Static
				|TopTopTop
				|LeftLeft
				|
				""".trimMargin().wrapWithAnsiSynchronizedUpdate().replaceLineEndingsWithCRLF(),
			)
		}
	}

	@Test fun withoutTrailingSpaces() = runTest {
		runMosaicTest(RenderingSnapshots(rendering)) {
			val snapshot = setContentAndSnapshot {
				Text("OneTwoThree   ")
			}

			assertThat(snapshot).isEqualTo(
				"""
				|OneTwoThree
				|
				""".trimMargin().wrapWithAnsiSynchronizedUpdate().replaceLineEndingsWithCRLF(),
			)
		}
	}

	@Test fun withoutTrailingSpacesInContainer() = runTest {
		runMosaicTest(RenderingSnapshots(rendering)) {
			val snapshot = setContentAndSnapshot {
				Column {
					Text("OneTwoThree")
					Text("OneTwoThreeFour")
				}
			}

			assertThat(snapshot).isEqualTo(
				"""
				|OneTwoThree
				|OneTwoThreeFour
				|
				""".trimMargin().wrapWithAnsiSynchronizedUpdate().replaceLineEndingsWithCRLF(),
			)
		}
	}

	@Test fun withoutTrailingSpacesInContainerWithAnsiNone() = runTest {
		val rendering = AnsiRendering(
			TestTerminal.Capabilities(ansiLevel = AnsiLevel.NONE),
		)
		runMosaicTest(RenderingSnapshots(rendering)) {
			val snapshot = setContentAndSnapshot {
				Column {
					Text("OneTwoThree")
					Text("OneTwoThreeFour")
				}
			}

			assertThat(snapshot).isEqualTo(
				"""
				|OneTwoThree
				|OneTwoThreeFour
				|
				""".trimMargin().wrapWithAnsiSynchronizedUpdate().replaceLineEndingsWithCRLF(),
			)
		}
	}

	@Test fun withColoredTrailingSpacesInContainer() = runTest {
		runMosaicTest(RenderingSnapshots(rendering)) {
			val snapshot = setContentAndSnapshot {
				Column(modifier = Modifier.background(Color.Red)) {
					Text("OneTwoThree")
					Text("OneTwoThreeFour")
				}
			}

			val red = "${CSI}48;2;255;0;0m"
			assertThat(snapshot).isEqualTo(
				"""
				|${red}OneTwoThree    $ansiReset$ansiClosingCharacter
				|${red}OneTwoThreeFour$ansiReset$ansiClosingCharacter
				|
				""".trimMargin().wrapWithAnsiSynchronizedUpdate().replaceLineEndingsWithCRLF(),
			)
		}
	}
}
