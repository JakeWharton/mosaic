package com.jakewharton.mosaic.layout

import androidx.compose.runtime.Composable
import assertk.assertThat
import assertk.assertions.isEqualTo
import com.jakewharton.mosaic.ConstrainedBox
import com.jakewharton.mosaic.Marker
import com.jakewharton.mosaic.fillWith
import com.jakewharton.mosaic.filledRect
import com.jakewharton.mosaic.modifier.Modifier
import com.jakewharton.mosaic.testIntrinsics
import com.jakewharton.mosaic.testing.runMosaicTest
import com.jakewharton.mosaic.ui.Box
import com.jakewharton.mosaic.ui.Column
import com.jakewharton.mosaic.ui.Layout
import com.jakewharton.mosaic.ui.Row
import com.jakewharton.mosaic.ui.unit.Constraints
import com.jakewharton.mosaic.ui.unit.constrainHeight
import com.jakewharton.mosaic.ui.unit.constrainWidth
import kotlin.test.Test
import kotlinx.coroutines.test.runTest

class IntrinsicTest {
	@Test fun testMaxIntrinsic_HandleNegative() = runTest {
		runMosaicTest {
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
			val layouts: @Composable () -> Unit = {
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

			setContent {
				Column {
					Row {
						layouts()
						Marker()
					}
					Column {
						layouts()
						Marker()
					}
				}
			}

			assertThat(awaitSnapshot()).isEqualTo(
				"""
				|#
				|#
				""".trimMargin(),
			)
		}
	}

	@Test fun testMinIntrinsicWidth() = runTest {
		runMosaicTest {
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

			assertThat(awaitSnapshot()).isEqualTo(filledRect(10, 50, 'X'))
		}
	}

	@Test fun testMinIntrinsicHeight() = runTest {
		runMosaicTest {
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

			assertThat(awaitSnapshot()).isEqualTo(filledRect(20, 40, 'X'))
		}
	}

	@Test fun testMaxIntrinsicWidth() = runTest {
		runMosaicTest {
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

			assertThat(awaitSnapshot()).isEqualTo(filledRect(30, 50, 'X'))
		}
	}

	@Test fun testMaxIntrinsicHeight() = runTest {
		runMosaicTest {
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

			assertThat(awaitSnapshot()).isEqualTo(filledRect(20, 60, 'X'))
		}
	}

	@Test fun testMinIntrinsicWidth_respectsIncomingMaxConstraints() = runTest {
		runMosaicTest {
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

			assertThat(awaitSnapshot()).isEqualTo(filledRect(5, 50, 'X'))
		}
	}

	@Test fun testMinIntrinsicWidth_respectsIncomingMinConstraints() = runTest {
		runMosaicTest {
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

			assertThat(awaitSnapshot()).isEqualTo(filledRect(15, 50, 'X'))
		}
	}

	@Test fun testMinIntrinsicHeight_respectsMaxIncomingConstraints() = runTest {
		runMosaicTest {
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

			assertThat(awaitSnapshot()).isEqualTo(filledRect(20, 35, 'X'))
		}
	}

	@Test fun testMinIntrinsicHeight_respectsMinIncomingConstraints() = runTest {
		runMosaicTest {
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

			assertThat(awaitSnapshot()).isEqualTo(filledRect(20, 45, 'X'))
		}
	}

	@Test fun testMaxIntrinsicWidth_respectsMaxIncomingConstraints() = runTest {
		runMosaicTest {
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

			assertThat(awaitSnapshot()).isEqualTo(filledRect(25, 50, 'X'))
		}
	}

	@Test fun testMaxIntrinsicWidth_respectsMinIncomingConstraints() = runTest {
		runMosaicTest {
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

			assertThat(awaitSnapshot()).isEqualTo(filledRect(35, 50, 'X'))
		}
	}

	@Test fun testMaxIntrinsicHeight_respectsMaxIncomingConstraints() = runTest {
		runMosaicTest {
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

			assertThat(awaitSnapshot()).isEqualTo(filledRect(20, 55, 'X'))
		}
	}

	@Test fun testMaxIntrinsicHeight_respectsMinIncomingConstraints() = runTest {
		runMosaicTest {
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

			assertThat(awaitSnapshot()).isEqualTo(filledRect(20, 65, 'X'))
		}
	}

	@Test fun testRequiredMinIntrinsicWidth() = runTest {
		runMosaicTest {
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

			assertThat(awaitSnapshot()).isEqualTo(filledRect(10, 50, 'X'))
		}
	}

	@Test fun testRequiredMinIntrinsicHeight() = runTest {
		runMosaicTest {
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

			assertThat(awaitSnapshot()).isEqualTo(filledRect(20, 40, 'X'))
		}
	}

	@Test fun testRequiredMaxIntrinsicWidth() = runTest {
		runMosaicTest {
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

			assertThat(awaitSnapshot()).isEqualTo(filledRect(30, 50, 'X'))
		}
	}

	@Test fun testRequiredMaxIntrinsicHeight() = runTest {
		runMosaicTest {
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

			assertThat(awaitSnapshot()).isEqualTo(filledRect(20, 60, 'X'))
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
	Layout(content = {}, modifier = Modifier.fillWith('X').then(modifier), measurePolicy = measurePolicy)
}
