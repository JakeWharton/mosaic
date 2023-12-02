package com.jakewharton.mosaic.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import assertk.assertThat
import assertk.assertions.isEqualTo
import com.jakewharton.mosaic.AnsiRendering
import com.jakewharton.mosaic.ConstrainedBox
import com.jakewharton.mosaic.Container
import com.jakewharton.mosaic.TestChar
import com.jakewharton.mosaic.TestFiller
import com.jakewharton.mosaic.layout.MeasurePolicy
import com.jakewharton.mosaic.layout.aspectRatio
import com.jakewharton.mosaic.layout.fillMaxSize
import com.jakewharton.mosaic.layout.padding
import com.jakewharton.mosaic.layout.requiredSize
import com.jakewharton.mosaic.layout.requiredWidthIn
import com.jakewharton.mosaic.layout.size
import com.jakewharton.mosaic.layout.width
import com.jakewharton.mosaic.modifier.Modifier
import com.jakewharton.mosaic.mosaicNodes
import com.jakewharton.mosaic.mosaicNodesWithMeasureAndPlace
import com.jakewharton.mosaic.position
import com.jakewharton.mosaic.renderMosaic
import com.jakewharton.mosaic.replaceLineEndingsWithCRLF
import com.jakewharton.mosaic.s
import com.jakewharton.mosaic.size
import com.jakewharton.mosaic.testIntrinsics
import com.jakewharton.mosaic.ui.unit.Constraints
import com.jakewharton.mosaic.ui.unit.IntOffset
import com.jakewharton.mosaic.ui.unit.IntSize
import kotlin.test.Test

class BoxTest {
	@Test fun boxWithAlignedAndPositionedChildren() {
		val size = 6

		val content = @Composable {
			Container(alignment = Alignment.TopStart) {
				Box {
					Container(
						modifier = Modifier.align(Alignment.BottomEnd),
						width = size,
						height = size,
					)
					TestFiller(modifier = Modifier.matchParentSize().padding(2))
				}
			}
		}

		val rootNode = mosaicNodesWithMeasureAndPlace(content)
		val actual = renderMosaic(content)

		val boxNode = rootNode.children[0].children[0]

		val alignedChildContainerNode = boxNode.children[0]
		val positionedChildContainerNode = boxNode.children[1]

		assertThat(alignedChildContainerNode.size).isEqualTo(IntSize(size, size))
		assertThat(alignedChildContainerNode.position).isEqualTo(IntOffset.Zero)
		assertThat(positionedChildContainerNode.size).isEqualTo(IntSize(size, size))
		assertThat(positionedChildContainerNode.position).isEqualTo(IntOffset.Zero)

		assertThat(actual).isEqualTo(
			"""
				|     $s
				|     $s
				|  $TestChar$TestChar $s
				|  $TestChar$TestChar $s
				|     $s
				|     $s
				|
			""".trimMargin().replaceLineEndingsWithCRLF(),
		)
	}

	@Test fun boxWithMultipleAlignedChildren() {
		val size = 200
		val doubleSize = size * 2

		val rootNode = mosaicNodesWithMeasureAndPlace {
			Container(alignment = Alignment.TopStart) {
				Box {
					Container(
						modifier = Modifier.align(Alignment.BottomEnd),
						width = size,
						height = size,
					)
					Container(
						modifier = Modifier.align(Alignment.BottomEnd),
						width = doubleSize,
						height = doubleSize,
					)
				}
			}
		}

		val boxNode = rootNode.children[0].children[0]

		val firstChildContainerNode = boxNode.children[0]
		val secondChildContainerNode = boxNode.children[1]

		assertThat(boxNode.size).isEqualTo(IntSize(doubleSize, doubleSize))
		assertThat(firstChildContainerNode.size).isEqualTo(IntSize(size, size))
		assertThat(firstChildContainerNode.position).isEqualTo(IntOffset(size, size))
		assertThat(secondChildContainerNode.size).isEqualTo(IntSize(doubleSize, doubleSize))
		assertThat(secondChildContainerNode.position).isEqualTo(IntOffset.Zero)
	}

