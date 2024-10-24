package com.jakewharton.mosaic.layout

import assertk.assertThat
import assertk.assertions.isEqualTo
import com.jakewharton.mosaic.TestChar
import com.jakewharton.mosaic.TestFiller
import com.jakewharton.mosaic.modifier.Modifier
import com.jakewharton.mosaic.runMosaicTest
import com.jakewharton.mosaic.s
import com.jakewharton.mosaic.ui.Box
import com.jakewharton.mosaic.ui.unit.IntOffset
import kotlin.test.Test
import kotlinx.coroutines.test.runTest

class OffsetTest {
	@Test fun offsetHorizontalFixed() = runTest {
		runMosaicTest {
			setContent {
				Box(modifier = Modifier.size(6).offset(3, 0)) {
					TestFiller(modifier = Modifier.size(1))
				}
			}
			assertThat(awaitRenderSnapshot()).isEqualTo(
				"""
				|   $TestChar $s
				|     $s
				|     $s
				|     $s
				|     $s
				|     $s
				""".trimMargin(),
			)
		}
	}

	@Test fun offsetHorizontalFixedFarBeyondBorders() = runTest {
		val size = 6
		runMosaicTest {
			setContent {
				Box(modifier = Modifier.requiredSize(size).offset(30, 0)) {
					TestFiller(modifier = Modifier.size(1))
				}
			}
			assertThat(awaitRenderSnapshot()).isEqualTo(getBlankStringBlock(size))
		}
	}

	@Test fun offsetHorizontalFixedNegativeFarBeyondBorders() = runTest {
		val size = 6
		runMosaicTest {
			setContent {
				Box(modifier = Modifier.size(size).offset(-30, 0)) {
					TestFiller(modifier = Modifier.size(1))
				}
			}
			assertThat(awaitRenderSnapshot()).isEqualTo(getBlankStringBlock(size))
		}
	}

	@Test fun offsetHorizontalFixedBeyondBorders() = runTest {
		runMosaicTest {
			setContent {
				Box(modifier = Modifier.requiredSize(6).offset(4, 0)) {
					TestFiller(modifier = Modifier.size(3))
				}
			}
			assertThat(awaitRenderSnapshot()).isEqualTo(
				"""
				|    $TestChar$TestChar
				|    $TestChar$TestChar
				|    $TestChar$TestChar
				|     $s
				|     $s
				|     $s
				""".trimMargin(),
			)
		}
	}

	@Test fun offsetHorizontalFixedNegativeBeyondBorders() = runTest {
		runMosaicTest {
			setContent {
				Box(modifier = Modifier.size(6).offset(-1, 0)) {
					TestFiller(modifier = Modifier.size(3))
				}
			}
			assertThat(awaitRenderSnapshot()).isEqualTo(
				"""
				|$TestChar$TestChar   $s
				|$TestChar$TestChar   $s
				|$TestChar$TestChar   $s
				|     $s
				|     $s
				|     $s
				""".trimMargin(),
			)
		}
	}

	@Test fun offsetVerticalFixed() = runTest {
		runMosaicTest {
			setContent {
				Box(modifier = Modifier.size(6).offset(0, 4)) {
					TestFiller(modifier = Modifier.size(1))
				}
			}
			assertThat(awaitRenderSnapshot()).isEqualTo(
				"""
				|     $s
				|     $s
				|     $s
				|     $s
				|$TestChar    $s
				|     $s
				""".trimMargin(),
			)
		}
	}

	@Test fun offsetVerticalFixedFarBeyondBorders() = runTest {
		val size = 6
		runMosaicTest {
			setContent {
				Box(modifier = Modifier.size(size).offset(0, 40)) {
					TestFiller(modifier = Modifier.size(1))
				}
			}
			assertThat(awaitRenderSnapshot()).isEqualTo(getBlankStringBlock(size))
		}
	}

