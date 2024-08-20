package com.jakewharton.mosaic.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import assertk.assertThat
import assertk.assertions.isEqualTo
import com.jakewharton.mosaic.ConstrainedBox
import com.jakewharton.mosaic.Container
import com.jakewharton.mosaic.DumpSnapshots
import com.jakewharton.mosaic.NodeSnapshots
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
import com.jakewharton.mosaic.position
import com.jakewharton.mosaic.size
import com.jakewharton.mosaic.testIntrinsics
import com.jakewharton.mosaic.testing.runMosaicTest
import com.jakewharton.mosaic.ui.unit.Constraints
import com.jakewharton.mosaic.ui.unit.IntOffset
import com.jakewharton.mosaic.ui.unit.IntSize
import kotlin.test.Test
import kotlinx.coroutines.test.runTest

class BoxTest {
	@Test fun boxWithAlignedAndPositionedChildren() = runTest {
		val size = 6
		runMosaicTest(NodeSnapshots) {
			setContent {
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

			val rootNode = awaitSnapshot()

			val boxNode = rootNode.children[0].children[0]

			val alignedChildContainerNode = boxNode.children[0]
			val positionedChildContainerNode = boxNode.children[1]

			assertThat(alignedChildContainerNode.size).isEqualTo(IntSize(size, size))
			assertThat(alignedChildContainerNode.position).isEqualTo(IntOffset.Zero)
			assertThat(positionedChildContainerNode.size).isEqualTo(IntSize(size, size))
			assertThat(positionedChildContainerNode.position).isEqualTo(IntOffset.Zero)

			assertThat(rootNode.paint().render(AnsiLevel.NONE, false)).isEqualTo(
				"""
				|
				|
				|  $TestChar$TestChar
				|  $TestChar$TestChar
				|
				|
				""".trimMargin(),
			)
		}
	}

	@Test fun boxWithMultipleAlignedChildren() = runTest {
		runMosaicTest(NodeSnapshots) {
			val size = 200
			val doubleSize = size * 2

			setContent {
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

			val rootNode = awaitSnapshot()
			val boxNode = rootNode.children[0].children[0]

			val firstChildContainerNode = boxNode.children[0]
			val secondChildContainerNode = boxNode.children[1]

			assertThat(boxNode.size).isEqualTo(IntSize(doubleSize, doubleSize))
			assertThat(firstChildContainerNode.size).isEqualTo(IntSize(size, size))
			assertThat(firstChildContainerNode.position).isEqualTo(IntOffset(size, size))
			assertThat(secondChildContainerNode.size).isEqualTo(IntSize(doubleSize, doubleSize))
			assertThat(secondChildContainerNode.position).isEqualTo(IntOffset.Zero)
		}
	}

	@Test fun boxWithStretchChildrenPaddingLeftTop() = runTest {
		val size = 6
		val halfSize = size / 2
		val inset = 1
		runMosaicTest(NodeSnapshots) {
			setContent {
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

			val rootNode = awaitSnapshot()

			val boxNode = rootNode.children[0].children[0]

			val firstChildContainerNode = boxNode.children[0]
			val secondChildContainerNode = boxNode.children[1]

			assertThat(boxNode.size).isEqualTo(IntSize(size, size))
			assertThat(firstChildContainerNode.size).isEqualTo(IntSize(size, size))
			assertThat(firstChildContainerNode.position).isEqualTo(IntOffset.Zero)
			assertThat(secondChildContainerNode.size).isEqualTo(IntSize(size, size))
			assertThat(secondChildContainerNode.position).isEqualTo(IntOffset.Zero)

			assertThat(rootNode.paint().render(AnsiLevel.NONE, false)).isEqualTo(
				"""
				|
				| $TestChar$TestChar$TestChar$TestChar$TestChar
				| $TestChar$TestChar$TestChar$TestChar$TestChar
				| $TestChar$TestChar$TestChar$TestChar$TestChar
				| $TestChar$TestChar$TestChar$TestChar$TestChar
				| $TestChar$TestChar$TestChar$TestChar$TestChar
				""".trimMargin(),
			)
		}
	}

	@Test fun boxWithStretchChildrenPaddingRightBottom() = runTest {
		val size = 6
		val halfSize = size / 2
		val inset = 1
		runMosaicTest(NodeSnapshots) {
			setContent {
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

			val rootNode = awaitSnapshot()

			val boxNode = rootNode.children[0].children[0]

			val firstChildContainerNode = boxNode.children[0]
			val secondChildContainerNode = boxNode.children[1]

			assertThat(boxNode.size).isEqualTo(IntSize(size, size))
			assertThat(firstChildContainerNode.size).isEqualTo(IntSize(size, size))
			assertThat(firstChildContainerNode.position).isEqualTo(IntOffset.Zero)
			assertThat(secondChildContainerNode.size).isEqualTo(IntSize(size, size))
			assertThat(secondChildContainerNode.position).isEqualTo(IntOffset.Zero)

			assertThat(rootNode.paint().render(AnsiLevel.NONE, false)).isEqualTo(
				"""
				|$TestChar$TestChar$TestChar$TestChar$TestChar
				|$TestChar$TestChar$TestChar$TestChar$TestChar
				|$TestChar$TestChar$TestChar$TestChar$TestChar
				|$TestChar$TestChar$TestChar$TestChar$TestChar
				|$TestChar$TestChar$TestChar$TestChar$TestChar
				|
				""".trimMargin(),
			)
		}
	}

	@Test fun boxWithStretchChildrenPaddingLeftRight() = runTest {
		val size = 6
		val halfSize = size / 2
		val inset = 1
		runMosaicTest(NodeSnapshots) {
			setContent {
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

			val rootNode = awaitSnapshot()

			val boxNode = rootNode.children[0].children[0]

			val firstChildContainerNode = boxNode.children[0]
			val secondChildContainerNode = boxNode.children[1]

			assertThat(boxNode.size).isEqualTo(IntSize(size, size))
			assertThat(firstChildContainerNode.size).isEqualTo(IntSize(size, size))
			assertThat(firstChildContainerNode.position).isEqualTo(IntOffset.Zero)
			assertThat(secondChildContainerNode.size).isEqualTo(IntSize(size, size))
			assertThat(secondChildContainerNode.position).isEqualTo(IntOffset.Zero)

			assertThat(rootNode.paint().render(AnsiLevel.NONE, false)).isEqualTo(
				"""
				| $TestChar$TestChar$TestChar$TestChar
				| $TestChar$TestChar$TestChar$TestChar
				| $TestChar$TestChar$TestChar$TestChar
				| $TestChar$TestChar$TestChar$TestChar
				| $TestChar$TestChar$TestChar$TestChar
				| $TestChar$TestChar$TestChar$TestChar
				""".trimMargin(),
			)
		}
	}

	@Test fun boxWithStretchChildrenPaddingTopBottom() = runTest {
		val size = 6
		val halfSize = size / 2
		val inset = 1
		runMosaicTest(NodeSnapshots) {
			setContent {
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

			val rootNode = awaitSnapshot()

			val boxNode = rootNode.children[0].children[0]

			val firstChildContainerNode = boxNode.children[0]
			val secondChildContainerNode = boxNode.children[1]

			assertThat(boxNode.size).isEqualTo(IntSize(size, size))
			assertThat(firstChildContainerNode.size).isEqualTo(IntSize(size, size))
			assertThat(firstChildContainerNode.position).isEqualTo(IntOffset.Zero)
			assertThat(secondChildContainerNode.size).isEqualTo(IntSize(size, size))
			assertThat(secondChildContainerNode.position).isEqualTo(IntOffset.Zero)

			assertThat(rootNode.paint().render(AnsiLevel.NONE, false)).isEqualTo(
				"""
				|
				|$TestChar$TestChar$TestChar$TestChar$TestChar$TestChar
				|$TestChar$TestChar$TestChar$TestChar$TestChar$TestChar
				|$TestChar$TestChar$TestChar$TestChar$TestChar$TestChar
				|$TestChar$TestChar$TestChar$TestChar$TestChar$TestChar
				|
				""".trimMargin(),
			)
		}
	}

	@Test fun boxExpanded() = runTest {
		runMosaicTest(NodeSnapshots) {
			val size = 250
			val halfSize = 125

			setContent {
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

			val rootNode = awaitSnapshot()
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
	}

	@Test fun boxAlignmentParameter() = runTest {
		runMosaicTest(NodeSnapshots) {
			val outerSize = 50
			val innerSize = 10

			setContent {
				Box(
					contentAlignment = Alignment.BottomEnd,
					modifier = Modifier.requiredSize(outerSize),
				) {
					Box(Modifier.requiredSize(innerSize))
				}
			}

			val rootNode = awaitSnapshot()
			val innerBoxNode = rootNode.children[0].children[0]

			assertThat(innerBoxNode.position)
				.isEqualTo(IntOffset(outerSize - innerSize, outerSize - innerSize))
		}
	}

	@Test fun boxOutermostGravityWins() = runTest {
		runMosaicTest(NodeSnapshots) {
			val size = 10

			setContent {
				Box(Modifier.requiredSize(size)) {
					Box(Modifier.align(Alignment.BottomEnd).align(Alignment.TopStart))
				}
			}

			val rootNode = awaitSnapshot()
			val innerBoxNode = rootNode.children[0].children[0]

			assertThat(innerBoxNode.position).isEqualTo(IntOffset(size, size))
		}
	}

	@Test fun boxChildAffectsBoxSize() = runTest {
		val size = mutableIntStateOf(10)
		var measure = 0
		var layout = 0

		runMosaicTest {
			setContent {
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

			awaitSnapshot()

			assertThat(measure).isEqualTo(1)
			assertThat(layout).isEqualTo(1)

			size.value = 20
			awaitSnapshot()

			assertThat(measure).isEqualTo(2)
			assertThat(layout).isEqualTo(2)
		}
	}

	@Test fun boxCanPropagateMinConstraints() = runTest {
		runMosaicTest(NodeSnapshots) {
			setContent {
				Box(
					modifier = Modifier.requiredWidthIn(20, 40),
					propagateMinConstraints = true,
				) {
					Box(modifier = Modifier.width(10))
				}
			}

			val rootNode = awaitSnapshot()
			val innerBoxNode = rootNode.children[0].children[0]

			assertThat(innerBoxNode.width).isEqualTo(20)
		}
	}

	@Test fun boxTracksPropagateMinConstraintsChanges() = runTest {
		runMosaicTest(NodeSnapshots) {
			val pmc = mutableStateOf(true)

			setContent {
				Box(
					modifier = Modifier.requiredWidthIn(20, 40),
					propagateMinConstraints = pmc.value,
					contentAlignment = Alignment.Center,
				) {
					Box(modifier = Modifier.width(10))
				}
			}

			val firstRootNode = awaitSnapshot()
			assertThat(firstRootNode.children[0].children[0].width).isEqualTo(20)

			pmc.value = false
			val secondRootNode = awaitSnapshot()
			assertThat(secondRootNode.children[0].children[0].width).isEqualTo(10)
		}
	}

	@Test fun boxHasCorrectIntrinsicMeasurements() = runTest {
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

	@Test fun boxHasCorrectIntrinsicMeasurementsWithNoAlignedChildren() = runTest {
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

	@Test fun boxSimpleDebug() = runTest {
		runMosaicTest(DumpSnapshots) {
			setContent {
				Box()
			}
			assertThat(awaitSnapshot()).isEqualTo(
				"""
				|Box() x=0 y=0 w=0 h=0
				""".trimMargin(),
			)
		}
	}

	@Test fun boxDebug() = runTest {
		runMosaicTest(DumpSnapshots) {
			setContent {
				Box(contentAlignment = Alignment.BottomCenter, propagateMinConstraints = true) {}
			}
			assertThat(awaitSnapshot()).isEqualTo(
				"""
				|Box(alignment=Alignment(horizontalBias=0, verticalBias=1), propagateMinConstraints=true) x=0 y=0 w=0 h=0
				""".trimMargin(),
			)
		}
	}
}