	@Test fun boxWithStretchChildrenPaddingLeftTop() {
		val size = 6
		val halfSize = size / 2
		val inset = 1

		val content = @Composable {
			Container(alignment = Alignment.TopStart) {
				Box {
					Container(
						modifier = Modifier.align(Alignment.Center),
						width = size,
						height = size,
					)
					TestFiller(
						modifier = Modifier.matchParentSize().padding(left = inset, top = inset).size(halfSize),
					)
				}
			}
		}

		val rootNode = mosaicNodesWithMeasureAndPlace(content)
		val actual = renderMosaic(content)

		val boxNode = rootNode.children[0].children[0]

		val firstChildContainerNode = boxNode.children[0]
		val secondChildContainerNode = boxNode.children[1]

		assertThat(boxNode.size).isEqualTo(IntSize(size, size))
		assertThat(firstChildContainerNode.size).isEqualTo(IntSize(size, size))
		assertThat(firstChildContainerNode.position).isEqualTo(IntOffset.Zero)
		assertThat(secondChildContainerNode.size).isEqualTo(IntSize(size, size))
		assertThat(secondChildContainerNode.position).isEqualTo(IntOffset.Zero)

		assertThat(actual).isEqualTo(
			"""
			|     $s
			| $TestChar$TestChar$TestChar$TestChar$TestChar
			| $TestChar$TestChar$TestChar$TestChar$TestChar
			| $TestChar$TestChar$TestChar$TestChar$TestChar
			| $TestChar$TestChar$TestChar$TestChar$TestChar
			| $TestChar$TestChar$TestChar$TestChar$TestChar
			|
			""".trimMargin().replaceLineEndingsWithCRLF(),
		)
	}

	@Test fun boxWithStretchChildrenPaddingRightBottom() {
		val size = 6
		val halfSize = size / 2
		val inset = 1

		val content = @Composable {
			Container(alignment = Alignment.TopStart) {
				Box {
					Container(
						modifier = Modifier.align(Alignment.Center),
						width = size,
						height = size,
					)
					TestFiller(
						modifier = Modifier.matchParentSize().padding(right = inset, bottom = inset)
							.size(halfSize),
					)
				}
			}
		}

		val rootNode = mosaicNodesWithMeasureAndPlace(content)
		val actual = renderMosaic(content)

		val boxNode = rootNode.children[0].children[0]

		val firstChildContainerNode = boxNode.children[0]
		val secondChildContainerNode = boxNode.children[1]

		assertThat(boxNode.size).isEqualTo(IntSize(size, size))
		assertThat(firstChildContainerNode.size).isEqualTo(IntSize(size, size))
		assertThat(firstChildContainerNode.position).isEqualTo(IntOffset.Zero)
		assertThat(secondChildContainerNode.size).isEqualTo(IntSize(size, size))
		assertThat(secondChildContainerNode.position).isEqualTo(IntOffset.Zero)

		assertThat(actual).isEqualTo(
			"""
			|$TestChar$TestChar$TestChar$TestChar$TestChar$s
			|$TestChar$TestChar$TestChar$TestChar$TestChar$s
			|$TestChar$TestChar$TestChar$TestChar$TestChar$s
			|$TestChar$TestChar$TestChar$TestChar$TestChar$s
			|$TestChar$TestChar$TestChar$TestChar$TestChar$s
			|     $s
			|
			""".trimMargin().replaceLineEndingsWithCRLF(),
		)
	}

	@Test fun boxWithStretchChildrenPaddingLeftRight() {
		val size = 6
		val halfSize = size / 2
		val inset = 1

		val content = @Composable {
			Container(alignment = Alignment.TopStart) {
				Box {
					Container(
						modifier = Modifier.align(Alignment.Center),
						width = size,
						height = size,
					)
					TestFiller(
						modifier = Modifier.matchParentSize().padding(left = inset, right = inset)
							.size(halfSize),
					)
				}
			}
		}

		val rootNode = mosaicNodesWithMeasureAndPlace(content)
		val actual = renderMosaic(content)

		val boxNode = rootNode.children[0].children[0]

		val firstChildContainerNode = boxNode.children[0]
		val secondChildContainerNode = boxNode.children[1]

		assertThat(boxNode.size).isEqualTo(IntSize(size, size))
		assertThat(firstChildContainerNode.size).isEqualTo(IntSize(size, size))
		assertThat(firstChildContainerNode.position).isEqualTo(IntOffset.Zero)
		assertThat(secondChildContainerNode.size).isEqualTo(IntSize(size, size))
		assertThat(secondChildContainerNode.position).isEqualTo(IntOffset.Zero)

		assertThat(actual).isEqualTo(
			"""
			| $TestChar$TestChar$TestChar$TestChar$s
			| $TestChar$TestChar$TestChar$TestChar$s
			| $TestChar$TestChar$TestChar$TestChar$s
			| $TestChar$TestChar$TestChar$TestChar$s
			| $TestChar$TestChar$TestChar$TestChar$s
			| $TestChar$TestChar$TestChar$TestChar$s
			|
			""".trimMargin().replaceLineEndingsWithCRLF(),
		)
	}

