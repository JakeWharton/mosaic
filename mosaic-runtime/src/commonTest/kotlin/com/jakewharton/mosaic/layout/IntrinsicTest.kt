package com.jakewharton.mosaic.layout

import androidx.compose.runtime.Composable
import assertk.assertThat
import assertk.assertions.isEqualTo
import com.jakewharton.mosaic.ConstrainedBox
import com.jakewharton.mosaic.NodeSnapshots
import com.jakewharton.mosaic.modifier.Modifier
import com.jakewharton.mosaic.position
import com.jakewharton.mosaic.size
import com.jakewharton.mosaic.testIntrinsics
import com.jakewharton.mosaic.testing.runMosaicTest
import com.jakewharton.mosaic.ui.Box
import com.jakewharton.mosaic.ui.Column
import com.jakewharton.mosaic.ui.Layout
import com.jakewharton.mosaic.ui.unit.Constraints
import com.jakewharton.mosaic.ui.unit.IntOffset
import com.jakewharton.mosaic.ui.unit.IntSize
import com.jakewharton.mosaic.ui.unit.constrainHeight
import com.jakewharton.mosaic.ui.unit.constrainWidth
import kotlin.test.Test
import kotlinx.coroutines.test.runTest

class IntrinsicTest {
	@Test fun testMaxIntrinsic_HandleNegative() = runTest {
		val measurePolicy = object : MeasurePolicy {
			override fun MeasureScope.measure(
				measurables: List<Measurable>,
				constraints: Constraints,
			): MeasureResult {
				return layout(0, 0) {}
			}

			override fun minIntrinsicHeight(
				measurables: List<IntrinsicMeasurable>,
				width: Int,
			): Int {
				return -1
			}

			override fun minIntrinsicWidth(
				measurables: List<IntrinsicMeasurable>,
				height: Int,
			): Int {
				return -1
			}

			override fun maxIntrinsicHeight(
				measurables: List<IntrinsicMeasurable>,
				width: Int,
			): Int {
				return -1
			}

			override fun maxIntrinsicWidth(
				measurables: List<IntrinsicMeasurable>,
				height: Int,
			): Int {
				return -1
			}
		}
		runMosaicTest(NodeSnapshots) {
			setContent {
				Column {
					Layout(
						content = {},
						modifier = Modifier.width(IntrinsicSize.Min).height(IntrinsicSize.Min),
						measurePolicy = measurePolicy,
					)
					Layout(
						content = {},
						modifier = Modifier.width(IntrinsicSize.Max).height(IntrinsicSize.Max),
						measurePolicy = measurePolicy,
					)
				}
			}

			val parent = awaitSnapshot().children[0]
			val first = parent.children[0]
			val second = parent.children[1]

			assertThat(first.size).isEqualTo(IntSize.Zero)
			assertThat(second.size).isEqualTo(IntSize.Zero)
			assertThat(first.position).isEqualTo(IntOffset.Zero)
			assertThat(second.position).isEqualTo(IntOffset.Zero)
		}
	}

	@Test fun testMinIntrinsicWidth() = runTest {
		runMosaicTest(NodeSnapshots) {
			setContent {
				Box {
					FixedIntrinsicsBox(
						modifier = Modifier.width(IntrinsicSize.Min),
						minIntrinsicWidth = 10,
						width = 20,
						maxIntrinsicWidth = 30,
						minIntrinsicHeight = 40,
						height = 50,
						maxIntrinsicHeight = 60,
					)
				}
			}

			val node = awaitSnapshot().children[0].children[0]

			assertThat(node.size).isEqualTo(IntSize(10, 50))
			assertThat(node.position).isEqualTo(IntOffset.Zero)
		}
	}

