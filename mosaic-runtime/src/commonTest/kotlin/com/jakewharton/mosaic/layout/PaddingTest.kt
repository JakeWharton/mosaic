package com.jakewharton.mosaic.layout

import androidx.compose.runtime.Composable
import assertk.assertThat
import assertk.assertions.isEqualTo
import com.jakewharton.mosaic.Container
import com.jakewharton.mosaic.TestChar
import com.jakewharton.mosaic.TestFiller
import com.jakewharton.mosaic.modifier.Modifier
import com.jakewharton.mosaic.runMosaicTest
import com.jakewharton.mosaic.s
import com.jakewharton.mosaic.testIntrinsics
import com.jakewharton.mosaic.ui.Layout
import com.jakewharton.mosaic.ui.unit.Constraints
import kotlin.test.Test
import kotlin.test.assertFails
import kotlinx.coroutines.test.runTest

class PaddingTest {
	@Test fun paddingAllEqualsToPaddingWithExplicitSides() {
		assertThat(Modifier.padding(10))
			.isEqualTo(Modifier.padding(10, 10, 10, 10))
	}

	@Test fun paddingSymmerticEqualsToPaddingWithExplicitSides() {
		assertThat(Modifier.padding(10, 20))
			.isEqualTo(Modifier.padding(10, 20, 10, 20))
	}

	@Test fun paddingLeftNegative() {
		assertFails {
			Modifier.padding(left = -2)
		}
	}

	@Test fun paddingLeftZero() = runTest {
		runMosaicTest {
			setContent {
				SingleFiller(Modifier.padding(left = 0))
			}
			assertThat(awaitSnapshot()).isEqualTo(
				"""
				|$TestChar
				""".trimMargin(),
			)
		}
	}

	@Test fun paddingLeft() = runTest {
		runMosaicTest {
			setContent {
				SingleFiller(Modifier.padding(left = 2))
			}
			assertThat(awaitSnapshot()).isEqualTo(
				"""
				|  $TestChar
				""".trimMargin(),
			)
		}
	}

	@Test fun paddingLeftDebug() {
		val actual = Modifier.padding(left = 2).toString()
		assertThat(actual).isEqualTo("Padding(l=2, t=0, r=0, b=0)")
	}

	@Test fun paddingTopNegative() {
		assertFails {
			Modifier.padding(top = -2)
		}
	}

	@Test fun paddingTopZero() = runTest {
		runMosaicTest {
			setContent {
				SingleFiller(Modifier.padding(top = 0))
			}
			assertThat(awaitSnapshot()).isEqualTo(
				"""
				|$TestChar
				""".trimMargin(),
			)
		}
	}

	@Test fun paddingTop() = runTest {
		runMosaicTest {
			setContent {
				SingleFiller(Modifier.padding(top = 2))
			}
			assertThat(awaitSnapshot()).isEqualTo(
				"""
				|$s
				|$s
				|$TestChar
				""".trimMargin(),
			)
		}
	}

	@Test fun paddingTopDebug() {
		val actual = Modifier.padding(top = 2).toString()
		assertThat(actual).isEqualTo("Padding(l=0, t=2, r=0, b=0)")
	}

	@Test fun paddingRightNegative() {
		assertFails {
			Modifier.padding(right = -2)
		}
	}

	@Test fun paddingRightZero() = runTest {
		runMosaicTest {
			setContent {
				SingleFiller(Modifier.padding(right = 0))
			}
			assertThat(awaitSnapshot()).isEqualTo(
				"""
				|$TestChar
				""".trimMargin(),
			)
		}
	}

	@Test fun paddingRight() = runTest {
		runMosaicTest {
			setContent {
				SingleFiller(Modifier.padding(right = 2))
			}
			assertThat(awaitSnapshot()).isEqualTo(
				"""
				|$TestChar $s
				""".trimMargin(),
			)
		}
	}

	@Test fun paddingRightDebug() = runTest {
		val actual = Modifier.padding(right = 2).toString()
		assertThat(actual).isEqualTo("Padding(l=0, t=0, r=2, b=0)")
	}

	@Test fun paddingBottomNegative() {
		assertFails {
			Modifier.padding(bottom = -2)
		}
	}

	@Test fun paddingBottomZero() = runTest {
		runMosaicTest {
			setContent {
				SingleFiller(Modifier.padding(bottom = 0))
			}
			assertThat(awaitSnapshot()).isEqualTo(
				"""
				|$TestChar
				""".trimMargin(),
			)
		}
	}

	@Test fun paddingBottom() = runTest {
		runMosaicTest {
			setContent {
				SingleFiller(Modifier.padding(bottom = 2))
			}
			assertThat(awaitSnapshot()).isEqualTo(
				"""
				|$TestChar
				|$s
				|$s
				""".trimMargin(),
			)
		}
	}

	@Test fun paddingBottomDebug() {
		val actual = Modifier.padding(bottom = 2).toString()
		assertThat(actual).isEqualTo("Padding(l=0, t=0, r=0, b=2)")
	}

