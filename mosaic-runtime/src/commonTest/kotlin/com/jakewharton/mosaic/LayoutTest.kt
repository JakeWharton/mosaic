package com.jakewharton.mosaic

import kotlin.test.Test
import kotlin.test.assertEquals

class LayoutTest {
	@Test fun layoutDebugInfo() {
		val node = mosaicNodes {
			Layout(
				content = {
					Text("Hi!")
					Text("Hey!")
				},
				debugInfo = { "Custom()" },
			) {
				layout(0, 0) {
				}
			}
		}
		val expected = """
			|Custom() x=0 y=0 w=0 h=0
			|  Text("Hi!") x=0 y=0 w=0 h=0
			|  Text("Hey!") x=0 y=0 w=0 h=0
			""".trimMargin()
		assertEquals(expected, node.toString())
	}

	@Test fun noMeasureNoDraw() {
		val actual = renderMosaic {
			Layout({
				Text("CCC")
				Text("BB")
				Text("A")
			}) {
				layout(3, 1) {
				}
			}
		}
		val expected = """
			|  $s
			|""".trimMargin()
		assertEquals(expected, actual)
	}

	@Test fun noPlacementOverlaps() {
		val actual = renderMosaic {
			Layout({
				Text("CCC")
				Text("BB")
				Text("A")
			}) { measurables ->
				for (measurable in measurables) {
					measurable.measure()
				}
				layout(3, 1) {
				}
			}
		}
		val expected = """
			|ABC
			|""".trimMargin()
		assertEquals(expected, actual)
	}

	@Test fun placementWorks() {
		val actual = renderMosaic {
			Layout({
				Text("CCC")
				Text("BB")
				Text("A")
			}) { measurables ->
				val (c, b, a) = measurables.map(Measurable::measure)
				layout(8, 3) {
					a.place(0, 2)
					b.place(2, 1)
					c.place(5, 0)
				}
			}
		}
		val expected = """
			|     CCC
			|  BB   $s
			|A      $s
			|""".trimMargin()
		assertEquals(expected, actual)
	}
}
