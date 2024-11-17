package com.jakewharton.mosaic.layout

import androidx.compose.runtime.Composable
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNotEqualTo
import com.jakewharton.mosaic.Container
import com.jakewharton.mosaic.NodeSnapshots
import com.jakewharton.mosaic.modifier.Modifier
import com.jakewharton.mosaic.position
import com.jakewharton.mosaic.size
import com.jakewharton.mosaic.testIntrinsics
import com.jakewharton.mosaic.testing.runMosaicTest
import com.jakewharton.mosaic.ui.Alignment
import com.jakewharton.mosaic.ui.Box
import com.jakewharton.mosaic.ui.Column
import com.jakewharton.mosaic.ui.Layout
import com.jakewharton.mosaic.ui.Row
import com.jakewharton.mosaic.ui.unit.Constraints
import com.jakewharton.mosaic.ui.unit.IntOffset
import com.jakewharton.mosaic.ui.unit.IntSize
import kotlin.test.Test
import kotlinx.coroutines.test.runTest

class SizeTest {
	@Test fun testPreferredSize_withWidthSizeModifiers() = runTest {
		runMosaicTest(NodeSnapshots) {
			val size = 50

			setContent {
				Box {
					Column {
						Container(Modifier.widthIn(min = size, max = size * 2).height(size))
						Container(Modifier.widthIn(max = size * 2).height(size))
						Container(Modifier.widthIn(min = size).height(size))
						Container(Modifier.widthIn(max = size).widthIn(min = size * 2).height(size))
						Container(Modifier.widthIn(min = size * 2).widthIn(max = size).height(size))
						Container(Modifier.size(size))
					}
				}
			}

			val rootNode = awaitSnapshot()
			val tempNode = rootNode.children[0].children[0]

			val firstContainerNode = tempNode.children[0]
			val secondContainerNode = tempNode.children[1]
			val thirdContainerNode = tempNode.children[2]
			val fourthContainerNode = tempNode.children[3]
			val fifthContainerNode = tempNode.children[4]
			val sixthContainerNode = tempNode.children[5]

			assertThat(firstContainerNode.size).isEqualTo(IntSize(size, size))
			assertThat(firstContainerNode.position).isEqualTo(IntOffset.Zero)

			assertThat(secondContainerNode.size).isEqualTo(IntSize(0, size))
			assertThat(secondContainerNode.position).isEqualTo(IntOffset(0, size))

			assertThat(thirdContainerNode.size).isEqualTo(IntSize(size, size))
			assertThat(thirdContainerNode.position).isEqualTo(IntOffset(0, size * 2))

			assertThat(fourthContainerNode.size).isEqualTo(IntSize(size, size))
			assertThat(fourthContainerNode.position).isEqualTo(IntOffset(0, size * 3))

			assertThat(fifthContainerNode.size).isEqualTo(IntSize((size * 2), size))
			assertThat(fifthContainerNode.position).isEqualTo(IntOffset(0, size * 4))

			assertThat(sixthContainerNode.size).isEqualTo(IntSize(size, size))
			assertThat(sixthContainerNode.position).isEqualTo(IntOffset(0, size * 5))
		}
	}

	@Test fun testPreferredSize_withHeightSizeModifiers() = runTest {
		runMosaicTest(NodeSnapshots) {
			val size = 10

			setContent {
				Box {
					Row {
						Container(Modifier.heightIn(min = size, max = size * 2).width(size))
						Container(Modifier.heightIn(max = size * 2).width(size))
						Container(Modifier.heightIn(min = size).width(size))
						Container(Modifier.heightIn(max = size).heightIn(min = size * 2).width(size))
						Container(Modifier.heightIn(min = size * 2).heightIn(max = size).width(size))
						Container(Modifier.height(size).then(Modifier.width(size)))
					}
				}
			}

			val rootNode = awaitSnapshot()
			val tempNode = rootNode.children[0].children[0]

			val firstContainerNode = tempNode.children[0]
			val secondContainerNode = tempNode.children[1]
			val thirdContainerNode = tempNode.children[2]
			val fourthContainerNode = tempNode.children[3]
			val fifthContainerNode = tempNode.children[4]
			val sixthContainerNode = tempNode.children[5]

			assertThat(firstContainerNode.size).isEqualTo(IntSize(size, size))
			assertThat(firstContainerNode.position).isEqualTo(IntOffset.Zero)

			assertThat(secondContainerNode.size).isEqualTo(IntSize(size, 0))
			assertThat(secondContainerNode.position).isEqualTo(IntOffset(size, 0))

			assertThat(thirdContainerNode.size).isEqualTo(IntSize(size, size))
			assertThat(thirdContainerNode.position).isEqualTo(IntOffset(size * 2, 0))

			assertThat(fourthContainerNode.size).isEqualTo(IntSize(size, size))
			assertThat(fourthContainerNode.position).isEqualTo(IntOffset(size * 3, 0))

			assertThat(fifthContainerNode.size).isEqualTo(IntSize(size, (size * 2)))
			assertThat(fifthContainerNode.position).isEqualTo(IntOffset(size * 4, 0))

			assertThat(sixthContainerNode.size).isEqualTo(IntSize(size, size))
			assertThat(sixthContainerNode.position).isEqualTo(IntOffset(size * 5, 0))
		}
	}

	@Test fun testPreferredSize_withSizeModifiers() = runTest {
		runMosaicTest(NodeSnapshots) {
			val size = 50

			setContent {
				Box {
					Row {
						val maxSize = size * 2
						Container(
							Modifier.sizeIn(maxWidth = maxSize, maxHeight = maxSize)
								.sizeIn(minWidth = size, minHeight = size),
						)
						Container(
							Modifier.sizeIn(maxWidth = size, maxHeight = size)
								.sizeIn(minWidth = size * 2, minHeight = size),
						)
						val maxSize1 = size * 2
						Container(
							Modifier.sizeIn(minWidth = size, minHeight = size)
								.sizeIn(maxWidth = maxSize1, maxHeight = maxSize1),
						)
						val minSize = size * 2
						Container(
							Modifier.sizeIn(minWidth = minSize, minHeight = minSize)
								.sizeIn(maxWidth = size, maxHeight = size),
						)
						Container(Modifier.size(size))
					}
				}
			}

			val rootNode = awaitSnapshot()
			val tempNode = rootNode.children[0].children[0]

			val firstContainerNode = tempNode.children[0]
			val secondContainerNode = tempNode.children[1]
			val thirdContainerNode = tempNode.children[2]
			val fourthContainerNode = tempNode.children[3]
			val fifthContainerNode = tempNode.children[4]

			assertThat(firstContainerNode.size).isEqualTo(IntSize(size, size))
			assertThat(firstContainerNode.position).isEqualTo(IntOffset.Zero)

			assertThat(secondContainerNode.size).isEqualTo(IntSize(size, size))
			assertThat(secondContainerNode.position).isEqualTo(IntOffset(size, 0))

			assertThat(thirdContainerNode.size).isEqualTo(IntSize(size, size))
			assertThat(thirdContainerNode.position).isEqualTo(IntOffset(size * 2, 0))

			assertThat(fourthContainerNode.size).isEqualTo(IntSize(size * 2, size * 2))
			assertThat(fourthContainerNode.position).isEqualTo(IntOffset(size * 3, 0))

			assertThat(fifthContainerNode.size).isEqualTo(IntSize(size, size))
			assertThat(fifthContainerNode.position).isEqualTo(IntOffset(size * 5, 0))
		}
	}

