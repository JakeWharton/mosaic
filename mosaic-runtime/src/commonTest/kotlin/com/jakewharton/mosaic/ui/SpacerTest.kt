package com.jakewharton.mosaic.ui

import assertk.assertThat
import assertk.assertions.isEqualTo
import com.jakewharton.mosaic.Container
import com.jakewharton.mosaic.DumpSnapshots
import com.jakewharton.mosaic.Marker
import com.jakewharton.mosaic.fillWith
import com.jakewharton.mosaic.filledRect
import com.jakewharton.mosaic.layout.background
import com.jakewharton.mosaic.layout.height
import com.jakewharton.mosaic.layout.size
import com.jakewharton.mosaic.layout.width
import com.jakewharton.mosaic.modifier.Modifier
import com.jakewharton.mosaic.s
import com.jakewharton.mosaic.testing.runMosaicTest
import com.jakewharton.mosaic.ui.unit.Constraints
import kotlin.test.Test
import kotlinx.coroutines.test.runTest

class SpacerTest {
	private val bigConstraints = Constraints(maxWidth = 5000, maxHeight = 5000)

	@Test fun spacerFixed() = runTest {
		runMosaicTest {
			val width = 4
			val height = 6

			setContent {
				Spacer(Modifier.background(Color.Red).size(width = width, height = height))
			}
			assertThat(awaitSnapshot()).isEqualTo(
				"""
				|   $s
				|   $s
				|   $s
				|   $s
				|   $s
				|   $s
				""".trimMargin(),
			)
		}
	}

	@Test fun spacerFixedSize() = runTest {
		runMosaicTest {
			val width = 40
			val height = 71

			setContent {
				Container(constraints = bigConstraints) {
					Spacer(Modifier.size(width = width, height = height).fillWith('S'))
				}
			}

			assertThat(awaitSnapshot()).isEqualTo(filledRect(width, height, 'S'))
		}
	}

	@Test fun spacerFixedWithSmallerContainer() = runTest {
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
						Spacer(Modifier.size(width = width, height = height).fillWith('S'))
					}
				}
			}

			assertThat(awaitSnapshot()).isEqualTo(filledRect(containerWidth, containerHeight, 'S'))
		}
	}

	@Test fun spacerWidth() = runTest {
		runMosaicTest {
			val width = 71

			setContent {
				Row {
					Container(constraints = bigConstraints) {
						Spacer(Modifier.width(width).fillWith('S'))
					}
					Marker()
				}
			}

			assertThat(awaitSnapshot()).isEqualTo(" ".repeat(width) + "#")
		}
	}

	@Test fun spacerWidthWithSmallerContainer() = runTest {
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
							Spacer(Modifier.width(width).fillWith('S'))
						}
					}
					Marker()
				}
			}

			assertThat(awaitSnapshot()).isEqualTo(" ".repeat(containerWidth) + "#")
		}
	}

	@Test fun spacerHeight() = runTest {
		runMosaicTest {
			val height = 7

			setContent {
				Column {
					Container(constraints = bigConstraints) {
						Spacer(Modifier.height(height).fillWith('S'))
					}
					Marker()
				}
			}

			assertThat(awaitSnapshot()).isEqualTo("\n".repeat(height) + "#")
		}
	}

	@Test fun spacerHeightWithSmallerContainer() = runTest {
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
							Spacer(Modifier.height(height).fillWith('S'))
						}
					}
					Marker()
				}
			}

			assertThat(awaitSnapshot()).isEqualTo("\n".repeat(containerHeight) + "#")
		}
	}

	@Test fun spacerDebug() = runTest {
		runMosaicTest(DumpSnapshots) {
			setContent {
				Spacer()
			}
			assertThat(awaitSnapshot()).isEqualTo(
				"""
				|Spacer() x=0 y=0 w=0 h=0
				""".trimMargin(),
			)
		}
	}
}
