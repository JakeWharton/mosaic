package com.jakewharton.mosaic.ui

import assertk.assertThat
import assertk.assertions.isEqualTo
import com.jakewharton.mosaic.Container
import com.jakewharton.mosaic.layout.height
import com.jakewharton.mosaic.layout.size
import com.jakewharton.mosaic.layout.width
import com.jakewharton.mosaic.modifier.Modifier
import com.jakewharton.mosaic.mosaicNodes
import com.jakewharton.mosaic.mosaicNodesWithMeasureAndPlace
import com.jakewharton.mosaic.renderMosaic
import com.jakewharton.mosaic.replaceLineEndingsWithCRLF
import com.jakewharton.mosaic.s
import com.jakewharton.mosaic.size
import com.jakewharton.mosaic.ui.unit.Constraints
import com.jakewharton.mosaic.ui.unit.IntSize
import com.jakewharton.mosaic.wrapWithAnsiSynchronizedUpdate
import kotlin.test.Test

class SpacerTest {
	private val bigConstraints = Constraints(maxWidth = 5000, maxHeight = 5000)

	@Test fun spacerFixed() {
		val width = 4
		val height = 6

		val actual = renderMosaic {
			Spacer(Modifier.size(width = width, height = height))
		}

		assertThat(actual).isEqualTo(
			"""
			|   $s
			|   $s
			|   $s
			|   $s
			|   $s
			|   $s
			|
			""".trimMargin().wrapWithAnsiSynchronizedUpdate().replaceLineEndingsWithCRLF(),
		)
	}

	@Test fun spacerFixedSize() {
		val width = 40
		val height = 71

		val rootNode = mosaicNodesWithMeasureAndPlace {
			Container(constraints = bigConstraints) {
				Spacer(Modifier.size(width = width, height = height))
			}
		}

		val spacerNode = rootNode.children[0].children[0]
		assertThat(spacerNode.size).isEqualTo(IntSize(width, height))
	}

	@Test fun spacerFixedWithSmallerContainer() {
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
					Spacer(Modifier.size(width = width, height = height))
				}
			}
		}

		val spacerNode = rootNode.children[0].children[0].children[0]
		assertThat(spacerNode.size).isEqualTo(IntSize(containerWidth, containerHeight))
	}

	@Test fun spacerWidth() {
		val width = 71

		val rootNode = mosaicNodesWithMeasureAndPlace {
			Container(constraints = bigConstraints) {
				Spacer(Modifier.width(width))
			}
		}

		val spacerNode = rootNode.children[0].children[0]
		assertThat(spacerNode.size).isEqualTo(IntSize(width, 0))
	}

	@Test fun spacerWidthWithSmallerContainer() {
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
					Spacer(Modifier.width(width))
				}
			}
		}

		val spacerNode = rootNode.children[0].children[0].children[0]
		assertThat(spacerNode.size).isEqualTo(IntSize(containerWidth, 0))
	}

	@Test fun spacerHeight() {
		val height = 7

		val rootNode = mosaicNodesWithMeasureAndPlace {
			Container(constraints = bigConstraints) {
				Spacer(Modifier.height(height))
			}
		}

		val spacerNode = rootNode.children[0].children[0]
		assertThat(spacerNode.size).isEqualTo(IntSize(0, height))
	}

	@Test fun spacerHeightWithSmallerContainer() {
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
					Spacer(Modifier.height(height))
				}
			}
		}

		val spacerNode = rootNode.children[0].children[0].children[0]
		assertThat(spacerNode.size).isEqualTo(IntSize(0, containerHeight))
	}

	@Test fun spacerDebug() {
		val actual = mosaicNodes {
			Spacer()
		}

		assertThat(actual.toString()).isEqualTo(
			"""
			|Spacer() x=0 y=0 w=0 h=0
			""".trimMargin(),
		)
	}
}