	@Test fun testPreferredSizeModifiers_respectMaxConstraint() = runTest {
		runMosaicTest(NodeSnapshots) {
			val size = 100

			setContent {
				Box {
					Container(width = size, height = size) {
						Container(Modifier.width(size * 2).height(size * 3)) {
							Container(expanded = true)
						}
					}
				}
			}

			val rootNode = awaitSnapshot()
			val tempNode = rootNode.children[0].children[0]

			val parentContainerNode = tempNode.children[0]
			val childContainerNode = parentContainerNode.children[0]

			assertThat(parentContainerNode.size).isEqualTo(IntSize(size, size))
			assertThat(childContainerNode.size).isEqualTo(IntSize(size, size))
			assertThat(childContainerNode.position).isEqualTo(IntOffset.Zero)
		}
	}

	@Test fun testMaxModifiers_withInfiniteValue() = runTest {
		runMosaicTest(NodeSnapshots) {
			val size = 20

			setContent {
				Box {
					Row {
						Container(Modifier.widthIn(max = Constraints.Infinity)) {
							Container(width = size, height = size)
						}
						Container(Modifier.heightIn(max = Constraints.Infinity)) {
							Container(width = size, height = size)
						}
						Container(
							Modifier.width(size)
								.height(size)
								.widthIn(max = Constraints.Infinity)
								.heightIn(max = Constraints.Infinity),
						)
						Container(
							Modifier.sizeIn(
								maxWidth = Constraints.Infinity,
								maxHeight = Constraints.Infinity,
							),
						) {
							Container(width = size, height = size) {}
						}
					}
				}
			}

			val rootNode = awaitSnapshot()
			val tempNode = rootNode.children[0].children[0]

			val firstContainerNode = tempNode.children[0].children[0]
			val secondContainerNode = tempNode.children[1].children[0]
			val thirdContainerNode = tempNode.children[2]
			val fourthContainerNode = tempNode.children[3].children[0]

			assertThat(firstContainerNode.size).isEqualTo(IntSize(size, size))
			assertThat(secondContainerNode.size).isEqualTo(IntSize(size, size))
			assertThat(thirdContainerNode.size).isEqualTo(IntSize(size, size))
			assertThat(fourthContainerNode.size).isEqualTo(IntSize(size, size))
		}
	}

	@Test fun testMeasurementConstraints_preferredSatisfiable() = runTest {
		assertConstraints(
			Constraints(10, 30, 15, 35),
			Modifier.width(20),
			Constraints(20, 20, 15, 35),
		)
		assertConstraints(
			Constraints(10, 30, 15, 35),
			Modifier.height(20),
			Constraints(10, 30, 20, 20),
		)
		assertConstraints(
			Constraints(10, 30, 15, 35),
			Modifier.size(20),
			Constraints(20, 20, 20, 20),
		)
		assertConstraints(
			Constraints(10, 30, 15, 35),
			Modifier.widthIn(20, 25),
			Constraints(20, 25, 15, 35),
		)
		assertConstraints(
			Constraints(10, 30, 15, 35),
			Modifier.heightIn(20, 25),
			Constraints(10, 30, 20, 25),
		)
		assertConstraints(
			Constraints(10, 30, 15, 35),
			Modifier.sizeIn(20, 20, 25, 25),
			Constraints(20, 25, 20, 25),
		)
	}

	@Test fun testMeasurementConstraints_preferredUnsatisfiable() = runTest {
		assertConstraints(
			Constraints(20, 40, 15, 35),
			Modifier.width(15),
			Constraints(20, 20, 15, 35),
		)
		assertConstraints(
			Constraints(10, 30, 15, 35),
			Modifier.height(10),
			Constraints(10, 30, 15, 15),
		)
		assertConstraints(
			Constraints(10, 30, 15, 35),
			Modifier.size(40),
			Constraints(30, 30, 35, 35),
		)
		assertConstraints(
			Constraints(20, 30, 15, 35),
			Modifier.widthIn(10, 15),
			Constraints(20, 20, 15, 35),
		)
		assertConstraints(
			Constraints(10, 30, 15, 35),
			Modifier.heightIn(5, 10),
			Constraints(10, 30, 15, 15),
		)
		assertConstraints(
			Constraints(10, 30, 15, 35),
			Modifier.sizeIn(40, 50, 45, 55),
			Constraints(30, 30, 35, 35),
		)
	}

	@Test fun testMeasurementConstraints_compulsorySatisfiable() = runTest {
		assertConstraints(
			Constraints(10, 30, 15, 35),
			Modifier.requiredWidth(20),
			Constraints(20, 20, 15, 35),
		)
		assertConstraints(
			Constraints(10, 30, 15, 35),
			Modifier.requiredHeight(20),
			Constraints(10, 30, 20, 20),
		)
		assertConstraints(
			Constraints(10, 30, 15, 35),
			Modifier.requiredSize(20),
			Constraints(20, 20, 20, 20),
		)
		assertConstraints(
			Constraints(10, 30, 15, 35),
			Modifier.requiredWidthIn(20, 25),
			Constraints(20, 25, 15, 35),
		)
		assertConstraints(
			Constraints(10, 30, 15, 35),
			Modifier.requiredHeightIn(20, 25),
			Constraints(10, 30, 20, 25),
		)
		assertConstraints(
			Constraints(10, 30, 15, 35),
			Modifier.requiredSizeIn(20, 20, 25, 25),
			Constraints(20, 25, 20, 25),
		)
	}

