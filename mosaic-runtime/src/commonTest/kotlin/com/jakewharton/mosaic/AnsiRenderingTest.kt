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

		// TODO We should not draw trailing whitespace.
		assertEquals(
			"""
			|Hello$s
			|World!
			|""".trimMargin(),
			rendering.render(helloCanvas).toString(),
		)
	}

	@Test fun subsequentLongerRenderClearsRenderedLines() {
		val firstCanvas = TextSurface(6, 2).apply {
			write(0, 0, "Hello")
			write(1, 0, "World!")
		}

		assertEquals(
			"""
			|Hello$s
			|World!
			|""".trimMargin(),
			rendering.render(firstCanvas).toString(),
		)

		val secondCanvas = TextSurface(3, 4).apply {
			write(0, 0, "Hel")
			write(1, 0, "lo")
			write(2, 0, "Wor")
			write(3, 0, "ld!")
		}

		assertEquals(
			"""
			|$cursorUp${cursorUp}Hel$clearLine
			|lo $clearLine
			|Wor
			|ld!
			|""".trimMargin(),
			rendering.render(secondCanvas).toString(),
		)
	}

	@Test fun subsequentShorterRenderClearsRenderedLines() {
		val firstCanvas = TextSurface(3, 4).apply {
			write(0, 0, "Hel")
			write(1, 0, "lo")
			write(2, 0, "Wor")
			write(3, 0, "ld!")
		}

		assertEquals(
			"""
			|Hel
			|lo$s
			|Wor
			|ld!
			|""".trimMargin(),
			rendering.render(firstCanvas).toString(),
		)

		val secondCanvas = TextSurface(6, 2).apply {
			write(0, 0, "Hello")
			write(1, 0, "World!")
		}

		assertEquals(
			"""
			|$cursorUp$cursorUp$cursorUp${cursorUp}Hello $clearLine
			|World!$clearLine
			|$clearLine
			|$clearLine$cursorUp
			""".trimMargin(),
			rendering.render(secondCanvas).toString(),
		)
	}
}