	@Test fun offsetVerticalFixedNegativeFarBeyondBorders() = runTest {
		val size = 6
		runMosaicTest {
			setContent {
				Box(modifier = Modifier.size(size).offset(0, -40)) {
					TestFiller(modifier = Modifier.size(1))
				}
			}
			assertThat(awaitRenderSnapshot()).isEqualTo(getBlankStringBlock(size))
		}
	}

	@Test fun offsetVerticalFixedBeyondBorders() = runTest {
		val size = 6
		runMosaicTest {
			setContent {
				Box(modifier = Modifier.size(size).offset(0, 4)) {
					TestFiller(modifier = Modifier.size(3))
				}
			}
			assertThat(awaitRenderSnapshot()).isEqualTo(
				"""
				|     $s
				|     $s
				|     $s
				|     $s
				|$TestChar$TestChar$TestChar  $s
				|$TestChar$TestChar$TestChar  $s
				""".trimMargin(),
			)
		}
	}

	@Test fun offsetVerticalFixedNegativeBeyondBorders() = runTest {
		val size = 6
		runMosaicTest {
			setContent {
				Box(modifier = Modifier.size(size).offset(0, -1)) {
					TestFiller(modifier = Modifier.size(3))
				}
			}
			assertThat(awaitRenderSnapshot()).isEqualTo(
				"""
				|$TestChar$TestChar$TestChar  $s
				|$TestChar$TestChar$TestChar  $s
				|     $s
				|     $s
				|     $s
				|     $s
				""".trimMargin(),
			)
		}
	}

	@Test fun offsetFixed() = runTest {
		runMosaicTest {
			setContent {
				Box(modifier = Modifier.size(6).offset(3, 4)) {
					TestFiller(modifier = Modifier.size(1))
				}
			}
			assertThat(awaitRenderSnapshot()).isEqualTo(
				"""
				|     $s
				|     $s
				|     $s
				|     $s
				|   $TestChar $s
				|     $s
				""".trimMargin(),
			)
		}
	}

	@Test fun offsetFixedFarBeyondBorders() = runTest {
		val size = 6
		runMosaicTest {
			setContent {
				Box(modifier = Modifier.size(size).offset(30, 40)) {
					TestFiller(modifier = Modifier.size(1))
				}
			}
			assertThat(awaitRenderSnapshot()).isEqualTo(getBlankStringBlock(size))
		}
	}

	@Test fun offsetFixedNegativeFarBeyondBorders() = runTest {
		val size = 6
		runMosaicTest {
			setContent {
				Box(modifier = Modifier.size(size).offset(-30, -40)) {
					TestFiller(modifier = Modifier.size(1))
				}
			}
			assertThat(awaitRenderSnapshot()).isEqualTo(getBlankStringBlock(size))
		}
	}

	@Test fun offsetFixedBeyondBorders() = runTest {
		val size = 6
		runMosaicTest {
			setContent {
				Box(modifier = Modifier.size(size).offset(4, 5)) {
					TestFiller(modifier = Modifier.size(3))
				}
			}
			assertThat(awaitRenderSnapshot()).isEqualTo(
				"""
				|     $s
				|     $s
				|     $s
				|     $s
				|     $s
				|    $TestChar$TestChar
				""".trimMargin(),
			)
		}
	}

	@Test fun offsetFixedNegativeBeyondBorders() = runTest {
		val size = 6
		runMosaicTest {
			setContent {
				Box(modifier = Modifier.size(size).offset(-1, -2)) {
					TestFiller(modifier = Modifier.size(3))
				}
			}
			assertThat(awaitRenderSnapshot()).isEqualTo(
				"""
				|$TestChar$TestChar   $s
				|     $s
				|     $s
				|     $s
				|     $s
				|     $s
				""".trimMargin(),
			)
		}
	}

	@Test fun offsetFixedDebug() {
		val actual = Modifier.offset(3, 4).toString()
		assertThat(actual).isEqualTo("Offset(x=3, y=4)")
	}