	@Test fun testMeasurementConstraints_compulsoryUnsatisfiable() = runTest {
		assertConstraints(
			Constraints(20, 40, 15, 35),
			Modifier.requiredWidth(15),
			Constraints(15, 15, 15, 35),
		)
		assertConstraints(
			Constraints(10, 30, 15, 35),
			Modifier.requiredHeight(10),
			Constraints(10, 30, 10, 10),
		)
		assertConstraints(
			Constraints(10, 30, 15, 35),
			Modifier.requiredSize(40),
			Constraints(40, 40, 40, 40),
		)
		assertConstraints(
			Constraints(20, 30, 15, 35),
			Modifier.requiredWidthIn(10, 15),
			Constraints(10, 15, 15, 35),
		)
		assertConstraints(
			Constraints(10, 30, 15, 35),
			Modifier.requiredHeightIn(5, 10),
			Constraints(10, 30, 5, 10),
		)
		assertConstraints(
			Constraints(10, 30, 15, 35),
			Modifier.requiredSizeIn(40, 50, 45, 55),
			Constraints(40, 45, 50, 55),
		)
		// When one dimension is unspecified and the other contradicts the incoming constraint.
		assertConstraints(
			Constraints(10, 10, 10, 10),
			Modifier.requiredSizeIn(20, 30, Int.MIN_VALUE, Int.MIN_VALUE),
			Constraints(20, 20, 30, 30),
		)
		assertConstraints(
			Constraints(40, 40, 40, 40),
			Modifier.requiredSizeIn(Int.MIN_VALUE, Int.MIN_VALUE, 20, 30),
			Constraints(20, 20, 30, 30),
		)
	}

	private suspend fun assertConstraints(
		incomingConstraints: Constraints,
		modifier: Modifier,
		expectedConstraints: Constraints,
	) {
		// Capture constraints and assert on test thread
		var actualConstraints: Constraints? = null
		// Clear contents before each test so that we don't recompose the BoxWithConstraints call;
		// doing so would recompose the old subcomposition with old constraints in the presence of
		// new content before the measurement performs explicit composition the new constraints.
		runMosaicTest {
			setContent {
				Layout({
					Layout(
						content = {},
						modifier = modifier,
						measurePolicy = { _, constraints ->
							actualConstraints = constraints
							layout(0, 0) {}
						},
					)
				}) { measurables, _ ->
					measurables[0].measure(incomingConstraints)
					layout(0, 0) {}
				}
			}
		}
		assertThat(actualConstraints).isEqualTo(expectedConstraints)
	}

	@Test fun testDefaultMinSize() = runTest {
		runMosaicTest {
			setContent {
				// Constraints are applied.
				Layout(
					{},
					Modifier.wrapContentSize()
						.requiredSizeIn(maxWidth = 30, maxHeight = 40)
						.defaultMinSize(minWidth = 10, minHeight = 20),
				) { _, constraints ->
					assertThat(constraints.minWidth).isEqualTo(10)
					assertThat(constraints.minHeight).isEqualTo(20)
					assertThat(constraints.maxWidth).isEqualTo(30)
					assertThat(constraints.maxHeight).isEqualTo(40)
					layout(0, 0) {}
				}
				// Constraints are not applied
				Layout(
					{},
					Modifier.requiredSizeIn(
						minWidth = 10,
						minHeight = 20,
						maxWidth = 100,
						maxHeight = 110,
					).defaultMinSize(
						minWidth = 50,
						minHeight = 50,
					),
				) { _, constraints ->
					assertThat(constraints.minWidth).isEqualTo(10)
					assertThat(constraints.minHeight).isEqualTo(20)
					assertThat(constraints.maxWidth).isEqualTo(100)
					assertThat(constraints.maxHeight).isEqualTo(110)
					layout(0, 0) {}
				}
				// Defaults values are not changing
				Layout(
					{},
					Modifier.requiredSizeIn(
						minWidth = 10,
						minHeight = 20,
						maxWidth = 100,
						maxHeight = 110,
					).defaultMinSize(),
				) { _, constraints ->
					assertThat(constraints.minWidth).isEqualTo(10)
					assertThat(constraints.minHeight).isEqualTo(20)
					assertThat(constraints.maxWidth).isEqualTo(100)
					assertThat(constraints.maxHeight).isEqualTo(110)
					layout(0, 0) {}
				}
			}
		}
	}

	@Test fun testDefaultMinSize_withCoercingMaxConstraints() = runTest {
		runMosaicTest {
			setContent {
				Layout(
					{},
					Modifier.wrapContentSize()
						.requiredSizeIn(maxWidth = 30, maxHeight = 40)
						.defaultMinSize(minWidth = 70, minHeight = 80),
				) { _, constraints ->
					assertThat(constraints.minWidth).isEqualTo(30)
					assertThat(constraints.minHeight).isEqualTo(40)
					assertThat(constraints.maxWidth).isEqualTo(30)
					assertThat(constraints.maxHeight).isEqualTo(40)
					layout(0, 0) {}
				}
			}
		}
	}

	@Test fun testMinWidthModifier_hasCorrectIntrinsicMeasurements() = runTest {
		testIntrinsics(
			@Composable {
				Container(Modifier.widthIn(min = 10)) {
					Container(Modifier.aspectRatio(1f)) { }
				}
			},
		) { minIntrinsicWidth, minIntrinsicHeight, maxIntrinsicWidth, maxIntrinsicHeight ->
			// Min width
			assertThat(minIntrinsicWidth(0)).isEqualTo(10)
			assertThat(minIntrinsicWidth(5)).isEqualTo(10)
			assertThat(minIntrinsicWidth(50)).isEqualTo(50)
			assertThat(minIntrinsicWidth(Constraints.Infinity)).isEqualTo(10)
			// Min height
			assertThat(minIntrinsicHeight(0)).isEqualTo(0)
			assertThat(minIntrinsicHeight(35)).isEqualTo(35)
			assertThat(minIntrinsicHeight(50)).isEqualTo(50)
			assertThat(minIntrinsicHeight(Constraints.Infinity)).isEqualTo(0)
			// Max width
			assertThat(maxIntrinsicWidth(0)).isEqualTo(10)
			assertThat(maxIntrinsicWidth(5)).isEqualTo(10)
			assertThat(maxIntrinsicWidth(50)).isEqualTo(50)
			assertThat(maxIntrinsicWidth(Constraints.Infinity)).isEqualTo(10)
			// Max height
			assertThat(maxIntrinsicHeight(0)).isEqualTo(0)
			assertThat(maxIntrinsicHeight(35)).isEqualTo(35)
			assertThat(maxIntrinsicHeight(50)).isEqualTo(50)
			assertThat(maxIntrinsicHeight(Constraints.Infinity)).isEqualTo(0)
		}
	}

