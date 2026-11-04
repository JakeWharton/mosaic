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
import com.jakewharton.mosaic.TestFiller
import com.jakewharton.mosaic.fillWith
import com.jakewharton.mosaic.layout.MeasurePolicy
import com.jakewharton.mosaic.layout.aspectRatio
import com.jakewharton.mosaic.layout.drawBehind
import com.jakewharton.mosaic.layout.fillMaxSize
import com.jakewharton.mosaic.layout.height
import com.jakewharton.mosaic.layout.padding
import com.jakewharton.mosaic.layout.requiredSize
import com.jakewharton.mosaic.layout.requiredWidthIn
import com.jakewharton.mosaic.layout.size
import com.jakewharton.mosaic.layout.width
import com.jakewharton.mosaic.modifier.Modifier
import com.jakewharton.mosaic.testIntrinsics
import com.jakewharton.mosaic.testing.runMosaicTest
import com.jakewharton.mosaic.ui.unit.Constraints
import kotlin.test.Test
import kotlinx.coroutines.test.runTest

class BoxTest {
	@Test fun boxWithAlignedAndPositionedChildren() = runTest {
		runMosaicTest {
			val size = 6

			setContent {
				Container(alignment = Alignment.TopStart) {
					Box(Modifier.fillWith('.')) {
						Container(
							modifier = Modifier.align(Alignment.BottomEnd).fillWith('A'),
							width = size,
							height = size,
						)
						TestFiller(modifier = Modifier.matchParentSize().padding(2))
					}
				}
			}

			assertThat(awaitSnapshot()).isEqualTo(
				"""
				|AAAAAA
				|AAAAAA
				|AAXXAA
				|AAXXAA
				|AAAAAA
				|AAAAAA
				""".trimMargin(),
			)
		}
	}

	@Test fun boxWithMultipleAlignedChildren() = runTest {
		runMosaicTest {
			val size = 3
			val doubleSize = size * 2
			val drawSecond = mutableStateOf(false)

			setContent {
				Container(alignment = Alignment.TopStart) {
					Box(Modifier.fillWith('.')) {
						Container(
							modifier = Modifier.align(Alignment.BottomEnd).fillWith('A'),
							width = size,
							height = size,
						)
						Container(
							modifier = Modifier.align(Alignment.BottomEnd).drawBehind {
								if (drawSecond.value) drawRect('B')
							},
							width = doubleSize,
							height = doubleSize,
						)
					}
				}
			}

			assertThat(awaitSnapshot()).isEqualTo(
				"""
				|......
				|......
				|......
				|...AAA
				|...AAA
				|...AAA
				""".trimMargin(),
			)

			drawSecond.value = true

			assertThat(awaitSnapshot()).isEqualTo(
				"""
				|BBBBBB
				|BBBBBB
				|BBBBBB
				|BBBBBB
				|BBBBBB
				|BBBBBB
				""".trimMargin(),
			)
		}
	}

	@Test fun boxWithStretchChildrenPaddingLeftTop() = runTest {
		runMosaicTest {
			val size = 6
			val halfSize = size / 2
			val inset = 1

			setContent {
				Container(alignment = Alignment.TopStart) {
					Box(Modifier.fillWith('.')) {
						Container(
							modifier = Modifier.align(Alignment.Center).fillWith('A'),
							width = size,
							height = size,
						)
						TestFiller(
							modifier = Modifier.matchParentSize().padding(left = inset, top = inset).size(halfSize),
						)
					}
				}
			}

			assertThat(awaitSnapshot()).isEqualTo(
				"""
				|AAAAAA
				|AXXXXX
				|AXXXXX
				|AXXXXX
				|AXXXXX
				|AXXXXX
				""".trimMargin(),
			)
		}
	}

