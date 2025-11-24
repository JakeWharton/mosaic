package com.jakewharton.mosaic

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import assertk.assertThat
import assertk.assertions.isEqualTo
import com.jakewharton.mosaic.testing.TestTerminal
import com.jakewharton.mosaic.testing.runMosaicTest
import com.jakewharton.mosaic.ui.Box
import com.jakewharton.mosaic.ui.Text
import kotlin.test.Test
import kotlinx.coroutines.test.runTest

class WindowTitleTest {
	private val rendering = AnsiRendering(TestTerminal.Capabilities())

	@Test fun singleWindowTitle() = runTest {
		runMosaicTest(RenderingSnapshots(rendering)) {
			setContent {
				WindowTitle("Title")
				Text("Hello")
			}

			assertThat(awaitSnapshot()).isEqualTo(
				"""
				|${ansiWindowTitle("Title")}Hello
				|
				""".trimMargin().wrapWithAnsiSynchronizedUpdate().replaceLineEndingsWithCRLF(),
			)
		}
	}

	@Test fun emptyWindowTitle() = runTest {
		runMosaicTest(RenderingSnapshots(rendering)) {
			setContent {
				WindowTitle("")
				Text("Hello")
			}

			assertThat(awaitSnapshot()).isEqualTo(
				"""
				|${ansiWindowTitle("")}Hello
				|
				""".trimMargin().wrapWithAnsiSynchronizedUpdate().replaceLineEndingsWithCRLF(),
			)
		}
	}

	@Test fun nestedWindowTitle() = runTest {
		@Composable
		fun NestedTitle() {
			WindowTitle("Title 2")
		}

		runMosaicTest(RenderingSnapshots(rendering)) {
			setContent {
				WindowTitle("Title 1")
				NestedTitle()
				Text("Hello")
			}

			assertThat(awaitSnapshot()).isEqualTo(
				"""
				|${ansiWindowTitle("Title 2")}Hello
				|
				""".trimMargin().wrapWithAnsiSynchronizedUpdate().replaceLineEndingsWithCRLF(),
			)
		}
	}

	@Test fun nestedWindowTitleWithBox() = runTest {
		runMosaicTest(RenderingSnapshots(rendering)) {
			setContent {
				WindowTitle("Title 1")
				Box {
					WindowTitle("Title 2")
					Text("Hello")
				}
			}

			assertThat(awaitSnapshot()).isEqualTo(
				"""
				|${ansiWindowTitle("Title 2")}Hello
				|
				""".trimMargin().wrapWithAnsiSynchronizedUpdate().replaceLineEndingsWithCRLF(),
			)
		}
	}

	@Test fun siblingWindowTitles() = runTest {
		runMosaicTest(RenderingSnapshots(rendering)) {
			setContent {
				WindowTitle("Title 1")
				WindowTitle("Title 2")
				Text("Hello")
			}

			assertThat(awaitSnapshot()).isEqualTo(
				"""
				|${ansiWindowTitle("Title 2")}Hello
				|
				""".trimMargin().wrapWithAnsiSynchronizedUpdate().replaceLineEndingsWithCRLF(),
			)
		}
	}

	@Test fun removingWindowTitleRestoresPrevious() = runTest {
		runMosaicTest(RenderingSnapshots(rendering)) {
			var showSecondTitle by mutableStateOf(true)

			setContent {
				WindowTitle("Title 1")
				if (showSecondTitle) {
					WindowTitle("Title 2")
				}
				Text("Hello")
			}

			assertThat(awaitSnapshot()).isEqualTo(
				"""
				|${ansiWindowTitle("Title 2")}Hello
				|
				""".trimMargin().wrapWithAnsiSynchronizedUpdate().replaceLineEndingsWithCRLF(),
			)

			showSecondTitle = false

			assertThat(awaitSnapshot()).isEqualTo(
				"""
				|${ansiWindowTitle("Title 1")}${cursorUp(1)}${clearLine}Hello
				|
				""".trimMargin().wrapWithAnsiSynchronizedUpdate().replaceLineEndingsWithCRLF(),
			)
		}
	}