	@Test fun testMaxWidthModifier_hasCorrectIntrinsicMeasurements() = runTest {
		testIntrinsics(
			@Composable {
				Container(Modifier.widthIn(max = 20)) {
					Container(Modifier.aspectRatio(1f)) { }
				}
			},
		) { minIntrinsicWidth, minIntrinsicHeight, maxIntrinsicWidth, maxIntrinsicHeight ->
			// Min width
			assertThat(minIntrinsicWidth(0)).isEqualTo(0)
			assertThat(minIntrinsicWidth(15)).isEqualTo(15)
			assertThat(minIntrinsicWidth(50)).isEqualTo(20)
			assertThat(minIntrinsicWidth(Constraints.Infinity)).isEqualTo(0)
			// Min height
			assertThat(minIntrinsicHeight(0)).isEqualTo(0)
			assertThat(minIntrinsicHeight(15)).isEqualTo(15)
			assertThat(minIntrinsicHeight(50)).isEqualTo(50)
			assertThat(minIntrinsicHeight(Constraints.Infinity)).isEqualTo(0)
			// Max width
			assertThat(maxIntrinsicWidth(0)).isEqualTo(0)
			assertThat(maxIntrinsicWidth(15)).isEqualTo(15)
			assertThat(maxIntrinsicWidth(50)).isEqualTo(20)
			assertThat(maxIntrinsicWidth(Constraints.Infinity)).isEqualTo(0)
			// Max height
			assertThat(maxIntrinsicHeight(0)).isEqualTo(0)
			assertThat(maxIntrinsicHeight(15)).isEqualTo(15)
			assertThat(maxIntrinsicHeight(50)).isEqualTo(50)
			assertThat(maxIntrinsicHeight(Constraints.Infinity)).isEqualTo(0)
		}
	}

	@Test fun testMinHeightModifier_hasCorrectIntrinsicMeasurements() = runTest {
		testIntrinsics(
			@Composable {
				Container(Modifier.heightIn(min = 30)) {
					Container(Modifier.aspectRatio(1f)) { }
				}
			},
		) { minIntrinsicWidth, minIntrinsicHeight, maxIntrinsicWidth, maxIntrinsicHeight ->
			// Min width
			assertThat(minIntrinsicWidth(0)).isEqualTo(0)
			assertThat(minIntrinsicWidth(15)).isEqualTo(15)
			assertThat(minIntrinsicWidth(50)).isEqualTo(50)
			assertThat(minIntrinsicWidth(Constraints.Infinity)).isEqualTo(0)
			// Min height
			assertThat(minIntrinsicHeight(0)).isEqualTo(30)
			assertThat(minIntrinsicHeight(15)).isEqualTo(30)
			assertThat(minIntrinsicHeight(50)).isEqualTo(50)
			assertThat(minIntrinsicHeight(Constraints.Infinity)).isEqualTo(30)
			// Max width
			assertThat(maxIntrinsicWidth(0)).isEqualTo(0)
			assertThat(maxIntrinsicWidth(15)).isEqualTo(15)
			assertThat(maxIntrinsicWidth(50)).isEqualTo(50)
			assertThat(maxIntrinsicWidth(Constraints.Infinity)).isEqualTo(0)
			// Max height
			assertThat(maxIntrinsicHeight(0)).isEqualTo(30)
			assertThat(maxIntrinsicHeight(15)).isEqualTo(30)
			assertThat(maxIntrinsicHeight(50)).isEqualTo(50)
			assertThat(maxIntrinsicHeight(Constraints.Infinity)).isEqualTo(30)
		}
	}

	@Test fun testMaxHeightModifier_hasCorrectIntrinsicMeasurements() = runTest {
		testIntrinsics(
			@Composable {
				Container(Modifier.heightIn(max = 40)) {
					Container(Modifier.aspectRatio(1f)) { }
				}
			},
		) { minIntrinsicWidth, minIntrinsicHeight, maxIntrinsicWidth, maxIntrinsicHeight ->
			// Min width
			assertThat(minIntrinsicWidth(0)).isEqualTo(0)
			assertThat(minIntrinsicWidth(15)).isEqualTo(15)
			assertThat(minIntrinsicWidth(50)).isEqualTo(50)
			assertThat(minIntrinsicWidth(Constraints.Infinity)).isEqualTo(0)
			// Min height
			assertThat(minIntrinsicHeight(0)).isEqualTo(0)
			assertThat(minIntrinsicHeight(15)).isEqualTo(15)
			assertThat(minIntrinsicHeight(50)).isEqualTo(40)
			assertThat(minIntrinsicHeight(Constraints.Infinity)).isEqualTo(0)
			// Max width
			assertThat(maxIntrinsicWidth(0)).isEqualTo(0)
			assertThat(maxIntrinsicWidth(15)).isEqualTo(15)
			assertThat(maxIntrinsicWidth(50)).isEqualTo(50)
			assertThat(maxIntrinsicWidth(Constraints.Infinity)).isEqualTo(0)
			// Max height
			assertThat(maxIntrinsicHeight(0)).isEqualTo(0)
			assertThat(maxIntrinsicHeight(15)).isEqualTo(15)
			assertThat(maxIntrinsicHeight(50)).isEqualTo(40)
			assertThat(maxIntrinsicHeight(Constraints.Infinity)).isEqualTo(0)
		}
	}

