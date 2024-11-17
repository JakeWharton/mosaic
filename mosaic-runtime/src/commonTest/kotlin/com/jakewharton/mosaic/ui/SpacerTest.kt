package com.jakewharton.mosaic.ui

import assertk.assertThat
import assertk.assertions.isEqualTo
import com.jakewharton.mosaic.Container
import com.jakewharton.mosaic.DumpSnapshots
import com.jakewharton.mosaic.NodeSnapshots
import com.jakewharton.mosaic.layout.height
import com.jakewharton.mosaic.layout.size
import com.jakewharton.mosaic.layout.width
import com.jakewharton.mosaic.modifier.Modifier
import com.jakewharton.mosaic.s
import com.jakewharton.mosaic.size
import com.jakewharton.mosaic.testing.runMosaicTest
import com.jakewharton.mosaic.ui.unit.Constraints
import com.jakewharton.mosaic.ui.unit.IntSize
import kotlin.test.Test
import kotlinx.coroutines.test.runTest

class SpacerTest {
	private val bigConstraints = Constraints(maxWidth = 5000, maxHeight = 5000)

	@Test fun spacerFixed() = runTest {
		val width = 4
		val height = 6

		runMosaicTest {
			setContent {
				Spacer(Modifier.size(width = width, height = height))
			}
			assertThat(awaitSnapshot()).isEqualTo(
				"""
				|   $s
				|   $s
				|   $s
				|   $s
				|   $s
				|   $s
				""".trimMargin(),
			)
		}
	}

	@Test fun spacerFixedSize() = runTest {
		runMosaicTest(NodeSnapshots) {
			val width = 40
			val height = 71

			setContent {
				Container(constraints = bigConstraints) {
					Spacer(Modifier.size(width = width, height = height))
				}
			}

			val rootNode = awaitSnapshot()
			val spacerNode = rootNode.children[0].children[0]
			assertThat(spacerNode.size).isEqualTo(IntSize(width, height))
		}
	}

	@Test fun spacerFixedWithSmallerContainer() = runTest {
		runMosaicTest(NodeSnapshots) {
			val width = 40
			val height = 71

			val containerWidth = 5
			val containerHeight = 7

			setContent {
				Box {
					Container(
						constraints = Constraints(
							maxWidth = containerWidth,
							maxHeight = containerHeight,
						),
					) {
						Spacer(Modifier.size(width = width, height = height))
					}
				}
			}

			val rootNode = awaitSnapshot()
			val spacerNode = rootNode.children[0].children[0].children[0]
			assertThat(spacerNode.size).isEqualTo(IntSize(containerWidth, containerHeight))
		}
	}

	@Test fun spacerWidth() = runTest {
		runMosaicTest(NodeSnapshots) {
			val width = 71

			setContent {
				Container(constraints = bigConstraints) {
					Spacer(Modifier.width(width))
				}
			}

			val rootNode = awaitSnapshot()
			val spacerNode = rootNode.children[0].children[0]
			assertThat(spacerNode.size).isEqualTo(IntSize(width, 0))
		}
	}

	@Test fun spacerWidthWithSmallerContainer() = runTest {
		runMosaicTest(NodeSnapshots) {
			val width = 40

			val containerWidth = 5
			val containerHeight = 7

			setContent {
				Box {
					Container(
						constraints = Constraints(
							maxWidth = containerWidth,
							maxHeight = containerHeight,
						),
					) {
						Spacer(Modifier.width(width))
					}
				}
			}

			val rootNode = awaitSnapshot()
			val spacerNode = rootNode.children[0].children[0].children[0]
			assertThat(spacerNode.size).isEqualTo(IntSize(containerWidth, 0))
		}
	}

	@Test fun spacerHeight() = runTest {
		runMosaicTest(NodeSnapshots) {
			val height = 7

			setContent {
				Container(constraints = bigConstraints) {
					Spacer(Modifier.height(height))
				}
			}

			val rootNode = awaitSnapshot()
			val spacerNode = rootNode.children[0].children[0]
			assertThat(spacerNode.size).isEqualTo(IntSize(0, height))
		}
	}

	@Test fun spacerHeightWithSmallerContainer() = runTest {
		runMosaicTest(NodeSnapshots) {
			val height = 23

			val containerWidth = 5
			val containerHeight = 7

			setContent {
				Box {
					Container(
						constraints = Constraints(
							maxWidth = containerWidth,
							maxHeight = containerHeight,
						),
					) {
						Spacer(Modifier.height(height))
					}
				}
			}

			val rootNode = awaitSnapshot()
			val spacerNode = rootNode.children[0].children[0].children[0]
			assertThat(spacerNode.size).isEqualTo(IntSize(0, containerHeight))
		}
	}

	@Test fun spacerDebug() = runTest {
		runMosaicTest(DumpSnapshots) {
			setContent {
				Spacer()
			}
			assertThat(awaitSnapshot()).isEqualTo(
				"""
				|Spacer() x=0 y=0 w=0 h=0
				""".trimMargin(),
			)
		}
	}
}
