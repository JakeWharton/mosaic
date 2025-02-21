package com.jakewharton.mosaic

import assertk.assertFailure
import assertk.assertThat
import assertk.assertions.containsMatch
import assertk.assertions.isEqualTo
import assertk.assertions.isInstanceOf
import assertk.assertions.isNotNull
import assertk.assertions.message
import com.jakewharton.mosaic.layout.drawBehind
import com.jakewharton.mosaic.modifier.Modifier
import com.jakewharton.mosaic.testing.runMosaicTest
import com.jakewharton.mosaic.ui.AnsiLevel
import com.jakewharton.mosaic.ui.Layout
import com.jakewharton.mosaic.ui.Row
import com.jakewharton.mosaic.ui.Static
import com.jakewharton.mosaic.ui.Text
import kotlin.test.Test
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.TestTimeSource
import kotlinx.coroutines.test.runTest

class DebugRenderingTest {
	private val timeSource = TestTimeSource()
	private val rendering = DebugRendering(
		ansiLevel = AnsiLevel.TRUECOLOR,
		supportsKittyUnderlines = false,
		systemClock = timeSource,
	)

	@Test fun drawFailureStillRendersMeasuredAndPlacedNodes() = runTest {
		runMosaicTest(RenderingSnapshots(rendering)) {
			setContent {
				Row {
					Text("Hello ")
					Layout(modifier = Modifier.drawBehind { throw UnsupportedOperationException() }) {
						layout(5, 1)
					}
				}
			}

			assertFailure {
				awaitSnapshot()
			}.isInstanceOf<RuntimeException>()
				.message()
				.isNotNull()
				.containsMatch(
					"""
					|Failed
					|
					|NODES:
					|Row\(arrangement=Arrangement#Start, alignment=Vertical\(bias=-1\)\) x=0 y=0 w=11 h=1
					|  Text\("Hello "\) x=0 y=0 w=6 h=1 DrawBehind
					|  Layout\(\) x=6 y=0 w=5 h=1 DrawBehind
					|
					|OUTPUT:
					|(kotlin\.|java\.lang\.)?UnsupportedOperationException
					""".trimMargin().replaceLineEndingsWithCRLF().toRegex(),
				)
		}
	}

	@Test fun framesIncludeStatics() = runTest {
		runMosaicTest(RenderingSnapshots(rendering)) {
			setContent {
				Text("Hello")
				Static {
					Text("Static")
				}
			}

			assertThat(awaitSnapshot()).isEqualTo(
				"""
				|NODES:
				|Text("Hello") x=0 y=0 w=5 h=1 DrawBehind
				|Static()
				|  Text("Static") x=0 y=0 w=6 h=1 DrawBehind
				|
				|STATIC:
				|Static
				|
				|OUTPUT:
				|Hello
				|
				""".trimMargin().replaceLineEndingsWithCRLF(),
			)
		}
	}

	@Test fun framesAfterFirstHaveTimeHeader() = runTest {
		runMosaicTest(RenderingSnapshots(rendering)) {
			setContent {
				Text("Hello")
			}

			assertThat(awaitSnapshot()).isEqualTo(
				"""
				|NODES:
				|Text("Hello") x=0 y=0 w=5 h=1 DrawBehind
				|
				|OUTPUT:
				|Hello
				|
				""".trimMargin().replaceLineEndingsWithCRLF(),
			)

			timeSource += 100.milliseconds
			setContent {
				Text("Hey!")
			}

			assertThat(awaitSnapshot()).isEqualTo(
				"""
				|~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ +100ms
				|NODES:
				|Text("Hey!") x=0 y=0 w=4 h=1 DrawBehind
				|
				|OUTPUT:
				|Hey!
				|
				""".trimMargin().replaceLineEndingsWithCRLF(),
			)
		}
	}
}