	@Test fun offsetHorizontalModifiable() = runTest {
		runMosaicTest {
			setContent {
				Box(modifier = Modifier.size(6).offset { IntOffset(3, 0) }) {
					TestFiller(modifier = Modifier.size(1))
				}
			}
			assertThat(awaitRenderSnapshot()).isEqualTo(
				"""
				|   $TestChar $s
				|     $s
				|     $s
				|     $s
				|     $s
				|     $s
				""".trimMargin(),
			)
		}
	}

	@Test fun offsetHorizontalModifiableFarBeyondBorders() = runTest {
		val size = 6
		runMosaicTest {
			setContent {
				Box(modifier = Modifier.size(size).offset { IntOffset(30, 0) }) {
					TestFiller(modifier = Modifier.size(1))
				}
			}
			assertThat(awaitRenderSnapshot()).isEqualTo(getBlankStringBlock(size))
		}
	}

	@Test fun offsetHorizontalModifiableNegativeFarBeyondBorders() = runTest {
		val size = 6
		runMosaicTest {
			setContent {
				Box(modifier = Modifier.size(size).offset { IntOffset(-30, 0) }) {
					TestFiller(modifier = Modifier.size(1))
				}
			}
			assertThat(awaitRenderSnapshot()).isEqualTo(getBlankStringBlock(size))
		}
	}

	@Test fun offsetHorizontalModifiableBeyondBorders() = runTest {
		runMosaicTest {
			setContent {
				Box(modifier = Modifier.size(6).offset { IntOffset(4, 0) }) {
					TestFiller(modifier = Modifier.size(3))
				}
			}
			assertThat(awaitRenderSnapshot()).isEqualTo(
				"""
				|    $TestChar$TestChar
				|    $TestChar$TestChar
				|    $TestChar$TestChar
				|     $s
				|     $s
				|     $s
				""".trimMargin(),
			)
		}
	}

	@Test fun offsetHorizontalModifiableNegativeBeyondBorders() = runTest {
		runMosaicTest {
			setContent {
				Box(modifier = Modifier.size(6).offset { IntOffset(-1, 0) }) {
					TestFiller(modifier = Modifier.size(3))
				}
			}
			assertThat(awaitRenderSnapshot()).isEqualTo(
				"""
				|$TestChar$TestChar   $s
				|$TestChar$TestChar   $s
				|$TestChar$TestChar   $s
				|     $s
				|     $s
				|     $s
				""".trimMargin(),
			)
		}
	}

	@Test fun offsetVerticalModifiable() = runTest {
		runMosaicTest {
			setContent {
				Box(modifier = Modifier.size(6).offset { IntOffset(0, 4) }) {
					TestFiller(modifier = Modifier.size(1))
				}
			}
			assertThat(awaitRenderSnapshot()).isEqualTo(
				"""
				|     $s
				|     $s
				|     $s
				|     $s
				|$TestChar    $s
				|     $s
				""".trimMargin(),
			)
		}
	}

	@Test fun offsetVerticalModifiableFarBeyondBorders() = runTest {
		val size = 6
		runMosaicTest {
			setContent {
				Box(modifier = Modifier.size(size).offset { IntOffset(0, 40) }) {
					TestFiller(modifier = Modifier.size(1))
				}
			}
			assertThat(awaitRenderSnapshot()).isEqualTo(getBlankStringBlock(size))
		}
	}

	@Test fun offsetVerticalModifiableNegativeFarBeyondBorders() = runTest {
		val size = 6
		runMosaicTest {
			setContent {
				Box(modifier = Modifier.size(size).offset { IntOffset(0, -40) }) {
					TestFiller(modifier = Modifier.size(1))
				}
			}
			assertThat(awaitRenderSnapshot()).isEqualTo(getBlankStringBlock(size))
		}
	}

