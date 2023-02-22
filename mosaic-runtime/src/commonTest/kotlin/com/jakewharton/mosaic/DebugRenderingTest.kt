package com.jakewharton.mosaic

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.ExperimentalTime
import kotlin.time.TestTimeSource

@OptIn(ExperimentalTime::class)
class DebugRenderingTest {
	private val timeSource = TestTimeSource()
	private val rendering = DebugRendering(timeSource)

	@Test fun framesIncludeStatics() {
		val helloCanvas = TextSurface(5, 1)
		helloCanvas.write(0, 0, "Hello")
		val staticCanvas = TextSurface(6, 1)
		staticCanvas.write(0, 0, "Static")

		assertEquals(
			"""
			|Static
			|Hello
			|""".trimMargin(),
			rendering.render(helloCanvas, listOf(staticCanvas)),
		)
	}

	@Test fun framesAfterFirstHaveTimeHeader() {
		val helloCanvas = TextSurface(5, 1)
		helloCanvas.write(0, 0, "Hello")

		assertEquals(
			"""
			|Hello
			|""".trimMargin(),
			rendering.render(helloCanvas),
		)

		timeSource += 100.milliseconds
		assertEquals(
			"""
			|~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ +100ms
			|Hello
			|""".trimMargin(),
			rendering.render(helloCanvas),
		)
	}
}