	@Test fun testMinIntrinsicHeight() = runTest {
		runMosaicTest(NodeSnapshots) {
			setContent {
				Box {
					FixedIntrinsicsBox(
						modifier = Modifier.height(IntrinsicSize.Min),
						minIntrinsicWidth = 10,
						width = 20,
						maxIntrinsicWidth = 30,
						minIntrinsicHeight = 40,
						height = 50,
						maxIntrinsicHeight = 60,
					)
				}
			}

			val node = awaitSnapshot().children[0].children[0]

			assertThat(node.size).isEqualTo(IntSize(20, 40))
			assertThat(node.position).isEqualTo(IntOffset.Zero)
		}
	}

	@Test fun testMaxIntrinsicWidth() = runTest {
		runMosaicTest(NodeSnapshots) {
			setContent {
				Box {
					FixedIntrinsicsBox(
						modifier = Modifier.width(IntrinsicSize.Max),
						minIntrinsicWidth = 10,
						width = 20,
						maxIntrinsicWidth = 30,
						minIntrinsicHeight = 40,
						height = 50,
						maxIntrinsicHeight = 60,
					)
				}
			}

			val node = awaitSnapshot().children[0].children[0]

			assertThat(node.size).isEqualTo(IntSize(30, 50))
			assertThat(node.position).isEqualTo(IntOffset.Zero)
		}
	}

	@Test fun testMaxIntrinsicHeight() = runTest {
		runMosaicTest(NodeSnapshots) {
			setContent {
				Box {
					FixedIntrinsicsBox(
						modifier = Modifier.height(IntrinsicSize.Max),
						minIntrinsicWidth = 10,
						width = 20,
						maxIntrinsicWidth = 30,
						minIntrinsicHeight = 40,
						height = 50,
						maxIntrinsicHeight = 60,
					)
				}
			}

			val node = awaitSnapshot().children[0].children[0]

			assertThat(node.size).isEqualTo(IntSize(20, 60))
			assertThat(node.position).isEqualTo(IntOffset.Zero)
		}
	}

	@Test fun testMinIntrinsicWidth_respectsIncomingMaxConstraints() = runTest {
		runMosaicTest(NodeSnapshots) {
			setContent {
				Box {
					ConstrainedBox(Constraints(maxWidth = 5)) {
						FixedIntrinsicsBox(
							modifier = Modifier.width(IntrinsicSize.Min),
							minIntrinsicWidth = 10,
							width = 20,
							maxIntrinsicWidth = 30,
							minIntrinsicHeight = 40,
							height = 50,
							maxIntrinsicHeight = 60,
						)
					}
				}
			}

			val node = awaitSnapshot().children[0].children[0].children[0]

			assertThat(node.size).isEqualTo(IntSize(5, 50))
			assertThat(node.position).isEqualTo(IntOffset.Zero)
		}
	}

	@Test fun testMinIntrinsicWidth_respectsIncomingMinConstraints() = runTest {
		runMosaicTest(NodeSnapshots) {
			setContent {
				Box {
					ConstrainedBox(Constraints(minWidth = 15)) {
						FixedIntrinsicsBox(
							modifier = Modifier.width(IntrinsicSize.Min),
							minIntrinsicWidth = 10,
							width = 20,
							maxIntrinsicWidth = 30,
							minIntrinsicHeight = 40,
							height = 50,
							maxIntrinsicHeight = 60,
						)
					}
				}
			}

			val node = awaitSnapshot().children[0].children[0].children[0]

			assertThat(node.size).isEqualTo(IntSize(15, 50))
			assertThat(node.position).isEqualTo(IntOffset.Zero)
		}
	}

	@Test fun testMinIntrinsicHeight_respectsMaxIncomingConstraints() = runTest {
		runMosaicTest(NodeSnapshots) {
			setContent {
				Box {
					ConstrainedBox(Constraints(maxHeight = 35)) {
						FixedIntrinsicsBox(
							modifier = Modifier.height(IntrinsicSize.Min),
							minIntrinsicWidth = 10,
							width = 20,
							maxIntrinsicWidth = 30,
							minIntrinsicHeight = 40,
							height = 50,
							maxIntrinsicHeight = 60,
						)
					}
				}
			}

			val node = awaitSnapshot().children[0].children[0].children[0]

			assertThat(node.size).isEqualTo(IntSize(20, 35))
			assertThat(node.position).isEqualTo(IntOffset.Zero)
		}
	}

