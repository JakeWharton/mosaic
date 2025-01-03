package com.jakewharton.mosaic.ui

import androidx.compose.runtime.Composable
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isZero
import com.jakewharton.mosaic.ConstrainedBox
import com.jakewharton.mosaic.Container
import com.jakewharton.mosaic.Holder
import com.jakewharton.mosaic.NodeSnapshots
import com.jakewharton.mosaic.assertFailure
import com.jakewharton.mosaic.layout.aspectRatio
import com.jakewharton.mosaic.layout.fillMaxHeight
import com.jakewharton.mosaic.layout.fillMaxWidth
import com.jakewharton.mosaic.layout.height
import com.jakewharton.mosaic.layout.heightIn
import com.jakewharton.mosaic.layout.padding
import com.jakewharton.mosaic.layout.requiredSize
import com.jakewharton.mosaic.layout.size
import com.jakewharton.mosaic.layout.sizeIn
import com.jakewharton.mosaic.layout.width
import com.jakewharton.mosaic.layout.widthIn
import com.jakewharton.mosaic.layout.wrapContentSize
import com.jakewharton.mosaic.modifier.Modifier
import com.jakewharton.mosaic.position
import com.jakewharton.mosaic.runMosaicTest
import com.jakewharton.mosaic.size
import com.jakewharton.mosaic.testIntrinsics
import com.jakewharton.mosaic.ui.unit.Constraints
import com.jakewharton.mosaic.ui.unit.IntOffset
import com.jakewharton.mosaic.ui.unit.IntSize
import kotlin.math.min
import kotlin.test.Test
import kotlinx.coroutines.test.runTest

class RowColumnTest {
	// region Size and position tests for Row and Column
	@Test fun testRow() = runTest {
		runMosaicTest(NodeSnapshots) {
			val size = 50

			setContent {
				Container(alignment = Alignment.TopStart) {
					Row {
						Container(width = size, height = size)
						Container(width = size * 2, height = size * 2)
					}
				}
			}

			val rootNode = awaitSnapshot()
			val firstChildContainerNode = rootNode.children[0].children[0].children[0]
			val secondChildContainerNode = rootNode.children[0].children[0].children[1]

			assertThat(firstChildContainerNode.size).isEqualTo(IntSize(size, size))
			assertThat(firstChildContainerNode.position).isEqualTo(IntOffset.Zero)
			assertThat(secondChildContainerNode.size).isEqualTo(IntSize(size * 2, size * 2))
			assertThat(secondChildContainerNode.position).isEqualTo(IntOffset(size, 0))
		}
	}

	@Test fun testRow_withChildrenWithWeight() = runTest {
		runMosaicTest(NodeSnapshots) {
			val width = 50
			val height = 80

			setContent {
				Container(alignment = Alignment.TopStart) {
					Row {
						Container(modifier = Modifier.weight(1.0f), width = width, height = height)
						Container(modifier = Modifier.weight(2.0f), width = width, height = height)
					}
				}
			}

			val rootNode = awaitSnapshot()
			val rootWidth = rootNode.width

			val firstChildContainerNode = rootNode.children[0].children[0].children[0]
			val secondChildContainerNode = rootNode.children[0].children[0].children[1]

			assertThat(firstChildContainerNode.size).isEqualTo(IntSize(rootWidth / 3, height))
			assertThat(firstChildContainerNode.position).isEqualTo(IntOffset.Zero)
			assertThat(secondChildContainerNode.size).isEqualTo(IntSize(rootWidth * 2 / 3, height))
			assertThat(secondChildContainerNode.position).isEqualTo(IntOffset(rootWidth / 3, 0))
		}
	}

	@Test fun testRow_withChildrenWithWeightNonFilling() = runTest {
		runMosaicTest(NodeSnapshots) {
			val width = 50
			val height = 80

			setContent {
				Container(
					modifier = Modifier.sizeIn(maxWidth = 300, maxHeight = 300),
					alignment = Alignment.TopStart,
				) {
					Row {
						Container(
							modifier = Modifier.weight(1.0f, fill = false),
							width = width,
							height = height,
						)
						Container(
							modifier = Modifier.weight(2.0f, fill = false),
							width = width,
							height = height * 2,
						)
					}
				}
			}

			val rootNode = awaitSnapshot()
			val firstChildContainerNode = rootNode.children[0].children[0].children[0]
			val secondChildContainerNode = rootNode.children[0].children[0].children[1]

			assertThat(firstChildContainerNode.size).isEqualTo(IntSize(width, height))
			assertThat(firstChildContainerNode.position).isEqualTo(IntOffset.Zero)
			assertThat(secondChildContainerNode.size).isEqualTo(IntSize(width, height * 2))
			assertThat(secondChildContainerNode.position).isEqualTo(IntOffset(width, 0))
		}
	}

	@Test fun testRow_withChildrenWithMaxValueWeight() = runTest {
		runMosaicTest(NodeSnapshots) {
			val width = 50
			val height = 80

			setContent {
				Container(alignment = Alignment.TopStart) {
					Row {
						Container(modifier = Modifier.weight(Float.MAX_VALUE), width = width, height = height)
						Container(modifier = Modifier.weight(1.0f), width = width, height = height)
					}
				}
			}

			val rootNode = awaitSnapshot()
			val rootWidth = rootNode.width

			val firstChildContainerNode = rootNode.children[0].children[0].children[0]
			val secondChildContainerNode = rootNode.children[0].children[0].children[1]

			assertThat(firstChildContainerNode.size).isEqualTo(IntSize(rootWidth, height))
			assertThat(firstChildContainerNode.position).isEqualTo(IntOffset.Zero)
			assertThat(secondChildContainerNode.size).isEqualTo(IntSize(0, height))
			assertThat(secondChildContainerNode.position).isEqualTo(IntOffset(rootWidth, 0))
		}
	}

	@Test fun testRow_withChildrenWithPositiveInfinityWeight() = runTest {
		runMosaicTest(NodeSnapshots) {
			val width = 50
			val height = 80

			setContent {
				Container(alignment = Alignment.TopStart) {
					Row {
						Container(
							modifier = Modifier.weight(Float.POSITIVE_INFINITY),
							width = width,
							height = height,
						)
						Container(modifier = Modifier.weight(1.0f), width = width, height = height)
					}
				}
			}

			val rootNode = awaitSnapshot()
			val rootWidth = rootNode.width

			val firstChildContainerNode = rootNode.children[0].children[0].children[0]
			val secondChildContainerNode = rootNode.children[0].children[0].children[1]

			assertThat(firstChildContainerNode.size).isEqualTo(IntSize(rootWidth, height))
			assertThat(firstChildContainerNode.position).isEqualTo(IntOffset.Zero)
			assertThat(secondChildContainerNode.size).isEqualTo(IntSize(0, height))
			assertThat(secondChildContainerNode.position).isEqualTo(IntOffset(rootWidth, 0))
		}
	}

	@Test fun testRow_invalidWeight() {
		with(RowScopeInstance) {
			assertFailure<IllegalArgumentException> {
				Modifier.weight(-1.0f)
			}
			assertFailure<IllegalArgumentException> {
				Modifier.weight(Float.NaN)
			}
			assertFailure<IllegalArgumentException> {
				Modifier.weight(Float.NEGATIVE_INFINITY)
			}
		}
	}

	@Test fun testColumn() = runTest {
		runMosaicTest(NodeSnapshots) {
			val size = 50

			setContent {
				Container(alignment = Alignment.TopStart) {
					Column {
						Container(width = size, height = size)
						Container(width = (size * 2), height = (size * 2))
					}
				}
			}

			val rootNode = awaitSnapshot()
			val firstChildContainerNode = rootNode.children[0].children[0].children[0]
			val secondChildContainerNode = rootNode.children[0].children[0].children[1]

			assertThat(firstChildContainerNode.size).isEqualTo(IntSize(size, size))
			assertThat(firstChildContainerNode.position).isEqualTo(IntOffset.Zero)
			assertThat(secondChildContainerNode.size).isEqualTo(IntSize(size * 2, size * 2))
			assertThat(secondChildContainerNode.position).isEqualTo(IntOffset(0, size))
		}
	}

	@Test fun testColumn_withChildrenWithWeight() = runTest {
		runMosaicTest(NodeSnapshots) {
			val width = 80
			val height = 50

			setContent {
				Container(alignment = Alignment.TopStart) {
					Column {
						Container(modifier = Modifier.weight(1.0f), width = width, height = height)
						Container(modifier = Modifier.weight(2.0f), width = width, height = height)
					}
				}
			}

			val rootNode = awaitSnapshot()
			val rootHeight = rootNode.height

			val firstChildContainerNode = rootNode.children[0].children[0].children[0]
			val secondChildContainerNode = rootNode.children[0].children[0].children[1]

			assertThat(firstChildContainerNode.size).isEqualTo(IntSize(width, rootHeight / 3))
			assertThat(firstChildContainerNode.position).isEqualTo(IntOffset.Zero)
			assertThat(secondChildContainerNode.size).isEqualTo(IntSize(width, rootHeight * 2 / 3))
			assertThat(secondChildContainerNode.position).isEqualTo(IntOffset(0, rootHeight / 3))
		}
	}

	@Test fun testColumn_withChildrenWithWeightNonFilling() = runTest {
		runMosaicTest(NodeSnapshots) {
			val width = 80
			val height = 50

			setContent {
				Container(
					modifier = Modifier.sizeIn(maxWidth = 300, maxHeight = 300),
					alignment = Alignment.TopStart,
				) {
					Column {
						Container(
							modifier = Modifier.weight(1.0f, fill = false),
							width = width,
							height = height,
						)
						Container(
							modifier = Modifier.weight(2.0f, fill = false),
							width = width,
							height = height,
						)
					}
				}
			}

			val rootNode = awaitSnapshot()
			val firstChildContainerNode = rootNode.children[0].children[0].children[0]
			val secondChildContainerNode = rootNode.children[0].children[0].children[1]

			assertThat(firstChildContainerNode.size).isEqualTo(IntSize(width, height))
			assertThat(firstChildContainerNode.position).isEqualTo(IntOffset.Zero)
			assertThat(secondChildContainerNode.size).isEqualTo(IntSize(width, height))
			assertThat(secondChildContainerNode.position).isEqualTo(IntOffset(0, height))
		}
	}

	@Test fun testColumn_withChildrenWithMaxValueWeight() = runTest {
		runMosaicTest(NodeSnapshots) {
			val width = 80
			val height = 50

			setContent {
				Container(alignment = Alignment.TopStart) {
					Column {
						Container(modifier = Modifier.weight(Float.MAX_VALUE), width = width, height = height)
						Container(modifier = Modifier.weight(1.0f), width = width, height = height)
					}
				}
			}

			val rootNode = awaitSnapshot()
			val rootHeight = rootNode.height

			val firstChildContainerNode = rootNode.children[0].children[0].children[0]
			val secondChildContainerNode = rootNode.children[0].children[0].children[1]

			assertThat(firstChildContainerNode.size).isEqualTo(IntSize(width, rootHeight))
			assertThat(firstChildContainerNode.position).isEqualTo(IntOffset.Zero)
			assertThat(secondChildContainerNode.size).isEqualTo(IntSize(width, 0))
			assertThat(secondChildContainerNode.position).isEqualTo(IntOffset(0, rootHeight))
		}
	}

