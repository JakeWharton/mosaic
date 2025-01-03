package com.jakewharton.mosaic.ui

import assertk.assertThat
import assertk.assertions.isEqualTo
import com.jakewharton.mosaic.Container
import com.jakewharton.mosaic.DumpSnapshots
import com.jakewharton.mosaic.NodeSnapshots
import com.jakewharton.mosaic.TestChar
import com.jakewharton.mosaic.TestFiller
import com.jakewharton.mosaic.layout.height
import com.jakewharton.mosaic.layout.padding
import com.jakewharton.mosaic.layout.size
import com.jakewharton.mosaic.layout.width
import com.jakewharton.mosaic.modifier.Modifier
import com.jakewharton.mosaic.runMosaicTest
import com.jakewharton.mosaic.s
import com.jakewharton.mosaic.size
import com.jakewharton.mosaic.ui.unit.Constraints
import com.jakewharton.mosaic.ui.unit.IntSize
import kotlin.test.Test
import kotlinx.coroutines.test.runTest

class FillerTest {
	private val bigConstraints = Constraints(maxWidth = 5000, maxHeight = 5000)

	@Test fun fillerFixed() = runTest {
		val width = 4
		val height = 6
		runMosaicTest {
			setContent {
				TestFiller(Modifier.size(width = width, height = height))
			}
			assertThat(awaitSnapshot()).isEqualTo(
				"""
				|$TestChar$TestChar$TestChar$TestChar
				|$TestChar$TestChar$TestChar$TestChar
				|$TestChar$TestChar$TestChar$TestChar
				|$TestChar$TestChar$TestChar$TestChar
				|$TestChar$TestChar$TestChar$TestChar
				|$TestChar$TestChar$TestChar$TestChar
				""".trimMargin(),
			)
		}
	}

	@Test fun fillerFixedWithPadding() = runTest {
		val width = 4
		val height = 6
		runMosaicTest {
			setContent {
				TestFiller(Modifier.size(width = width, height = height).padding(1))
			}
			assertThat(awaitSnapshot()).isEqualTo(
				"""
				|   $s
				| $TestChar$TestChar$s
				| $TestChar$TestChar$s
				| $TestChar$TestChar$s
				| $TestChar$TestChar$s
				|   $s
				""".trimMargin(),
			)
		}
	}

	@Test fun fillerFixedSize() = runTest {
		runMosaicTest(NodeSnapshots) {
			val width = 40
			val height = 71

			setContent {
				Container(constraints = bigConstraints) {
					TestFiller(Modifier.size(width = width, height = height))
				}
			}

			val rootNode = awaitSnapshot()
			val fillerNode = rootNode.children[0].children[0]
			assertThat(fillerNode.size).isEqualTo(IntSize(width, height))
		}
	}

	@Test fun fillerFixedWithSmallerContainer() = runTest {
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
						TestFiller(Modifier.size(width = width, height = height))
					}
				}
			}

			val rootNode = awaitSnapshot()
			val fillerNode = rootNode.children[0].children[0].children[0]
			assertThat(fillerNode.size).isEqualTo(IntSize(containerWidth, containerHeight))
		}
	}

	@Test fun fillerWidth() = runTest {
		runMosaicTest(NodeSnapshots) {
			val width = 71

			setContent {
				Container(constraints = bigConstraints) {
					TestFiller(Modifier.width(width))
				}
			}

			val rootNode = awaitSnapshot()
			val fillerNode = rootNode.children[0].children[0]
			assertThat(fillerNode.size).isEqualTo(IntSize(width, 0))
		}
	}

	@Test fun fillerWidthWithSmallerContainer() = runTest {
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
						TestFiller(Modifier.width(width))
					}
				}
			}

			val rootNode = awaitSnapshot()
			val fillerNode = rootNode.children[0].children[0].children[0]
			assertThat(fillerNode.size).isEqualTo(IntSize(containerWidth, 0))
		}
	}

	@Test fun fillerHeight() = runTest {
		runMosaicTest(NodeSnapshots) {
			val height = 7

			setContent {
				Container(constraints = bigConstraints) {
					TestFiller(Modifier.height(height))
				}
			}

			val rootNode = awaitSnapshot()
			val fillerNode = rootNode.children[0].children[0]
			assertThat(fillerNode.size).isEqualTo(IntSize(0, height))
		}
	}

	@Test fun fillerHeightWithSmallerContainer() = runTest {
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
						TestFiller(Modifier.height(height))
					}
				}
			}

			val rootNode = awaitSnapshot()
			val fillerNode = rootNode.children[0].children[0].children[0]
			assertThat(fillerNode.size).isEqualTo(IntSize(0, containerHeight))
		}
	}

	@Test fun fillerDebug() = runTest {
		runMosaicTest(DumpSnapshots) {
			setContent {
				TestFiller()
			}
			assertThat(awaitSnapshot()).isEqualTo(
				"""
				|Filler('$TestChar') x=0 y=0 w=0 h=0 DrawBehind
				""".trimMargin(),
			)
		}
	}
}