	@Test fun testMinIntrinsicHeight_respectsMinIncomingConstraints() = runTest {
		runMosaicTest(NodeSnapshots) {
			setContent {
				Box {
					ConstrainedBox(Constraints(minHeight = 45)) {
						FixedIntrinsicsBox(
							modifier = Modifier.height(IntrinsicSize.Min),
							minIntrinsicWidth = 10,
							width = 20,
							maxIntrinsicWidth = 30,
							minIntrinsicHeight = 40,
							height = 50,
							maxIntrinsicHeight = 60,
						)
					}
				}
			}

			val node = awaitSnapshot().children[0].children[0].children[0]

			assertThat(node.size).isEqualTo(IntSize(20, 45))
			assertThat(node.position).isEqualTo(IntOffset.Zero)
		}
	}

	@Test fun testMaxIntrinsicWidth_respectsMaxIncomingConstraints() = runTest {
		runMosaicTest(NodeSnapshots) {
			setContent {
				Box {
					ConstrainedBox(Constraints(maxWidth = 25)) {
						FixedIntrinsicsBox(
							modifier = Modifier.width(IntrinsicSize.Max),
							minIntrinsicWidth = 10,
							width = 20,
							maxIntrinsicWidth = 30,
							minIntrinsicHeight = 40,
							height = 50,
							maxIntrinsicHeight = 60,
						)
					}
				}
			}

			val node = awaitSnapshot().children[0].children[0].children[0]

			assertThat(node.size).isEqualTo(IntSize(25, 50))
			assertThat(node.position).isEqualTo(IntOffset.Zero)
		}
	}

	@Test fun testMaxIntrinsicWidth_respectsMinIncomingConstraints() = runTest {
		runMosaicTest(NodeSnapshots) {
			setContent {
				Box {
					ConstrainedBox(Constraints(minWidth = 35)) {
						FixedIntrinsicsBox(
							modifier = Modifier.width(IntrinsicSize.Max),
							minIntrinsicWidth = 10,
							width = 20,
							maxIntrinsicWidth = 30,
							minIntrinsicHeight = 40,
							height = 50,
							maxIntrinsicHeight = 60,
						)
					}
				}
			}

			val node = awaitSnapshot().children[0].children[0].children[0]

			assertThat(node.size).isEqualTo(IntSize(35, 50))
			assertThat(node.position).isEqualTo(IntOffset.Zero)
		}
	}

	@Test fun testMaxIntrinsicHeight_respectsMaxIncomingConstraints() = runTest {
		runMosaicTest(NodeSnapshots) {
			setContent {
				Box {
					ConstrainedBox(Constraints(maxHeight = 55)) {
						FixedIntrinsicsBox(
							modifier = Modifier.height(IntrinsicSize.Max),
							minIntrinsicWidth = 10,
							width = 20,
							maxIntrinsicWidth = 30,
							minIntrinsicHeight = 40,
							height = 50,
							maxIntrinsicHeight = 60,
						)
					}
				}
			}

			val node = awaitSnapshot().children[0].children[0].children[0]

			assertThat(node.size).isEqualTo(IntSize(20, 55))
			assertThat(node.position).isEqualTo(IntOffset.Zero)
		}
	}

	@Test fun testMaxIntrinsicHeight_respectsMinIncomingConstraints() = runTest {
		runMosaicTest(NodeSnapshots) {
			setContent {
				Box {
					ConstrainedBox(Constraints(minHeight = 65)) {
						FixedIntrinsicsBox(
							modifier = Modifier.height(IntrinsicSize.Max),
							minIntrinsicWidth = 10,
							width = 20,
							maxIntrinsicWidth = 30,
							minIntrinsicHeight = 40,
							height = 50,
							maxIntrinsicHeight = 60,
						)
					}
				}
			}

			val node = awaitSnapshot().children[0].children[0].children[0]

			assertThat(node.size).isEqualTo(IntSize(20, 65))
			assertThat(node.position).isEqualTo(IntOffset.Zero)
		}
	}