	@Test fun testColumn_withChildrenWithPositiveInfinityWeight() = runTest {
		runMosaicTest(NodeSnapshots) {
			val width = 80
			val height = 50

			setContent {
				Container(alignment = Alignment.TopStart) {
					Column {
						Container(
							modifier = Modifier.weight(Float.POSITIVE_INFINITY),
							width = width,
							height = height,
						)
						Container(modifier = Modifier.weight(1.0f), width = width, height = height)
					}
				}
			}

			val rootNode = awaitSnapshot()
			val rootHeight = rootNode.height

			val firstChildContainerNode = rootNode.children[0].children[0].children[0]
			val secondChildContainerNode = rootNode.children[0].children[0].children[1]

			assertThat(firstChildContainerNode.size).isEqualTo(IntSize(width, rootHeight))
			assertThat(firstChildContainerNode.position).isEqualTo(IntOffset.Zero)
			assertThat(secondChildContainerNode.size).isEqualTo(IntSize(width, 0))
			assertThat(secondChildContainerNode.position).isEqualTo(IntOffset(0, rootHeight))
		}
	}

	@Test fun testColumn_invalidWeight() {
		with(ColumnScopeInstance) {
			assertFailure<IllegalArgumentException> {
				Modifier.weight(-1.0f)
			}
			assertFailure<IllegalArgumentException> {
				Modifier.weight(Float.NaN)
			}
			assertFailure<IllegalArgumentException> {
				Modifier.weight(Float.NEGATIVE_INFINITY)
			}
		}
	}

	@Test fun testRow_doesNotPlaceChildrenOutOfBounds_becauseOfRoundings() = runTest {
		runMosaicTest(NodeSnapshots) {
			val expectedRowWidth = 11
			val leftPadding = 1

			setContent {
				Row(
					Modifier.wrapContentSize(Alignment.TopStart)
						.padding(left = leftPadding)
						.widthIn(max = expectedRowWidth),
				) {
					Container(Modifier.weight(1.0f))
					Container(Modifier.weight(1.0f))
				}
			}

			val rootNode = awaitSnapshot()
			val rowNode = rootNode.children[0]

			val firstChildContainerNode = rowNode.children[0]
			val secondChildContainerNode = rowNode.children[1]

			assertThat(rowNode.width - leftPadding).isEqualTo(expectedRowWidth)
			assertThat(firstChildContainerNode.x).isEqualTo(leftPadding)
			assertThat(secondChildContainerNode.x).isEqualTo(leftPadding + firstChildContainerNode.width)
			assertThat(firstChildContainerNode.width + secondChildContainerNode.width)
				.isEqualTo(rowNode.width - leftPadding)
		}
	}

	@Test fun testRow_isNotLargerThanItsChildren_becauseOfRoundings() = runTest {
		runMosaicTest(NodeSnapshots) {
			val expectedRowWidth = 8
			val leftPadding = 1

			setContent {
				Row(
					Modifier.wrapContentSize(Alignment.TopStart)
						.padding(left = leftPadding)
						.widthIn(max = expectedRowWidth),
				) {
					Container(Modifier.weight(2.0f))
					Container(Modifier.weight(2.0f))
					Container(Modifier.weight(3.0f))
				}
			}

			val rootNode = awaitSnapshot()
			val rowNode = rootNode.children[0]

			val firstChildContainerNode = rowNode.children[0]
			val secondChildContainerNode = rowNode.children[1]
			val thirdChildContainerNode = rowNode.children[2]

			assertThat(rowNode.width - leftPadding).isEqualTo(expectedRowWidth)
			assertThat(firstChildContainerNode.x).isEqualTo(leftPadding)
			assertThat(secondChildContainerNode.x).isEqualTo(leftPadding + firstChildContainerNode.width)
			assertThat(thirdChildContainerNode.x)
				.isEqualTo(leftPadding + firstChildContainerNode.width + secondChildContainerNode.width)
			assertThat(rowNode.width - leftPadding)
				.isEqualTo(firstChildContainerNode.width + secondChildContainerNode.width + thirdChildContainerNode.width)
		}
	}

	@Test fun testColumn_isNotLargetThanItsChildren_becauseOfRoundings() = runTest {
		runMosaicTest(NodeSnapshots) {
			val expectedColumnHeight = 8
			val topPadding = 1

			setContent {
				Column(
					Modifier.wrapContentSize(Alignment.TopStart)
						.padding(top = topPadding)
						.heightIn(max = expectedColumnHeight),
				) {
					Container(Modifier.weight(1.0f))
					Container(Modifier.weight(1.0f))
					Container(Modifier.weight(1.0f))
				}
			}

			val rootNode = awaitSnapshot()
			val columnNode = rootNode.children[0]

			val firstChildContainerNode = columnNode.children[0]
			val secondChildContainerNode = columnNode.children[1]
			val thirdChildContainerNode = columnNode.children[2]

			assertThat(columnNode.height - topPadding).isEqualTo(expectedColumnHeight)
			assertThat(firstChildContainerNode.y).isEqualTo(topPadding)
			assertThat(secondChildContainerNode.y).isEqualTo(topPadding + firstChildContainerNode.height)
			assertThat(thirdChildContainerNode.y)
				.isEqualTo(topPadding + firstChildContainerNode.height + secondChildContainerNode.height)
			assertThat(columnNode.height - topPadding)
				.isEqualTo(firstChildContainerNode.height + secondChildContainerNode.height + thirdChildContainerNode.height)
		}
	}

	@Test fun testColumn_doesNotPlaceChildrenOutOfBounds_becauseOfRoundings() = runTest {
		runMosaicTest(NodeSnapshots) {
			val expectedColumnHeight = 11
			val topPadding = 1

			setContent {
				Column(
					Modifier.wrapContentSize(Alignment.TopStart)
						.padding(top = topPadding)
						.heightIn(max = expectedColumnHeight),
				) {
					Container(Modifier.weight(1.0f))
					Container(Modifier.weight(1.0f))
				}
			}

			val rootNode = awaitSnapshot()
			val columnNode = rootNode.children[0]

			val firstChildContainerNode = columnNode.children[0]
			val secondChildContainerNode = columnNode.children[1]

			assertThat(columnNode.height - topPadding).isEqualTo(expectedColumnHeight)
			assertThat(firstChildContainerNode.height).isEqualTo((expectedColumnHeight - topPadding) / 2)
			assertThat(secondChildContainerNode.height)
				.isEqualTo(topPadding + firstChildContainerNode.height)
			assertThat(columnNode.height - topPadding)
				.isEqualTo(firstChildContainerNode.height + secondChildContainerNode.height)
		}
	}
	// endregion

	// region Cross axis alignment tests in Row
	@Test fun testRow_withStretchCrossAxisAlignment() = runTest {
		runMosaicTest(NodeSnapshots) {
			val size = 50

			setContent {
				Row(modifier = Modifier.height(size * 2)) {
					Container(modifier = Modifier.fillMaxHeight(), width = size, height = size)
					Container(modifier = Modifier.fillMaxHeight(), width = size * 2, height = size * 2)
				}
			}

			val rootNode = awaitSnapshot()
			val rootHeight = rootNode.height

			val firstChildContainerNode = rootNode.children[0].children[0]
			val secondChildContainerNode = rootNode.children[0].children[1]

			assertThat(firstChildContainerNode.size).isEqualTo(IntSize(size, rootHeight))
			assertThat(firstChildContainerNode.position).isEqualTo(IntOffset.Zero)
			assertThat(secondChildContainerNode.size).isEqualTo(IntSize(size * 2, rootHeight))
			assertThat(secondChildContainerNode.position).isEqualTo(IntOffset(size, 0))
		}
	}

	@Test fun testRow_withGravityModifier_andGravityParameter() = runTest {
		runMosaicTest(NodeSnapshots) {
			val size = 50

			setContent {
				Row(Modifier.fillMaxHeight(), verticalAlignment = Alignment.CenterVertically) {
					Container(modifier = Modifier.align(Alignment.Top), width = size, height = size)
					Container(width = size, height = size)
					Container(modifier = Modifier.align(Alignment.Bottom), width = size, height = size)
				}
			}

			val rootNode = awaitSnapshot()
			val rootHeight = rootNode.height

			val firstChildContainerNode = rootNode.children[0].children[0]
			val secondChildContainerNode = rootNode.children[0].children[1]
			val thirdChildContainerNode = rootNode.children[0].children[2]

			assertThat(firstChildContainerNode.size).isEqualTo(IntSize(size, size))
			assertThat(firstChildContainerNode.position).isEqualTo(IntOffset.Zero)
			assertThat(secondChildContainerNode.size).isEqualTo(IntSize(size, size))
			assertThat(secondChildContainerNode.position).isEqualTo(
				IntOffset(
					size,
					(rootHeight - size) / 2,
				),
			)
			assertThat(thirdChildContainerNode.size).isEqualTo(IntSize(size, size))
			assertThat(thirdChildContainerNode.position).isEqualTo(IntOffset(size * 2, rootHeight - size))
		}
	}

	@Test fun testRow_withGravityModifier() = runTest {
		runMosaicTest(NodeSnapshots) {
			val size = 50

			setContent {
				Row(Modifier.fillMaxHeight()) {
					Container(modifier = Modifier.align(Alignment.Top), width = size, height = size)
					Container(
						modifier = Modifier.align(Alignment.CenterVertically),
						width = size,
						height = size,
					)
					Container(modifier = Modifier.align(Alignment.Bottom), width = size, height = size)
				}
			}

			val rootNode = awaitSnapshot()
			val rootHeight = rootNode.height

			val firstChildContainerNode = rootNode.children[0].children[0]
			val secondChildContainerNode = rootNode.children[0].children[1]
			val thirdChildContainerNode = rootNode.children[0].children[2]

			assertThat(firstChildContainerNode.size).isEqualTo(IntSize(size, size))
			assertThat(firstChildContainerNode.position).isEqualTo(IntOffset.Zero)
			assertThat(secondChildContainerNode.size).isEqualTo(IntSize(size, size))
			assertThat(secondChildContainerNode.position).isEqualTo(
				IntOffset(
					size,
					(rootHeight - size) / 2,
				),
			)
			assertThat(thirdChildContainerNode.size).isEqualTo(IntSize(size, size))
			assertThat(thirdChildContainerNode.position).isEqualTo(IntOffset(size * 2, rootHeight - size))
		}
	}
	// endregion

	// region Cross axis alignment tests in Column
	@Test fun testColumn_withStretchCrossAxisAlignment() = runTest {
		runMosaicTest(NodeSnapshots) {
			val size = 50

			setContent {
				Column(modifier = Modifier.width(size * 2)) {
					Container(modifier = Modifier.fillMaxWidth(), width = size, height = size)
					Container(modifier = Modifier.fillMaxWidth(), width = size * 2, height = size * 2)
				}
			}

			val rootNode = awaitSnapshot()
			val rootWidth = rootNode.width

			val firstChildContainerNode = rootNode.children[0].children[0]
			val secondChildContainerNode = rootNode.children[0].children[1]

			assertThat(firstChildContainerNode.size).isEqualTo(IntSize(rootWidth, size))
			assertThat(firstChildContainerNode.position).isEqualTo(IntOffset.Zero)
			assertThat(secondChildContainerNode.size).isEqualTo(IntSize(rootWidth, size * 2))
			assertThat(secondChildContainerNode.position).isEqualTo(IntOffset(0, size))
		}
	}