	@Test fun testWidthModifier_hasCorrectIntrinsicMeasurements() = runTest {
		testIntrinsics(
			@Composable {
				Container(Modifier.width(10)) {
					Container(Modifier.aspectRatio(1f)) { }
				}
			},
		) { minIntrinsicWidth, minIntrinsicHeight, maxIntrinsicWidth, maxIntrinsicHeight ->
			// Min width
			assertThat(minIntrinsicWidth(0)).isEqualTo(10)
			assertThat(minIntrinsicWidth(10)).isEqualTo(10)
			assertThat(minIntrinsicWidth(75)).isEqualTo(10)
			assertThat(minIntrinsicWidth(Constraints.Infinity)).isEqualTo(10)
			// Min height
			assertThat(minIntrinsicHeight(0)).isEqualTo(0)
			assertThat(minIntrinsicHeight(35)).isEqualTo(35)
			assertThat(minIntrinsicHeight(70)).isEqualTo(70)
			assertThat(minIntrinsicHeight(Constraints.Infinity)).isEqualTo(0)
			// Max width
			assertThat(maxIntrinsicWidth(0)).isEqualTo(10)
			assertThat(maxIntrinsicWidth(15)).isEqualTo(10)
			assertThat(maxIntrinsicWidth(75)).isEqualTo(10)
			assertThat(maxIntrinsicWidth(Constraints.Infinity)).isEqualTo(10)
			// Max height
			assertThat(maxIntrinsicHeight(0)).isEqualTo(0)
			assertThat(maxIntrinsicHeight(35)).isEqualTo(35)
			assertThat(maxIntrinsicHeight(70)).isEqualTo(70)
			assertThat(maxIntrinsicHeight(Constraints.Infinity)).isEqualTo(0)
		}
	}

	@Test fun testHeightModifier_hasCorrectIntrinsicMeasurements() = runTest {
		testIntrinsics(
			@Composable {
				Container(Modifier.height(10)) {
					Container(Modifier.aspectRatio(1f)) { }
				}
			},
		) { minIntrinsicWidth, minIntrinsicHeight, maxIntrinsicWidth, maxIntrinsicHeight ->
			// Min width
			assertThat(minIntrinsicWidth(0)).isEqualTo(0)
			assertThat(minIntrinsicWidth(15)).isEqualTo(15)
			assertThat(minIntrinsicWidth(75)).isEqualTo(75)
			assertThat(minIntrinsicWidth(Constraints.Infinity)).isEqualTo(0)
			// Min height
			assertThat(minIntrinsicHeight(0)).isEqualTo(10)
			assertThat(minIntrinsicHeight(35)).isEqualTo(10)
			assertThat(minIntrinsicHeight(70)).isEqualTo(10)
			assertThat(minIntrinsicHeight(Constraints.Infinity)).isEqualTo(10)
			// Max width
			assertThat(maxIntrinsicWidth(0)).isEqualTo(0)
			assertThat(maxIntrinsicWidth(15)).isEqualTo(15)
			assertThat(maxIntrinsicWidth(75)).isEqualTo(75)
			assertThat(maxIntrinsicWidth(Constraints.Infinity)).isEqualTo(0)
			// Max height
			assertThat(maxIntrinsicHeight(0)).isEqualTo(10)
			assertThat(maxIntrinsicHeight(35)).isEqualTo(10)
			assertThat(maxIntrinsicHeight(70)).isEqualTo(10)
			assertThat(maxIntrinsicHeight(Constraints.Infinity)).isEqualTo(10)
		}
	}

	@Test fun testWidthHeightModifiers_hasCorrectIntrinsicMeasurements() = runTest {
		testIntrinsics(
			@Composable {
				Container(
					Modifier.sizeIn(
						minWidth = 10,
						maxWidth = 20,
						minHeight = 30,
						maxHeight = 40,
					),
				) {
					Container(Modifier.aspectRatio(1f)) { }
				}
			},
		) { minIntrinsicWidth, minIntrinsicHeight, maxIntrinsicWidth, maxIntrinsicHeight ->
			// Min width
			assertThat(minIntrinsicWidth(0)).isEqualTo(10)
			assertThat(minIntrinsicWidth(15)).isEqualTo(15)
			assertThat(minIntrinsicWidth(50)).isEqualTo(20)
			assertThat(minIntrinsicWidth(Constraints.Infinity)).isEqualTo(10)
			// Min height
			assertThat(minIntrinsicHeight(0)).isEqualTo(30)
			assertThat(minIntrinsicHeight(35)).isEqualTo(35)
			assertThat(minIntrinsicHeight(50)).isEqualTo(40)
			assertThat(minIntrinsicHeight(Constraints.Infinity)).isEqualTo(30)
			// Max width
			assertThat(maxIntrinsicWidth(0)).isEqualTo(10)
			assertThat(maxIntrinsicWidth(15)).isEqualTo(15)
			assertThat(maxIntrinsicWidth(50)).isEqualTo(20)
			assertThat(maxIntrinsicWidth(Constraints.Infinity)).isEqualTo(10)
			// Max height
			assertThat(maxIntrinsicHeight(0)).isEqualTo(30)
			assertThat(maxIntrinsicHeight(35)).isEqualTo(35)
			assertThat(maxIntrinsicHeight(50)).isEqualTo(40)
			assertThat(maxIntrinsicHeight(Constraints.Infinity)).isEqualTo(30)
		}
	}

	@Test fun testMinSizeModifier_hasCorrectIntrinsicMeasurements() = runTest {
		testIntrinsics(
			@Composable {
				Container(Modifier.sizeIn(minWidth = 20, minHeight = 30)) {
					Container(Modifier.aspectRatio(1f)) { }
				}
			},
		) { minIntrinsicWidth, minIntrinsicHeight, maxIntrinsicWidth, maxIntrinsicHeight ->
			// Min width
			assertThat(minIntrinsicWidth(0)).isEqualTo(20)
			assertThat(minIntrinsicWidth(10)).isEqualTo(20)
			assertThat(minIntrinsicWidth(50)).isEqualTo(50)
			assertThat(minIntrinsicWidth(Constraints.Infinity)).isEqualTo(20)
			// Min height
			assertThat(minIntrinsicHeight(0)).isEqualTo(30)
			assertThat(minIntrinsicHeight(10)).isEqualTo(30)
			assertThat(minIntrinsicHeight(50)).isEqualTo(50)
			assertThat(minIntrinsicHeight(Constraints.Infinity)).isEqualTo(30)
			// Max width
			assertThat(maxIntrinsicWidth(0)).isEqualTo(20)
			assertThat(maxIntrinsicWidth(10)).isEqualTo(20)
			assertThat(maxIntrinsicWidth(50)).isEqualTo(50)
			assertThat(maxIntrinsicWidth(Constraints.Infinity)).isEqualTo(20)
			// Max height
			assertThat(maxIntrinsicHeight(0)).isEqualTo(30)
			assertThat(maxIntrinsicHeight(10)).isEqualTo(30)
			assertThat(maxIntrinsicHeight(50)).isEqualTo(50)
			assertThat(maxIntrinsicHeight(Constraints.Infinity)).isEqualTo(30)
		}
	}

