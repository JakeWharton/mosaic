package com.jakewharton.mosaic.layout

import assertk.assertThat
import assertk.assertions.isEqualTo
import com.jakewharton.mosaic.Container
import com.jakewharton.mosaic.TestFiller
import com.jakewharton.mosaic.assertFailure
import com.jakewharton.mosaic.filledRect
import com.jakewharton.mosaic.modifier.Modifier
import com.jakewharton.mosaic.testIntrinsics
import com.jakewharton.mosaic.testing.runMosaicTest
import com.jakewharton.mosaic.ui.Layout
import com.jakewharton.mosaic.ui.unit.Constraints
import kotlin.test.Test
import kotlinx.coroutines.test.runTest

class AspectRatioTest {
	private val parentSize = 100

	@Test fun aspectRatioNegative() {
		assertFailure<IllegalArgumentException> {
			Modifier.aspectRatio(-2.0f)
		}
	}

	@Test fun aspectRatioZero() {
		assertFailure<IllegalArgumentException> {
			Modifier.aspectRatio(0.0f)
		}
	}

	@Test fun aspectRatioDefault() = runTest {
		assertThat(render(1f, Constraints(maxWidth = 30))).isEqualTo(rectInParent(30, 30))
		assertThat(render(2f, Constraints(maxWidth = 30))).isEqualTo(rectInParent(30, 15))
		assertThat(render(1f, Constraints(maxWidth = 30, maxHeight = 10))).isEqualTo(rectInParent(10, 10))
		assertThat(render(2f, Constraints(maxWidth = 30, maxHeight = 10))).isEqualTo(rectInParent(20, 10))
		assertThat(render(2f, Constraints(minWidth = 10, minHeight = 5))).isEqualTo(rectInParent(10, 5))
		assertThat(render(2f, Constraints(minWidth = 5, minHeight = 10))).isEqualTo(rectInParent(20, 10))
		assertThat(render(2f, Constraints.fixed(20, 20))).isEqualTo(rectInParent(20, 10))
		assertThat(render(2f, Constraints(minWidth = 50, minHeight = 20))).isEqualTo(rectInParent(50, 25))
	}

	@Test fun aspectRatioMatchHeightConstraintsFirstTrue() = runTest {
		assertThat(render(1f, Constraints(maxHeight = 30), true)).isEqualTo(rectInParent(30, 30))
		assertThat(render(0.5f, Constraints(maxHeight = 30), true)).isEqualTo(rectInParent(15, 30))
		assertThat(render(1f, Constraints(maxWidth = 10, maxHeight = 30), true))
			.isEqualTo(rectInParent(10, 10))
		assertThat(render(0.5f, Constraints(maxWidth = 10, maxHeight = 30), true))
			.isEqualTo(rectInParent(10, 20))
		assertThat(render(0.5f, Constraints(minWidth = 5, minHeight = 10), true))
			.isEqualTo(rectInParent(5, 10))
		assertThat(render(0.5f, Constraints(minWidth = 10, minHeight = 5), true))
			.isEqualTo(rectInParent(10, 20))
		assertThat(render(0.5f, Constraints.fixed(20, 20), true))
			.isEqualTo(rectInParent(10, 20))
		assertThat(render(0.5f, Constraints(minWidth = 20, minHeight = 50), true))
			.isEqualTo(rectInParent(25, 50))
	}

	@Test fun aspectRatioIntrinsicDimensions() = runTest {
		testIntrinsics(
			{
				Container(modifier = Modifier.aspectRatio(2f), width = 30, height = 40)
			},
		) { minIntrinsicWidth, minIntrinsicHeight, maxIntrinsicWidth, maxIntrinsicHeight ->
			assertThat(minIntrinsicWidth(20)).isEqualTo(40)
			assertThat(maxIntrinsicWidth(20)).isEqualTo(40)
			assertThat(minIntrinsicHeight(40)).isEqualTo(20)
			assertThat(maxIntrinsicHeight(40)).isEqualTo(20)

			assertThat(minIntrinsicWidth(Constraints.Infinity)).isEqualTo(30)
			assertThat(maxIntrinsicWidth(Constraints.Infinity)).isEqualTo(30)
			assertThat(minIntrinsicHeight(Constraints.Infinity)).isEqualTo(40)
			assertThat(maxIntrinsicHeight(Constraints.Infinity)).isEqualTo(40)
		}
	}

	@Test fun aspectRatioDebug() {
		val actual = Modifier.aspectRatio(1.5f).toString()
		assertThat(actual).isEqualTo("AspectRatio(1.5, matchHeightConstraintsFirst=false)")
	}

	@Test fun aspectRatioMatchHeightConstraintsFirstTrueDebug() {
		val actual = Modifier.aspectRatio(1.5f, true).toString()
		assertThat(actual).isEqualTo("AspectRatio(1.5, matchHeightConstraintsFirst=true)")
	}

	private suspend fun render(
		aspectRatio: Float,
		childContraints: Constraints,
		matchHeightConstraintsFirst: Boolean = false,
	): String {
		lateinit var snapshot: String
		runMosaicTest {
			setContent {
				Layout(
					content = {
						TestFiller(Modifier.aspectRatio(aspectRatio, matchHeightConstraintsFirst))
					},
					modifier = Modifier.widthIn(max = parentSize).heightIn(max = parentSize),
					measurePolicy = { measurables, incomingConstraints ->
						require(measurables.isNotEmpty())
						val placeable = measurables.first().measure(childContraints)
						layout(incomingConstraints.maxWidth, incomingConstraints.maxHeight) {
							placeable.place(0, 0)
						}
					},
				)
			}

			snapshot = awaitSnapshot()
		}
		return snapshot
	}

	private fun rectInParent(width: Int, height: Int): String {
		return filledRect(width, height) + "\n".repeat(parentSize - height)
	}
}
