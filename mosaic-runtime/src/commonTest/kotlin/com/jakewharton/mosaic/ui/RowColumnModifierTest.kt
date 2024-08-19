package com.jakewharton.mosaic.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import assertk.assertThat
import assertk.assertions.isEqualTo
import com.jakewharton.mosaic.layout.height
import com.jakewharton.mosaic.layout.size
import com.jakewharton.mosaic.layout.width
import com.jakewharton.mosaic.layout.wrapContentHeight
import com.jakewharton.mosaic.layout.wrapContentWidth
import com.jakewharton.mosaic.modifier.Modifier
import com.jakewharton.mosaic.mosaicNodesWithMeasureAndPlace
import kotlin.test.Test
import kotlinx.coroutines.test.runTest

class RowColumnModifierTest {
	@Test fun rowUpdatesOnAlignmentChange() = runTest {
		val count = 5
		var alignment by mutableStateOf(Alignment.Top)

		val content = @Composable {
			Box(Modifier.size(100)) {
				Row(Modifier.wrapContentHeight()) {
					repeat(count) { index ->
						Box(Modifier.width(20).height(if (index == count - 1) 10 else 20).align(alignment))
					}
				}
			}
		}

		mosaicNodesWithMeasureAndPlace(content).let { rootNode ->
			val fifthChildBoxNode = rootNode.children[0].children[0].children[count - 1]
			assertThat(fifthChildBoxNode.y).isEqualTo(0)
		}

		alignment = Alignment.CenterVertically

		mosaicNodesWithMeasureAndPlace(content).let { rootNode ->
			val fifthChildBoxNode = rootNode.children[0].children[0].children[count - 1]
			assertThat(fifthChildBoxNode.y).isEqualTo(5)
		}
	}

	@Test fun rowUpdatesOnWeightChange() = runTest {
		val count = 5
		var fill by mutableStateOf(false)

		val content = @Composable {
			Box(Modifier.size(200)) {
				Row(Modifier.wrapContentHeight()) {
					repeat(count) {
						Box(Modifier.size(20).weight(1f, fill))
					}
				}
			}
		}

		mosaicNodesWithMeasureAndPlace(content).let { rootNode ->
			repeat(count) { index ->
				val childBoxNode = rootNode.children[0].children[0].children[index]
				assertThat(childBoxNode.width).isEqualTo(20)
			}
		}

		fill = true

		mosaicNodesWithMeasureAndPlace(content).let { rootNode ->
			repeat(count) { index ->
				val childBoxNode = rootNode.children[0].children[0].children[index]
				assertThat(childBoxNode.width).isEqualTo(40)
			}
		}
	}

	@Test fun rowUpdatesOnWeightAndAlignmentChange() = runTest {
		val count = 5
		var fill by mutableStateOf(false)
		var alignment by mutableStateOf(Alignment.Top)

		val content = @Composable {
			Box(Modifier.size(200)) {
				Row(Modifier.wrapContentHeight()) {
					repeat(count) { index ->
						Box(
							Modifier.width(20).height(if (index == count - 1) 10 else 20)
								.weight(1f, fill)
								.align(alignment),
						)
					}
				}
			}
		}

		mosaicNodesWithMeasureAndPlace(content).let { rootNode ->
			repeat(count) { index ->
				val childBoxNode = rootNode.children[0].children[0].children[index]
				assertThat(childBoxNode.width).isEqualTo(20)
			}
			val childBoxNode = rootNode.children[0].children[0].children[count - 1]
			assertThat(childBoxNode.y).isEqualTo(0)
		}

		alignment = Alignment.CenterVertically
		fill = true

		mosaicNodesWithMeasureAndPlace(content).let { rootNode ->
			repeat(count) { index ->
				val childBoxNode = rootNode.children[0].children[0].children[index]
				assertThat(childBoxNode.width).isEqualTo(40)
			}
			val childBoxNode = rootNode.children[0].children[0].children[count - 1]
			assertThat(childBoxNode.y).isEqualTo(5)
		}
	}

	@Test fun columnUpdatesOnAlignmentChange() = runTest {
		val count = 5
		var alignment by mutableStateOf(Alignment.Start)

		val content = @Composable {
			Box(Modifier.size(100)) {
				Column(Modifier.wrapContentWidth().wrapContentHeight()) {
					repeat(count) { index ->
						Box(Modifier.height(20).width(if (index == count - 1) 10 else 20).align(alignment))
					}
				}
			}
		}

		mosaicNodesWithMeasureAndPlace(content).let { rootNode ->
			val fifthChildBoxNode = rootNode.children[0].children[0].children[count - 1]
			assertThat(fifthChildBoxNode.x).isEqualTo(0)
		}

		alignment = Alignment.CenterHorizontally

		mosaicNodesWithMeasureAndPlace(content).let { rootNode ->
			val fifthChildBoxNode = rootNode.children[0].children[0].children[count - 1]
			assertThat(fifthChildBoxNode.x).isEqualTo(5)
		}
	}

	@Test fun columnUpdatesOnWeightChange() = runTest {
		val count = 5
		var fill by mutableStateOf(false)

		val content = @Composable {
			Box(Modifier.size(200)) {
				Column(Modifier.wrapContentHeight()) {
					repeat(count) {
						Box(Modifier.size(20).weight(1f, fill))
					}
				}
			}
		}

		mosaicNodesWithMeasureAndPlace(content).let { rootNode ->
			repeat(count) { index ->
				val childBoxNode = rootNode.children[0].children[0].children[index]
				assertThat(childBoxNode.height).isEqualTo(20)
			}
		}

		fill = true

		mosaicNodesWithMeasureAndPlace(content).let { rootNode ->
			repeat(count) { index ->
				val childBoxNode = rootNode.children[0].children[0].children[index]
				assertThat(childBoxNode.height).isEqualTo(40)
			}
		}
	}

	@Test fun columnUpdatesOnWeightAndAlignmentChange() = runTest {
		val count = 5
		var fill by mutableStateOf(false)
		var alignment by mutableStateOf(Alignment.Start)

		val content = @Composable {
			Box(Modifier.size(200)) {
				Column(Modifier.wrapContentHeight()) {
					repeat(count) { index ->
						Box(
							Modifier.height(20).width(if (index == count - 1) 10 else 20).weight(1f, fill)
								.align(alignment),
						)
					}
				}
			}
		}

		mosaicNodesWithMeasureAndPlace(content).let { rootNode ->
			repeat(count) { index ->
				val childBoxNode = rootNode.children[0].children[0].children[index]
				assertThat(childBoxNode.height).isEqualTo(20)
			}
			val childBoxNode = rootNode.children[0].children[0].children[count - 1]
			assertThat(childBoxNode.x).isEqualTo(0)
		}

		alignment = Alignment.CenterHorizontally
		fill = true

		mosaicNodesWithMeasureAndPlace(content).let { rootNode ->
			repeat(count) { index ->
				val childBoxNode = rootNode.children[0].children[0].children[index]
				assertThat(childBoxNode.height).isEqualTo(40)
			}
			val childBoxNode = rootNode.children[0].children[0].children[count - 1]
			assertThat(childBoxNode.x).isEqualTo(5)
		}
	}
}
