package com.jakewharton.mosaic

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.ExperimentalTime
import kotlin.time.TestTimeSource
import kotlinx.coroutines.flow.flowOf

@OptIn(ExperimentalTime::class)
class DebugRenderingTest {
	private val timeSource = TestTimeSource()
	private val rendering = DebugRendering(timeSource)

	@Test fun framesIncludeStatics() {
		val nodes = mosaicNodes {
			Text("Hello")
			Static(flowOf("Static")) {
				Text(it)
			}
		}

		assertEquals(
			"""
			|NODES:
			|Text("Hello", x=0, y=0, width=5, height=1)
			|Static()
			|  Text("Static", x=0, y=0, width=6, height=1)
			|
			|STATIC:
			|Static
			|
			|OUTPUT:
			|Hello
			|""".trimMargin(),
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
			|Text("Hello", x=0, y=0, width=5, height=1)
			|
			|OUTPUT:
			|Hello
			|""".trimMargin(),
			rendering.render(hello),
		)

		timeSource += 100.milliseconds
		assertEquals(
			"""
			|~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ +100ms
			|NODES:
			|Text("Hello", x=0, y=0, width=5, height=1)
			|
			|OUTPUT:
			|Hello
			|""".trimMargin(),
			rendering.render(hello),
		)
	}
}
