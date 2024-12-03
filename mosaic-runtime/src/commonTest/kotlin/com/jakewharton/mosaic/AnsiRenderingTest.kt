package com.jakewharton.mosaic

import assertk.assertThat
import assertk.assertions.isEqualTo
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
			|${ansiMoveCursorToFirstColumn}Hello $ansiClearLineAfterCursor
			|World!$ansiClearLineAfterCursor$ansiClearAllAfterCursor
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
			|${ansiMoveCursorToFirstColumn}Hello $ansiClearLineAfterCursor
			|World!$ansiClearLineAfterCursor$ansiClearAllAfterCursor
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
			|${ansiMoveCursorUp(1)}${ansiMoveCursorToFirstColumn}Hel$ansiClearLineAfterCursor
			|lo $ansiClearLineAfterCursor
			|Wor$ansiClearLineAfterCursor
			|ld!$ansiClearLineAfterCursor$ansiClearAllAfterCursor
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
			|${ansiMoveCursorToFirstColumn}Hel$ansiClearLineAfterCursor
			|lo $ansiClearLineAfterCursor
			|Wor$ansiClearLineAfterCursor
			|ld!$ansiClearLineAfterCursor$ansiClearAllAfterCursor
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
			|${ansiMoveCursorUp(3)}${ansiMoveCursorToFirstColumn}Hello $ansiClearLineAfterCursor
			|World!$ansiClearLineAfterCursor$ansiClearAllAfterCursor
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
			|${ansiMoveCursorToFirstColumn}World!$ansiClearLineAfterCursor
			|Hello$ansiClearLineAfterCursor$ansiClearAllAfterCursor
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
			|${ansiMoveCursorToFirstColumn}One$ansiClearLineAfterCursor
			|Two$ansiClearLineAfterCursor$ansiClearAllAfterCursor
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
			|${ansiMoveCursorToFirstColumn}Three$ansiClearLineAfterCursor
			|Four$ansiClearLineAfterCursor$ansiClearAllAfterCursor
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
			|${ansiMoveCursorToFirstColumn}One$ansiClearLineAfterCursor
			|Two$ansiClearLineAfterCursor
			|Three$ansiClearLineAfterCursor
			|Four$ansiClearLineAfterCursor
			|Five$ansiClearLineAfterCursor
			|Sup$ansiClearLineAfterCursor$ansiClearAllAfterCursor
			""".trimMargin().wrapWithAnsiSynchronizedUpdate().replaceLineEndingsWithCRLF(),
		)
	}

	@Test fun staticInPositionedElement() {
		val firstRootNode = renderMosaicNode {
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

		assertThat(rendering.render(firstRootNode).toString()).isEqualTo(
			"""
			|${ansiMoveCursorToFirstColumn}Static$ansiClearLineAfterCursor
			|TopTopTop$ansiClearLineAfterCursor
			|LeftLeft $ansiClearLineAfterCursor$ansiClearAllAfterCursor
			""".trimMargin().wrapWithAnsiSynchronizedUpdate().replaceLineEndingsWithCRLF(),
		)
	}
}