	@Test fun testRequiredMinIntrinsicWidth() = runTest {
		runMosaicTest(NodeSnapshots) {
			setContent {
				Box {
					ConstrainedBox(Constraints.fixed(100, 100)) {
						FixedIntrinsicsBox(
							modifier = Modifier.requiredWidth(IntrinsicSize.Min),
							minIntrinsicWidth = 10,
							width = 20,
							maxIntrinsicWidth = 30,
							minIntrinsicHeight = 40,
							height = 50,
							maxIntrinsicHeight = 60,
						)
					}
				}
			}

			val node = awaitSnapshot().children[0].children[0].children[0]

			assertThat(node.size).isEqualTo(IntSize(10, 50))
		}
	}

	@Test fun testRequiredMinIntrinsicHeight() = runTest {
		runMosaicTest(NodeSnapshots) {
			setContent {
				Box {
					ConstrainedBox(Constraints.fixed(100, 100)) {
						FixedIntrinsicsBox(
							modifier = Modifier.requiredHeight(IntrinsicSize.Min),
							minIntrinsicWidth = 10,
							width = 20,
							maxIntrinsicWidth = 30,
							minIntrinsicHeight = 40,
							height = 50,
							maxIntrinsicHeight = 60,
						)
					}
				}
			}

			val node = awaitSnapshot().children[0].children[0].children[0]

			assertThat(node.size).isEqualTo(IntSize(20, 40))
		}
	}

	@Test fun testRequiredMaxIntrinsicWidth() = runTest {
		runMosaicTest(NodeSnapshots) {
			setContent {
				Box {
					ConstrainedBox(Constraints.fixed(100, 100)) {
						FixedIntrinsicsBox(
							modifier = Modifier.requiredWidth(IntrinsicSize.Max),
							minIntrinsicWidth = 10,
							width = 20,
							maxIntrinsicWidth = 30,
							minIntrinsicHeight = 40,
							height = 50,
							maxIntrinsicHeight = 60,
						)
					}
				}
			}

			val node = awaitSnapshot().children[0].children[0].children[0]

			assertThat(node.size).isEqualTo(IntSize(30, 50))
		}
	}

	@Test fun testRequiredMaxIntrinsicHeight() = runTest {
		runMosaicTest(NodeSnapshots) {
			setContent {
				Box {
					ConstrainedBox(Constraints.fixed(100, 100)) {
						FixedIntrinsicsBox(
							modifier = Modifier.requiredHeight(IntrinsicSize.Max),
							minIntrinsicWidth = 10,
							width = 20,
							maxIntrinsicWidth = 30,
							minIntrinsicHeight = 40,
							height = 50,
							maxIntrinsicHeight = 60,
						)
					}
				}
			}

			val node = awaitSnapshot().children[0].children[0].children[0]

			assertThat(node.size).isEqualTo(IntSize(20, 60))
		}
	}

	@Test fun testMinIntrinsicWidth_intrinsicMeasurements() = runTest {
		testIntrinsics({
			FixedIntrinsicsBox(
				modifier = Modifier.width(IntrinsicSize.Min),
				minIntrinsicWidth = 10,
				width = 20,
				maxIntrinsicWidth = 30,
				minIntrinsicHeight = 40,
				height = 50,
				maxIntrinsicHeight = 60,
			)
		}) { minIntrinsicWidth, minIntrinsicHeight, maxIntrinsicWidth, maxIntrinsicHeight ->
			assertThat(minIntrinsicWidth(0)).isEqualTo(10)
			assertThat(minIntrinsicHeight(0)).isEqualTo(40)
			assertThat(maxIntrinsicWidth(0)).isEqualTo(10)
			assertThat(maxIntrinsicHeight(0)).isEqualTo(60)
		}
	}