	@Test fun paddingLeftBottomNegative() {
		assertFails {
			Modifier.padding(left = -1, bottom = -2)
		}
	}

	@Test fun paddingLeftBottomZero() = runTest {
		runMosaicTest {
			setContent {
				SingleFiller(Modifier.padding(left = 0, bottom = 0))
			}
			assertThat(awaitSnapshot()).isEqualTo(
				"""
				|$TestChar
				""".trimMargin(),
			)
		}
	}

	@Test fun paddingLeftBottom() = runTest {
		runMosaicTest {
			setContent {
				SingleFiller(Modifier.padding(left = 1, bottom = 2))
			}
			assertThat(awaitSnapshot()).isEqualTo(
				"""
				| $TestChar
				| $s
				| $s
				""".trimMargin(),
			)
		}
	}

	@Test fun paddingLeftBottomDebug() {
		val actual = Modifier.padding(left = 1, bottom = 2).toString()
		assertThat(actual).isEqualTo("Padding(l=1, t=0, r=0, b=2)")
	}

	@Test fun paddingAllDebug() {
		val actual = Modifier.padding(2).toString()
		assertThat(actual).isEqualTo("Padding(2)")
	}

	@Test fun paddingHorizontalDebug() {
		val actual = Modifier.padding(horizontal = 2).toString()
		assertThat(actual).isEqualTo("Padding(h=2, v=0)")
	}

	@Test fun paddingVerticalDebug() {
		val actual = Modifier.padding(vertical = 2).toString()
		assertThat(actual).isEqualTo("Padding(h=0, v=2)")
	}

	@Test fun intrinsicMeasurements() = runTest {
		val padding = 100

		testIntrinsics(
			@Composable {
				TestBox(modifier = Modifier.padding(padding)) {
					Container(Modifier.aspectRatio(2f)) { }
				}
			},
		) { minIntrinsicWidth, minIntrinsicHeight, maxIntrinsicWidth, maxIntrinsicHeight ->
			// Spacing is applied on both sides of an axis
			val totalAxisSpacing = padding * 2

			// When the width/height is measured as 3 x the padding
			val testDimension = padding * 3
			// The actual dimension for the AspectRatio will be: test dimension - total padding
			val actualAspectRatioDimension = testDimension - totalAxisSpacing

			// When we measure the width first, the height will be half
			val expectedAspectRatioHeight = actualAspectRatioDimension / 2f
			// When we measure the height first, the width will be double
			val expectedAspectRatioWidth = actualAspectRatioDimension * 2

			// Add back the padding on both sides to get the total expected height
			val expectedTotalHeight = (expectedAspectRatioHeight + totalAxisSpacing).toInt()
			// Add back the padding on both sides to get the total expected height
			val expectedTotalWidth = expectedAspectRatioWidth + totalAxisSpacing

			// Min width
			assertThat(totalAxisSpacing).isEqualTo(minIntrinsicWidth(0))
			assertThat(expectedTotalWidth).isEqualTo(minIntrinsicWidth(testDimension))
			assertThat(totalAxisSpacing).isEqualTo(minIntrinsicWidth(Constraints.Infinity))
			// Min height
			assertThat(totalAxisSpacing).isEqualTo(minIntrinsicHeight(0))
			assertThat(expectedTotalHeight).isEqualTo(minIntrinsicHeight(testDimension))
			assertThat(totalAxisSpacing).isEqualTo(minIntrinsicHeight(Constraints.Infinity))
			// Max width
			assertThat(totalAxisSpacing).isEqualTo(maxIntrinsicWidth(0))
			assertThat(expectedTotalWidth).isEqualTo(maxIntrinsicWidth(testDimension))
			assertThat(totalAxisSpacing).isEqualTo(maxIntrinsicWidth(Constraints.Infinity))
			// Max height
			assertThat(totalAxisSpacing).isEqualTo(maxIntrinsicHeight(0))
			assertThat(expectedTotalHeight).isEqualTo(maxIntrinsicHeight(testDimension))
			assertThat(totalAxisSpacing).isEqualTo(maxIntrinsicHeight(Constraints.Infinity))
		}
	}

	/**
	 * A trivial layout that applies a [Modifier] and measures/lays out a single child
	 * with the same constraints it received.
	 */
	@Composable
	private fun TestBox(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
		Layout(content = content, modifier = modifier) { measurables, constraints ->
			require(measurables.size == 1) {
				"TestBox received ${measurables.size} children; must have exactly 1"
			}
			val placeable = measurables.first().measure(constraints)
			layout(
				placeable.width.coerceAtMost(constraints.maxWidth),
				placeable.height.coerceAtMost(constraints.maxHeight),
			) {
				placeable.place(0, 0)
			}
		}
	}

	@Composable
	private fun SingleFiller(modifier: Modifier = Modifier) {
		TestFiller(modifier = modifier.size(1))
	}
}