	@Test fun testColumn_withGravityModifier() = runTest {
		runMosaicTest(NodeSnapshots) {
			val size = 50

			setContent {
				Column(Modifier.fillMaxWidth()) {
					Container(modifier = Modifier.align(Alignment.Start), width = size, height = size)
					Container(
						modifier = Modifier.align(Alignment.CenterHorizontally),
						width = size,
						height = size,
					)
					Container(modifier = Modifier.align(Alignment.End), width = size, height = size)
				}
			}

			val rootNode = awaitSnapshot()
			val rootWidth = rootNode.width

			val firstChildContainerNode = rootNode.children[0].children[0]
			val secondChildContainerNode = rootNode.children[0].children[1]
			val thirdChildContainerNode = rootNode.children[0].children[2]

			assertThat(firstChildContainerNode.size).isEqualTo(IntSize(size, size))
			assertThat(firstChildContainerNode.position).isEqualTo(IntOffset.Zero)
			assertThat(secondChildContainerNode.size).isEqualTo(IntSize(size, size))
			assertThat(secondChildContainerNode.position).isEqualTo(
				IntOffset(
					(rootWidth - size) / 2,
					size,
				),
			)
			assertThat(thirdChildContainerNode.size).isEqualTo(IntSize(size, size))
			assertThat(thirdChildContainerNode.position).isEqualTo(IntOffset(rootWidth - size, size * 2))
		}
	}

