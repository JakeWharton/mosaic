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
import com.jakewharton.mosaic.ui.Layout
import com.jakewharton.mosaic.ui.Row
import com.jakewharton.mosaic.ui.Static
import com.jakewharton.mosaic.ui.Text
import kotlin.test.Test
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.ExperimentalTime
import kotlin.time.TestTimeSource

@OptIn(ExperimentalTime::class)
class DebugRenderingTest {
	private val timeSource = TestTimeSource()
	private val rendering = DebugRendering(timeSource)

	@Test fun drawFailureStillRendersMeasuredAndPlacedNodes() {
		val nodes = mosaicNodes {
			Row {
				Text("Hello ")
				Layout(modifiers = Modifier.drawBehind { throw UnsupportedOperationException() }) {
					layout(5, 1)
				}
			}
		}

		assertFailure {
			rendering.render(nodes)
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
				|(kotlin\.|java\.lang\.)?UnsupportedOperationException:?
				""".trimMargin().toRegex(),
			)
	}

	@Test fun framesIncludeStatics() {
		val nodes = mosaicNodes {
			Text("Hello")
			Static(snapshotStateListOf("Static")) {
				Text(it)
			}
		}

		assertThat(rendering.render(nodes)).isEqualTo(
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
			""".trimMargin(),
		)
	}

	@Test fun framesAfterFirstHaveTimeHeader() {
		val hello = mosaicNodes {
			Text("Hello")
		}

		assertThat(rendering.render(hello)).isEqualTo(
			"""
			|NODES:
			|Text("Hello") x=0 y=0 w=5 h=1 DrawBehind
			|
			|OUTPUT:
			|Hello
			|
			""".trimMargin(),
		)

		timeSource += 100.milliseconds
		assertThat(rendering.render(hello)).isEqualTo(
			"""
			|~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ +100ms
			|NODES:
			|Text("Hello") x=0 y=0 w=5 h=1 DrawBehind
			|
			|OUTPUT:
			|Hello
			|
			""".trimMargin(),
		)
	}
}
