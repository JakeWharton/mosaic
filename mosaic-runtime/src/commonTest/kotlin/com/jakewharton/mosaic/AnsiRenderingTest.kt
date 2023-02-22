package com.jakewharton.mosaic

import kotlin.test.Test
import kotlin.test.assertEquals

class AnsiRenderingTest {
	private val rendering = AnsiRendering()

	@Test fun firstRender() {
		val helloCanvas = TextSurface(6, 2).apply {
			write(0, 0, "Hello")
			write(1, 0, "World!")
		}

		// TODO We should not clear lines that we have never drawn before.
		// TODO We should not draw trailing whitespace.
		assertEquals(
			"""
			|Hello $esc[K
			|World!$esc[K
			|""".trimMargin(),
			rendering.render(helloCanvas).toString(),
		)
	}
}
