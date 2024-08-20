package com.jakewharton.mosaic.ui

import assertk.assertThat
import assertk.assertions.isEqualTo
import com.jakewharton.mosaic.Container
import com.jakewharton.mosaic.TestChar
import com.jakewharton.mosaic.TestFiller
import com.jakewharton.mosaic.layout.height
import com.jakewharton.mosaic.layout.padding
import com.jakewharton.mosaic.layout.size
import com.jakewharton.mosaic.layout.width
import com.jakewharton.mosaic.modifier.Modifier
import com.jakewharton.mosaic.mosaicNodesWithMeasureAndPlace
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
			assertThat(awaitRenderSnapshot()).isEqualTo(
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
			assertThat(awaitRenderSnapshot()).isEqualTo(
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
		val width = 40
		val height = 71

		val rootNode = mosaicNodesWithMeasureAndPlace {
			Container(constraints = bigConstraints) {
				TestFiller(Modifier.size(width = width, height = height))
			}
		}

		val fillerNode = rootNode.children[0].children[0]
		assertThat(fillerNode.size).isEqualTo(IntSize(width, height))
	}

	@Test fun fillerFixedWithSmallerContainer() = runTest {
		val width = 40
		val height = 71

		val containerWidth = 5
		val containerHeight = 7

		val rootNode = mosaicNodesWithMeasureAndPlace {
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

		val fillerNode = rootNode.children[0].children[0].children[0]
		assertThat(fillerNode.size).isEqualTo(IntSize(containerWidth, containerHeight))
	}

	@Test fun fillerWidth() = runTest {
		val width = 71

		val rootNode = mosaicNodesWithMeasureAndPlace {
			Container(constraints = bigConstraints) {
				TestFiller(Modifier.width(width))
			}
		}

		val fillerNode = rootNode.children[0].children[0]
		assertThat(fillerNode.size).isEqualTo(IntSize(width, 0))
	}

	@Test fun fillerWidthWithSmallerContainer() = runTest {
		val width = 40

		val containerWidth = 5
		val containerHeight = 7

		val rootNode = mosaicNodesWithMeasureAndPlace {
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

		val fillerNode = rootNode.children[0].children[0].children[0]
		assertThat(fillerNode.size).isEqualTo(IntSize(containerWidth, 0))
	}

	@Test fun fillerHeight() = runTest {
		val height = 7

		val rootNode = mosaicNodesWithMeasureAndPlace {
			Container(constraints = bigConstraints) {
				TestFiller(Modifier.height(height))
			}
		}

		val fillerNode = rootNode.children[0].children[0]
		assertThat(fillerNode.size).isEqualTo(IntSize(0, height))
	}

	@Test fun fillerHeightWithSmallerContainer() = runTest {
		val height = 23

		val containerWidth = 5
		val containerHeight = 7

		val rootNode = mosaicNodesWithMeasureAndPlace {
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

		val fillerNode = rootNode.children[0].children[0].children[0]
		assertThat(fillerNode.size).isEqualTo(IntSize(0, containerHeight))
	}

	@Test fun fillerDebug() = runTest {
		runMosaicTest {
			setContent {
				TestFiller()
			}
			assertThat(awaitNodeSnapshot().toString()).isEqualTo(
				"""
				|Filler('$TestChar') x=0 y=0 w=0 h=0 DrawBehind
				""".trimMargin(),
			)
		}
	}
}