	@Test fun boxWithStretchChildrenPaddingRightBottom() = runTest {
		runMosaicTest {
			val size = 6
			val halfSize = size / 2
			val inset = 1

			setContent {
				Container(alignment = Alignment.TopStart) {
					Box(Modifier.fillWith('.')) {
						Container(
							modifier = Modifier.align(Alignment.Center).fillWith('A'),
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

			assertThat(awaitSnapshot()).isEqualTo(
				"""
				|XXXXXA
				|XXXXXA
				|XXXXXA
				|XXXXXA
				|XXXXXA
				|AAAAAA
				""".trimMargin(),
			)
		}
	}

	@Test fun boxWithStretchChildrenPaddingLeftRight() = runTest {
		runMosaicTest {
			val size = 6
			val halfSize = size / 2
			val inset = 1

			setContent {
				Container(alignment = Alignment.TopStart) {
					Box(Modifier.fillWith('.')) {
						Container(
							modifier = Modifier.align(Alignment.Center).fillWith('A'),
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

			assertThat(awaitSnapshot()).isEqualTo(
				"""
				|AXXXXA
				|AXXXXA
				|AXXXXA
				|AXXXXA
				|AXXXXA
				|AXXXXA
				""".trimMargin(),
			)
		}
	}

	@Test fun boxWithStretchChildrenPaddingTopBottom() = runTest {
		runMosaicTest {
			val size = 6
			val halfSize = size / 2
			val inset = 1

			setContent {
				Container(alignment = Alignment.TopStart) {
					Box(Modifier.fillWith('.')) {
						Container(
							modifier = Modifier.align(Alignment.Center).fillWith('A'),
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

			assertThat(awaitSnapshot()).isEqualTo(
				"""
				|AAAAAA
				|XXXXXX
				|XXXXXX
				|XXXXXX
				|XXXXXX
				|AAAAAA
				""".trimMargin(),
			)
		}
	}

	@Test fun boxExpanded() = runTest {
		runMosaicTest {
			val size = 6
			val halfSize = 3

			setContent {
				Container(alignment = Alignment.TopStart) {
					Container(modifier = Modifier.size(size)) {
						Box(Modifier.fillWith('.')) {
							Container(modifier = Modifier.fillMaxSize().fillWith('A'))
							Container(
								modifier = Modifier.align(Alignment.BottomEnd).fillWith('B'),
								width = halfSize,
								height = halfSize,
							)
						}
					}
				}
			}

			assertThat(awaitSnapshot()).isEqualTo(
				"""
				|AAAAAA
				|AAAAAA
				|AAAAAA
				|AAABBB
				|AAABBB
				|AAABBB
				""".trimMargin(),
			)
		}
	}

	@Test fun boxAlignmentParameter() = runTest {
		runMosaicTest {
			val outerSize = 5
			val innerSize = 2

			setContent {
				Box(
					contentAlignment = Alignment.BottomEnd,
					modifier = Modifier.requiredSize(outerSize).fillWith('.'),
				) {
					Box(Modifier.requiredSize(innerSize).fillWith('A'))
				}
			}

			assertThat(awaitSnapshot()).isEqualTo(
				"""
				|.....
				|.....
				|.....
				|...AA
				|...AA
				""".trimMargin(),
			)
		}
	}

	@Test fun boxOutermostGravityWins() = runTest {
		runMosaicTest {
			val size = 3

			setContent {
				Box(Modifier.requiredSize(size).fillWith('.')) {
					Box(Modifier.align(Alignment.BottomEnd).align(Alignment.TopStart).size(1).fillWith('A'))
				}
			}

			assertThat(awaitSnapshot()).isEqualTo(
				"""
				|...
				|...
				|..A
				""".trimMargin(),
			)
		}
	}

	@Test fun boxChildAffectsBoxSize() = runTest {
		runMosaicTest {
			val size = mutableIntStateOf(10)
			var measure = 0
			var layout = 0

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
		runMosaicTest {
			setContent {
				Box(
					modifier = Modifier.requiredWidthIn(4, 8).fillWith('.'),
					propagateMinConstraints = true,
				) {
					Box(modifier = Modifier.width(2).height(1).fillWith('A'))
				}
			}

			assertThat(awaitSnapshot()).isEqualTo("AAAA")
		}
	}

	@Test fun boxTracksPropagateMinConstraintsChanges() = runTest {
		runMosaicTest {
			val pmc = mutableStateOf(true)

			setContent {
				Box(
					modifier = Modifier.requiredWidthIn(4, 8).fillWith('.'),
					propagateMinConstraints = pmc.value,
					contentAlignment = Alignment.Center,
				) {
					Box(modifier = Modifier.width(2).height(1).fillWith('A'))
				}
			}

			assertThat(awaitSnapshot()).isEqualTo("AAAA")

			pmc.value = false
			assertThat(awaitSnapshot()).isEqualTo(".AA.")
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
