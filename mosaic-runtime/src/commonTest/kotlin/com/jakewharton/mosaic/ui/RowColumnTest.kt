package com.jakewharton.mosaic.ui

import androidx.compose.runtime.Composable
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isZero
import com.jakewharton.mosaic.ConstrainedBox
import com.jakewharton.mosaic.Container
import com.jakewharton.mosaic.Holder
import com.jakewharton.mosaic.assertFailure
import com.jakewharton.mosaic.fillWith
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
import com.jakewharton.mosaic.testIntrinsics
import com.jakewharton.mosaic.testing.runMosaicTest
import com.jakewharton.mosaic.ui.unit.Constraints
import com.jakewharton.mosaic.ui.unit.IntSize
import kotlin.test.Test
import kotlinx.coroutines.test.runTest

class RowColumnTest {
	// region Size and position tests for Row and Column
	@Test fun testRow() = runTest {
		runMosaicTest {
			val size = 2

			setContent {
				Container(alignment = Alignment.TopStart) {
					Row {
						Container(modifier = Modifier.fillWith('A'), width = size, height = size)
						Container(modifier = Modifier.fillWith('B'), width = size * 2, height = size * 2)
					}
				}
			}

			assertThat(awaitSnapshot()).isEqualTo(
				"""
				|AABBBB
				|AABBBB
				|  BBBB
				|  BBBB
				""".trimMargin(),
			)
		}
	}

	@Test fun testRow_withChildrenWithWeight() = runTest {
		runMosaicTest {
			val rowWidth = 6
			val width = 3
			val height = 2

			setContent {
				Container(alignment = Alignment.TopStart, width = rowWidth) {
					Row {
						Container(modifier = Modifier.fillWith('A').weight(1.0f), width = width, height = height)
						Container(modifier = Modifier.fillWith('B').weight(2.0f), width = width, height = height)
					}
				}
			}

			assertThat(awaitSnapshot()).isEqualTo(
				"""
				|AABBBB
				|AABBBB
				""".trimMargin(),
			)
		}
	}

	@Test fun testRow_withChildrenWithWeightNonFilling() = runTest {
		runMosaicTest {
			val width = 2
			val height = 1

			setContent {
				Container(
					modifier = Modifier.sizeIn(maxWidth = 12, maxHeight = 12),
					alignment = Alignment.TopStart,
				) {
					Row {
						Container(
							modifier = Modifier.fillWith('A').weight(1.0f, fill = false),
							width = width,
							height = height,
						)
						Container(
							modifier = Modifier.fillWith('B').weight(2.0f, fill = false),
							width = width,
							height = height * 2,
						)
					}
				}
			}

			assertThat(awaitSnapshot()).isEqualTo(
				"""
				|AABB
				|  BB
				""".trimMargin(),
			)
		}
	}

	@Test fun testRow_withChildrenWithMaxValueWeight() = runTest {
		runMosaicTest {
			val rowWidth = 6
			val width = 3
			val height = 2

			setContent {
				Container(alignment = Alignment.TopStart, width = rowWidth) {
					Row {
						Container(modifier = Modifier.fillWith('A').weight(Float.MAX_VALUE), width = width, height = height)
						Container(modifier = Modifier.fillWith('B').weight(1.0f), width = width, height = height)
					}
				}
			}

			assertThat(awaitSnapshot()).isEqualTo(
				"""
				|AAAAAA
				|AAAAAA
				""".trimMargin(),
			)
		}
	}