	@Test fun testMaxSizeModifier_hasCorrectIntrinsicMeasurements() = runTest {
		testIntrinsics(
			@Composable {
				Container(Modifier.sizeIn(maxWidth = 40, maxHeight = 50)) {
					Container(Modifier.aspectRatio(1f)) { }
				}
			},
		) { minIntrinsicWidth, minIntrinsicHeight, maxIntrinsicWidth, maxIntrinsicHeight ->
			// Min width
			assertThat(minIntrinsicWidth(0)).isEqualTo(0)
			assertThat(minIntrinsicWidth(15)).isEqualTo(15)
			assertThat(minIntrinsicWidth(50)).isEqualTo(40)
			assertThat(minIntrinsicWidth(Constraints.Infinity)).isEqualTo(0)
			// Min height
			assertThat(minIntrinsicHeight(0)).isEqualTo(0)
			assertThat(minIntrinsicHeight(15)).isEqualTo(15)
			assertThat(minIntrinsicHeight(75)).isEqualTo(50)
			assertThat(minIntrinsicHeight(Constraints.Infinity)).isEqualTo(0)
			// Max width
			assertThat(maxIntrinsicWidth(0)).isEqualTo(0)
			assertThat(maxIntrinsicWidth(15)).isEqualTo(15)
			assertThat(maxIntrinsicWidth(50)).isEqualTo(40)
			assertThat(maxIntrinsicWidth(Constraints.Infinity)).isEqualTo(0)
			// Max height
			assertThat(maxIntrinsicHeight(0)).isEqualTo(0)
			assertThat(maxIntrinsicHeight(15)).isEqualTo(15)
			assertThat(maxIntrinsicHeight(75)).isEqualTo(50)
			assertThat(maxIntrinsicHeight(Constraints.Infinity)).isEqualTo(0)
		}
	}

	@Test fun testPreferredSizeModifier_hasCorrectIntrinsicMeasurements() = runTest {
		testIntrinsics(
			@Composable {
				Container(Modifier.size(40, 50)) {
					Container(Modifier.aspectRatio(1f)) { }
				}
			},
		) { minIntrinsicWidth, minIntrinsicHeight, maxIntrinsicWidth, maxIntrinsicHeight ->
			// Min width
			assertThat(minIntrinsicWidth(0)).isEqualTo(40)
			assertThat(minIntrinsicWidth(35)).isEqualTo(40)
			assertThat(minIntrinsicWidth(75)).isEqualTo(40)
			assertThat(minIntrinsicWidth(Constraints.Infinity)).isEqualTo(40)
			// Min height
			assertThat(minIntrinsicHeight(0)).isEqualTo(50)
			assertThat(minIntrinsicHeight(35)).isEqualTo(50)
			assertThat(minIntrinsicHeight(70)).isEqualTo(50)
			assertThat(minIntrinsicHeight(Constraints.Infinity)).isEqualTo(50)
			// Max width
			assertThat(maxIntrinsicWidth(0)).isEqualTo(40)
			assertThat(maxIntrinsicWidth(35)).isEqualTo(40)
			assertThat(maxIntrinsicWidth(75)).isEqualTo(40)
			assertThat(maxIntrinsicWidth(Constraints.Infinity)).isEqualTo(40)
			// Max height
			assertThat(maxIntrinsicHeight(0)).isEqualTo(50)
			assertThat(maxIntrinsicHeight(35)).isEqualTo(50)
			assertThat(maxIntrinsicHeight(70)).isEqualTo(50)
			assertThat(maxIntrinsicHeight(Constraints.Infinity)).isEqualTo(50)
		}
	}

	@Test fun testFillModifier_correctSize() = runTest {
		val parentWidth = 100
		val parentHeight = 80
		val parentModifier = Modifier.requiredSize(parentWidth, parentHeight)
		val childWidth = 40
		val childHeight = 30
		val childModifier = Modifier.size(childWidth, childHeight)

		assertThat(calculateSizeFor(parentModifier, childModifier))
			.isEqualTo(IntSize(childWidth, childHeight))
		assertThat(calculateSizeFor(parentModifier, Modifier.fillMaxWidth().then(childModifier)))
			.isEqualTo(IntSize(parentWidth, childHeight))
		assertThat(calculateSizeFor(parentModifier, Modifier.fillMaxHeight().then(childModifier)))
			.isEqualTo(IntSize(childWidth, parentHeight))
		assertThat(calculateSizeFor(parentModifier, Modifier.fillMaxSize().then(childModifier)))
			.isEqualTo(IntSize(parentWidth, parentHeight))
	}

	@Test fun testFractionalFillModifier_correctSize_whenSmallerChild() = runTest {
		val parentWidth = 100
		val parentHeight = 80
		val parentModifier = Modifier.requiredSize(parentWidth, parentHeight)
		val childWidth = 40
		val childHeight = 30
		val childModifier = Modifier.size(childWidth, childHeight)

		assertThat(calculateSizeFor(parentModifier, childModifier))
			.isEqualTo(IntSize(childWidth, childHeight))
		assertThat(calculateSizeFor(parentModifier, Modifier.fillMaxWidth(0.5f).then(childModifier)))
			.isEqualTo(IntSize(parentWidth / 2, childHeight))
		assertThat(calculateSizeFor(parentModifier, Modifier.fillMaxHeight(0.5f).then(childModifier)))
			.isEqualTo(IntSize(childWidth, parentHeight / 2))
		assertThat(calculateSizeFor(parentModifier, Modifier.fillMaxSize(0.5f).then(childModifier)))
			.isEqualTo(IntSize(parentWidth / 2, parentHeight / 2))
	}

	@Test fun testFractionalFillModifier_correctSize_whenLargerChild() = runTest {
		val parentWidth = 100
		val parentHeight = 80
		val parentModifier = Modifier.requiredSize(parentWidth, parentHeight)
		val childWidth = 70
		val childHeight = 50
		val childModifier = Modifier.size(childWidth, childHeight)

		assertThat(calculateSizeFor(parentModifier, childModifier))
			.isEqualTo(IntSize(childWidth, childHeight))
		assertThat(calculateSizeFor(parentModifier, Modifier.fillMaxWidth(0.5f).then(childModifier)))
			.isEqualTo(IntSize(parentWidth / 2, childHeight))
		assertThat(calculateSizeFor(parentModifier, Modifier.fillMaxHeight(0.5f).then(childModifier)))
			.isEqualTo(IntSize(childWidth, parentHeight / 2))
		assertThat(calculateSizeFor(parentModifier, Modifier.fillMaxSize(0.5f).then(childModifier)))
			.isEqualTo(IntSize(parentWidth / 2, parentHeight / 2))
	}

