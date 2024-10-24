package com.jakewharton.mosaic

import assertk.assertThat
import assertk.assertions.isEqualTo
import com.jakewharton.mosaic.layout.background
import com.jakewharton.mosaic.modifier.Modifier
import com.jakewharton.mosaic.ui.Alignment
import com.jakewharton.mosaic.ui.AnsiLevel
import com.jakewharton.mosaic.ui.Color
import com.jakewharton.mosaic.ui.Column
import com.jakewharton.mosaic.ui.Row
import com.jakewharton.mosaic.ui.Static
import com.jakewharton.mosaic.ui.Text
import kotlin.test.Test
import kotlinx.coroutines.test.runTest

class AnsiRenderingTest {
	private val rendering = AnsiRendering()

	@Test fun firstRender() {
		val rootNode = renderMosaicNode {
			Column {
				Text("Hello")
				Text("World!")
			}
		}

		// TODO We should not draw trailing whitespace.
		assertThat(rendering.render(rootNode).toString()).isEqualTo(
			"""
			|Hello
			|World!
			|
			""".trimMargin().wrapWithAnsiSynchronizedUpdate().replaceLineEndingsWithCRLF(),
		)
	}

	@Test fun subsequentLongerRenderClearsRenderedLines() {
		val firstRootNode = renderMosaicNode {
			Column {
				Text("Hello")
				Text("World!")
			}
		}

		assertThat(rendering.render(firstRootNode).toString()).isEqualTo(
			"""
			|Hello
			|World!
			|
			""".trimMargin().wrapWithAnsiSynchronizedUpdate().replaceLineEndingsWithCRLF(),
		)

		val secondRootNode = renderMosaicNode {
			Column {
				Text("Hel")
				Text("lo")
				Text("Wor")
				Text("ld!")
			}
		}

		assertThat(rendering.render(secondRootNode).toString()).isEqualTo(
			"""
			|$cursorUp${cursorUp}Hel$clearLine
			|lo$clearLine
			|Wor
			|ld!
			|
			""".trimMargin().wrapWithAnsiSynchronizedUpdate().replaceLineEndingsWithCRLF(),
		)
	}

	@Test fun subsequentShorterRenderClearsRenderedLines() {
		val firstRootNode = renderMosaicNode {
			Column {
				Text("Hel")
				Text("lo")
				Text("Wor")
				Text("ld!")
			}
		}

		assertThat(rendering.render(firstRootNode).toString()).isEqualTo(
			"""
			|Hel
			|lo
			|Wor
			|ld!
			|
			""".trimMargin().wrapWithAnsiSynchronizedUpdate().replaceLineEndingsWithCRLF(),
		)

		val secondRootNode = renderMosaicNode {
			Column {
				Text("Hello")
				Text("World!")
			}
		}

		assertThat(rendering.render(secondRootNode).toString()).isEqualTo(
			"""
			|$cursorUp$cursorUp$cursorUp${cursorUp}Hello$clearLine
			|World!$clearLine
			|$clearLine
			|$clearLine$cursorUp
			""".trimMargin().wrapWithAnsiSynchronizedUpdate().replaceLineEndingsWithCRLF(),
		)
	}

	@Test fun staticRendersFirst() {
		val rootNode = renderMosaicNode {
			Text("Hello")
			Static(snapshotStateListOf("World!")) {
				Text(it)
			}
		}

		assertThat(rendering.render(rootNode).toString()).isEqualTo(
			"""
			|World!
			|Hello
			|
			""".trimMargin().wrapWithAnsiSynchronizedUpdate().replaceLineEndingsWithCRLF(),
		)
	}

	@Test fun staticLinesNotErased() = runTest {
		val firstRootNode = renderMosaicNode {
			Static(snapshotStateListOf("One")) {
				Text(it)
			}
			Text("Two")
		}

		assertThat(rendering.render(firstRootNode).toString()).isEqualTo(
			"""
			|One
			|Two
			|
			""".trimMargin().wrapWithAnsiSynchronizedUpdate().replaceLineEndingsWithCRLF(),
		)

		val secondRootNode = renderMosaicNode {
			Static(snapshotStateListOf("Three")) {
				Text(it)
			}
			Text("Four")
		}

		assertThat(rendering.render(secondRootNode).toString()).isEqualTo(
			"""
			|${cursorUp}Three$clearLine
			|Four
			|
			""".trimMargin().wrapWithAnsiSynchronizedUpdate().replaceLineEndingsWithCRLF(),
		)
	}