	@Test fun interleavedWindowTitleRemoval() = runTest {
		runMosaicTest(RenderingSnapshots(rendering)) {
			var showMiddleTitle by mutableStateOf(true)

			setContent {
				WindowTitle("Title 1")
				if (showMiddleTitle) {
					WindowTitle("Title 2")
				}
				WindowTitle("Title 3")
				Text("Hello")
			}

			assertThat(awaitSnapshot()).isEqualTo(
				"""
				|${ansiWindowTitle("Title 3")}Hello
				|
				""".trimMargin().wrapWithAnsiSynchronizedUpdate().replaceLineEndingsWithCRLF(),
			)

			showMiddleTitle = false

			// Title 3 is still active, so no change in title output
			assertThat(awaitSnapshot()).isEqualTo(
				"""
				|${cursorUp(1)}${clearLine}Hello
				|
				""".trimMargin().wrapWithAnsiSynchronizedUpdate().replaceLineEndingsWithCRLF(),
			)
		}
	}

	@Test fun updatableWindowTitle() = runTest {
		runMosaicTest(RenderingSnapshots(rendering)) {
			var count by mutableIntStateOf(0)

			setContent {
				WindowTitle("Title $count")
				Text("Hello")
			}

			assertThat(awaitSnapshot()).isEqualTo(
				"""
				|${ansiWindowTitle("Title 0")}Hello
				|
				""".trimMargin().wrapWithAnsiSynchronizedUpdate().replaceLineEndingsWithCRLF(),
			)

			count++

			assertThat(awaitSnapshot()).isEqualTo(
				"""
				|${ansiWindowTitle("Title 1")}${cursorUp(1)}${clearLine}Hello
				|
				""".trimMargin().wrapWithAnsiSynchronizedUpdate().replaceLineEndingsWithCRLF(),
			)
		}
	}

	@Test fun updatableWindowTitleBeforeAnotherTitle() = runTest {
		runMosaicTest(RenderingSnapshots(rendering)) {
			var count by mutableIntStateOf(0)

			setContent {
				WindowTitle("Title 1 $count")
				WindowTitle("Title 2")
				Text("Hello")
			}

			assertThat(awaitSnapshot()).isEqualTo(
				"""
				|${ansiWindowTitle("Title 2")}Hello
				|
				""".trimMargin().wrapWithAnsiSynchronizedUpdate().replaceLineEndingsWithCRLF(),
			)

			count++

			// Title 2 is still active, so no change in title output
			assertThat(awaitSnapshot()).isEqualTo(
				"""
				|${cursorUp(1)}${clearLine}Hello
				|
				""".trimMargin().wrapWithAnsiSynchronizedUpdate().replaceLineEndingsWithCRLF(),
			)
		}
	}

	@Test fun complexWindowTitleScenario() = runTest {
		runMosaicTest(RenderingSnapshots(rendering)) {
			var baseTitleCount by mutableIntStateOf(0)
			var showNested by mutableStateOf(true)
			var showDeepest by mutableStateOf(false)

			setContent {
				WindowTitle("Base $baseTitleCount")
				if (showNested) {
					Box {
						WindowTitle("Nested")
						if (showDeepest) {
							WindowTitle("Deepest")
						}
						Text("HelloNested")
					}
				} else {
					Text("Hello")
				}
			}

			// Initial state: Base 0 -> Nested. Active: Nested
			assertThat(awaitSnapshot()).isEqualTo(
				"""
				|${ansiWindowTitle("Nested")}HelloNested
				|
				""".trimMargin().wrapWithAnsiSynchronizedUpdate().replaceLineEndingsWithCRLF(),
			)

			// Update base title: Base 1 -> Nested. Active: Nested (no change in output)
			baseTitleCount++
			assertThat(awaitSnapshot()).isEqualTo(
				"""
				|${cursorUp(1)}${clearLine}HelloNested
				|
				""".trimMargin().wrapWithAnsiSynchronizedUpdate().replaceLineEndingsWithCRLF(),
			)

			// Show deepest: Base 1 -> Nested -> Deepest. Active: Deepest
			showDeepest = true
			assertThat(awaitSnapshot()).isEqualTo(
				"""
				|${ansiWindowTitle("Deepest")}${cursorUp(1)}${clearLine}HelloNested
				|
				""".trimMargin().wrapWithAnsiSynchronizedUpdate().replaceLineEndingsWithCRLF(),
			)

			// Hide nested structure: Base 1. Active: Base 1
			showNested = false
			assertThat(awaitSnapshot()).isEqualTo(
				"""
				|${ansiWindowTitle("Base 1")}${cursorUp(1)}${clearLine}Hello
				|
				""".trimMargin().wrapWithAnsiSynchronizedUpdate().replaceLineEndingsWithCRLF(),
			)
		}
	}

	@Suppress("NOTHING_TO_INLINE")
	private inline fun ansiWindowTitle(title: String): String {
		return "${OSC}0;$title$BEL"
	}
}