	@Test fun testFractionalFillModifier_coerced() = runTest {
		val childMinWidth = 40
		val childMinHeight = 30
		val childMaxWidth = 60
		val childMaxHeight = 50
		val childModifier = Modifier.requiredSizeIn(
			childMinWidth,
			childMinHeight,
			childMaxWidth,
			childMaxHeight,
		)

		assertThat(calculateSizeFor(Modifier, childModifier.then(Modifier.fillMaxSize(0.1f))))
			.isEqualTo(IntSize(childMinWidth, childMinHeight))
		assertThat(calculateSizeFor(Modifier, childModifier.then(Modifier.fillMaxSize(1.0f))))
			.isEqualTo(IntSize(childMaxWidth, childMaxHeight))
	}

	private suspend fun calculateSizeFor(parentModifier: Modifier, modifier: Modifier): IntSize {
		return runMosaicTest(NodeSnapshots) {
			setContent {
				Box(parentModifier) {
					Box(modifier)
				}
			}

			val rootNode = awaitSnapshot()
			val innerBoxNode = rootNode.children[0].children[0]

			IntSize(innerBoxNode.width, innerBoxNode.height)
		}
	}

	@Test fun testDefaultMinSizeModifier_hasCorrectIntrinsicMeasurements() = runTest {
		testIntrinsics(
			@Composable {
				Container(Modifier.defaultMinSize(40, 50)) {
					Container(Modifier.aspectRatio(1f)) {}
				}
			},
		) { minIntrinsicWidth, minIntrinsicHeight, _, _ ->
			// Min width
			assertThat(minIntrinsicWidth(0)).isEqualTo(40)
			assertThat(minIntrinsicWidth(35)).isEqualTo(40)
			assertThat(minIntrinsicWidth(55)).isEqualTo(55)
			assertThat(minIntrinsicWidth(Constraints.Infinity)).isEqualTo(40)
			// Min height
			assertThat(minIntrinsicHeight(0)).isEqualTo(50)
			assertThat(minIntrinsicHeight(35)).isEqualTo(50)
			assertThat(minIntrinsicHeight(55)).isEqualTo(55)
			assertThat(minIntrinsicHeight(Constraints.Infinity)).isEqualTo(50)
			// Max width
			assertThat(minIntrinsicWidth(0)).isEqualTo(40)
			assertThat(minIntrinsicWidth(35)).isEqualTo(40)
			assertThat(minIntrinsicWidth(55)).isEqualTo(55)
			assertThat(minIntrinsicWidth(Constraints.Infinity)).isEqualTo(40)
			// Max height
			assertThat(minIntrinsicHeight(0)).isEqualTo(50)
			assertThat(minIntrinsicHeight(35)).isEqualTo(50)
			assertThat(minIntrinsicHeight(55)).isEqualTo(55)
			assertThat(minIntrinsicHeight(Constraints.Infinity)).isEqualTo(50)
		}
	}

	@Test fun testFillModifier_noChangeIntrinsicMeasurements() = runTest {
		verifyIntrinsicMeasurements(Modifier.fillMaxWidth())
		verifyIntrinsicMeasurements(Modifier.fillMaxHeight())
		verifyIntrinsicMeasurements(Modifier.fillMaxSize())
	}

	private suspend fun verifyIntrinsicMeasurements(expandedModifier: Modifier) {
		// intrinsic measurements do not change with the ExpandedModifier
		testIntrinsics(
			@Composable {
				Container(
					expandedModifier.then(Modifier.aspectRatio(2f)),
					width = 30,
					height = 40,
				)
			},
		) { minIntrinsicWidth, minIntrinsicHeight, maxIntrinsicWidth, maxIntrinsicHeight ->
			// Width
			assertThat(minIntrinsicWidth(20)).isEqualTo(40)
			assertThat(minIntrinsicWidth(Constraints.Infinity)).isEqualTo(30)

			assertThat(maxIntrinsicWidth(20)).isEqualTo(40)
			assertThat(maxIntrinsicWidth(Constraints.Infinity)).isEqualTo(30)

			// Height
			assertThat(minIntrinsicHeight(40)).isEqualTo(20)
			assertThat(minIntrinsicHeight(Constraints.Infinity)).isEqualTo(40)

			assertThat(maxIntrinsicHeight(40)).isEqualTo(20)
			assertThat(maxIntrinsicHeight(Constraints.Infinity)).isEqualTo(40)
		}
	}

	@Test fun test2DWrapContentSize() = runTest {
		runMosaicTest(NodeSnapshots) {
			val size = 50

			setContent {
				Container {
					Container(
						Modifier.fillMaxSize()
							.wrapContentSize(Alignment.BottomEnd)
							.size(size),
					)
				}
			}

			val rootNode = awaitSnapshot()
			val innerContainerNode = rootNode.children[0]

			assertThat(rootNode.position).isEqualTo(IntOffset.Zero)
			assertThat(innerContainerNode.size).isEqualTo(IntSize(size, size))
			assertThat(innerContainerNode.position)
				.isEqualTo(IntOffset(rootNode.width - size, rootNode.height - size))
		}
	}

	@Test fun test1DWrapContentSize() = runTest {
		runMosaicTest(NodeSnapshots) {
			val size = 50

			setContent {
				Container {
					Container(
						Modifier.fillMaxSize()
							.wrapContentWidth(Alignment.End)
							.width(size),
					)
				}
			}

			val rootNode = awaitSnapshot()
			val innerContainerNode = rootNode.children[0]

			assertThat(rootNode.position).isEqualTo(IntOffset.Zero)
			assertThat(innerContainerNode.size).isEqualTo(IntSize(size, rootNode.height))
			assertThat(innerContainerNode.position).isEqualTo(IntOffset(rootNode.width - size, 0))
		}
	}