	@Test fun testColumn_withGravityModifier_andGravityParameter() = runTest {
		runMosaicTest(NodeSnapshots) {
			val size = 50

			setContent {
				Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
					Container(modifier = Modifier.align(Alignment.Start), width = size, height = size)
					Container(width = size, height = size)
					Container(modifier = Modifier.align(Alignment.End), width = size, height = size)
				}
			}

			val rootNode = awaitSnapshot()
			val rootWidth = rootNode.width

			val firstChildContainerNode = rootNode.children[0].children[0]
			val secondChildContainerNode = rootNode.children[0].children[1]
			val thirdChildContainerNode = rootNode.children[0].children[2]

			assertThat(firstChildContainerNode.size).isEqualTo(IntSize(size, size))
			assertThat(firstChildContainerNode.position).isEqualTo(IntOffset.Zero)
			assertThat(secondChildContainerNode.size).isEqualTo(IntSize(size, size))
			assertThat(secondChildContainerNode.position).isEqualTo(IntOffset((rootWidth - size) / 2, size))
			assertThat(thirdChildContainerNode.size).isEqualTo(IntSize(size, size))
			assertThat(thirdChildContainerNode.position).isEqualTo(IntOffset(rootWidth - size, size * 2))
		}
	}
	// endregion

	// region Size tests in Row
	@Test fun testRow_expandedWidth_withExpandedModifier() = runTest {
		runMosaicTest(NodeSnapshots) {
			val size = 50

			setContent {
				Center {
					Row(Modifier.fillMaxWidth()) {
						Spacer(Modifier.size(width = size, height = size))
						Spacer(Modifier.size(width = (size * 2), height = (size * 2)))
					}
				}
			}

			val rootNode = awaitSnapshot()
			val rowNode = rootNode.children[0]

			assertThat(rootNode.width).isEqualTo(rowNode.width)
		}
	}

	@Test fun testRow_wrappedWidth_withNoWeightChildren() = runTest {
		runMosaicTest(NodeSnapshots) {
			val size = 50

			setContent {
				Center {
					Row {
						Spacer(Modifier.size(width = size, height = size))
						Spacer(Modifier.size(width = (size * 2), height = (size * 2)))
					}
				}
			}

			val rootNode = awaitSnapshot()
			val rowNode = rootNode.children[0]

			assertThat(rowNode.width).isEqualTo(size * 3)
		}
	}

	@Test fun testRow_expandedWidth_withWeightChildren() = runTest {
		runMosaicTest(NodeSnapshots) {
			val size = 50

			setContent {
				Center {
					Row {
						Container(modifier = Modifier.weight(1.0f), width = size, height = size)
						Container(width = size * 2, height = size * 2)
					}
				}
			}

			val rootNode = awaitSnapshot()
			val rowNode = rootNode.children[0]

			assertThat(rowNode.width).isEqualTo(rootNode.width)
		}
	}

	@Test fun testRow_withMaxCrossAxisSize() = runTest {
		runMosaicTest(NodeSnapshots) {
			val size = 50

			setContent {
				Center {
					Row(Modifier.fillMaxHeight()) {
						Spacer(Modifier.size(width = size, height = size))
						Spacer(Modifier.size(width = (size * 2), height = (size * 2)))
					}
				}
			}

			val rootNode = awaitSnapshot()
			val rowNode = rootNode.children[0]

			assertThat(rowNode.height).isEqualTo(rootNode.height)
		}
	}

	@Test fun testRow_withMinCrossAxisSize() = runTest {
		runMosaicTest(NodeSnapshots) {
			val size = 50

			setContent {
				Center {
					Row {
						Spacer(Modifier.size(width = size, height = size))
						Spacer(Modifier.size(width = (size * 2), height = (size * 2)))
					}
				}
			}

			val rootNode = awaitSnapshot()
			val rowNode = rootNode.children[0]

			assertThat(rowNode.height).isEqualTo(size * 2)
		}
	}

	@Test fun testRow_withExpandedModifier_respectsMaxWidthConstraint() = runTest {
		runMosaicTest(NodeSnapshots) {
			val size = 50
			val rowWidth = 250

			setContent {
				Center {
					ConstrainedBox(constraints = Constraints(maxWidth = rowWidth)) {
						Row(Modifier.fillMaxWidth()) {
							Spacer(Modifier.size(width = size, height = size))
							Spacer(Modifier.size(width = size * 2, height = size * 2))
						}
					}
				}
			}

			val rootNode = awaitSnapshot()
			val rowNode = rootNode.children[0].children[0]

			assertThat(rowNode.width).isEqualTo(rootNode.width)
		}
	}

	@Test fun testRow_withChildrenWithWeight_respectsMaxWidthConstraint() = runTest {
		runMosaicTest(NodeSnapshots) {
			val size = 50
			val rowWidth = 250

			setContent {
				Center {
					ConstrainedBox(constraints = Constraints(maxWidth = rowWidth)) {
						Row {
							Container(modifier = Modifier.weight(1.0f), width = size, height = size)
							Container(width = size * 2, height = size * 2)
						}
					}
				}
			}

			val rootNode = awaitSnapshot()
			val rowNode = rootNode.children[0].children[0]

			assertThat(rowNode.width).isEqualTo(min(rootNode.width, rowWidth))
		}
	}

	@Test fun testRow_withNoWeightChildren_respectsMinWidthConstraint() = runTest {
		runMosaicTest(NodeSnapshots) {
			val size = 50
			val rowWidth = 250

			setContent {
				Center {
					ConstrainedBox(constraints = Constraints(minWidth = rowWidth)) {
						Row {
							Spacer(Modifier.size(width = size, height = size))
							Spacer(Modifier.size(width = size * 2, height = size * 2))
						}
					}
				}
			}

			val rootNode = awaitSnapshot()
			val rowNode = rootNode.children[0].children[0]

			assertThat(rowNode.width).isEqualTo(rowWidth)
		}
	}

	@Test fun testRow_withMaxCrossAxisSize_respectsMaxHeightConstraint() = runTest {
		runMosaicTest(NodeSnapshots) {
			val size = 50
			val rowHeight = 250

			setContent {
				Center {
					ConstrainedBox(constraints = Constraints(maxHeight = rowHeight)) {
						Row(Modifier.fillMaxHeight()) {
							Spacer(Modifier.size(width = size, height = size))
							Spacer(Modifier.size(width = size * 2, height = size * 2))
						}
					}
				}
			}

			val rootNode = awaitSnapshot()
			val rowNode = rootNode.children[0].children[0]

			assertThat(rowNode.height).isEqualTo(min(rootNode.height, rowHeight))
		}
	}

	@Test fun testRow_withMinCrossAxisSize_respectsMinHeightConstraint() = runTest {
		runMosaicTest(NodeSnapshots) {
			val size = 50
			val rowHeight = 150

			setContent {
				Center {
					ConstrainedBox(constraints = Constraints(minHeight = rowHeight)) {
						Row {
							Spacer(Modifier.size(width = size, height = size))
							Spacer(Modifier.size(width = size * 2, height = size * 2))
						}
					}
				}
			}

			val rootNode = awaitSnapshot()
			val rowNode = rootNode.children[0].children[0]

			assertThat(rowNode.height).isEqualTo(rowHeight)
		}
	}

	@Test fun testRow_protectsAgainstOverflow() = runTest {
		runMosaicTest {
			val rowMinWidth = 0
			val counter = Holder(3)

			setContent {
				WithInfiniteConstraints {
					ConstrainedBox(Constraints(minWidth = rowMinWidth)) {
						Row(horizontalArrangement = Arrangement.spacedBy(2)) {
							Layout(
								content = {},
								measurePolicy = { _, constraints ->
									assertThat(constraints).isEqualTo(Constraints())
									layout(Constraints.Infinity, 100) {
										counter.value--
									}
								},
							)
							Box(modifier = Modifier.weight(1.0f, true)) {
								counter.value--
							}

							Box(modifier = Modifier.weight(0.000001f, true)) {
								counter.value--
							}
						}
					}
				}
			}

			assertThat(counter.value).isZero()
		}
	}

	@Test fun testRow_doesNotExpand_whenWeightChildrenDoNotFill() = runTest {
		runMosaicTest(NodeSnapshots) {
			val size = 10

			setContent {
				Row(Modifier.sizeIn(maxWidth = 100, maxHeight = 100)) {
					Box(Modifier.weight(1.0f, false).size(size))
				}
			}

			val rootNode = awaitSnapshot()
			assertThat(rootNode.width).isEqualTo(size)
		}
	}

	@Test fun testRow_includesSpacing_withWeightChildren() = runTest {
		runMosaicTest(NodeSnapshots) {
			val rowWidth = 40
			val space = 8

			setContent {
				Row(
					modifier = Modifier.widthIn(max = rowWidth),
					horizontalArrangement = Arrangement.spacedBy(space),
				) {
					Box(Modifier.weight(1.0f))
					Box(Modifier.weight(1.0f))
				}
			}

			val rootNode = awaitSnapshot()
			val firstChildBoxNode = rootNode.children[0].children[0]
			val secondChildBoxNode = rootNode.children[0].children[1]

			assertThat(firstChildBoxNode.width).isEqualTo((rowWidth - space) / 2)
			assertThat(firstChildBoxNode.x).isEqualTo(0)
			assertThat(secondChildBoxNode.width).isEqualTo((rowWidth - space) / 2)
			assertThat(secondChildBoxNode.x).isEqualTo((rowWidth - space) / 2 + space)
		}
	}
	// endregion

	// region Size tests in Column
	@Test fun testColumn_expandedHeight_withExpandedModifier() = runTest {
		runMosaicTest(NodeSnapshots) {
			val size = 50

			setContent {
				Center {
					Column(Modifier.fillMaxHeight()) {
						Spacer(Modifier.size(width = size, height = size))
						Spacer(Modifier.size(width = (size * 2), height = (size * 2)))
					}
				}
			}

			val rootNode = awaitSnapshot()
			val columnNode = rootNode.children[0]

			assertThat(columnNode.height).isEqualTo(rootNode.height)
		}
	}

	@Test fun testColumn_wrappedHeight_withNoChildrenWithWeight() = runTest {
		runMosaicTest(NodeSnapshots) {
			val size = 50

			setContent {
				Center {
					Column {
						Spacer(Modifier.size(width = size, height = size))
						Spacer(Modifier.size(width = (size * 2), height = (size * 2)))
					}
				}
			}

			val rootNode = awaitSnapshot()
			val columnNode = rootNode.children[0]

			assertThat(columnNode.height).isEqualTo(size * 3)
		}
	}

	@Test fun testColumn_expandedHeight_withWeightChildren() = runTest {
		runMosaicTest(NodeSnapshots) {
			val size = 50

			setContent {
				Center {
					Column {
						Container(modifier = Modifier.weight(1.0f), width = size, height = size)
						Container(width = size * 2, height = size * 2)
					}
				}
			}

			val rootNode = awaitSnapshot()
			val columnNode = rootNode.children[0]

			assertThat(columnNode.height).isEqualTo(rootNode.height)
		}
	}

	@Test fun testColumn_withMaxCrossAxisSize() = runTest {
		runMosaicTest(NodeSnapshots) {
			val size = 50

			setContent {
				Center {
					Column(Modifier.fillMaxWidth()) {
						Spacer(Modifier.size(width = size, height = size))
						Spacer(Modifier.size(width = (size * 2), height = (size * 2)))
					}
				}
			}

			val rootNode = awaitSnapshot()
			val columnNode = rootNode.children[0]

			assertThat(columnNode.width).isEqualTo(rootNode.width)
		}
	}

	@Test fun testColumn_withMinCrossAxisSize() = runTest {
		runMosaicTest(NodeSnapshots) {
			val size = 50

			setContent {
				Center {
					Column {
						Spacer(Modifier.size(width = size, height = size))
						Spacer(Modifier.size(width = (size * 2), height = (size * 2)))
					}
				}
			}

			val rootNode = awaitSnapshot()
			val columnNode = rootNode.children[0]

			assertThat(columnNode.width).isEqualTo(size * 2)
		}
	}

	@Test fun testColumn_withExpandedModifier_respectsMaxHeightConstraint() = runTest {
		runMosaicTest(NodeSnapshots) {
			val size = 50
			val columnHeight = 250

			setContent {
				Center {
					ConstrainedBox(constraints = Constraints(maxHeight = columnHeight)) {
						Column(Modifier.fillMaxHeight()) {
							Spacer(Modifier.size(width = size, height = size))
							Spacer(Modifier.size(width = size * 2, height = size * 2))
						}
					}
				}
			}

			val rootNode = awaitSnapshot()
			val columnNode = rootNode.children[0]

			assertThat(columnNode.height).isEqualTo(min(rootNode.height, columnHeight))
		}
	}

	@Test fun testColumn_withWeightChildren_respectsMaxHeightConstraint() = runTest {
		runMosaicTest(NodeSnapshots) {
			val size = 50
			val columnHeight = 250

			setContent {
				Center {
					ConstrainedBox(constraints = Constraints(maxHeight = columnHeight)) {
						Column {
							Container(modifier = Modifier.weight(1.0f), width = size, height = size)
							Container(width = size * 2, height = size * 2)
						}
					}
				}
			}

			val rootNode = awaitSnapshot()
			val columnNode = rootNode.children[0].children[0]

			assertThat(columnNode.height).isEqualTo(min(rootNode.height, columnHeight))
		}
	}

	@Test fun testColumn_withChildren_respectsMinHeightConstraint() = runTest {
		runMosaicTest(NodeSnapshots) {
			val size = 50
			val columnHeight = 250

			setContent {
				Center {
					ConstrainedBox(constraints = Constraints(minHeight = columnHeight)) {
						Column {
							Spacer(Modifier.size(width = size, height = size))
							Spacer(Modifier.size(width = size * 2, height = size * 2))
						}
					}
				}
			}

			val rootNode = awaitSnapshot()
			val columnNode = rootNode.children[0].children[0]

			assertThat(columnNode.height).isEqualTo(columnHeight)
		}
	}

	@Test fun testColumn_withMaxCrossAxisSize_respectsMaxWidthConstraint() = runTest {
		runMosaicTest(NodeSnapshots) {
			val size = 50
			val columnWidth = 250

			setContent {
				Center {
					ConstrainedBox(constraints = Constraints(maxWidth = columnWidth)) {
						Column(Modifier.fillMaxWidth()) {
							Spacer(Modifier.size(width = size, height = size))
							Spacer(Modifier.size(width = size * 2, height = size * 2))
						}
					}
				}
			}

			val rootNode = awaitSnapshot()
			val columnNode = rootNode.children[0].children[0]

			assertThat(columnNode.width).isEqualTo(min(rootNode.width, columnWidth))
		}
	}

	@Test fun testColumn_withMinCrossAxisSize_respectsMinWidthConstraint() = runTest {
		runMosaicTest(NodeSnapshots) {
			val size = 50
			val columnWidth = 150

			setContent {
				Center {
					ConstrainedBox(constraints = Constraints(minWidth = columnWidth)) {
						Column {
							Spacer(Modifier.size(width = size, height = size))
							Spacer(Modifier.size(width = size * 2, height = size * 2))
						}
					}
				}
			}

			val rootNode = awaitSnapshot()
			val columnNode = rootNode.children[0].children[0]

			assertThat(columnNode.width).isEqualTo(columnWidth)
		}
	}

	@Test fun testColumn_doesNotExpand_whenWeightChildrenDoNotFill() = runTest {
		runMosaicTest(NodeSnapshots) {
			val size = 10

			setContent {
				Column(Modifier.sizeIn(maxWidth = 100, maxHeight = 100)) {
					Box(Modifier.weight(1.0f, false).size(size))
				}
			}

			val rootNode = awaitSnapshot()
			assertThat(rootNode.height).isEqualTo(size)
		}
	}

	@Test fun testColumn_includesSpacing_withWeightChildren() = runTest {
		runMosaicTest(NodeSnapshots) {
			val columnHeight = 40
			val space = 8

			setContent {
				Column(
					modifier = Modifier.height(columnHeight),
					verticalArrangement = Arrangement.spacedBy(space),
				) {
					Box(Modifier.weight(1.0f))
					Box(Modifier.weight(1.0f))
				}
			}

			val rootNode = awaitSnapshot()
			val firstChildBoxNode = rootNode.children[0].children[0]
			val secondChildBoxNode = rootNode.children[0].children[1]

			assertThat(firstChildBoxNode.height).isEqualTo((columnHeight - space) / 2)
			assertThat(firstChildBoxNode.y).isEqualTo(0)
			assertThat(secondChildBoxNode.height).isEqualTo((columnHeight - space) / 2)
			assertThat(secondChildBoxNode.y).isEqualTo((columnHeight - space) / 2 + space)
		}
	}
	// endregion

	// region Main axis alignment tests in Row
	@Test fun testRow_withStartArrangement() = runTest {
		runMosaicTest(NodeSnapshots) {
			val size = 50

			setContent {
				Center {
					Row(Modifier.fillMaxWidth()) {
						Container(width = size, height = size)
						Container(width = size, height = size)
						Container(width = size, height = size)
					}
				}
			}

			val rootNode = awaitSnapshot()
			val firstChildContainerNode = rootNode.children[0].children[0].children[0]
			val secondChildContainerNode = rootNode.children[0].children[0].children[1]
			val thirdChildContainerNode = rootNode.children[0].children[0].children[2]

			assertThat(firstChildContainerNode.position).isEqualTo(IntOffset.Zero)
			assertThat(secondChildContainerNode.position).isEqualTo(IntOffset(size, 0))
			assertThat(thirdChildContainerNode.position).isEqualTo(IntOffset(size * 2, 0))
		}
	}

	@Test fun testRow_withEndArrangement() = runTest {
		runMosaicTest(NodeSnapshots) {
			val size = 50

			setContent {
				Center {
					Row(
						modifier = Modifier.fillMaxWidth(),
						horizontalArrangement = Arrangement.End,
					) {
						Container(width = size, height = size)
						Container(width = size, height = size)
						Container(width = size, height = size)
					}
				}
			}

			val rootNode = awaitSnapshot()
			val firstChildContainerNode = rootNode.children[0].children[0].children[0]
			val secondChildContainerNode = rootNode.children[0].children[0].children[1]
			val thirdChildContainerNode = rootNode.children[0].children[0].children[2]

			assertThat(firstChildContainerNode.position).isEqualTo(IntOffset(rootNode.width - size * 3, 0))
			assertThat(secondChildContainerNode.position).isEqualTo(IntOffset(rootNode.width - size * 2, 0))
			assertThat(thirdChildContainerNode.position).isEqualTo(IntOffset(rootNode.width - size, 0))
		}
	}

	@Test fun testRow_withCenterArrangement() = runTest {
		runMosaicTest(NodeSnapshots) {
			val size = 50

			setContent {
				Center {
					Row(
						modifier = Modifier.fillMaxWidth(),
						horizontalArrangement = Arrangement.Center,
					) {
						Container(width = size, height = size)
						Container(width = size, height = size)
						Container(width = size, height = size)
					}
				}
			}

			val rootNode = awaitSnapshot()
			val extraSpace = rootNode.width - size * 3

			val firstChildContainerNode = rootNode.children[0].children[0].children[0]
			val secondChildContainerNode = rootNode.children[0].children[0].children[1]
			val thirdChildContainerNode = rootNode.children[0].children[0].children[2]

			assertThat(firstChildContainerNode.position).isEqualTo(IntOffset(extraSpace / 2, 0))
			assertThat(secondChildContainerNode.position).isEqualTo(IntOffset((extraSpace / 2) + size, 0))
			assertThat(thirdChildContainerNode.position).isEqualTo(
				IntOffset(
					(extraSpace / 2) + size * 2,
					0,
				),
			)
		}
	}

	@Test fun testRow_withSpaceEvenlyArrangement() = runTest {
		runMosaicTest(NodeSnapshots) {
			val size = 50

			setContent {
				Center {
					Row(
						modifier = Modifier.fillMaxWidth(),
						horizontalArrangement = Arrangement.SpaceEvenly,
					) {
						Container(width = size, height = size)
						Container(width = size, height = size)
						Container(width = size, height = size)
					}
				}
			}

			val rootNode = awaitSnapshot()
			val gap = (rootNode.width - size * 3) / 4

			val firstChildContainerNode = rootNode.children[0].children[0].children[0]
			val secondChildContainerNode = rootNode.children[0].children[0].children[1]
			val thirdChildContainerNode = rootNode.children[0].children[0].children[2]

			assertThat(firstChildContainerNode.position).isEqualTo(IntOffset(gap, 0))
			assertThat(secondChildContainerNode.position).isEqualTo(IntOffset(size + gap * 2, 0))
			assertThat(thirdChildContainerNode.position).isEqualTo(IntOffset(size * 2 + gap * 3, 0))
		}
	}

	@Test fun testRow_withSpaceBetweenArrangement_singleItem() = runTest {
		runMosaicTest(NodeSnapshots) {
			val size = 50

			setContent {
				Center {
					Row(
						modifier = Modifier.fillMaxWidth(),
						horizontalArrangement = Arrangement.SpaceBetween,
					) {
						Container(width = size, height = size)
					}
				}
			}

			val rootNode = awaitSnapshot()
			val childContainerNode = rootNode.children[0].children[0]

			assertThat(childContainerNode.position).isEqualTo(IntOffset.Zero)
		}
	}

	@Test fun testRow_withSpaceBetweenArrangement_multipleItems() = runTest {
		runMosaicTest(NodeSnapshots) {
			val size = 50

			setContent {
				Center {
					Row(
						modifier = Modifier.fillMaxWidth(),
						horizontalArrangement = Arrangement.SpaceBetween,
					) {
						Container(width = size, height = size)
						Container(width = size, height = size)
						Container(width = size, height = size)
					}
				}
			}

			val rootNode = awaitSnapshot()
			val gap = (rootNode.width - size * 3) / 2

			val firstChildContainerNode = rootNode.children[0].children[0].children[0]
			val secondChildContainerNode = rootNode.children[0].children[0].children[1]
			val thirdChildContainerNode = rootNode.children[0].children[0].children[2]

			assertThat(firstChildContainerNode.position).isEqualTo(IntOffset.Zero)
			assertThat(secondChildContainerNode.position).isEqualTo(IntOffset(gap + size, 0))
			assertThat(thirdChildContainerNode.position).isEqualTo(IntOffset(gap * 2 + size * 2, 0))
		}
	}

	@Test fun testRow_withSpaceAroundArrangement() = runTest {
		runMosaicTest(NodeSnapshots) {
			val size = 50

			setContent {
				Center {
					Row(
						modifier = Modifier.fillMaxWidth(),
						horizontalArrangement = Arrangement.SpaceAround,
					) {
						Container(width = size, height = size)
						Container(width = size, height = size)
						Container(width = size, height = size)
					}
				}
			}

			val rootNode = awaitSnapshot()
			val gap = (rootNode.width - size * 3) / 3

			val firstChildContainerNode = rootNode.children[0].children[0].children[0]
			val secondChildContainerNode = rootNode.children[0].children[0].children[1]
			val thirdChildContainerNode = rootNode.children[0].children[0].children[2]

			assertThat(firstChildContainerNode.position).isEqualTo(IntOffset(gap / 2, 0))
			assertThat(secondChildContainerNode.position).isEqualTo(IntOffset((gap * 3 / 2) + size, 0))
			assertThat(thirdChildContainerNode.position).isEqualTo(IntOffset((gap * 5 / 2) + size * 2, 0))
		}
	}

	@Test fun testRow_withSpacedByArrangement() = runTest {
		runMosaicTest(NodeSnapshots) {
			val space = 10
			val size = 20

			setContent {
				Column {
					Row(horizontalArrangement = Arrangement.spacedBy(space)) {
						Box(Modifier.requiredSize(size))
						Box(Modifier.requiredSize(size))
					}
				}
			}

			val rootNode = awaitSnapshot()
			val firstChildBoxNode = rootNode.children[0].children[0].children[0]
			val secondChildBoxNode = rootNode.children[0].children[0].children[1]

			assertThat(firstChildBoxNode.x).isEqualTo(0)
			assertThat(secondChildBoxNode.x).isEqualTo(size + space)
		}
	}

	@Test fun testRow_withSpacedByAlignedArrangement() = runTest {
		runMosaicTest(NodeSnapshots) {
			val space = 10
			val size = 20
			val rowSize = 50

			setContent {
				Column {
					Row(
						modifier = Modifier.requiredSize(rowSize),
						horizontalArrangement = Arrangement.spacedBy(space, Alignment.End),
					) {
						Box(Modifier.requiredSize(size))
						Box(Modifier.requiredSize(size))
					}
				}
			}

			val rootNode = awaitSnapshot()
			val rowNode = rootNode.children[0]

			val firstChildBoxNode = rowNode.children[0].children[0]
			val secondChildBoxNode = rowNode.children[0].children[1]

			assertThat(rowNode.width).isEqualTo(rowSize)
			assertThat(firstChildBoxNode.x).isEqualTo(rowSize - space - size * 2)
			assertThat(secondChildBoxNode.x).isEqualTo(rowSize - size)
		}
	}

	@Test fun testRow_withSpacedByArrangement_insufficientSpace() = runTest {
		runMosaicTest(NodeSnapshots) {
			val space = 15
			val size = 20
			val rowSize = 50

			setContent {
				Column {
					Row(
						modifier = Modifier.requiredSize(rowSize),
						horizontalArrangement = Arrangement.spacedBy(space),
					) {
						Box(Modifier.size(size))
						Box(Modifier.size(size))
						Box(Modifier.size(size))
					}
				}
			}

			val rootNode = awaitSnapshot()
			val rowNode = rootNode.children[0].children[0]

			val firstChildBoxNode = rowNode.children[0]
			val secondChildBoxNode = rowNode.children[1]
			val thirdChildBoxNode = rowNode.children[2]

			assertThat(rowNode.width).isEqualTo(rowSize)
			assertThat(firstChildBoxNode.width).isEqualTo(size)
			assertThat(firstChildBoxNode.x).isEqualTo(0)
			assertThat(secondChildBoxNode.width).isEqualTo(rowSize - space - size)
			assertThat(secondChildBoxNode.x).isEqualTo(size + space)
			assertThat(thirdChildBoxNode.width).isEqualTo(0)
			assertThat(thirdChildBoxNode.x).isEqualTo(rowSize)
		}
	}

	@Test fun testRow_withAlignedArrangement() = runTest {
		runMosaicTest(NodeSnapshots) {
			val size = 20
			val rowSize = 50

			setContent {
				Column {
					Row(
						modifier = Modifier.requiredSize(rowSize),
						horizontalArrangement = Arrangement.aligned(Alignment.End),
					) {
						Box(Modifier.size(size))
						Box(Modifier.size(size))
					}
				}
			}

			val rootNode = awaitSnapshot()
			val rowNode = rootNode.children[0]

			val firstChildBoxNode = rowNode.children[0].children[0]
			val secondChildBoxNode = rowNode.children[0].children[1]

			assertThat(rowNode.width).isEqualTo(rowSize)
			assertThat(firstChildBoxNode.width).isEqualTo(size)
			assertThat(firstChildBoxNode.x).isEqualTo(rowSize - size * 2)
			assertThat(secondChildBoxNode.width).isEqualTo(size)
			assertThat(secondChildBoxNode.x).isEqualTo(rowSize - size)
		}
	}
	// endregion

	// region Main axis alignment tests in Column
	@Test fun testColumn_withTopArrangement() = runTest {
		runMosaicTest(NodeSnapshots) {
			val size = 50

			setContent {
				Center {
					Column(Modifier.fillMaxHeight()) {
						Container(width = size, height = size)
						Container(width = size, height = size)
						Container(width = size, height = size)
					}
				}
			}

			val rootNode = awaitSnapshot()
			val firstChildContainerNode = rootNode.children[0].children[0].children[0]
			val secondChildContainerNode = rootNode.children[0].children[0].children[1]
			val thirdChildContainerNode = rootNode.children[0].children[0].children[2]

			assertThat(firstChildContainerNode.position).isEqualTo(IntOffset.Zero)
			assertThat(secondChildContainerNode.position).isEqualTo(IntOffset(0, size))
			assertThat(thirdChildContainerNode.position).isEqualTo(IntOffset(0, size * 2))
		}
	}

	@Test fun testColumn_withBottomArrangement() = runTest {
		runMosaicTest(NodeSnapshots) {
			val size = 50

			setContent {
				Center {
					Column(
						modifier = Modifier.fillMaxHeight(),
						verticalArrangement = Arrangement.Bottom,
					) {
						Container(width = size, height = size)
						Container(width = size, height = size)
						Container(width = size, height = size)
					}
				}
			}

			val rootNode = awaitSnapshot()
			val rootHeight = rootNode.height

			val firstChildContainerNode = rootNode.children[0].children[0].children[0]
			val secondChildContainerNode = rootNode.children[0].children[0].children[1]
			val thirdChildContainerNode = rootNode.children[0].children[0].children[2]

			assertThat(firstChildContainerNode.position).isEqualTo(IntOffset(0, rootHeight - size * 3))
			assertThat(secondChildContainerNode.position).isEqualTo(IntOffset(0, rootHeight - size * 2))
			assertThat(thirdChildContainerNode.position).isEqualTo(IntOffset(0, rootHeight - size))
		}
	}

	@Test fun testColumn_withCenterArrangement() = runTest {
		runMosaicTest(NodeSnapshots) {
			val size = 50

			setContent {
				Center {
					Column(
						modifier = Modifier.fillMaxHeight(),
						verticalArrangement = Arrangement.Center,
					) {
						Container(width = size, height = size)
						Container(width = size, height = size)
						Container(width = size, height = size)
					}
				}
			}

			val rootNode = awaitSnapshot()
			val extraSpace = rootNode.height - size * 3

			val firstChildContainerNode = rootNode.children[0].children[0].children[0]
			val secondChildContainerNode = rootNode.children[0].children[0].children[1]
			val thirdChildContainerNode = rootNode.children[0].children[0].children[2]

			assertThat(firstChildContainerNode.position).isEqualTo(IntOffset(0, extraSpace / 2))
			assertThat(secondChildContainerNode.position).isEqualTo(IntOffset(0, (extraSpace / 2) + size))
			assertThat(thirdChildContainerNode.position).isEqualTo(
				IntOffset(
					0,
					(extraSpace / 2) + size * 2,
				),
			)
		}
	}

	@Test fun testColumn_withSpaceEvenlyArrangement() = runTest {
		runMosaicTest(NodeSnapshots) {
			val size = 50

			setContent {
				Center {
					Column(
						modifier = Modifier.fillMaxHeight(),
						verticalArrangement = Arrangement.SpaceEvenly,
					) {
						Container(width = size, height = size)
						Container(width = size, height = size)
						Container(width = size, height = size)
					}
				}
			}

			val rootNode = awaitSnapshot()
			val gap = (rootNode.height - size * 3) / 4

			val firstChildContainerNode = rootNode.children[0].children[0].children[0]
			val secondChildContainerNode = rootNode.children[0].children[0].children[1]
			val thirdChildContainerNode = rootNode.children[0].children[0].children[2]

			assertThat(firstChildContainerNode.position).isEqualTo(IntOffset(0, gap))
			assertThat(secondChildContainerNode.position).isEqualTo(IntOffset(0, size + gap * 2))
			assertThat(thirdChildContainerNode.position).isEqualTo(IntOffset(0, size * 2 + gap * 3))
		}
	}

	@Test fun testColumn_withSpaceBetweenArrangement() = runTest {
		runMosaicTest(NodeSnapshots) {
			val size = 50

			setContent {
				Center {
					Column(
						modifier = Modifier.fillMaxHeight(),
						verticalArrangement = Arrangement.SpaceBetween,
					) {
						Container(width = size, height = size)
						Container(width = size, height = size)
						Container(width = size, height = size)
					}
				}
			}

			val rootNode = awaitSnapshot()
			val gap = (rootNode.height - size * 3) / 2

			val firstChildContainerNode = rootNode.children[0].children[0].children[0]
			val secondChildContainerNode = rootNode.children[0].children[0].children[1]
			val thirdChildContainerNode = rootNode.children[0].children[0].children[2]

			assertThat(firstChildContainerNode.position).isEqualTo(IntOffset.Zero)
			assertThat(secondChildContainerNode.position).isEqualTo(IntOffset(0, size + gap))
			assertThat(thirdChildContainerNode.position).isEqualTo(IntOffset(0, size * 2 + gap * 2))
		}
	}

	@Test fun testColumn_withSpaceAroundArrangement() = runTest {
		runMosaicTest(NodeSnapshots) {
			val size = 50

			setContent {
				Center {
					Column(
						modifier = Modifier.fillMaxHeight(),
						verticalArrangement = Arrangement.SpaceAround,
					) {
						Container(width = size, height = size)
						Container(width = size, height = size)
						Container(width = size, height = size)
					}
				}
			}

			val rootNode = awaitSnapshot()
			val gap = (rootNode.height - size * 3) / 3

			val firstChildContainerNode = rootNode.children[0].children[0].children[0]
			val secondChildContainerNode = rootNode.children[0].children[0].children[1]
			val thirdChildContainerNode = rootNode.children[0].children[0].children[2]

			assertThat(firstChildContainerNode.position).isEqualTo(IntOffset(0, gap / 2))
			assertThat(secondChildContainerNode.position).isEqualTo(IntOffset(0, size + (gap * 3 / 2)))
			assertThat(thirdChildContainerNode.position).isEqualTo(IntOffset(0, size * 2 + (gap * 5 / 2)))
		}
	}

	@Test fun testColumn_withSpacedByArrangement() = runTest {
		runMosaicTest(NodeSnapshots) {
			val space = 10
			val size = 20

			setContent {
				Row {
					Column(verticalArrangement = Arrangement.spacedBy(space)) {
						Box(Modifier.requiredSize(size))
						Box(Modifier.requiredSize(size))
					}
				}
			}

			val rootNode = awaitSnapshot()
			val columnNode = rootNode.children[0]

			val firstChildBoxNode = columnNode.children[0].children[0]
			val secondChildBoxNode = columnNode.children[0].children[1]

			assertThat(columnNode.height).isEqualTo(size * 2 + space)
			assertThat(firstChildBoxNode.x).isEqualTo(0)
			assertThat(secondChildBoxNode.y).isEqualTo(size + space)
		}
	}

	@Test fun testColumn_withSpacedByAlignedArrangement() = runTest {
		runMosaicTest(NodeSnapshots) {
			val space = 10
			val size = 20
			val columnSize = 50

			setContent {
				Row {
					Column(
						modifier = Modifier.requiredSize(columnSize),
						verticalArrangement = Arrangement.spacedBy(space, Alignment.Bottom),
					) {
						Box(Modifier.requiredSize(size))
						Box(Modifier.requiredSize(size))
					}
				}
			}

			val rootNode = awaitSnapshot()
			val columnNode = rootNode.children[0]

			val firstChildBoxNode = columnNode.children[0].children[0]
			val secondChildBoxNode = columnNode.children[0].children[1]

			assertThat(columnNode.height).isEqualTo(columnSize)
			assertThat(firstChildBoxNode.y).isEqualTo(columnSize - space - size * 2)
			assertThat(secondChildBoxNode.y).isEqualTo(columnSize - size)
		}
	}

	@Test fun testColumn_withSpacedByArrangement_insufficientSpace() = runTest {
		runMosaicTest(NodeSnapshots) {
			val space = 15
			val size = 20
			val columnSize = 50

			setContent {
				Row {
					Column(
						modifier = Modifier.requiredSize(columnSize),
						verticalArrangement = Arrangement.spacedBy(space),
					) {
						Box(Modifier.size(size))
						Box(Modifier.size(size))
						Box(Modifier.size(size))
					}
				}
			}

			val rootNode = awaitSnapshot()
			val columnNode = rootNode.children[0].children[0]

			val firstChildBoxNode = columnNode.children[0]
			val secondChildBoxNode = columnNode.children[1]
			val thirdChildBoxNode = columnNode.children[2]

			assertThat(columnNode.height).isEqualTo(columnSize)
			assertThat(firstChildBoxNode.height).isEqualTo(size)
			assertThat(firstChildBoxNode.y).isEqualTo(0)
			assertThat(secondChildBoxNode.height).isEqualTo(columnSize - space - size)
			assertThat(secondChildBoxNode.y).isEqualTo(size + space)
			assertThat(thirdChildBoxNode.height).isEqualTo(0)
			assertThat(thirdChildBoxNode.y).isEqualTo(columnSize)
		}
	}

	@Test fun testColumn_withAlignedArrangement() = runTest {
		runMosaicTest(NodeSnapshots) {
			val size = 20
			val columnSize = 50

			setContent {
				Row {
					Column(
						modifier = Modifier.requiredSize(columnSize),
						verticalArrangement = Arrangement.aligned(Alignment.Bottom),
					) {
						Box(Modifier.requiredSize(size))
						Box(Modifier.requiredSize(size))
					}
				}
			}

			val rootNode = awaitSnapshot()
			val columnNode = rootNode.children[0]

			val firstChildBoxNode = columnNode.children[0].children[0]
			val secondChildBoxNode = columnNode.children[0].children[1]

			assertThat(columnNode.height).isEqualTo(columnSize)
			assertThat(firstChildBoxNode.y).isEqualTo(columnSize - size * 2)
			assertThat(secondChildBoxNode.y).isEqualTo(columnSize - size)
		}
	}

	@Test fun testRow_doesNotUseMinConstraintsOnChildren() = runTest {
		runMosaicTest(NodeSnapshots) {
			val size = 50
			val childSize = 30

			setContent {
				Center {
					ConstrainedBox(constraints = Constraints.fixed(size, size)) {
						Row {
							Spacer(Modifier.size(width = childSize, height = childSize))
						}
					}
				}
			}

			val rootNode = awaitSnapshot()
			val spacerNode = rootNode.children[0].children[0].children[0].children[0]

			assertThat(spacerNode.size).isEqualTo(IntSize(childSize, childSize))
		}
	}

	@Test fun testColumn_doesNotUseMinConstraintsOnChildren() = runTest {
		runMosaicTest(NodeSnapshots) {
			val size = 50
			val childSize = 30

			setContent {
				Center {
					ConstrainedBox(constraints = Constraints.fixed(size, size)) {
						Column {
							Spacer(Modifier.size(width = childSize, height = childSize))
						}
					}
				}
			}

			val rootNode = awaitSnapshot()
			val spacerNode = rootNode.children[0].children[0].children[0].children[0]

			assertThat(spacerNode.size).isEqualTo(IntSize(childSize, childSize))
		}
	}
	// endregion

	// region Intrinsic measurement tests
	@Test fun testRow_withNoWeightChildren_hasCorrectIntrinsicMeasurements() = runTest {
		testIntrinsics(
			@Composable {
				Row {
					Container(Modifier.aspectRatio(2.0f))
					ConstrainedBox(Constraints.fixed(50, 40))
				}
			},
			@Composable {
				Row(Modifier.fillMaxWidth()) {
					Container(Modifier.aspectRatio(2.0f))
					ConstrainedBox(Constraints.fixed(50, 40))
				}
			},
			@Composable {
				Row {
					Container(Modifier.aspectRatio(2.0f).align(Alignment.Top))
					ConstrainedBox(
						modifier = Modifier.align(Alignment.CenterVertically),
						constraints = Constraints.fixed(50, 40),
					)
				}
			},
			@Composable {
				Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Start) {
					Container(Modifier.aspectRatio(2.0f))
					ConstrainedBox(Constraints.fixed(50, 40))
				}
			},
			@Composable {
				Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
					Container(Modifier.align(Alignment.CenterVertically).aspectRatio(2.0f))
					ConstrainedBox(
						modifier = Modifier.align(Alignment.CenterVertically),
						constraints = Constraints.fixed(50, 40),
					)
				}
			},
			@Composable {
				Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
					Container(Modifier.align(Alignment.Bottom).aspectRatio(2.0f))
					ConstrainedBox(
						modifier = Modifier.align(Alignment.Bottom),
						constraints = Constraints.fixed(50, 40),
					)
				}
			},
			@Composable {
				Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
					Container(Modifier.fillMaxHeight().aspectRatio(2.0f))
					ConstrainedBox(
						constraints = Constraints.fixed(50, 40),
						modifier = Modifier.fillMaxHeight(),
					)
				}
			},
			@Composable {
				Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
					Container(Modifier.aspectRatio(2.0f))
					ConstrainedBox(Constraints.fixed(50, 40))
				}
			},
			@Composable {
				Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
					Container(Modifier.aspectRatio(2.0f))
					ConstrainedBox(Constraints.fixed(50, 40))
				}
			},
		) { minIntrinsicWidth, minIntrinsicHeight, maxIntrinsicWidth, maxIntrinsicHeight ->
			// Min width
			assertThat(minIntrinsicWidth(0)).isEqualTo(50)
			assertThat(minIntrinsicWidth(25)).isEqualTo(25 * 2 + 50)
			assertThat(minIntrinsicWidth(Constraints.Infinity)).isEqualTo(50)
			// Min height
			assertThat(minIntrinsicHeight(0)).isEqualTo(40)
			assertThat(minIntrinsicHeight(70)).isEqualTo(40)
			assertThat(minIntrinsicHeight(Constraints.Infinity)).isEqualTo(40)
			// Max width
			assertThat(maxIntrinsicWidth(0)).isEqualTo(50)
			assertThat(maxIntrinsicWidth(25)).isEqualTo(25 * 2 + 50)
			assertThat(maxIntrinsicWidth(Constraints.Infinity)).isEqualTo(50)
			// Max height
			assertThat(maxIntrinsicHeight(0)).isEqualTo(40)
			assertThat(maxIntrinsicHeight(70)).isEqualTo(40)
			assertThat(maxIntrinsicHeight(Constraints.Infinity)).isEqualTo(40)
		}
	}

	@Test fun testRow_withWeightChildren_hasCorrectIntrinsicMeasurements() = runTest {
		testIntrinsics(
			@Composable {
				Row {
					ConstrainedBox(
						modifier = Modifier.weight(3.0f),
						constraints = Constraints.fixed(20, 30),
					)
					ConstrainedBox(
						modifier = Modifier.weight(2.0f),
						constraints = Constraints.fixed(30, 40),
					)
					Container(Modifier.aspectRatio(2.0f).weight(2.0f))
					ConstrainedBox(Constraints.fixed(20, 30))
				}
			},
			@Composable {
				Row {
					ConstrainedBox(
						modifier = Modifier.weight(3.0f).align(Alignment.Top),
						constraints = Constraints.fixed(20, 30),
					)
					ConstrainedBox(
						modifier = Modifier.weight(2.0f).align(Alignment.CenterVertically),
						constraints = Constraints.fixed(30, 40),
					)
					Container(Modifier.aspectRatio(2.0f).weight(2.0f))
					ConstrainedBox(
						modifier = Modifier.align(Alignment.Bottom),
						constraints = Constraints.fixed(20, 30),
					)
				}
			},
			@Composable {
				Row(horizontalArrangement = Arrangement.Start) {
					ConstrainedBox(
						constraints = Constraints.fixed(20, 30),
						modifier = Modifier.weight(3.0f),
					)
					ConstrainedBox(
						constraints = Constraints.fixed(30, 40),
						modifier = Modifier.weight(2.0f),
					)
					Container(Modifier.aspectRatio(2.0f).weight(2.0f))
					ConstrainedBox(Constraints.fixed(20, 30))
				}
			},
			@Composable {
				Row(horizontalArrangement = Arrangement.Center) {
					ConstrainedBox(
						modifier = Modifier.weight(3.0f).align(Alignment.CenterVertically),
						constraints = Constraints.fixed(20, 30),
					)
					ConstrainedBox(
						modifier = Modifier.weight(2.0f).align(Alignment.CenterVertically),
						constraints = Constraints.fixed(30, 40),
					)
					Container(Modifier.aspectRatio(2.0f).weight(2.0f).align(Alignment.CenterVertically))
					ConstrainedBox(
						modifier = Modifier.align(Alignment.CenterVertically),
						constraints = Constraints.fixed(20, 30),
					)
				}
			},
			@Composable {
				Row(horizontalArrangement = Arrangement.End) {
					ConstrainedBox(
						modifier = Modifier.weight(3.0f).align(Alignment.Bottom),
						constraints = Constraints.fixed(20, 30),
					)
					ConstrainedBox(
						modifier = Modifier.weight(2.0f).align(Alignment.Bottom),
						constraints = Constraints.fixed(30, 40),
					)
					Container(Modifier.aspectRatio(2.0f).weight(2.0f).align(Alignment.Bottom))
					ConstrainedBox(
						modifier = Modifier.align(Alignment.Bottom),
						constraints = Constraints.fixed(20, 30),
					)
				}
			},
			@Composable {
				Row(horizontalArrangement = Arrangement.SpaceAround) {
					ConstrainedBox(
						modifier = Modifier.weight(3.0f).fillMaxHeight(),
						constraints = Constraints.fixed(20, 30),
					)
					ConstrainedBox(
						modifier = Modifier.weight(2.0f).fillMaxHeight(),
						constraints = Constraints.fixed(30, 40),
					)
					Container(Modifier.aspectRatio(2.0f).weight(2.0f).fillMaxHeight())
					ConstrainedBox(
						modifier = Modifier.fillMaxHeight(),
						constraints = Constraints.fixed(20, 30),
					)
				}
			},
			@Composable {
				Row(horizontalArrangement = Arrangement.SpaceBetween) {
					ConstrainedBox(
						modifier = Modifier.weight(3.0f),
						constraints = Constraints.fixed(20, 30),
					)
					ConstrainedBox(
						modifier = Modifier.weight(2.0f),
						constraints = Constraints.fixed(30, 40),
					)
					Container(Modifier.aspectRatio(2.0f).weight(2.0f))
					ConstrainedBox(Constraints.fixed(20, 30))
				}
			},
			@Composable {
				Row(horizontalArrangement = Arrangement.SpaceEvenly) {
					ConstrainedBox(
						modifier = Modifier.weight(3.0f),
						constraints = Constraints.fixed(20, 30),
					)
					ConstrainedBox(
						modifier = Modifier.weight(2.0f),
						constraints = Constraints.fixed(30, 40),
					)
					Container(Modifier.aspectRatio(2.0f).weight(2.0f))
					ConstrainedBox(Constraints.fixed(20, 30))
				}
			},
		) { minIntrinsicWidth, minIntrinsicHeight, maxIntrinsicWidth, maxIntrinsicHeight ->
			// Min width
			assertThat(minIntrinsicWidth(0)).isEqualTo(30 / 2 * 7 + 20)
			assertThat(minIntrinsicWidth(10)).isEqualTo(30 / 2 * 7 + 20)
			assertThat(minIntrinsicWidth(25)).isEqualTo(25 * 2 / 2 * 7 + 20)
			assertThat(minIntrinsicWidth(Constraints.Infinity)).isEqualTo(30 / 2 * 7 + 20)
			// Min height
			assertThat(minIntrinsicHeight(0)).isEqualTo(40)
			assertThat(minIntrinsicHeight(125)).isEqualTo(40)
			assertThat(minIntrinsicHeight(370)).isEqualTo(50)
			assertThat(minIntrinsicHeight(Constraints.Infinity)).isEqualTo(40)
			// Max width
			assertThat(maxIntrinsicWidth(0)).isEqualTo(30 / 2 * 7 + 20)
			assertThat(maxIntrinsicWidth(10)).isEqualTo(30 / 2 * 7 + 20)
			assertThat(maxIntrinsicWidth(25)).isEqualTo(25 * 2 / 2 * 7 + 20)
			assertThat(maxIntrinsicWidth(Constraints.Infinity)).isEqualTo(30 / 2 * 7 + 20)
			// Max height
			assertThat(maxIntrinsicHeight(0)).isEqualTo(40)
			assertThat(maxIntrinsicHeight(125)).isEqualTo(40)
			assertThat(maxIntrinsicHeight(370)).isEqualTo(50)
			assertThat(maxIntrinsicHeight(Constraints.Infinity)).isEqualTo(40)
		}
	}

	@Test fun testRow_withArrangementSpacing() = runTest {
		val spacing = 5
		val childSize = 10
		testIntrinsics(
			@Composable {
				Row(horizontalArrangement = Arrangement.spacedBy(spacing)) {
					Box(Modifier.size(childSize))
					Box(Modifier.size(childSize))
					Box(Modifier.size(childSize))
				}
			},
		) { minIntrinsicWidth, _, maxIntrinsicWidth, _ ->
			assertThat(minIntrinsicWidth(Constraints.Infinity)).isEqualTo(childSize * 3 + 2 * spacing)
			assertThat(maxIntrinsicWidth(Constraints.Infinity)).isEqualTo(childSize * 3 + 2 * spacing)
		}
	}

	@Test fun testColumn_withNoWeightChildren_hasCorrectIntrinsicMeasurements() = runTest {
		testIntrinsics(
			@Composable {
				Column {
					Container(Modifier.aspectRatio(2.0f))
					ConstrainedBox(Constraints.fixed(50, 40))
				}
			},
			@Composable {
				Column {
					Container(Modifier.aspectRatio(2.0f).align(Alignment.Start))
					ConstrainedBox(
						modifier = Modifier.align(Alignment.End),
						constraints = Constraints.fixed(50, 40),
					)
				}
			},
			@Composable {
				Column(Modifier.fillMaxHeight()) {
					Container(Modifier.aspectRatio(2.0f))
					ConstrainedBox(Constraints.fixed(50, 40))
				}
			},
			@Composable {
				Column(Modifier.fillMaxHeight(), verticalArrangement = Arrangement.Top) {
					Container(Modifier.aspectRatio(2.0f))
					ConstrainedBox(Constraints.fixed(50, 40))
				}
			},
			@Composable {
				Column(Modifier.fillMaxHeight(), verticalArrangement = Arrangement.Center) {
					Container(Modifier.align(Alignment.CenterHorizontally).aspectRatio(2.0f))
					ConstrainedBox(Constraints.fixed(50, 40))
				}
			},
			@Composable {
				Column(Modifier.fillMaxHeight(), verticalArrangement = Arrangement.Bottom) {
					Container(Modifier.align(Alignment.End).aspectRatio(2.0f))
					ConstrainedBox(
						modifier = Modifier.align(Alignment.End),
						constraints = Constraints.fixed(50, 40),
					)
				}
			},
			@Composable {
				Column(Modifier.fillMaxHeight(), verticalArrangement = Arrangement.SpaceAround) {
					Container(Modifier.fillMaxWidth().aspectRatio(2.0f))
					ConstrainedBox(
						modifier = Modifier.fillMaxWidth(),
						constraints = Constraints.fixed(50, 40),
					)
				}
			},
			@Composable {
				Column(Modifier.fillMaxHeight(), verticalArrangement = Arrangement.SpaceBetween) {
					Container(Modifier.aspectRatio(2.0f))
					ConstrainedBox(Constraints.fixed(50, 40))
				}
			},
			@Composable {
				Column(Modifier.fillMaxHeight(), verticalArrangement = Arrangement.SpaceEvenly) {
					Container(Modifier.aspectRatio(2.0f))
					ConstrainedBox(Constraints.fixed(50, 40))
				}
			},
		) { minIntrinsicWidth, minIntrinsicHeight, maxIntrinsicWidth, maxIntrinsicHeight ->
			// Min width
			assertThat(minIntrinsicWidth(0)).isEqualTo(50)
			assertThat(minIntrinsicWidth(25)).isEqualTo(50)
			assertThat(minIntrinsicWidth(Constraints.Infinity)).isEqualTo(50)
			// Min height
			assertThat(minIntrinsicHeight(0)).isEqualTo(40)
			assertThat(minIntrinsicHeight(50)).isEqualTo(50 / 2 + 40)
			assertThat(minIntrinsicHeight(Constraints.Infinity)).isEqualTo(40)
			// Max width
			assertThat(maxIntrinsicWidth(0)).isEqualTo(50)
			assertThat(maxIntrinsicWidth(25)).isEqualTo(50)
			assertThat(maxIntrinsicWidth(Constraints.Infinity)).isEqualTo(50)
			// Max height
			assertThat(maxIntrinsicHeight(0)).isEqualTo(40)
			assertThat(maxIntrinsicHeight(50)).isEqualTo(50 / 2 + 40)
			assertThat(maxIntrinsicHeight(Constraints.Infinity)).isEqualTo(40)
		}
	}

	@Test fun testColumn_withWeightChildren_hasCorrectIntrinsicMeasurements() = runTest {
		testIntrinsics(
			@Composable {
				Column {
					ConstrainedBox(
						modifier = Modifier.weight(3.0f),
						constraints = Constraints.fixed(30, 20),
					)
					ConstrainedBox(
						modifier = Modifier.weight(2.0f),
						constraints = Constraints.fixed(40, 30),
					)
					Container(Modifier.aspectRatio(0.5f).weight(2.0f))
					ConstrainedBox(Constraints.fixed(30, 20))
				}
			},
			@Composable {
				Column {
					ConstrainedBox(
						modifier = Modifier.weight(3.0f).align(Alignment.Start),
						constraints = Constraints.fixed(30, 20),
					)
					ConstrainedBox(
						modifier = Modifier.weight(2.0f).align(Alignment.CenterHorizontally),
						constraints = Constraints.fixed(40, 30),
					)
					Container(Modifier.aspectRatio(0.5f).weight(2.0f))
					ConstrainedBox(
						modifier = Modifier.align(Alignment.End),
						constraints = Constraints.fixed(30, 20),
					)
				}
			},
			@Composable {
				Column(verticalArrangement = Arrangement.Top) {
					ConstrainedBox(
						modifier = Modifier.weight(3.0f),
						constraints = Constraints.fixed(30, 20),
					)
					ConstrainedBox(
						modifier = Modifier.weight(2.0f),
						constraints = Constraints.fixed(40, 30),
					)
					Container(Modifier.aspectRatio(0.5f).weight(2f))
					ConstrainedBox(Constraints.fixed(30, 20))
				}
			},
			@Composable {
				Column(verticalArrangement = Arrangement.Center) {
					ConstrainedBox(
						modifier = Modifier.weight(3.0f).align(Alignment.CenterHorizontally),
						constraints = Constraints.fixed(30, 20),
					)
					ConstrainedBox(
						modifier = Modifier.weight(2.0f).align(Alignment.CenterHorizontally),
						constraints = Constraints.fixed(40, 30),
					)
					Container(Modifier.aspectRatio(0.5f).weight(2.0f).align(Alignment.CenterHorizontally))
					ConstrainedBox(
						modifier = Modifier.align(Alignment.CenterHorizontally),
						constraints = Constraints.fixed(30, 20),
					)
				}
			},
			@Composable {
				Column(verticalArrangement = Arrangement.Bottom) {
					ConstrainedBox(
						modifier = Modifier.weight(3.0f).align(Alignment.End),
						constraints = Constraints.fixed(30, 20),
					)
					ConstrainedBox(
						modifier = Modifier.weight(2.0f).align(Alignment.End),
						constraints = Constraints.fixed(40, 30),
					)
					Container(Modifier.aspectRatio(0.5f).weight(2.0f).align(Alignment.End))
					ConstrainedBox(
						modifier = Modifier.align(Alignment.End),
						constraints = Constraints.fixed(30, 20),
					)
				}
			},
			@Composable {
				Column(verticalArrangement = Arrangement.SpaceAround) {
					ConstrainedBox(
						modifier = Modifier.weight(3.0f).fillMaxWidth(),
						constraints = Constraints.fixed(30, 20),
					)
					ConstrainedBox(
						modifier = Modifier.weight(2.0f).fillMaxWidth(),
						constraints = Constraints.fixed(40, 30),
					)
					Container(Modifier.aspectRatio(0.5f).weight(2.0f).fillMaxWidth())
					ConstrainedBox(
						modifier = Modifier.fillMaxWidth(),
						constraints = Constraints.fixed(30, 20),
					)
				}
			},
			@Composable {
				Column(verticalArrangement = Arrangement.SpaceBetween) {
					ConstrainedBox(
						modifier = Modifier.weight(3.0f),
						constraints = Constraints.fixed(30, 20),
					)
					ConstrainedBox(
						modifier = Modifier.weight(2.0f),
						constraints = Constraints.fixed(40, 30),
					)
					Container(Modifier.aspectRatio(0.5f).then(Modifier.weight(2.0f)))
					ConstrainedBox(Constraints.fixed(30, 20))
				}
			},
			@Composable {
				Column(verticalArrangement = Arrangement.SpaceEvenly) {
					ConstrainedBox(
						modifier = Modifier.weight(3.0f),
						constraints = Constraints.fixed(30, 20),
					)
					ConstrainedBox(
						modifier = Modifier.weight(2.0f),
						constraints = Constraints.fixed(40, 30),
					)
					Container(Modifier.aspectRatio(0.5f).then(Modifier.weight(2.0f)))
					ConstrainedBox(Constraints.fixed(30, 20))
				}
			},
		) { minIntrinsicWidth, minIntrinsicHeight, maxIntrinsicWidth, maxIntrinsicHeight ->
			// Min width
			assertThat(minIntrinsicWidth(0)).isEqualTo(40)
			assertThat(minIntrinsicWidth(125)).isEqualTo(40)
			assertThat(minIntrinsicWidth(370)).isEqualTo(50)
			assertThat(minIntrinsicWidth(Constraints.Infinity)).isEqualTo(40)
			// Min height
			assertThat(minIntrinsicHeight(0)).isEqualTo(30 / 2 * 7 + 20)
			assertThat(minIntrinsicHeight(10)).isEqualTo(30 / 2 * 7 + 20)
			assertThat(minIntrinsicHeight(25)).isEqualTo(25 * 2 / 2 * 7 + 20)
			assertThat(minIntrinsicHeight(Constraints.Infinity)).isEqualTo(30 / 2 * 7 + 20)
			// Max width
			assertThat(maxIntrinsicWidth(0)).isEqualTo(40)
			assertThat(maxIntrinsicWidth(125)).isEqualTo(40)
			assertThat(maxIntrinsicWidth(370)).isEqualTo(50)
			assertThat(maxIntrinsicWidth(Constraints.Infinity)).isEqualTo(40)
			// Max height
			assertThat(maxIntrinsicHeight(0)).isEqualTo(30 / 2 * 7 + 20)
			assertThat(maxIntrinsicHeight(10)).isEqualTo(30 / 2 * 7 + 20)
			assertThat(maxIntrinsicHeight(25)).isEqualTo(25 * 2 / 2 * 7 + 20)
			assertThat(maxIntrinsicHeight(Constraints.Infinity)).isEqualTo(30 / 2 * 7 + 20)
		}
	}

	@Test fun testColumn_withArrangementSpacing() = runTest {
		val spacing = 5
		val childSize = 10
		testIntrinsics(
			@Composable {
				Column(verticalArrangement = Arrangement.spacedBy(spacing)) {
					Box(Modifier.size(childSize))
					Box(Modifier.size(childSize))
					Box(Modifier.size(childSize))
				}
			},
		) { _, minIntrinsicHeight, _, maxIntrinsicHeight ->
			assertThat(minIntrinsicHeight(Constraints.Infinity)).isEqualTo(childSize * 3 + 2 * spacing)
			assertThat(maxIntrinsicHeight(Constraints.Infinity)).isEqualTo(childSize * 3 + 2 * spacing)
		}
	}
	// endregion

	// region Modifiers specific tests
	@Test fun testRowColumnModifiersChain_leftMostWins() = runTest {
		runMosaicTest(NodeSnapshots) {
			val columnHeight = 24

			setContent {
				Box {
					Column(Modifier.height(columnHeight)) {
						Container(Modifier.weight(2.0f).weight(1.0f))
						Container(Modifier.weight(1.0f))
					}
				}
			}

			val rootNode = awaitSnapshot()
			val firstChildContainerNode = rootNode.children[0].children[0].children[0]

			assertThat(firstChildContainerNode.height).isEqualTo(columnHeight * 2 / 3)
		}
	}
	// endregion

	// region AbsoluteArrangement tests
	@Test fun testRow_absoluteArrangementLeft() = runTest {
		runMosaicTest(NodeSnapshots) {
			val size = 100

			setContent {
				Row(
					modifier = Modifier.fillMaxWidth(),
					horizontalArrangement = Arrangement.Absolute.Left,
				) {
					Container(width = size, height = size)
					Container(width = size, height = size)
					Container(width = size, height = size)
				}
			}

			val rootNode = awaitSnapshot()
			val firstChildContainerNode = rootNode.children[0].children[0]
			val secondChildContainerNode = rootNode.children[0].children[1]
			val thirdChildContainerNode = rootNode.children[0].children[2]

			assertThat(firstChildContainerNode.position).isEqualTo(IntOffset.Zero)
			assertThat(secondChildContainerNode.position).isEqualTo(IntOffset(size, 0))
			assertThat(thirdChildContainerNode.position).isEqualTo(IntOffset(size * 2, 0))
		}
	}

	@Test fun testRow_absoluteArrangementRight() = runTest {
		runMosaicTest(NodeSnapshots) {
			val size = 100

			setContent {
				Row(
					modifier = Modifier.fillMaxWidth(),
					horizontalArrangement = Arrangement.Absolute.Right,
				) {
					Container(width = size, height = size)
					Container(width = size, height = size)
					Container(width = size, height = size)
				}
			}

			val rootNode = awaitSnapshot()
			val rootWidth = rootNode.width

			val firstChildContainerNode = rootNode.children[0].children[0]
			val secondChildContainerNode = rootNode.children[0].children[1]
			val thirdChildContainerNode = rootNode.children[0].children[2]

			assertThat(firstChildContainerNode.position).isEqualTo(IntOffset(rootWidth - size * 3, 0))
			assertThat(secondChildContainerNode.position).isEqualTo(IntOffset(rootWidth - size * 2, 0))
			assertThat(thirdChildContainerNode.position).isEqualTo(IntOffset(rootWidth - size, 0))
		}
	}

	@Test fun testRow_absoluteArrangementCenter() = runTest {
		runMosaicTest(NodeSnapshots) {
			val size = 100

			setContent {
				Row(
					modifier = Modifier.fillMaxWidth(),
					horizontalArrangement = Arrangement.Absolute.Center,
				) {
					Container(width = size, height = size)
					Container(width = size, height = size)
					Container(width = size, height = size)
				}
			}

			val rootNode = awaitSnapshot()
			val extraSpace = rootNode.width - size * 3

			val firstChildContainerNode = rootNode.children[0].children[0]
			val secondChildContainerNode = rootNode.children[0].children[1]
			val thirdChildContainerNode = rootNode.children[0].children[2]

			assertThat(firstChildContainerNode.position).isEqualTo(IntOffset(extraSpace / 2, 0))
			assertThat(secondChildContainerNode.position).isEqualTo(IntOffset((extraSpace / 2) + size, 0))
			assertThat(thirdChildContainerNode.position).isEqualTo(
				IntOffset(
					(extraSpace / 2) + size * 2,
					0,
				),
			)
		}
	}

	@Test fun testRow_absoluteArrangementSpaceEvenly() = runTest {
		runMosaicTest(NodeSnapshots) {
			val size = 100

			setContent {
				Row(
					modifier = Modifier.fillMaxWidth(),
					horizontalArrangement = Arrangement.Absolute.SpaceEvenly,
				) {
					Container(width = size, height = size)
					Container(width = size, height = size)
					Container(width = size, height = size)
				}
			}

			val rootNode = awaitSnapshot()
			val gap = (rootNode.width - size * 3) / 4

			val firstChildContainerNode = rootNode.children[0].children[0]
			val secondChildContainerNode = rootNode.children[0].children[1]
			val thirdChildContainerNode = rootNode.children[0].children[2]

			assertThat(firstChildContainerNode.position).isEqualTo(IntOffset(gap, 0))
			assertThat(secondChildContainerNode.position).isEqualTo(IntOffset(size + gap * 2, 0))
			assertThat(thirdChildContainerNode.position).isEqualTo(IntOffset(size * 2 + gap * 3, 0))
		}
	}

	@Test fun testRow_absoluteArrangementSpaceBetween() = runTest {
		runMosaicTest(NodeSnapshots) {
			val size = 100

			setContent {
				Row(
					modifier = Modifier.fillMaxWidth(),
					horizontalArrangement = Arrangement.Absolute.SpaceBetween,
				) {
					Container(width = size, height = size)
					Container(width = size, height = size)
					Container(width = size, height = size)
				}
			}

			val rootNode = awaitSnapshot()
			val gap = (rootNode.width - size * 3) / 2

			val firstChildContainerNode = rootNode.children[0].children[0]
			val secondChildContainerNode = rootNode.children[0].children[1]
			val thirdChildContainerNode = rootNode.children[0].children[2]

			assertThat(firstChildContainerNode.position).isEqualTo(IntOffset.Zero)
			assertThat(secondChildContainerNode.position).isEqualTo(IntOffset(gap + size, 0))
			assertThat(thirdChildContainerNode.position).isEqualTo(IntOffset(gap * 2 + size * 2, 0))
		}
	}

	@Test fun testRow_absoluteArrangementSpaceAround() = runTest {
		runMosaicTest(NodeSnapshots) {
			val size = 100

			setContent {
				Row(
					modifier = Modifier.fillMaxWidth(),
					horizontalArrangement = Arrangement.Absolute.SpaceAround,
				) {
					Container(width = size, height = size)
					Container(width = size, height = size)
					Container(width = size, height = size)
				}
			}

			val rootNode = awaitSnapshot()
			val gap = (rootNode.width - size * 3) / 3

			val firstChildContainerNode = rootNode.children[0].children[0]
			val secondChildContainerNode = rootNode.children[0].children[1]
			val thirdChildContainerNode = rootNode.children[0].children[2]

			assertThat(firstChildContainerNode.position).isEqualTo(IntOffset(gap / 2, 0))
			assertThat(secondChildContainerNode.position).isEqualTo(IntOffset((gap * 3 / 2) + size, 0))
			assertThat(thirdChildContainerNode.position).isEqualTo(IntOffset((gap * 5 / 2) + size * 2, 0))
		}
	}
	// endregion
}