	@Test fun boxWithStretchChildrenPaddingTopBottom() {
		val size = 6
		val halfSize = size / 2
		val inset = 1

		val content = @Composable {
			Container(alignment = Alignment.TopStart) {
				Box {
					Container(
						modifier = Modifier.align(Alignment.Center),
						width = size,
						height = size,
					)
					TestFiller(
						modifier = Modifier.matchParentSize().padding(top = inset, bottom = inset)
							.size(halfSize),
					)
				}
			}
		}

		val rootNode = mosaicNodesWithMeasureAndPlace(content)
		val actual = renderMosaic(content)

		val boxNode = rootNode.children[0].children[0]

		val firstChildContainerNode = boxNode.children[0]
		val secondChildContainerNode = boxNode.children[1]

		assertThat(boxNode.size).isEqualTo(IntSize(size, size))
		assertThat(firstChildContainerNode.size).isEqualTo(IntSize(size, size))
		assertThat(firstChildContainerNode.position).isEqualTo(IntOffset.Zero)
		assertThat(secondChildContainerNode.size).isEqualTo(IntSize(size, size))
		assertThat(secondChildContainerNode.position).isEqualTo(IntOffset.Zero)

		assertThat(actual).isEqualTo(
			"""
			|     $s
			|$TestChar$TestChar$TestChar$TestChar$TestChar$TestChar
			|$TestChar$TestChar$TestChar$TestChar$TestChar$TestChar
			|$TestChar$TestChar$TestChar$TestChar$TestChar$TestChar
			|$TestChar$TestChar$TestChar$TestChar$TestChar$TestChar
			|     $s
			|
			""".trimMargin().replaceLineEndingsWithCRLF(),
		)
	}

	@Test fun boxExpanded() {
		val size = 250
		val halfSize = 125

		val rootNode = mosaicNodesWithMeasureAndPlace {
			Container(alignment = Alignment.TopStart) {
				Container(modifier = Modifier.size(size)) {
					Box {
						Container(modifier = Modifier.fillMaxSize())
						Container(
							modifier = Modifier.align(Alignment.BottomEnd),
							width = halfSize,
							height = halfSize,
						)
					}
				}
			}
		}

		val outerContainerNode = rootNode.children[0].children[0]
		val boxNode = outerContainerNode.children[0]

		val firstChildContainerNode = boxNode.children[0]
		val secondChildContainerNode = boxNode.children[1]

		assertThat(outerContainerNode.size).isEqualTo(IntSize(size, size))
		assertThat(firstChildContainerNode.size).isEqualTo(IntSize(size, size))
		assertThat(firstChildContainerNode.position).isEqualTo(IntOffset.Zero)
		assertThat(secondChildContainerNode.size).isEqualTo(IntSize(halfSize, halfSize))
		assertThat(secondChildContainerNode.position).isEqualTo(
			IntOffset(
				size - halfSize,
				size - halfSize,
			),
		)
	}

	@Test fun boxAlignmentParameter() {
		val outerSize = 50
		val innerSize = 10

		val rootNode = mosaicNodesWithMeasureAndPlace {
			Box(
				contentAlignment = Alignment.BottomEnd,
				modifier = Modifier.requiredSize(outerSize),
			) {
				Box(Modifier.requiredSize(innerSize))
			}
		}

		val innerBoxNode = rootNode.children[0].children[0]

		assertThat(innerBoxNode.position)
			.isEqualTo(IntOffset(outerSize - innerSize, outerSize - innerSize))
	}

	@Test fun boxOutermostGravityWins() {
		val size = 10

		val rootNode = mosaicNodesWithMeasureAndPlace {
			Box(Modifier.requiredSize(size)) {
				Box(Modifier.align(Alignment.BottomEnd).align(Alignment.TopStart))
			}
		}

		val innerBoxNode = rootNode.children[0].children[0]

		assertThat(innerBoxNode.position).isEqualTo(IntOffset(size, size))
	}

	@Test fun boxChildAffectsBoxSize() {
		val size = mutableIntStateOf(10)
		var measure = 0
		var layout = 0

		val rootNode = mosaicNodes {
			Box {
				Layout(
					content = {
						Box {
							Box(
								Modifier.requiredSize(size.value, 10),
							)
						}
					},
					measurePolicy = remember {
						MeasurePolicy { measurables, constraints ->
							val placeable = measurables.first().measure(constraints)
							++measure
							layout(placeable.width, placeable.height) {
								placeable.place(0, 0)
								++layout
							}
						}
					},
				)
			}
		}

		val rendering = AnsiRendering()
		rendering.render(rootNode)

		assertThat(measure).isEqualTo(1)
		assertThat(layout).isEqualTo(1)

		size.value = 20
		rendering.render(rootNode)

		assertThat(measure).isEqualTo(2)
		assertThat(layout).isEqualTo(2)
	}

	@Test fun boxCanPropagateMinConstraints() {
		val rootNode = mosaicNodesWithMeasureAndPlace {
			Box(
				modifier = Modifier.requiredWidthIn(20, 40),
				propagateMinConstraints = true,
			) {
				Box(modifier = Modifier.width(10))
			}
		}

		val innerBoxNode = rootNode.children[0].children[0]

		assertThat(innerBoxNode.width).isEqualTo(20)
	}

