package com.jakewharton.mosaic

import assertk.assertThat
import assertk.assertions.isEqualTo
import com.jakewharton.mosaic.layout.drawBehind
import com.jakewharton.mosaic.modifier.Modifier
import com.jakewharton.mosaic.ui.Column
import com.jakewharton.mosaic.ui.Layout
import com.jakewharton.mosaic.ui.Row
import com.jakewharton.mosaic.ui.Text
import kotlin.test.Test

class LayoutTest {
	@Test fun layoutDebugInfo() {
		val node = mosaicNodes {
			Layout(
				content = {
					Text("Hi!")
					Text("Hey!")
				},
				debugInfo = { "Custom()" },
			) { _, _ ->
				layout(0, 0) {
				}
			}
		}
		val expected = """
			|Custom() x=0 y=0 w=0 h=0
			|  Text("Hi!") x=0 y=0 w=0 h=0 DrawBehind
			|  Text("Hey!") x=0 y=0 w=0 h=0 DrawBehind
		""".trimMargin()
		assertThat(node.toString()).isEqualTo(expected)
	}

	@Test fun noMeasureNoDraw() {
		val actual = renderMosaic {
			Layout({
				Text("CCC")
				Text("BB")
				Text("A")
			}) { _, _ ->
				layout(3, 1) {
				}
			}
		}
		val expected = """
			|  $s
			|
		""".trimMargin()
		assertThat(actual).isEqualTo(expected)
	}

	@Test fun noPlacementOverlaps() {
		val actual = renderMosaic {
			Layout({
				Text("CCC")
				Text("BB")
				Text("A")
			}) { measurables, constraints ->
				for (measurable in measurables) {
					measurable.measure(constraints)
				}
				layout(3, 1) {
				}
			}
		}
		val expected = """
			|ABC
			|
		""".trimMargin()
		assertThat(actual).isEqualTo(expected)
	}

	@Test fun placementWorks() {
		val actual = renderMosaic {
			Layout({
				Text("CCC")
				Text("BB")
				Text("A")
			}) { measurables, constraints ->
				val (c, b, a) = measurables.map { it.measure(constraints) }
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
			|
		""".trimMargin()
		assertThat(actual).isEqualTo(expected)
	}

	@Test fun canvasIsNotClipped() {
		val actual = renderMosaic {
			Column {
				Row {
					// Force the width of the canvas to be 6.
					Text("123456")
				}
				Row {
					Text("..")
					Layout(
						modifiers = Modifier.drawBehind {
							repeat(4) { row ->
								drawText(row, 0, "XXXX")
							}
						},
					) {
						layout(2, 2)
					}
					Text(".")
				}
				Row {
					Text("...")
				}
				Row {
					Text(".....")
				}
			}
		}
		val expected = """
			|123456
			|..XX.X
			|  XXXX
			|...XXX
			|.....X
			|
		""".trimMargin()
		assertThat(actual).isEqualTo(expected)
	}
}