	@Test fun testRow_withChildrenWithPositiveInfinityWeight() = runTest {
		runMosaicTest {
			val rowWidth = 6
			val width = 3
			val height = 2

			setContent {
				Container(alignment = Alignment.TopStart, width = rowWidth) {
					Row {
						Container(
							modifier = Modifier.fillWith('A').weight(Float.POSITIVE_INFINITY),
							width = width,
							height = height,
						)
						Container(modifier = Modifier.fillWith('B').weight(1.0f), width = width, height = height)
					}
				}
			}

			assertThat(awaitSnapshot()).isEqualTo(
				"""
				|AAAAAA
				|AAAAAA
				""".trimMargin(),
			)
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
		runMosaicTest {
			val size = 2

			setContent {
				Container(alignment = Alignment.TopStart) {
					Column {
						Container(modifier = Modifier.fillWith('A'), width = size, height = size)
						Container(modifier = Modifier.fillWith('B'), width = (size * 2), height = (size * 2))
					}
				}
			}

			assertThat(awaitSnapshot()).isEqualTo(
				"""
				|AA
				|AA
				|BBBB
				|BBBB
				|BBBB
				|BBBB
				""".trimMargin(),
			)
		}
	}

	@Test fun testColumn_withChildrenWithWeight() = runTest {
		runMosaicTest {
			val columnHeight = 6
			val width = 2
			val height = 3

			setContent {
				Container(alignment = Alignment.TopStart, height = columnHeight) {
					Column {
						Container(modifier = Modifier.fillWith('A').weight(1.0f), width = width, height = height)
						Container(modifier = Modifier.fillWith('B').weight(2.0f), width = width, height = height)
					}
				}
			}

			assertThat(awaitSnapshot()).isEqualTo(
				"""
				|AA
				|AA
				|BB
				|BB
				|BB
				|BB
				""".trimMargin(),
			)
		}
	}

	@Test fun testColumn_withChildrenWithWeightNonFilling() = runTest {
		runMosaicTest {
			val width = 2
			val height = 1

			setContent {
				Container(
					modifier = Modifier.sizeIn(maxWidth = 12, maxHeight = 12),
					alignment = Alignment.TopStart,
				) {
					Column {
						Container(
							modifier = Modifier.fillWith('A').weight(1.0f, fill = false),
							width = width,
							height = height,
						)
						Container(
							modifier = Modifier.fillWith('B').weight(2.0f, fill = false),
							width = width,
							height = height,
						)
					}
				}
			}

			assertThat(awaitSnapshot()).isEqualTo(
				"""
				|AA
				|BB
				""".trimMargin(),
			)
		}
	}

	@Test fun testColumn_withChildrenWithMaxValueWeight() = runTest {
		runMosaicTest {
			val columnHeight = 6
			val width = 2
			val height = 3

			setContent {
				Container(alignment = Alignment.TopStart, height = columnHeight) {
					Column {
						Container(modifier = Modifier.fillWith('A').weight(Float.MAX_VALUE), width = width, height = height)
						Container(modifier = Modifier.fillWith('B').weight(1.0f), width = width, height = height)
					}
				}
			}

			assertThat(awaitSnapshot()).isEqualTo(
				"""
				|AA
				|AA
				|AA
				|AA
				|AA
				|AA
				""".trimMargin(),
			)
		}
	}

	@Test fun testColumn_withChildrenWithPositiveInfinityWeight() = runTest {
		runMosaicTest {
			val columnHeight = 6
			val width = 2
			val height = 3

			setContent {
				Container(alignment = Alignment.TopStart, height = columnHeight) {
					Column {
						Container(
							modifier = Modifier.fillWith('A').weight(Float.POSITIVE_INFINITY),
							width = width,
							height = height,
						)
						Container(modifier = Modifier.fillWith('B').weight(1.0f), width = width, height = height)
					}
				}
			}

			assertThat(awaitSnapshot()).isEqualTo(
				"""
				|AA
				|AA
				|AA
				|AA
				|AA
				|AA
				""".trimMargin(),
			)
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
		runMosaicTest {
			val expectedRowWidth = 11
			val leftPadding = 1

			setContent {
				Row(
					Modifier.fillWith('.')
						.wrapContentSize(Alignment.TopStart)
						.padding(left = leftPadding)
						.widthIn(max = expectedRowWidth),
				) {
					Container(Modifier.fillWith('A').weight(1.0f), height = 1)
					Container(Modifier.fillWith('B').weight(1.0f), height = 1)
				}
			}

			assertThat(awaitSnapshot()).isEqualTo(
				"""
				|.AAAAABBBBBB
				""".trimMargin(),
			)
		}
	}

	@Test fun testRow_isNotLargerThanItsChildren_becauseOfRoundings() = runTest {
		runMosaicTest {
			val expectedRowWidth = 8
			val leftPadding = 1

			setContent {
				Row(
					Modifier.fillWith('.')
						.wrapContentSize(Alignment.TopStart)
						.padding(left = leftPadding)
						.widthIn(max = expectedRowWidth),
				) {
					Container(Modifier.fillWith('A').weight(2.0f), height = 1)
					Container(Modifier.fillWith('B').weight(2.0f), height = 1)
					Container(Modifier.fillWith('C').weight(3.0f), height = 1)
				}
			}

			assertThat(awaitSnapshot()).isEqualTo(
				"""
				|.AAABBCCC
				""".trimMargin(),
			)
		}
	}

	@Test fun testColumn_isNotLargetThanItsChildren_becauseOfRoundings() = runTest {
		runMosaicTest {
			val expectedColumnHeight = 8
			val topPadding = 1

			setContent {
				Column(
					Modifier.fillWith('.')
						.wrapContentSize(Alignment.TopStart)
						.padding(top = topPadding)
						.heightIn(max = expectedColumnHeight),
				) {
					Container(Modifier.fillWith('A').weight(1.0f), width = 1)
					Container(Modifier.fillWith('B').weight(1.0f), width = 1)
					Container(Modifier.fillWith('C').weight(1.0f), width = 1)
				}
			}

			assertThat(awaitSnapshot()).isEqualTo(
				"""
				|.
				|A
				|A
				|B
				|B
				|B
				|C
				|C
				|C
				""".trimMargin(),
			)
		}
	}

	@Test fun testColumn_doesNotPlaceChildrenOutOfBounds_becauseOfRoundings() = runTest {
		runMosaicTest {
			val expectedColumnHeight = 11
			val topPadding = 1

			setContent {
				Column(
					Modifier.fillWith('.')
						.wrapContentSize(Alignment.TopStart)
						.padding(top = topPadding)
						.heightIn(max = expectedColumnHeight),
				) {
					Container(Modifier.fillWith('A').weight(1.0f), width = 1)
					Container(Modifier.fillWith('B').weight(1.0f), width = 1)
				}
			}

			assertThat(awaitSnapshot()).isEqualTo(
				"""
				|.
				|A
				|A
				|A
				|A
				|A
				|B
				|B
				|B
				|B
				|B
				|B
				""".trimMargin(),
			)
		}
	}
	// endregion

	// region Cross axis alignment tests in Row
	@Test fun testRow_withStretchCrossAxisAlignment() = runTest {
		runMosaicTest {
			val size = 2

			setContent {
				Row(modifier = Modifier.height(size * 2)) {
					Container(modifier = Modifier.fillWith('A').fillMaxHeight(), width = size, height = size)
					Container(modifier = Modifier.fillWith('B').fillMaxHeight(), width = size * 2, height = size * 2)
				}
			}

			assertThat(awaitSnapshot()).isEqualTo(
				"""
				|AABBBB
				|AABBBB
				|AABBBB
				|AABBBB
				""".trimMargin(),
			)
		}
	}

	@Test fun testRow_withGravityModifier_andGravityParameter() = runTest {
		runMosaicTest {
			val size = 2
			val rowHeight = 6

			setContent {
				Container(height = rowHeight) {
					Row(Modifier.fillWith('.').fillMaxHeight(), verticalAlignment = Alignment.CenterVertically) {
						Container(modifier = Modifier.fillWith('A').align(Alignment.Top), width = size, height = size)
						Container(modifier = Modifier.fillWith('B'), width = size, height = size)
						Container(modifier = Modifier.fillWith('C').align(Alignment.Bottom), width = size, height = size)
					}
				}
			}

			assertThat(awaitSnapshot()).isEqualTo(
				"""
				|AA....
				|AA....
				|..BB..
				|..BB..
				|....CC
				|....CC
				""".trimMargin(),
			)
		}
	}

	@Test fun testRow_withGravityModifier() = runTest {
		runMosaicTest {
			val size = 2
			val rowHeight = 6

			setContent {
				Container(height = rowHeight) {
					Row(Modifier.fillWith('.').fillMaxHeight()) {
						Container(modifier = Modifier.fillWith('A').align(Alignment.Top), width = size, height = size)
						Container(
							modifier = Modifier.fillWith('B').align(Alignment.CenterVertically),
							width = size,
							height = size,
						)
						Container(modifier = Modifier.fillWith('C').align(Alignment.Bottom), width = size, height = size)
					}
				}
			}

			assertThat(awaitSnapshot()).isEqualTo(
				"""
				|AA....
				|AA....
				|..BB..
				|..BB..
				|....CC
				|....CC
				""".trimMargin(),
			)
		}
	}
	// endregion

	// region Cross axis alignment tests in Column
	@Test fun testColumn_withStretchCrossAxisAlignment() = runTest {
		runMosaicTest {
			val size = 2

			setContent {
				Column(modifier = Modifier.width(size * 2)) {
					Container(modifier = Modifier.fillWith('A').fillMaxWidth(), width = size, height = size)
					Container(modifier = Modifier.fillWith('B').fillMaxWidth(), width = size * 2, height = size * 2)
				}
			}

			assertThat(awaitSnapshot()).isEqualTo(
				"""
				|AAAA
				|AAAA
				|BBBB
				|BBBB
				|BBBB
				|BBBB
				""".trimMargin(),
			)
		}
	}

	@Test fun testColumn_withGravityModifier() = runTest {
		runMosaicTest {
			val size = 2
			val columnWidth = 6

			setContent {
				Container(width = columnWidth) {
					Column(Modifier.fillWith('.').fillMaxWidth()) {
						Container(modifier = Modifier.fillWith('A').align(Alignment.Start), width = size, height = size)
						Container(
							modifier = Modifier.fillWith('B').align(Alignment.CenterHorizontally),
							width = size,
							height = size,
						)
						Container(modifier = Modifier.fillWith('C').align(Alignment.End), width = size, height = size)
					}
				}
			}

			assertThat(awaitSnapshot()).isEqualTo(
				"""
				|AA....
				|AA....
				|..BB..
				|..BB..
				|....CC
				|....CC
				""".trimMargin(),
			)
		}
	}

	@Test fun testColumn_withGravityModifier_andGravityParameter() = runTest {
		runMosaicTest {
			val size = 2
			val columnWidth = 6

			setContent {
				Container(width = columnWidth) {
					Column(Modifier.fillWith('.').fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
						Container(modifier = Modifier.fillWith('A').align(Alignment.Start), width = size, height = size)
						Container(modifier = Modifier.fillWith('B'), width = size, height = size)
						Container(modifier = Modifier.fillWith('C').align(Alignment.End), width = size, height = size)
					}
				}
			}

			assertThat(awaitSnapshot()).isEqualTo(
				"""
				|AA....
				|AA....
				|..BB..
				|..BB..
				|....CC
				|....CC
				""".trimMargin(),
			)
		}
	}
	// endregion

	// region Size tests in Row
	@Test fun testRow_expandedWidth_withExpandedModifier() = runTest {
		runMosaicTest {
			val size = 2
			val rowWidth = 10

			setContent {
				Container(width = rowWidth) {
					Center {
						Row(Modifier.fillWith('.').fillMaxWidth()) {
							Spacer(Modifier.fillWith('A').size(width = size, height = size))
							Spacer(Modifier.fillWith('B').size(width = (size * 2), height = (size * 2)))
						}
					}
				}
			}

			assertThat(awaitSnapshot()).isEqualTo(
				"""
				|AABBBB....
				|AABBBB....
				|..BBBB....
				|..BBBB....
				""".trimMargin(),
			)
		}
	}

	@Test fun testRow_wrappedWidth_withNoWeightChildren() = runTest {
		runMosaicTest {
			val size = 2
			val rowWidth = 10

			setContent {
				Container(width = rowWidth) {
					Center {
						Row(Modifier.fillWith('.')) {
							Spacer(Modifier.fillWith('A').size(width = size, height = size))
							Spacer(Modifier.fillWith('B').size(width = (size * 2), height = (size * 2)))
						}
					}
				}
			}

			assertThat(awaitSnapshot()).isEqualTo(
				"""
				|  AABBBB
				|  AABBBB
				|  ..BBBB
				|  ..BBBB
				""".trimMargin(),
			)
		}
	}

	@Test fun testRow_expandedWidth_withWeightChildren() = runTest {
		runMosaicTest {
			val size = 2
			val rowWidth = 10

			setContent {
				Container(width = rowWidth) {
					Center {
						Row(Modifier.fillWith('.')) {
							Container(modifier = Modifier.fillWith('A').weight(1.0f), width = size, height = size)
							Container(modifier = Modifier.fillWith('B'), width = size * 2, height = size * 2)
						}
					}
				}
			}

			assertThat(awaitSnapshot()).isEqualTo(
				"""
				|AAAAAABBBB
				|AAAAAABBBB
				|......BBBB
				|......BBBB
				""".trimMargin(),
			)
		}
	}

	@Test fun testRow_withMaxCrossAxisSize() = runTest {
		runMosaicTest {
			val size = 2
			val rowHeight = 6

			setContent {
				Container(height = rowHeight) {
					Center {
						Row(Modifier.fillWith('.').fillMaxHeight()) {
							Spacer(Modifier.fillWith('A').size(width = size, height = size))
							Spacer(Modifier.fillWith('B').size(width = (size * 2), height = (size * 2)))
						}
					}
				}
			}

			assertThat(awaitSnapshot()).isEqualTo(
				"""
				|AABBBB
				|AABBBB
				|..BBBB
				|..BBBB
				|......
				|......
				""".trimMargin(),
			)
		}
	}

	@Test fun testRow_withMinCrossAxisSize() = runTest {
		runMosaicTest {
			val size = 2
			val rowHeight = 6

			setContent {
				Container(height = rowHeight) {
					Center {
						Row(Modifier.fillWith('.')) {
							Spacer(Modifier.fillWith('A').size(width = size, height = size))
							Spacer(Modifier.fillWith('B').size(width = (size * 2), height = (size * 2)))
						}
					}
				}
			}

			assertThat(awaitSnapshot()).isEqualTo(
				"""
				|
				|AABBBB
				|AABBBB
				|..BBBB
				|..BBBB
				|
				""".trimMargin(),
			)
		}
	}

	@Test fun testRow_withExpandedModifier_respectsMaxWidthConstraint() = runTest {
		runMosaicTest {
			val size = 2
			val rowWidth = 10

			setContent {
				Center {
					ConstrainedBox(constraints = Constraints(maxWidth = rowWidth)) {
						Row(Modifier.fillWith('.').fillMaxWidth()) {
							Spacer(Modifier.fillWith('A').size(width = size, height = size))
							Spacer(Modifier.fillWith('B').size(width = size * 2, height = size * 2))
						}
					}
				}
			}

			assertThat(awaitSnapshot()).isEqualTo(
				"""
				|AABBBB....
				|AABBBB....
				|..BBBB....
				|..BBBB....
				""".trimMargin(),
			)
		}
	}

	@Test fun testRow_withChildrenWithWeight_respectsMaxWidthConstraint() = runTest {
		runMosaicTest {
			val size = 2
			val rowWidth = 10

			setContent {
				Center {
					ConstrainedBox(constraints = Constraints(maxWidth = rowWidth)) {
						Row(Modifier.fillWith('.')) {
							Container(modifier = Modifier.fillWith('A').weight(1.0f), width = size, height = size)
							Container(modifier = Modifier.fillWith('B'), width = size * 2, height = size * 2)
						}
					}
				}
			}

			assertThat(awaitSnapshot()).isEqualTo(
				"""
				|AAAAAABBBB
				|AAAAAABBBB
				|......BBBB
				|......BBBB
				""".trimMargin(),
			)
		}
	}

	@Test fun testRow_withNoWeightChildren_respectsMinWidthConstraint() = runTest {
		runMosaicTest {
			val size = 2
			val rowWidth = 10

			setContent {
				Center {
					ConstrainedBox(constraints = Constraints(minWidth = rowWidth)) {
						Row(Modifier.fillWith('.')) {
							Spacer(Modifier.fillWith('A').size(width = size, height = size))
							Spacer(Modifier.fillWith('B').size(width = size * 2, height = size * 2))
						}
					}
				}
			}

			assertThat(awaitSnapshot()).isEqualTo(
				"""
				|AABBBB....
				|AABBBB....
				|..BBBB....
				|..BBBB....
				""".trimMargin(),
			)
		}
	}

	@Test fun testRow_withMaxCrossAxisSize_respectsMaxHeightConstraint() = runTest {
		runMosaicTest {
			val size = 2
			val rowHeight = 10

			setContent {
				Center {
					ConstrainedBox(constraints = Constraints(maxHeight = rowHeight)) {
						Row(Modifier.fillWith('.').fillMaxHeight()) {
							Spacer(Modifier.fillWith('A').size(width = size, height = size))
							Spacer(Modifier.fillWith('B').size(width = size * 2, height = size * 2))
						}
					}
				}
			}

			assertThat(awaitSnapshot()).isEqualTo(
				"""
				|AABBBB
				|AABBBB
				|..BBBB
				|..BBBB
				|......
				|......
				|......
				|......
				|......
				|......
				""".trimMargin(),
			)
		}
	}

	@Test fun testRow_withMinCrossAxisSize_respectsMinHeightConstraint() = runTest {
		runMosaicTest {
			val size = 2
			val rowHeight = 6

			setContent {
				Center {
					ConstrainedBox(constraints = Constraints(minHeight = rowHeight)) {
						Row(Modifier.fillWith('.')) {
							Spacer(Modifier.fillWith('A').size(width = size, height = size))
							Spacer(Modifier.fillWith('B').size(width = size * 2, height = size * 2))
						}
					}
				}
			}

			assertThat(awaitSnapshot()).isEqualTo(
				"""
				|AABBBB
				|AABBBB
				|..BBBB
				|..BBBB
				|......
				|......
				""".trimMargin(),
			)
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
		runMosaicTest {
			val size = 2

			setContent {
				Row(Modifier.fillWith('.').sizeIn(maxWidth = 20, maxHeight = 20)) {
					Box(Modifier.fillWith('A').weight(1.0f, false).size(size))
				}
			}

			assertThat(awaitSnapshot()).isEqualTo(
				"""
				|AA
				|AA
				""".trimMargin(),
			)
		}
	}

	@Test fun testRow_includesSpacing_withWeightChildren() = runTest {
		runMosaicTest {
			val rowWidth = 10
			val space = 2

			setContent {
				Row(
					modifier = Modifier.fillWith('.').widthIn(max = rowWidth),
					horizontalArrangement = Arrangement.spacedBy(space),
				) {
					Box(Modifier.fillWith('A').weight(1.0f).height(1))
					Box(Modifier.fillWith('B').weight(1.0f).height(1))
				}
			}

			assertThat(awaitSnapshot()).isEqualTo(
				"""
				|AAAA..BBBB
				""".trimMargin(),
			)
		}
	}
	// endregion

	// region Size tests in Column
	@Test fun testColumn_expandedHeight_withExpandedModifier() = runTest {
		runMosaicTest {
			val size = 2
			val columnHeight = 10

			setContent {
				Container(height = columnHeight) {
					Center {
						Column(Modifier.fillWith('.').fillMaxHeight()) {
							Spacer(Modifier.fillWith('A').size(width = size, height = size))
							Spacer(Modifier.fillWith('B').size(width = (size * 2), height = (size * 2)))
						}
					}
				}
			}

			assertThat(awaitSnapshot()).isEqualTo(
				"""
				|AA..
				|AA..
				|BBBB
				|BBBB
				|BBBB
				|BBBB
				|....
				|....
				|....
				|....
				""".trimMargin(),
			)
		}
	}

	@Test fun testColumn_wrappedHeight_withNoChildrenWithWeight() = runTest {
		runMosaicTest {
			val size = 2
			val columnHeight = 10

			setContent {
				Container(height = columnHeight) {
					Center {
						Column(Modifier.fillWith('.')) {
							Spacer(Modifier.fillWith('A').size(width = size, height = size))
							Spacer(Modifier.fillWith('B').size(width = (size * 2), height = (size * 2)))
						}
					}
				}
			}

			assertThat(awaitSnapshot()).isEqualTo(
				"""
				|
				|
				|AA..
				|AA..
				|BBBB
				|BBBB
				|BBBB
				|BBBB
				|
				|
				""".trimMargin(),
			)
		}
	}

	@Test fun testColumn_expandedHeight_withWeightChildren() = runTest {
		runMosaicTest {
			val size = 2
			val columnHeight = 10

			setContent {
				Container(height = columnHeight) {
					Center {
						Column(Modifier.fillWith('.')) {
							Container(modifier = Modifier.fillWith('A').weight(1.0f), width = size, height = size)
							Container(modifier = Modifier.fillWith('B'), width = size * 2, height = size * 2)
						}
					}
				}
			}

			assertThat(awaitSnapshot()).isEqualTo(
				"""
				|AA..
				|AA..
				|AA..
				|AA..
				|AA..
				|AA..
				|BBBB
				|BBBB
				|BBBB
				|BBBB
				""".trimMargin(),
			)
		}
	}

	@Test fun testColumn_withMaxCrossAxisSize() = runTest {
		runMosaicTest {
			val size = 2
			val columnWidth = 6

			setContent {
				Container(width = columnWidth) {
					Center {
						Column(Modifier.fillWith('.').fillMaxWidth()) {
							Spacer(Modifier.fillWith('A').size(width = size, height = size))
							Spacer(Modifier.fillWith('B').size(width = (size * 2), height = (size * 2)))
						}
					}
				}
			}

			assertThat(awaitSnapshot()).isEqualTo(
				"""
				|AA....
				|AA....
				|BBBB..
				|BBBB..
				|BBBB..
				|BBBB..
				""".trimMargin(),
			)
		}
	}

	@Test fun testColumn_withMinCrossAxisSize() = runTest {
		runMosaicTest {
			val size = 2
			val columnWidth = 6

			setContent {
				Container(width = columnWidth) {
					Center {
						Column(Modifier.fillWith('.')) {
							Spacer(Modifier.fillWith('A').size(width = size, height = size))
							Spacer(Modifier.fillWith('B').size(width = (size * 2), height = (size * 2)))
						}
					}
				}
			}

			assertThat(awaitSnapshot()).isEqualTo(
				"""
				| AA..
				| AA..
				| BBBB
				| BBBB
				| BBBB
				| BBBB
				""".trimMargin(),
			)
		}
	}

	@Test fun testColumn_withExpandedModifier_respectsMaxHeightConstraint() = runTest {
		runMosaicTest {
			val size = 2
			val columnHeight = 10

			setContent {
				Center {
					ConstrainedBox(constraints = Constraints(maxHeight = columnHeight)) {
						Column(Modifier.fillWith('.').fillMaxHeight()) {
							Spacer(Modifier.fillWith('A').size(width = size, height = size))
							Spacer(Modifier.fillWith('B').size(width = size * 2, height = size * 2))
						}
					}
				}
			}

			assertThat(awaitSnapshot()).isEqualTo(
				"""
				|AA..
				|AA..
				|BBBB
				|BBBB
				|BBBB
				|BBBB
				|....
				|....
				|....
				|....
				""".trimMargin(),
			)
		}
	}

	@Test fun testColumn_withWeightChildren_respectsMaxHeightConstraint() = runTest {
		runMosaicTest {
			val size = 2
			val columnHeight = 10

			setContent {
				Center {
					ConstrainedBox(constraints = Constraints(maxHeight = columnHeight)) {
						Column(Modifier.fillWith('.')) {
							Container(modifier = Modifier.fillWith('A').weight(1.0f), width = size, height = size)
							Container(modifier = Modifier.fillWith('B'), width = size * 2, height = size * 2)
						}
					}
				}
			}

			assertThat(awaitSnapshot()).isEqualTo(
				"""
				|AA..
				|AA..
				|AA..
				|AA..
				|AA..
				|AA..
				|BBBB
				|BBBB
				|BBBB
				|BBBB
				""".trimMargin(),
			)
		}
	}

	@Test fun testColumn_withChildren_respectsMinHeightConstraint() = runTest {
		runMosaicTest {
			val size = 2
			val columnHeight = 10

			setContent {
				Center {
					ConstrainedBox(constraints = Constraints(minHeight = columnHeight)) {
						Column(Modifier.fillWith('.')) {
							Spacer(Modifier.fillWith('A').size(width = size, height = size))
							Spacer(Modifier.fillWith('B').size(width = size * 2, height = size * 2))
						}
					}
				}
			}

			assertThat(awaitSnapshot()).isEqualTo(
				"""
				|AA..
				|AA..
				|BBBB
				|BBBB
				|BBBB
				|BBBB
				|....
				|....
				|....
				|....
				""".trimMargin(),
			)
		}
	}

	@Test fun testColumn_withMaxCrossAxisSize_respectsMaxWidthConstraint() = runTest {
		runMosaicTest {
			val size = 2
			val columnWidth = 10

			setContent {
				Center {
					ConstrainedBox(constraints = Constraints(maxWidth = columnWidth)) {
						Column(Modifier.fillWith('.').fillMaxWidth()) {
							Spacer(Modifier.fillWith('A').size(width = size, height = size))
							Spacer(Modifier.fillWith('B').size(width = size * 2, height = size * 2))
						}
					}
				}
			}

			assertThat(awaitSnapshot()).isEqualTo(
				"""
				|AA........
				|AA........
				|BBBB......
				|BBBB......
				|BBBB......
				|BBBB......
				""".trimMargin(),
			)
		}
	}

	@Test fun testColumn_withMinCrossAxisSize_respectsMinWidthConstraint() = runTest {
		runMosaicTest {
			val size = 2
			val columnWidth = 6

			setContent {
				Center {
					ConstrainedBox(constraints = Constraints(minWidth = columnWidth)) {
						Column(Modifier.fillWith('.')) {
							Spacer(Modifier.fillWith('A').size(width = size, height = size))
							Spacer(Modifier.fillWith('B').size(width = size * 2, height = size * 2))
						}
					}
				}
			}

			assertThat(awaitSnapshot()).isEqualTo(
				"""
				|AA....
				|AA....
				|BBBB..
				|BBBB..
				|BBBB..
				|BBBB..
				""".trimMargin(),
			)
		}
	}

	@Test fun testColumn_doesNotExpand_whenWeightChildrenDoNotFill() = runTest {
		runMosaicTest {
			val size = 2

			setContent {
				Column(Modifier.fillWith('.').sizeIn(maxWidth = 20, maxHeight = 20)) {
					Box(Modifier.fillWith('A').weight(1.0f, false).size(size))
				}
			}

			assertThat(awaitSnapshot()).isEqualTo(
				"""
				|AA
				|AA
				""".trimMargin(),
			)
		}
	}

	@Test fun testColumn_includesSpacing_withWeightChildren() = runTest {
		runMosaicTest {
			val columnHeight = 10
			val space = 2

			setContent {
				Column(
					modifier = Modifier.fillWith('.').height(columnHeight),
					verticalArrangement = Arrangement.spacedBy(space),
				) {
					Box(Modifier.fillWith('A').weight(1.0f).width(1))
					Box(Modifier.fillWith('B').weight(1.0f).width(1))
				}
			}

			assertThat(awaitSnapshot()).isEqualTo(
				"""
				|A
				|A
				|A
				|A
				|.
				|.
				|B
				|B
				|B
				|B
				""".trimMargin(),
			)
		}
	}
	// endregion

	// region Main axis alignment tests in Row
	@Test fun testRow_withStartArrangement() = runTest {
		runMosaicTest {
			val size = 2
			val rowWidth = 18

			setContent {
				Container(width = rowWidth) {
					Center {
						Row(Modifier.fillWith('.').fillMaxWidth()) {
							Container(modifier = Modifier.fillWith('A'), width = size, height = size)
							Container(modifier = Modifier.fillWith('B'), width = size, height = size)
							Container(modifier = Modifier.fillWith('C'), width = size, height = size)
						}
					}
				}
			}

			assertThat(awaitSnapshot()).isEqualTo(
				"""
				|AABBCC............
				|AABBCC............
				""".trimMargin(),
			)
		}
	}

	@Test fun testRow_withEndArrangement() = runTest {
		runMosaicTest {
			val size = 2
			val rowWidth = 18

			setContent {
				Container(width = rowWidth) {
					Center {
						Row(
							modifier = Modifier.fillWith('.').fillMaxWidth(),
							horizontalArrangement = Arrangement.End,
						) {
							Container(modifier = Modifier.fillWith('A'), width = size, height = size)
							Container(modifier = Modifier.fillWith('B'), width = size, height = size)
							Container(modifier = Modifier.fillWith('C'), width = size, height = size)
						}
					}
				}
			}

			assertThat(awaitSnapshot()).isEqualTo(
				"""
				|............AABBCC
				|............AABBCC
				""".trimMargin(),
			)
		}
	}

	@Test fun testRow_withCenterArrangement() = runTest {
		runMosaicTest {
			val size = 2
			val rowWidth = 18

			setContent {
				Container(width = rowWidth) {
					Center {
						Row(
							modifier = Modifier.fillWith('.').fillMaxWidth(),
							horizontalArrangement = Arrangement.Center,
						) {
							Container(modifier = Modifier.fillWith('A'), width = size, height = size)
							Container(modifier = Modifier.fillWith('B'), width = size, height = size)
							Container(modifier = Modifier.fillWith('C'), width = size, height = size)
						}
					}
				}
			}

			assertThat(awaitSnapshot()).isEqualTo(
				"""
				|......AABBCC......
				|......AABBCC......
				""".trimMargin(),
			)
		}
	}

	@Test fun testRow_withSpaceEvenlyArrangement() = runTest {
		runMosaicTest {
			val size = 2
			val rowWidth = 18

			setContent {
				Container(width = rowWidth) {
					Center {
						Row(
							modifier = Modifier.fillWith('.').fillMaxWidth(),
							horizontalArrangement = Arrangement.SpaceEvenly,
						) {
							Container(modifier = Modifier.fillWith('A'), width = size, height = size)
							Container(modifier = Modifier.fillWith('B'), width = size, height = size)
							Container(modifier = Modifier.fillWith('C'), width = size, height = size)
						}
					}
				}
			}

			assertThat(awaitSnapshot()).isEqualTo(
				"""
				|...AA...BB...CC...
				|...AA...BB...CC...
				""".trimMargin(),
			)
		}
	}

	@Test fun testRow_withSpaceBetweenArrangement_singleItem() = runTest {
		runMosaicTest {
			val size = 2
			val rowWidth = 18

			setContent {
				Container(width = rowWidth) {
					Center {
						Row(
							modifier = Modifier.fillWith('.').fillMaxWidth(),
							horizontalArrangement = Arrangement.SpaceBetween,
						) {
							Container(modifier = Modifier.fillWith('A'), width = size, height = size)
						}
					}
				}
			}

			assertThat(awaitSnapshot()).isEqualTo(
				"""
				|AA................
				|AA................
				""".trimMargin(),
			)
		}
	}

	@Test fun testRow_withSpaceBetweenArrangement_multipleItems() = runTest {
		runMosaicTest {
			val size = 2
			val rowWidth = 18

			setContent {
				Container(width = rowWidth) {
					Center {
						Row(
							modifier = Modifier.fillWith('.').fillMaxWidth(),
							horizontalArrangement = Arrangement.SpaceBetween,
						) {
							Container(modifier = Modifier.fillWith('A'), width = size, height = size)
							Container(modifier = Modifier.fillWith('B'), width = size, height = size)
							Container(modifier = Modifier.fillWith('C'), width = size, height = size)
						}
					}
				}
			}

			assertThat(awaitSnapshot()).isEqualTo(
				"""
				|AA......BB......CC
				|AA......BB......CC
				""".trimMargin(),
			)
		}
	}

	@Test fun testRow_withSpaceAroundArrangement() = runTest {
		runMosaicTest {
			val size = 2
			val rowWidth = 18

			setContent {
				Container(width = rowWidth) {
					Center {
						Row(
							modifier = Modifier.fillWith('.').fillMaxWidth(),
							horizontalArrangement = Arrangement.SpaceAround,
						) {
							Container(modifier = Modifier.fillWith('A'), width = size, height = size)
							Container(modifier = Modifier.fillWith('B'), width = size, height = size)
							Container(modifier = Modifier.fillWith('C'), width = size, height = size)
						}
					}
				}
			}

			assertThat(awaitSnapshot()).isEqualTo(
				"""
				|..AA....BB....CC..
				|..AA....BB....CC..
				""".trimMargin(),
			)
		}
	}

	@Test fun testRow_withSpacedByArrangement() = runTest {
		runMosaicTest {
			val space = 1
			val size = 2

			setContent {
				Column {
					Row(modifier = Modifier.fillWith('.'), horizontalArrangement = Arrangement.spacedBy(space)) {
						Box(Modifier.fillWith('A').requiredSize(size))
						Box(Modifier.fillWith('B').requiredSize(size))
					}
				}
			}

			assertThat(awaitSnapshot()).isEqualTo(
				"""
				|AA.BB
				|AA.BB
				""".trimMargin(),
			)
		}
	}

	@Test fun testRow_withSpacedByAlignedArrangement() = runTest {
		runMosaicTest {
			val space = 1
			val size = 2
			val rowSize = 7

			setContent {
				Column {
					Row(
						modifier = Modifier.fillWith('.').requiredSize(rowSize),
						horizontalArrangement = Arrangement.spacedBy(space, Alignment.End),
					) {
						Box(Modifier.fillWith('A').requiredSize(size))
						Box(Modifier.fillWith('B').requiredSize(size))
					}
				}
			}

			assertThat(awaitSnapshot()).isEqualTo(
				"""
				|..AA.BB
				|..AA.BB
				|.......
				|.......
				|.......
				|.......
				|.......
				""".trimMargin(),
			)
		}
	}

	@Test fun testRow_withSpacedByArrangement_insufficientSpace() = runTest {
		runMosaicTest {
			val space = 3
			val size = 4
			val rowSize = 10

			setContent {
				Column {
					Row(
						modifier = Modifier.fillWith('.').requiredSize(rowSize),
						horizontalArrangement = Arrangement.spacedBy(space),
					) {
						Box(Modifier.fillWith('A').size(size))
						Box(Modifier.fillWith('B').size(size))
						Box(Modifier.fillWith('C').size(size))
					}
				}
			}

			assertThat(awaitSnapshot()).isEqualTo(
				"""
				|AAAA...BBB
				|AAAA...BBB
				|AAAA...BBB
				|AAAA...BBB
				|..........
				|..........
				|..........
				|..........
				|..........
				|..........
				""".trimMargin(),
			)
		}
	}

	@Test fun testRow_withAlignedArrangement() = runTest {
		runMosaicTest {
			val size = 2
			val rowSize = 5

			setContent {
				Column {
					Row(
						modifier = Modifier.fillWith('.').requiredSize(rowSize),
						horizontalArrangement = Arrangement.aligned(Alignment.End),
					) {
						Box(Modifier.fillWith('A').size(size))
						Box(Modifier.fillWith('B').size(size))
					}
				}
			}

			assertThat(awaitSnapshot()).isEqualTo(
				"""
				|.AABB
				|.AABB
				|.....
				|.....
				|.....
				""".trimMargin(),
			)
		}
	}
	// endregion

	// region Main axis alignment tests in Column
	@Test fun testColumn_withTopArrangement() = runTest {
		runMosaicTest {
			val size = 2
			val columnHeight = 18

			setContent {
				Container(height = columnHeight) {
					Center {
						Column(Modifier.fillWith('.').fillMaxHeight()) {
							Container(modifier = Modifier.fillWith('A'), width = size, height = size)
							Container(modifier = Modifier.fillWith('B'), width = size, height = size)
							Container(modifier = Modifier.fillWith('C'), width = size, height = size)
						}
					}
				}
			}

			assertThat(awaitSnapshot()).isEqualTo(
				"""
				|AA
				|AA
				|BB
				|BB
				|CC
				|CC
				|..
				|..
				|..
				|..
				|..
				|..
				|..
				|..
				|..
				|..
				|..
				|..
				""".trimMargin(),
			)
		}
	}

	@Test fun testColumn_withBottomArrangement() = runTest {
		runMosaicTest {
			val size = 2
			val columnHeight = 18

			setContent {
				Container(height = columnHeight) {
					Center {
						Column(
							modifier = Modifier.fillWith('.').fillMaxHeight(),
							verticalArrangement = Arrangement.Bottom,
						) {
							Container(modifier = Modifier.fillWith('A'), width = size, height = size)
							Container(modifier = Modifier.fillWith('B'), width = size, height = size)
							Container(modifier = Modifier.fillWith('C'), width = size, height = size)
						}
					}
				}
			}

			assertThat(awaitSnapshot()).isEqualTo(
				"""
				|..
				|..
				|..
				|..
				|..
				|..
				|..
				|..
				|..
				|..
				|..
				|..
				|AA
				|AA
				|BB
				|BB
				|CC
				|CC
				""".trimMargin(),
			)
		}
	}

	@Test fun testColumn_withCenterArrangement() = runTest {
		runMosaicTest {
			val size = 2
			val columnHeight = 18

			setContent {
				Container(height = columnHeight) {
					Center {
						Column(
							modifier = Modifier.fillWith('.').fillMaxHeight(),
							verticalArrangement = Arrangement.Center,
						) {
							Container(modifier = Modifier.fillWith('A'), width = size, height = size)
							Container(modifier = Modifier.fillWith('B'), width = size, height = size)
							Container(modifier = Modifier.fillWith('C'), width = size, height = size)
						}
					}
				}
			}

			assertThat(awaitSnapshot()).isEqualTo(
				"""
				|..
				|..
				|..
				|..
				|..
				|..
				|AA
				|AA
				|BB
				|BB
				|CC
				|CC
				|..
				|..
				|..
				|..
				|..
				|..
				""".trimMargin(),
			)
		}
	}

	@Test fun testColumn_withSpaceEvenlyArrangement() = runTest {
		runMosaicTest {
			val size = 2
			val columnHeight = 18

			setContent {
				Container(height = columnHeight) {
					Center {
						Column(
							modifier = Modifier.fillWith('.').fillMaxHeight(),
							verticalArrangement = Arrangement.SpaceEvenly,
						) {
							Container(modifier = Modifier.fillWith('A'), width = size, height = size)
							Container(modifier = Modifier.fillWith('B'), width = size, height = size)
							Container(modifier = Modifier.fillWith('C'), width = size, height = size)
						}
					}
				}
			}

			assertThat(awaitSnapshot()).isEqualTo(
				"""
				|..
				|..
				|..
				|AA
				|AA
				|..
				|..
				|..
				|BB
				|BB
				|..
				|..
				|..
				|CC
				|CC
				|..
				|..
				|..
				""".trimMargin(),
			)
		}
	}

	@Test fun testColumn_withSpaceBetweenArrangement() = runTest {
		runMosaicTest {
			val size = 2
			val columnHeight = 18

			setContent {
				Container(height = columnHeight) {
					Center {
						Column(
							modifier = Modifier.fillWith('.').fillMaxHeight(),
							verticalArrangement = Arrangement.SpaceBetween,
						) {
							Container(modifier = Modifier.fillWith('A'), width = size, height = size)
							Container(modifier = Modifier.fillWith('B'), width = size, height = size)
							Container(modifier = Modifier.fillWith('C'), width = size, height = size)
						}
					}
				}
			}

			assertThat(awaitSnapshot()).isEqualTo(
				"""
				|AA
				|AA
				|..
				|..
				|..
				|..
				|..
				|..
				|BB
				|BB
				|..
				|..
				|..
				|..
				|..
				|..
				|CC
				|CC
				""".trimMargin(),
			)
		}
	}

	@Test fun testColumn_withSpaceAroundArrangement() = runTest {
		runMosaicTest {
			val size = 2
			val columnHeight = 18

			setContent {
				Container(height = columnHeight) {
					Center {
						Column(
							modifier = Modifier.fillWith('.').fillMaxHeight(),
							verticalArrangement = Arrangement.SpaceAround,
						) {
							Container(modifier = Modifier.fillWith('A'), width = size, height = size)
							Container(modifier = Modifier.fillWith('B'), width = size, height = size)
							Container(modifier = Modifier.fillWith('C'), width = size, height = size)
						}
					}
				}
			}

			assertThat(awaitSnapshot()).isEqualTo(
				"""
				|..
				|..
				|AA
				|AA
				|..
				|..
				|..
				|..
				|BB
				|BB
				|..
				|..
				|..
				|..
				|CC
				|CC
				|..
				|..
				""".trimMargin(),
			)
		}
	}

	@Test fun testColumn_withSpacedByArrangement() = runTest {
		runMosaicTest {
			val space = 1
			val size = 2

			setContent {
				Row {
					Column(modifier = Modifier.fillWith('.'), verticalArrangement = Arrangement.spacedBy(space)) {
						Box(Modifier.fillWith('A').requiredSize(size))
						Box(Modifier.fillWith('B').requiredSize(size))
					}
				}
			}

			assertThat(awaitSnapshot()).isEqualTo(
				"""
				|AA
				|AA
				|..
				|BB
				|BB
				""".trimMargin(),
			)
		}
	}

	@Test fun testColumn_withSpacedByAlignedArrangement() = runTest {
		runMosaicTest {
			val space = 1
			val size = 2
			val columnSize = 7

			setContent {
				Row {
					Column(
						modifier = Modifier.fillWith('.').requiredSize(columnSize),
						verticalArrangement = Arrangement.spacedBy(space, Alignment.Bottom),
					) {
						Box(Modifier.fillWith('A').requiredSize(size))
						Box(Modifier.fillWith('B').requiredSize(size))
					}
				}
			}

			assertThat(awaitSnapshot()).isEqualTo(
				"""
				|.......
				|.......
				|AA.....
				|AA.....
				|.......
				|BB.....
				|BB.....
				""".trimMargin(),
			)
		}
	}

	@Test fun testColumn_withSpacedByArrangement_insufficientSpace() = runTest {
		runMosaicTest {
			val space = 3
			val size = 4
			val columnSize = 10

			setContent {
				Row {
					Column(
						modifier = Modifier.fillWith('.').requiredSize(columnSize),
						verticalArrangement = Arrangement.spacedBy(space),
					) {
						Box(Modifier.fillWith('A').size(size))
						Box(Modifier.fillWith('B').size(size))
						Box(Modifier.fillWith('C').size(size))
					}
				}
			}

			assertThat(awaitSnapshot()).isEqualTo(
				"""
				|AAAA......
				|AAAA......
				|AAAA......
				|AAAA......
				|..........
				|..........
				|..........
				|BBBB......
				|BBBB......
				|BBBB......
				""".trimMargin(),
			)
		}
	}

	@Test fun testColumn_withAlignedArrangement() = runTest {
		runMosaicTest {
			val size = 2
			val columnSize = 5

			setContent {
				Row {
					Column(
						modifier = Modifier.fillWith('.').requiredSize(columnSize),
						verticalArrangement = Arrangement.aligned(Alignment.Bottom),
					) {
						Box(Modifier.fillWith('A').requiredSize(size))
						Box(Modifier.fillWith('B').requiredSize(size))
					}
				}
			}

			assertThat(awaitSnapshot()).isEqualTo(
				"""
				|.....
				|AA...
				|AA...
				|BB...
				|BB...
				""".trimMargin(),
			)
		}
	}

	@Test fun testRow_doesNotUseMinConstraintsOnChildren() = runTest {
		runMosaicTest {
			val size = 5
			val childSize = 3

			setContent {
				Center {
					ConstrainedBox(constraints = Constraints.fixed(size, size)) {
						Row(Modifier.fillWith('.')) {
							Spacer(Modifier.fillWith('A').size(width = childSize, height = childSize))
						}
					}
				}
			}

			assertThat(awaitSnapshot()).isEqualTo(
				"""
				|AAA..
				|AAA..
				|AAA..
				|.....
				|.....
				""".trimMargin(),
			)
		}
	}

	@Test fun testColumn_doesNotUseMinConstraintsOnChildren() = runTest {
		runMosaicTest {
			val size = 5
			val childSize = 3

			setContent {
				Center {
					ConstrainedBox(constraints = Constraints.fixed(size, size)) {
						Column(Modifier.fillWith('.')) {
							Spacer(Modifier.fillWith('A').size(width = childSize, height = childSize))
						}
					}
				}
			}

			assertThat(awaitSnapshot()).isEqualTo(
				"""
				|AAA..
				|AAA..
				|AAA..
				|.....
				|.....
				""".trimMargin(),
			)
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
		runMosaicTest {
			val columnHeight = 6

			setContent {
				Box {
					Column(Modifier.height(columnHeight)) {
						Container(Modifier.fillWith('A').weight(2.0f).weight(1.0f), width = 1)
						Container(Modifier.fillWith('B').weight(1.0f), width = 1)
					}
				}
			}

			assertThat(awaitSnapshot()).isEqualTo(
				"""
				|A
				|A
				|A
				|A
				|B
				|B
				""".trimMargin(),
			)
		}
	}
	// endregion

	// region AbsoluteArrangement tests
	@Test fun testRow_absoluteArrangementLeft() = runTest {
		runMosaicTest {
			val size = 2
			val rowWidth = 18

			setContent {
				Container(width = rowWidth) {
					Row(
						modifier = Modifier.fillWith('.').fillMaxWidth(),
						horizontalArrangement = Arrangement.Absolute.Left,
					) {
						Container(modifier = Modifier.fillWith('A'), width = size, height = size)
						Container(modifier = Modifier.fillWith('B'), width = size, height = size)
						Container(modifier = Modifier.fillWith('C'), width = size, height = size)
					}
				}
			}

			assertThat(awaitSnapshot()).isEqualTo(
				"""
				|AABBCC............
				|AABBCC............
				""".trimMargin(),
			)
		}
	}

	@Test fun testRow_absoluteArrangementRight() = runTest {
		runMosaicTest {
			val size = 2
			val rowWidth = 18

			setContent {
				Container(width = rowWidth) {
					Row(
						modifier = Modifier.fillWith('.').fillMaxWidth(),
						horizontalArrangement = Arrangement.Absolute.Right,
					) {
						Container(modifier = Modifier.fillWith('A'), width = size, height = size)
						Container(modifier = Modifier.fillWith('B'), width = size, height = size)
						Container(modifier = Modifier.fillWith('C'), width = size, height = size)
					}
				}
			}

			assertThat(awaitSnapshot()).isEqualTo(
				"""
				|............AABBCC
				|............AABBCC
				""".trimMargin(),
			)
		}
	}

	@Test fun testRow_absoluteArrangementCenter() = runTest {
		runMosaicTest {
			val size = 2
			val rowWidth = 18

			setContent {
				Container(width = rowWidth) {
					Row(
						modifier = Modifier.fillWith('.').fillMaxWidth(),
						horizontalArrangement = Arrangement.Absolute.Center,
					) {
						Container(modifier = Modifier.fillWith('A'), width = size, height = size)
						Container(modifier = Modifier.fillWith('B'), width = size, height = size)
						Container(modifier = Modifier.fillWith('C'), width = size, height = size)
					}
				}
			}

			assertThat(awaitSnapshot()).isEqualTo(
				"""
				|......AABBCC......
				|......AABBCC......
				""".trimMargin(),
			)
		}
	}

	@Test fun testRow_absoluteArrangementSpaceEvenly() = runTest {
		runMosaicTest {
			val size = 2
			val rowWidth = 18

			setContent {
				Container(width = rowWidth) {
					Row(
						modifier = Modifier.fillWith('.').fillMaxWidth(),
						horizontalArrangement = Arrangement.Absolute.SpaceEvenly,
					) {
						Container(modifier = Modifier.fillWith('A'), width = size, height = size)
						Container(modifier = Modifier.fillWith('B'), width = size, height = size)
						Container(modifier = Modifier.fillWith('C'), width = size, height = size)
					}
				}
			}

			assertThat(awaitSnapshot()).isEqualTo(
				"""
				|...AA...BB...CC...
				|...AA...BB...CC...
				""".trimMargin(),
			)
		}
	}

	@Test fun testRow_absoluteArrangementSpaceBetween() = runTest {
		runMosaicTest {
			val size = 2
			val rowWidth = 18

			setContent {
				Container(width = rowWidth) {
					Row(
						modifier = Modifier.fillWith('.').fillMaxWidth(),
						horizontalArrangement = Arrangement.Absolute.SpaceBetween,
					) {
						Container(modifier = Modifier.fillWith('A'), width = size, height = size)
						Container(modifier = Modifier.fillWith('B'), width = size, height = size)
						Container(modifier = Modifier.fillWith('C'), width = size, height = size)
					}
				}
			}

			assertThat(awaitSnapshot()).isEqualTo(
				"""
				|AA......BB......CC
				|AA......BB......CC
				""".trimMargin(),
			)
		}
	}

	@Test fun testRow_absoluteArrangementSpaceAround() = runTest {
		runMosaicTest {
			val size = 2
			val rowWidth = 18

			setContent {
				Container(width = rowWidth) {
					Row(
						modifier = Modifier.fillWith('.').fillMaxWidth(),
						horizontalArrangement = Arrangement.Absolute.SpaceAround,
					) {
						Container(modifier = Modifier.fillWith('A'), width = size, height = size)
						Container(modifier = Modifier.fillWith('B'), width = size, height = size)
						Container(modifier = Modifier.fillWith('C'), width = size, height = size)
					}
				}
			}

			assertThat(awaitSnapshot()).isEqualTo(
				"""
				|..AA....BB....CC..
				|..AA....BB....CC..
				""".trimMargin(),
			)
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