@Composable
private fun Center(content: @Composable () -> Unit) {
	Layout(content) { measurables, constraints ->
		val measurable = measurables.firstOrNull()
		// The child cannot be larger than our max constraints, but we ignore min constraints.
		val placeable = measurable?.measure(
			constraints.copy(
				minWidth = 0,
				minHeight = 0,
			),
		)

		// The layout is as large as possible for bounded constraints,
		// or wrap content otherwise.
		val layoutWidth = if (constraints.hasBoundedWidth) {
			constraints.maxWidth
		} else {
			placeable?.width ?: constraints.minWidth
		}
		val layoutHeight = if (constraints.hasBoundedHeight) {
			constraints.maxHeight
		} else {
			placeable?.height ?: constraints.minHeight
		}

		layout(layoutWidth, layoutHeight) {
			if (placeable != null) {
				val position = Alignment.Center.align(
					IntSize(placeable.width, placeable.height),
					IntSize(layoutWidth, layoutHeight),
				)
				placeable.place(position.x, position.y)
			}
		}
	}
}

@Composable
private fun WithInfiniteConstraints(content: @Composable () -> Unit) {
	Layout(content) { measurables, _ ->
		val placeables = measurables.map { it.measure(Constraints()) }
		layout(0, 0) {
			placeables.forEach { it.place(0, 0) }
		}
	}
}