	@Test fun staticOrderingIsDfs() {
		val rootNode = renderMosaicNode {
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

		assertThat(rendering.render(rootNode).toString()).isEqualTo(
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

	@Test fun staticInPositionedElement() {
		val rootNode = renderMosaicNode {
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

		assertThat(rendering.render(rootNode).toString()).isEqualTo(
			"""
			|Static
			|TopTopTop
			|LeftLeft
			|
			""".trimMargin().wrapWithAnsiSynchronizedUpdate().replaceLineEndingsWithCRLF(),
		)
	}

	@Test fun withoutTrailingSpaces() {
		val rootNode = renderMosaicNode {
			Column {
				Text("OneTwoThree   ")
			}
		}

		assertThat(rendering.render(rootNode).toString()).isEqualTo(
			"""
			|OneTwoThree
			|
			""".trimMargin().wrapWithAnsiSynchronizedUpdate().replaceLineEndingsWithCRLF(),
		)
	}

	@Test fun withoutTrailingSpacesInContainer() {
		val rootNode = renderMosaicNode {
			Column {
				Text("OneTwoThree")
				Text("OneTwoThreeFour")
			}
		}

		assertThat(rendering.render(rootNode).toString()).isEqualTo(
			"""
			|OneTwoThree
			|OneTwoThreeFour
			|
			""".trimMargin().wrapWithAnsiSynchronizedUpdate().replaceLineEndingsWithCRLF(),
		)
	}

	@Test fun withoutTrailingSpacesInContainerWithAnsiNone() {
		val renderingAnsiNone = AnsiRendering(AnsiLevel.NONE)
		val rootNode = renderMosaicNode {
			Column {
				Text("OneTwoThree")
				Text("OneTwoThreeFour")
			}
		}

		assertThat(renderingAnsiNone.render(rootNode).toString()).isEqualTo(
			"""
			|OneTwoThree
			|OneTwoThreeFour
			|
			""".trimMargin().wrapWithAnsiSynchronizedUpdate().replaceLineEndingsWithCRLF(),
		)
	}

	@Test fun withColoredTrailingSpacesInContainer() {
		val rootNode = renderMosaicNode {
			Column(modifier = Modifier.background(Color.Red)) {
				Text("OneTwoThree")
				Text("OneTwoThreeFour")
			}
		}
		val redBackgroundCommand = listOf("$CSI$ansiBgColorSelector", ansiSelectorColorRgb, Color.Red.redInt, Color.Red.greenInt, Color.Red.blueInt)
			.joinToString(ansiSeparator) + ansiClosingCharacter
		assertThat(rendering.render(rootNode).toString()).isEqualTo(
			"""
			|${redBackgroundCommand}OneTwoThree    $ansiReset$ansiClosingCharacter
			|${redBackgroundCommand}OneTwoThreeFour$ansiReset$ansiClosingCharacter
			|
			""".trimMargin().wrapWithAnsiSynchronizedUpdate().replaceLineEndingsWithCRLF(),
		)
	}

	@Test fun withColoredTrailingSpacesInContainerWithAnsiNone() {
		val renderingAnsiNone = AnsiRendering(AnsiLevel.NONE)
		val rootNode = renderMosaicNode {
			Column(modifier = Modifier.background(Color.Red)) {
				Text("OneTwoThree")
				Text("OneTwoThreeFour")
			}
		}
		assertThat(renderingAnsiNone.render(rootNode).toString()).isEqualTo(
			"""
			|OneTwoThree   $s
			|OneTwoThreeFour
			|
			""".trimMargin().wrapWithAnsiSynchronizedUpdate().replaceLineEndingsWithCRLF(),
		)
	}

	@Test fun withLeadingSpaces() {
		val rootNode = renderMosaicNode {
			Column {
				Text("   OneTwoThree")
			}
		}

		assertThat(rendering.render(rootNode).toString()).isEqualTo(
			"""
			|   OneTwoThree
			|
			""".trimMargin().wrapWithAnsiSynchronizedUpdate().replaceLineEndingsWithCRLF(),
		)
	}

	@Test fun withLeadingSpacesInContainer() {
		val rootNode = renderMosaicNode {
			Column(horizontalAlignment = Alignment.End) {
				Text("OneTwoThree")
				Text("OneTwoThreeFour")
			}
		}

		assertThat(rendering.render(rootNode).toString()).isEqualTo(
			"""
			|    OneTwoThree
			|OneTwoThreeFour
			|
			""".trimMargin().wrapWithAnsiSynchronizedUpdate().replaceLineEndingsWithCRLF(),
		)
	}

	@Test fun withLeadingSpacesInContainerWithAnsiNone() {
		val renderingAnsiNone = AnsiRendering(AnsiLevel.NONE)
		val rootNode = renderMosaicNode {
			Column(horizontalAlignment = Alignment.End) {
				Text("OneTwoThree")
				Text("OneTwoThreeFour")
			}
		}

		assertThat(renderingAnsiNone.render(rootNode).toString()).isEqualTo(
			"""
			|    OneTwoThree
			|OneTwoThreeFour
			|
			""".trimMargin().wrapWithAnsiSynchronizedUpdate().replaceLineEndingsWithCRLF(),
		)
	}

	@Test fun withColoredLeadingSpacesInContainer() {
		val rootNode = renderMosaicNode {
			Column(modifier = Modifier.background(Color.Red), horizontalAlignment = Alignment.End) {
				Text("OneTwoThree")
				Text("OneTwoThreeFour")
			}
		}
		val redBackgroundCommand = listOf("$CSI$ansiBgColorSelector", ansiSelectorColorRgb, Color.Red.redInt, Color.Red.greenInt, Color.Red.blueInt)
			.joinToString(ansiSeparator) + ansiClosingCharacter
		assertThat(rendering.render(rootNode).toString()).isEqualTo(
			"""
			|$redBackgroundCommand    OneTwoThree$ansiReset$ansiClosingCharacter
			|${redBackgroundCommand}OneTwoThreeFour$ansiReset$ansiClosingCharacter
			|
			""".trimMargin().wrapWithAnsiSynchronizedUpdate().replaceLineEndingsWithCRLF(),
		)
	}

	@Test fun withColoredLeadingSpacesInContainerWithAnsiNone() {
		val renderingAnsiNone = AnsiRendering(AnsiLevel.NONE)
		val rootNode = renderMosaicNode {
			Column(modifier = Modifier.background(Color.Red), horizontalAlignment = Alignment.End) {
				Text("OneTwoThree")
				Text("OneTwoThreeFour")
			}
		}
		assertThat(renderingAnsiNone.render(rootNode).toString()).isEqualTo(
			"""
			|    OneTwoThree
			|OneTwoThreeFour
			|
			""".trimMargin().wrapWithAnsiSynchronizedUpdate().replaceLineEndingsWithCRLF(),
		)
	}
}