	@Test fun boxTracksPropagateMinConstraintsChanges() {
		val pmc = mutableStateOf(true)

		val content = @Composable {
			Box(
				modifier = Modifier.requiredWidthIn(20, 40),
				propagateMinConstraints = pmc.value,
				contentAlignment = Alignment.Center,
			) {
				Box(modifier = Modifier.width(10))
			}
		}

		val firstRootNode = mosaicNodesWithMeasureAndPlace(content)
		assertThat(firstRootNode.children[0].children[0].width).isEqualTo(20)

		pmc.value = false
		val secondRootNode = mosaicNodesWithMeasureAndPlace(content)
		assertThat(secondRootNode.children[0].children[0].width).isEqualTo(10)
	}

	@Test fun boxHasCorrectIntrinsicMeasurements() {
		val testWidth = 90
		val testHeight = 80

		val testDimension = 200
		// When measuring the height with testDimension, width should be double
		val expectedWidth = testDimension * 2
		// When measuring the width with testDimension, height should be half
		val expectedHeight = testDimension / 2

		testIntrinsics(
			@Composable {
				Box {
					Container(modifier = Modifier.align(Alignment.TopStart).aspectRatio(2f))
					ConstrainedBox(
						constraints = Constraints.fixed(testWidth, testHeight),
						modifier = Modifier.align(Alignment.BottomCenter),
					)
					ConstrainedBox(
						constraints = Constraints.fixed(200, 200),
						modifier = Modifier.matchParentSize().padding(10),
					)
				}
			},
		) { minIntrinsicWidth, minIntrinsicHeight, maxIntrinsicWidth, maxIntrinsicHeight ->
			// Min width
			assertThat(minIntrinsicWidth(0)).isEqualTo(testWidth)
			assertThat(minIntrinsicWidth(testDimension)).isEqualTo(expectedWidth)
			assertThat(minIntrinsicWidth(Constraints.Infinity)).isEqualTo(testWidth)
			// Min height
			assertThat(minIntrinsicHeight(0)).isEqualTo(testHeight)
			assertThat(minIntrinsicHeight(testDimension)).isEqualTo(expectedHeight)
			assertThat(minIntrinsicHeight(Constraints.Infinity)).isEqualTo(testHeight)
			// Max width
			assertThat(maxIntrinsicWidth(0)).isEqualTo(testWidth)
			assertThat(maxIntrinsicWidth(testDimension)).isEqualTo(expectedWidth)
			assertThat(maxIntrinsicWidth(Constraints.Infinity)).isEqualTo(testWidth)
			// Max height
			assertThat(maxIntrinsicHeight(0)).isEqualTo(testHeight)
			assertThat(maxIntrinsicHeight(testDimension)).isEqualTo(expectedHeight)
			assertThat(maxIntrinsicHeight(Constraints.Infinity)).isEqualTo(testHeight)
		}
	}

	@Test fun boxHasCorrectIntrinsicMeasurementsWithNoAlignedChildren() {
		testIntrinsics(
			@Composable {
				Box {
					ConstrainedBox(
						modifier = Modifier.matchParentSize().padding(10),
						constraints = Constraints.fixed(200, 200),
					)
				}
			},
		) { minIntrinsicWidth, minIntrinsicHeight, maxIntrinsicWidth, maxIntrinsicHeight ->
			// Min width
			assertThat(minIntrinsicWidth(50)).isEqualTo(0)
			assertThat(minIntrinsicWidth(Constraints.Infinity)).isEqualTo(0)
			// Min height
			assertThat(minIntrinsicHeight(50)).isEqualTo(0)
			assertThat(minIntrinsicHeight(Constraints.Infinity)).isEqualTo(0)
			// Max width
			assertThat(maxIntrinsicWidth(50)).isEqualTo(0)
			assertThat(maxIntrinsicWidth(Constraints.Infinity)).isEqualTo(0)
			// Max height
			assertThat(maxIntrinsicHeight(50)).isEqualTo(0)
			assertThat(maxIntrinsicHeight(Constraints.Infinity)).isEqualTo(0)
		}
	}

	@Test fun boxSimpleDebug() {
		val actual = mosaicNodes {
			Box()
		}

		assertThat(actual.toString()).isEqualTo(
			"""
			|Box() x=0 y=0 w=0 h=0
			""".trimMargin(),
		)
	}

	@Test fun boxDebug() {
		val actual = mosaicNodes {
			Box(contentAlignment = Alignment.BottomCenter, propagateMinConstraints = true) {}
		}

		assertThat(actual.toString()).isEqualTo(
			"""
			|Box(alignment=Alignment(horizontalBias=0, verticalBias=1), propagateMinConstraints=true) x=0 y=0 w=0 h=0
			""".trimMargin(),
		)
	}
}