	@Test fun offsetVerticalModifiableBeyondBorders() = runTest {
		runMosaicTest {
			setContent {
				Box(modifier = Modifier.size(6).offset { IntOffset(0, 4) }) {
					TestFiller(modifier = Modifier.size(3))
				}
			}
			assertThat(awaitRenderSnapshot()).isEqualTo(
				"""
				|     $s
				|     $s
				|     $s
				|     $s
				|$TestChar$TestChar$TestChar  $s
				|$TestChar$TestChar$TestChar  $s
				""".trimMargin(),
			)
		}
	}

	@Test fun offsetVerticalModifiableNegativeBeyondBorders() = runTest {
		runMosaicTest {
			setContent {
				Box(modifier = Modifier.size(6).offset { IntOffset(0, -1) }) {
					TestFiller(modifier = Modifier.size(3))
				}
			}
			assertThat(awaitRenderSnapshot()).isEqualTo(
				"""
				|$TestChar$TestChar$TestChar  $s
				|$TestChar$TestChar$TestChar  $s
				|     $s
				|     $s
				|     $s
				|     $s
				""".trimMargin(),
			)
		}
	}

	@Test fun offsetModifiable() = runTest {
		runMosaicTest {
			setContent {
				Box(modifier = Modifier.size(6).offset { IntOffset(3, 4) }) {
					TestFiller(modifier = Modifier.size(1))
				}
			}
			assertThat(awaitRenderSnapshot()).isEqualTo(
				"""
				|     $s
				|     $s
				|     $s
				|     $s
				|   $TestChar $s
				|     $s
				""".trimMargin(),
			)
		}
	}

	@Test fun offsetModifiableFarBeyondBorders() = runTest {
		val size = 6
		runMosaicTest {
			setContent {
				Box(modifier = Modifier.size(size).offset { IntOffset(30, 40) }) {
					TestFiller(modifier = Modifier.size(1))
				}
			}
			assertThat(awaitRenderSnapshot()).isEqualTo(getBlankStringBlock(size))
		}
	}

	@Test fun offsetModifiableNegativeFarBeyondBorders() = runTest {
		val size = 6
		runMosaicTest {
			setContent {
				Box(modifier = Modifier.size(size).offset { IntOffset(-30, -40) }) {
					TestFiller(modifier = Modifier.size(1))
				}
			}
			assertThat(awaitRenderSnapshot()).isEqualTo(getBlankStringBlock(size))
		}
	}

	@Test fun offsetModifiableBeyondBorders() = runTest {
		runMosaicTest {
			setContent {
				Box(modifier = Modifier.size(6).offset { IntOffset(4, 5) }) {
					TestFiller(modifier = Modifier.size(3))
				}
			}
			assertThat(awaitRenderSnapshot()).isEqualTo(
				"""
				|     $s
				|     $s
				|     $s
				|     $s
				|     $s
				|    $TestChar$TestChar
				""".trimMargin(),
			)
		}
	}

	@Test fun offsetModifiableNegativeBeyondBorders() = runTest {
		runMosaicTest {
			setContent {
				Box(modifier = Modifier.size(6).offset { IntOffset(-1, -2) }) {
					TestFiller(modifier = Modifier.size(3))
				}
			}
			assertThat(awaitRenderSnapshot()).isEqualTo(
				"""
				|$TestChar$TestChar   $s
				|     $s
				|     $s
				|     $s
				|     $s
				|     $s
				""".trimMargin(),
			)
		}
	}

	@Test fun offsetModifiableDebug() {
		val offsetLambda = { IntOffset(-3, -4) }
		val actual = Modifier.offset(offsetLambda).toString()
		assertThat(actual).isEqualTo("ChangeableOffset(offset=$offsetLambda)")
	}

	private fun getBlankStringBlock(size: Int): String {
		val line = s.repeat(size)
		return buildString {
			repeat(size) {
				appendLine(line)
			}
		}.removeSuffix("\n")
	}
}
