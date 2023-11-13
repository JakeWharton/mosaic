package com.jakewharton.mosaic

import com.jakewharton.mosaic.layout.drawBehind
import com.jakewharton.mosaic.modifier.Modifier
import com.jakewharton.mosaic.ui.Layout
import com.jakewharton.mosaic.ui.Row
import com.jakewharton.mosaic.ui.Static
import com.jakewharton.mosaic.ui.Text
import com.varabyte.truthish.assertThat
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
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

		val t = assertFailsWith<RuntimeException> {
			rendering.render(nodes)
		}
		assertThat(t.message!!)
			.containsMatch(
				"""
				|Failed
				|
				|NODES:
				|Row\(alignment=Vertical\(bias=-1\)\) x=0 y=0 w=11 h=1
				|  Text\("Hello "\) x=0 y=0 w=6 h=1 DrawBehind
				|  Layout\(\) x=6 y=0 w=5 h=1 DrawBehind
				|
				|OUTPUT:
				|(kotlin\.|java\.lang\.)?UnsupportedOperationException:?
				""".trimMargin(),
			)
	}

	@Test fun framesIncludeStatics() {
		val nodes = mosaicNodes {
			Text("Hello")
			Static(snapshotStateListOf("Static")) {
				Text(it)
			}
		}

		assertEquals(
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
			rendering.render(nodes),
		)
	}

	@Test fun framesAfterFirstHaveTimeHeader() {
		val hello = mosaicNodes {
			Text("Hello")
		}

		assertEquals(
			"""
			|NODES:
			|Text("Hello") x=0 y=0 w=5 h=1 DrawBehind
			|
			|OUTPUT:
			|Hello
			|
			""".trimMargin(),
			rendering.render(hello),
		)

		timeSource += 100.milliseconds
		assertEquals(
			"""
			|~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ +100ms
			|NODES:
			|Text("Hello") x=0 y=0 w=5 h=1 DrawBehind
			|
			|OUTPUT:
			|Hello
			|
			""".trimMargin(),
			rendering.render(hello),
		)
	}
}