	@Test fun testMinIntrinsicHeight_intrinsicMeasurements() = runTest {
		testIntrinsics({
			FixedIntrinsicsBox(
				modifier = Modifier.height(IntrinsicSize.Min),
				minIntrinsicWidth = 10,
				width = 20,
				maxIntrinsicWidth = 30,
				minIntrinsicHeight = 40,
				height = 50,
				maxIntrinsicHeight = 60,
			)
		}) { minIntrinsicWidth, minIntrinsicHeight, maxIntrinsicWidth, maxIntrinsicHeight ->
			assertThat(minIntrinsicWidth(0)).isEqualTo(10)
			assertThat(minIntrinsicHeight(0)).isEqualTo(40)
			assertThat(maxIntrinsicWidth(0)).isEqualTo(30)
			assertThat(maxIntrinsicHeight(0)).isEqualTo(40)
		}
	}

	@Test fun testMaxIntrinsicWidth_intrinsicMeasurements() = runTest {
		testIntrinsics({
			FixedIntrinsicsBox(
				modifier = Modifier.width(IntrinsicSize.Max),
				minIntrinsicWidth = 10,
				width = 20,
				maxIntrinsicWidth = 30,
				minIntrinsicHeight = 40,
				height = 50,
				maxIntrinsicHeight = 60,
			)
		}) { minIntrinsicWidth, minIntrinsicHeight, maxIntrinsicWidth, maxIntrinsicHeight ->
			assertThat(minIntrinsicWidth(0)).isEqualTo(30)
			assertThat(minIntrinsicHeight(0)).isEqualTo(40)
			assertThat(maxIntrinsicWidth(0)).isEqualTo(30)
			assertThat(maxIntrinsicHeight(0)).isEqualTo(60)
		}
	}

	@Test fun testMaxIntrinsicHeight_intrinsicMeasurements() = runTest {
		testIntrinsics({
			FixedIntrinsicsBox(
				modifier = Modifier.height(IntrinsicSize.Max),
				minIntrinsicWidth = 10,
				width = 20,
				maxIntrinsicWidth = 30,
				minIntrinsicHeight = 40,
				height = 50,
				maxIntrinsicHeight = 60,
			)
		}) { minIntrinsicWidth, minIntrinsicHeight, maxIntrinsicWidth, maxIntrinsicHeight ->
			assertThat(minIntrinsicWidth(0)).isEqualTo(10)
			assertThat(minIntrinsicHeight(0)).isEqualTo(60)
			assertThat(maxIntrinsicWidth(0)).isEqualTo(30)
			assertThat(maxIntrinsicHeight(0)).isEqualTo(60)
		}
	}
}

@Composable
private fun FixedIntrinsicsBox(
	minIntrinsicWidth: Int,
	width: Int,
	maxIntrinsicWidth: Int,
	minIntrinsicHeight: Int,
	height: Int,
	maxIntrinsicHeight: Int,
	modifier: Modifier = Modifier,
) {
	val measurePolicy =
		object : MeasurePolicy {
			override fun MeasureScope.measure(
				measurables: List<Measurable>,
				constraints: Constraints,
			): MeasureResult {
				return layout(
					constraints.constrainWidth(width),
					constraints.constrainHeight(height),
				) {}
			}

			override fun minIntrinsicWidth(
				measurables: List<IntrinsicMeasurable>,
				height: Int,
			) = minIntrinsicWidth

			override fun minIntrinsicHeight(
				measurables: List<IntrinsicMeasurable>,
				width: Int,
			) = minIntrinsicHeight

			override fun maxIntrinsicWidth(
				measurables: List<IntrinsicMeasurable>,
				height: Int,
			) = maxIntrinsicWidth

			override fun maxIntrinsicHeight(
				measurables: List<IntrinsicMeasurable>,
				width: Int,
			) = maxIntrinsicHeight
		}
	Layout(content = {}, modifier = modifier, measurePolicy = measurePolicy)
}