	@Test fun testModifier_wrapsContent() = runTest {
		runMosaicTest(NodeSnapshots) {
			val contentSize = 50

			setContent {
				Container {
					Container {
						Container(Modifier.wrapContentSize(Alignment.TopStart).size(contentSize))
					}
				}
			}

			val rootNode = awaitSnapshot()
			val middleContainerNode = rootNode.children[0].children[0]

			assertThat(middleContainerNode.size).isEqualTo(IntSize(contentSize, contentSize))
		}
	}

	@Test fun testWrapContentSize_wrapsContent_whenMeasuredWithInfiniteConstraints() = runTest {
		runMosaicTest(NodeSnapshots) {
			val size = 50

			setContent {
				Layout(
					modifier = Modifier.size(100),
					content = {
						Container {
							Container(Modifier.wrapContentSize(Alignment.BottomEnd).size(size))
						}
					},
					measurePolicy = { measurables, constraints ->
						val placeable = measurables.first().measure(Constraints())
						layout(constraints.maxWidth, constraints.maxHeight) {
							placeable.place(0, 0)
						}
					},
				)
			}

			val rootNode = awaitSnapshot()
			val alignContainerNode = rootNode.children[0].children[0]
			val childContainerNode = rootNode.children[0].children[0].children[0]

			assertThat(alignContainerNode.size).isEqualTo(IntSize(size, size))
			assertThat(alignContainerNode.position).isEqualTo(IntOffset.Zero)
			assertThat(childContainerNode.size).isEqualTo(IntSize(size, size))
			assertThat(childContainerNode.position).isEqualTo(IntOffset.Zero)
		}
	}

	@Test fun test2DAlignedModifier_hasCorrectIntrinsicMeasurements() = runTest {
		testIntrinsics(
			@Composable {
				Container(Modifier.wrapContentSize(Alignment.TopStart).aspectRatio(2f)) { }
			},
		) { minIntrinsicWidth, minIntrinsicHeight, maxIntrinsicWidth, maxIntrinsicHeight ->
			// Min width
			assertThat(minIntrinsicWidth(0)).isEqualTo(0)
			assertThat(minIntrinsicWidth(25)).isEqualTo(50)
			assertThat(minIntrinsicWidth(Constraints.Infinity)).isEqualTo(0)

			// Min height
			assertThat(minIntrinsicWidth(0)).isEqualTo(0)
			assertThat(minIntrinsicHeight(50)).isEqualTo(25)
			assertThat(minIntrinsicHeight(Constraints.Infinity)).isEqualTo(0)

			// Max width
			assertThat(minIntrinsicWidth(0)).isEqualTo(0)
			assertThat(maxIntrinsicWidth(25)).isEqualTo(50)
			assertThat(maxIntrinsicWidth(Constraints.Infinity)).isEqualTo(0)

			// Max height
			assertThat(minIntrinsicWidth(0)).isEqualTo(0)
			assertThat(maxIntrinsicHeight(50)).isEqualTo(25)
			assertThat(maxIntrinsicHeight(Constraints.Infinity)).isEqualTo(0)
		}
	}

	@Test fun test1DAlignedModifier_hasCorrectIntrinsicMeasurements() = runTest {
		testIntrinsics({
			Container(
				Modifier.wrapContentHeight(Alignment.CenterVertically)
					.aspectRatio(2f),
			) { }
		}) { minIntrinsicWidth, minIntrinsicHeight, maxIntrinsicWidth, maxIntrinsicHeight ->
			// Min width
			assertThat(minIntrinsicWidth(0)).isEqualTo(0)
			assertThat(minIntrinsicWidth(25)).isEqualTo(50)
			assertThat(minIntrinsicWidth(Constraints.Infinity)).isEqualTo(0)

			// Min height
			assertThat(minIntrinsicWidth(0)).isEqualTo(0)
			assertThat(minIntrinsicHeight(50)).isEqualTo(25)
			assertThat(minIntrinsicHeight(Constraints.Infinity)).isEqualTo(0)

			// Max width
			assertThat(minIntrinsicWidth(0)).isEqualTo(0)
			assertThat(maxIntrinsicWidth(25)).isEqualTo(50)
			assertThat(maxIntrinsicWidth(Constraints.Infinity)).isEqualTo(0)

			// Max height
			assertThat(minIntrinsicWidth(0)).isEqualTo(0)
			assertThat(maxIntrinsicHeight(50)).isEqualTo(25)
			assertThat(maxIntrinsicHeight(Constraints.Infinity)).isEqualTo(0)
		}
	}

	@Test fun testModifiers_equals() {
		assertThat(Modifier.size(10, 20)).isEqualTo(Modifier.size(10, 20))
		assertThat(Modifier.requiredSize(10, 20)).isEqualTo(Modifier.requiredSize(10, 20))
		assertThat(Modifier.wrapContentSize(Alignment.BottomEnd))
			.isEqualTo(Modifier.wrapContentSize(Alignment.BottomEnd))
		assertThat(Modifier.fillMaxSize(0.8f)).isEqualTo(Modifier.fillMaxSize(0.8f))
		assertThat(Modifier.defaultMinSize(10, 20)).isEqualTo(Modifier.defaultMinSize(10, 20))

		assertThat(Modifier.size(10, 20)).isNotEqualTo(Modifier.size(20, 10))
		assertThat(Modifier.requiredSize(10, 20)).isNotEqualTo(Modifier.requiredSize(20, 10))
		assertThat(Modifier.wrapContentSize(Alignment.BottomEnd))
			.isNotEqualTo(Modifier.wrapContentSize(Alignment.BottomCenter))
		assertThat(Modifier.fillMaxSize(0.8f)).isNotEqualTo(Modifier.fillMaxSize())
		assertThat(Modifier.defaultMinSize(10, 20)).isNotEqualTo(Modifier.defaultMinSize(20, 10))
	}

	@Test fun sizeModifiers_doNotCauseCrashesWhenCreatingConstraints() = runTest {
		runMosaicTest {
			setContent {
				Box(Modifier.sizeIn(minWidth = -1))
				Box(Modifier.sizeIn(minWidth = 10, maxWidth = 5))
				Box(Modifier.sizeIn(minHeight = -1))
				Box(Modifier.sizeIn(minHeight = 10, maxHeight = 5))
				Box(
					Modifier.sizeIn(
						minWidth = Constraints.Infinity,
						maxWidth = Constraints.Infinity,
						minHeight = Constraints.Infinity,
						maxHeight = Constraints.Infinity,
					),
				)
				Box(Modifier.defaultMinSize(minWidth = -1, minHeight = -1))
			}
		}
	}
}
