package com.jakewharton.mosaic.layout

import androidx.compose.runtime.Composable
import assertk.assertThat
import assertk.assertions.hasSize
import assertk.assertions.isEqualTo
import assertk.assertions.isNull
import com.jakewharton.mosaic.AtLeastSize
import com.jakewharton.mosaic.Holder
import com.jakewharton.mosaic.modifier.Modifier
import com.jakewharton.mosaic.testing.runMosaicTest
import com.jakewharton.mosaic.ui.Color
import com.jakewharton.mosaic.ui.Layout
import com.jakewharton.mosaic.ui.unit.Constraints
import kotlin.test.Test
import kotlinx.coroutines.test.runTest

class ParentDataModifierTest {
	// Test that parent data defaults to null
	@Test fun parentDataDefaultsToNull() = runTest {
		runMosaicTest {
			val parentData = Holder<Any?>(null)
			setContent {
				Layout(
					content = { SimpleDrawChild() },
					measurePolicy = { measurables, constraints ->
						assertThat(measurables).hasSize(1)
						parentData.value = measurables[0].parentData

						val placeable = measurables[0].measure(constraints)
						layout(placeable.width, placeable.height) { placeable.place(0, 0) }
					},
				)
			}
			awaitSnapshot()
			assertThat(parentData.value).isNull()
		}
	}

	// Test that parent data doesn't flow to grandchild measurables. They must be
	// reset on every Layout level
	@Test fun parentDataIsReset() = runTest {
		runMosaicTest {
			val parentData = Holder<Any?>(null)
			setContent {
				Layout(
					modifier = Modifier.layoutId("Hello"),
					content = { SimpleDrawChild() },
					measurePolicy = { measurables, constraints ->
						assertThat(measurables).hasSize(1)
						parentData.value = measurables[0].parentData

						val placeable = measurables[0].measure(constraints)
						layout(placeable.width, placeable.height) { placeable.place(0, 0) }
					},
				)
			}
			awaitSnapshot()
			assertThat(parentData.value).isNull()
		}
	}

	@Test fun multiChildLayoutTest_doesNotOverrideChildrenParentData() = runTest {
		runMosaicTest {
			setContent {
				val header =
					@Composable {
						Layout(modifier = Modifier.layoutId(0), content = {}) { _, _ ->
							layout(0, 0) {}
						}
					}
				val footer =
					@Composable {
						Layout(modifier = Modifier.layoutId(1), content = {}) { _, _ ->
							layout(0, 0) {}
						}
					}

				Layout({
					header()
					footer()
				}) { measurables, _ ->
					assertThat((measurables[0].parentData as? LayoutIdParentData)?.layoutId).isEqualTo(0)
					assertThat((measurables[1].parentData as? LayoutIdParentData)?.layoutId).isEqualTo(1)
					layout(0, 0) {}
				}
			}
			awaitSnapshot()
		}
	}

	@Test fun implementingBothParentDataAndLayoutModifier() = runTest {
		runMosaicTest {
			val parentData = "data"
			setContent {
				Layout({
					Layout(modifier = ParentDataAndLayoutModifier(parentData), content = {}) { _, _ ->
						layout(0, 0) {}
					}
				}) { measurables, _ ->
					assertThat(measurables[0].parentData).isEqualTo(parentData)
					layout(0, 0) {}
				}
			}
			awaitSnapshot()
		}
	}
}

@Composable
private fun SimpleDrawChild() {
	AtLeastSize(
		size = 10,
		modifier = Modifier.drawBehind {
			drawRect(background = Color.Magenta)
		},
	)
}

private class ParentDataAndLayoutModifier(private val data: String) :
	LayoutModifier,
	ParentDataModifier {
	override fun MeasureScope.measure(
		measurable: Measurable,
		constraints: Constraints,
	): MeasureResult {
		val placeable = measurable.measure(constraints)
		return layout(placeable.width, placeable.height) { placeable.place(0, 0) }
	}

	override fun modifyParentData(parentData: Any?) = data

	override fun toString(): String = "ParentDataAndLayoutNode($data)"
}
