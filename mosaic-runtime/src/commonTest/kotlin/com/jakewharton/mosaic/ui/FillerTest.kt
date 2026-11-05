package com.jakewharton.mosaic.ui

import assertk.assertThat
import assertk.assertions.isEqualTo
import com.jakewharton.mosaic.Container
import com.jakewharton.mosaic.DumpSnapshots
import com.jakewharton.mosaic.Marker
import com.jakewharton.mosaic.TestFiller
import com.jakewharton.mosaic.filledRect
import com.jakewharton.mosaic.layout.height
import com.jakewharton.mosaic.layout.padding
import com.jakewharton.mosaic.layout.size
import com.jakewharton.mosaic.layout.width
import com.jakewharton.mosaic.modifier.Modifier
import com.jakewharton.mosaic.testing.runMosaicTest
import com.jakewharton.mosaic.ui.unit.Constraints
import kotlin.test.Test
import kotlinx.coroutines.test.runTest

class FillerTest {
	private val bigConstraints = Constraints(maxWidth = 5000, maxHeight = 5000)

	@Test fun fillerFixed() = runTest {
		runMosaicTest {
			val width = 4
			val height = 6

			setContent {
				TestFiller(Modifier.size(width = width, height = height))
			}
			assertThat(awaitSnapshot()).isEqualTo(
				"""
				|XXXX
				|XXXX
				|XXXX
				|XXXX
				|XXXX
				|XXXX
				""".trimMargin(),
			)
		}
	}

	@Test fun fillerFixedWithPadding() = runTest {
		runMosaicTest {
			val width = 4
			val height = 6

			setContent {
				TestFiller(Modifier.size(width = width, height = height).padding(1))
			}
			assertThat(awaitSnapshot()).isEqualTo(
				"""
				|
				| XX
				| XX
				| XX
				| XX
				|
				""".trimMargin(),
			)
		}
	}

	@Test fun fillerFixedSize() = runTest {
		runMosaicTest {
			val width = 40
			val height = 71

			setContent {
				Container(constraints = bigConstraints) {
					TestFiller(Modifier.size(width = width, height = height))
				}
			}

			assertThat(awaitSnapshot()).isEqualTo(filledRect(width, height))
		}
	}

	@Test fun fillerFixedWithSmallerContainer() = runTest {
		runMosaicTest {
			val width = 40
			val height = 71

			val containerWidth = 5
			val containerHeight = 7

			setContent {
				Box {
					Container(
						constraints = Constraints(
							maxWidth = containerWidth,
							maxHeight = containerHeight,
						),
					) {
						TestFiller(Modifier.size(width = width, height = height))
					}
				}
			}

			assertThat(awaitSnapshot()).isEqualTo(filledRect(containerWidth, containerHeight))
		}
	}

	@Test fun fillerWidth() = runTest {
		runMosaicTest {
			val width = 71

			setContent {
				Row {
					Container(constraints = bigConstraints) {
						TestFiller(Modifier.width(width))
					}
					Marker()
				}
			}

			assertThat(awaitSnapshot()).isEqualTo(" ".repeat(width) + "#")
		}
	}

	@Test fun fillerWidthWithSmallerContainer() = runTest {
		runMosaicTest {
			val width = 40

			val containerWidth = 5
			val containerHeight = 7

			setContent {
				Row {
					Box {
						Container(
							constraints = Constraints(
								maxWidth = containerWidth,
								maxHeight = containerHeight,
							),
						) {
							TestFiller(Modifier.width(width))
						}
					}
					Marker()
				}
			}

			assertThat(awaitSnapshot()).isEqualTo(" ".repeat(containerWidth) + "#")
		}
	}

	@Test fun fillerHeight() = runTest {
		runMosaicTest {
			val height = 7

			setContent {
				Column {
					Container(constraints = bigConstraints) {
						TestFiller(Modifier.height(height))
					}
					Marker()
				}
			}

			assertThat(awaitSnapshot()).isEqualTo("\n".repeat(height) + "#")
		}
	}

	@Test fun fillerHeightWithSmallerContainer() = runTest {
		runMosaicTest {
			val height = 23

			val containerWidth = 5
			val containerHeight = 7

			setContent {
				Column {
					Box {
						Container(
							constraints = Constraints(
								maxWidth = containerWidth,
								maxHeight = containerHeight,
							),
						) {
							TestFiller(Modifier.height(height))
						}
					}
					Marker()
				}
			}

			assertThat(awaitSnapshot()).isEqualTo("\n".repeat(containerHeight) + "#")
		}
	}

	@Test fun fillerDebug() = runTest {
		runMosaicTest(DumpSnapshots) {
			setContent {
				TestFiller()
			}
			assertThat(awaitSnapshot()).isEqualTo(
				"""
				|Filler('X') x=0 y=0 w=0 h=0 DrawBehind
				""".trimMargin(),
			)
		}
	}
}
