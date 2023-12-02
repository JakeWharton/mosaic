package com.jakewharton.mosaic.layout

import assertk.assertThat
import assertk.assertions.isEqualTo
import com.jakewharton.mosaic.TestChar
import com.jakewharton.mosaic.TestFiller
import com.jakewharton.mosaic.modifier.Modifier
import com.jakewharton.mosaic.renderMosaic
import com.jakewharton.mosaic.s
import com.jakewharton.mosaic.ui.Box
import com.jakewharton.mosaic.ui.unit.IntOffset
import kotlin.test.Test

class OffsetTest {
	@Test fun offsetHorizontalFixed() {
		val actual = renderMosaic {
			Box(modifier = Modifier.size(6).offset(3, 0)) {
				TestFiller(modifier = Modifier.size(1))
			}
		}
		assertThat(actual).isEqualTo(
			"""
			|   $TestChar $s
			|     $s
			|     $s
			|     $s
			|     $s
			|     $s
			|
			""".trimMargin(),
		)
	}

	@Test fun offsetHorizontalFixedFarBeyondBorders() {
		val size = 6
		val actual = renderMosaic {
			Box(modifier = Modifier.requiredSize(size).offset(30, 0)) {
				TestFiller(modifier = Modifier.size(1))
			}
		}
		assertThat(actual).isEqualTo(getBlankStringBlock(size))
	}

	@Test fun offsetHorizontalFixedNegativeFarBeyondBorders() {
		val size = 6
		val actual = renderMosaic {
			Box(modifier = Modifier.size(size).offset(-30, 0)) {
				TestFiller(modifier = Modifier.size(1))
			}
		}
		assertThat(actual).isEqualTo(getBlankStringBlock(size))
	}

	@Test fun offsetHorizontalFixedBeyondBorders() {
		val size = 6
		val actual = renderMosaic {
			Box(modifier = Modifier.requiredSize(size).offset(4, 0)) {
				TestFiller(modifier = Modifier.size(3))
			}
		}
		assertThat(actual).isEqualTo(
			"""
				|    $TestChar$TestChar
				|    $TestChar$TestChar
				|    $TestChar$TestChar
				|     $s
				|     $s
				|     $s
				|
			""".trimMargin(),
		)
	}

	@Test fun offsetHorizontalFixedNegativeBeyondBorders() {
		val size = 6
		val actual = renderMosaic {
			Box(modifier = Modifier.size(size).offset(-1, 0)) {
				TestFiller(modifier = Modifier.size(3))
			}
		}
		assertThat(actual).isEqualTo(
			"""
				|$TestChar$TestChar   $s
				|$TestChar$TestChar   $s
				|$TestChar$TestChar   $s
				|     $s
				|     $s
				|     $s
				|
			""".trimMargin(),
		)
	}

	@Test fun offsetVerticalFixed() {
		val actual = renderMosaic {
			Box(modifier = Modifier.size(6).offset(0, 4)) {
				TestFiller(modifier = Modifier.size(1))
			}
		}
		assertThat(actual).isEqualTo(
			"""
			|     $s
			|     $s
			|     $s
			|     $s
			|$TestChar    $s
			|     $s
			|
			""".trimMargin(),
		)
	}

	@Test fun offsetVerticalFixedFarBeyondBorders() {
		val size = 6
		val actual = renderMosaic {
			Box(modifier = Modifier.size(size).offset(0, 40)) {
				TestFiller(modifier = Modifier.size(1))
			}
		}
		assertThat(actual).isEqualTo(getBlankStringBlock(size))
	}

	@Test fun offsetVerticalFixedNegativeFarBeyondBorders() {
		val size = 6
		val actual = renderMosaic {
			Box(modifier = Modifier.size(size).offset(0, -40)) {
				TestFiller(modifier = Modifier.size(1))
			}
		}
		assertThat(actual).isEqualTo(getBlankStringBlock(size))
	}

	@Test fun offsetVerticalFixedBeyondBorders() {
		val size = 6
		val actual = renderMosaic {
			Box(modifier = Modifier.size(size).offset(0, 4)) {
				TestFiller(modifier = Modifier.size(3))
			}
		}
		assertThat(actual).isEqualTo(
			"""
				|     $s
				|     $s
				|     $s
				|     $s
				|$TestChar$TestChar$TestChar  $s
				|$TestChar$TestChar$TestChar  $s
				|
			""".trimMargin(),
		)
	}

	@Test fun offsetVerticalFixedNegativeBeyondBorders() {
		val size = 6
		val actual = renderMosaic {
			Box(modifier = Modifier.size(size).offset(0, -1)) {
				TestFiller(modifier = Modifier.size(3))
			}
		}
		assertThat(actual).isEqualTo(
			"""
				|$TestChar$TestChar$TestChar  $s
				|$TestChar$TestChar$TestChar  $s
				|     $s
				|     $s
				|     $s
				|     $s
				|
			""".trimMargin(),
		)
	}

	@Test fun offsetFixed() {
		val actual = renderMosaic {
			Box(modifier = Modifier.size(6).offset(3, 4)) {
				TestFiller(modifier = Modifier.size(1))
			}
		}
		assertThat(actual).isEqualTo(
			"""
			|     $s
			|     $s
			|     $s
			|     $s
			|   $TestChar $s
			|     $s
			|
			""".trimMargin(),
		)
	}

	@Test fun offsetFixedFarBeyondBorders() {
		val size = 6
		val actual = renderMosaic {
			Box(modifier = Modifier.size(size).offset(30, 40)) {
				TestFiller(modifier = Modifier.size(1))
			}
		}
		assertThat(actual).isEqualTo(getBlankStringBlock(size))
	}

	@Test fun offsetFixedNegativeFarBeyondBorders() {
		val size = 6
		val actual = renderMosaic {
			Box(modifier = Modifier.size(size).offset(-30, -40)) {
				TestFiller(modifier = Modifier.size(1))
			}
		}
		assertThat(actual).isEqualTo(getBlankStringBlock(size))
	}

	@Test fun offsetFixedBeyondBorders() {
		val size = 6
		val actual = renderMosaic {
			Box(modifier = Modifier.size(size).offset(4, 5)) {
				TestFiller(modifier = Modifier.size(3))
			}
		}
		assertThat(actual).isEqualTo(
			"""
				|     $s
				|     $s
				|     $s
				|     $s
				|     $s
				|    $TestChar$TestChar
				|
			""".trimMargin(),
		)
	}

	@Test fun offsetFixedNegativeBeyondBorders() {
		val size = 6
		val actual = renderMosaic {
			Box(modifier = Modifier.size(size).offset(-1, -2)) {
				TestFiller(modifier = Modifier.size(3))
			}
		}
		assertThat(actual).isEqualTo(
			"""
				|$TestChar$TestChar   $s
				|     $s
				|     $s
				|     $s
				|     $s
				|     $s
				|
			""".trimMargin(),
		)
	}

	@Test fun offsetFixedDebug() {
		val actual = Modifier.offset(3, 4).toString()
		assertThat(actual).isEqualTo("Offset(x=3, y=4)")
	}

	@Test fun offsetHorizontalModifiable() {
		val actual = renderMosaic {
			Box(modifier = Modifier.size(6).offset { IntOffset(3, 0) }) {
				TestFiller(modifier = Modifier.size(1))
			}
		}
		assertThat(actual).isEqualTo(
			"""
			|   $TestChar $s
			|     $s
			|     $s
			|     $s
			|     $s
			|     $s
			|
			""".trimMargin(),
		)
	}

	@Test fun offsetHorizontalModifiableFarBeyondBorders() {
		val size = 6
		val actual = renderMosaic {
			Box(modifier = Modifier.size(size).offset { IntOffset(30, 0) }) {
				TestFiller(modifier = Modifier.size(1))
			}
		}
		assertThat(actual).isEqualTo(getBlankStringBlock(size))
	}

	@Test fun offsetHorizontalModifiableNegativeFarBeyondBorders() {
		val size = 6
		val actual = renderMosaic {
			Box(modifier = Modifier.size(size).offset { IntOffset(-30, 0) }) {
				TestFiller(modifier = Modifier.size(1))
			}
		}
		assertThat(actual).isEqualTo(getBlankStringBlock(size))
	}

	@Test fun offsetHorizontalModifiableBeyondBorders() {
		val size = 6
		val actual = renderMosaic {
			Box(modifier = Modifier.size(size).offset { IntOffset(4, 0) }) {
				TestFiller(modifier = Modifier.size(3))
			}
		}
		assertThat(actual).isEqualTo(
			"""
				|    $TestChar$TestChar
				|    $TestChar$TestChar
				|    $TestChar$TestChar
				|     $s
				|     $s
				|     $s
				|
			""".trimMargin(),
		)
	}

	@Test fun offsetHorizontalModifiableNegativeBeyondBorders() {
		val size = 6
		val actual = renderMosaic {
			Box(modifier = Modifier.size(size).offset { IntOffset(-1, 0) }) {
				TestFiller(modifier = Modifier.size(3))
			}
		}
		assertThat(actual).isEqualTo(
			"""
				|$TestChar$TestChar   $s
				|$TestChar$TestChar   $s
				|$TestChar$TestChar   $s
				|     $s
				|     $s
				|     $s
				|
			""".trimMargin(),
		)
	}

	@Test fun offsetVerticalModifiable() {
		val actual = renderMosaic {
			Box(modifier = Modifier.size(6).offset { IntOffset(0, 4) }) {
				TestFiller(modifier = Modifier.size(1))
			}
		}
		assertThat(actual).isEqualTo(
			"""
			|     $s
			|     $s
			|     $s
			|     $s
			|$TestChar    $s
			|     $s
			|
			""".trimMargin(),
		)
	}

	@Test fun offsetVerticalModifiableFarBeyondBorders() {
		val size = 6
		val actual = renderMosaic {
			Box(modifier = Modifier.size(size).offset { IntOffset(0, 40) }) {
				TestFiller(modifier = Modifier.size(1))
			}
		}
		assertThat(actual).isEqualTo(getBlankStringBlock(size))
	}

	@Test fun offsetVerticalModifiableNegativeFarBeyondBorders() {
		val size = 6
		val actual = renderMosaic {
			Box(modifier = Modifier.size(size).offset { IntOffset(0, -40) }) {
				TestFiller(modifier = Modifier.size(1))
			}
		}
		assertThat(actual).isEqualTo(getBlankStringBlock(size))
	}

	@Test fun offsetVerticalModifiableBeyondBorders() {
		val size = 6
		val actual = renderMosaic {
			Box(modifier = Modifier.size(size).offset { IntOffset(0, 4) }) {
				TestFiller(modifier = Modifier.size(3))
			}
		}
		assertThat(actual).isEqualTo(
			"""
				|     $s
				|     $s
				|     $s
				|     $s
				|$TestChar$TestChar$TestChar  $s
				|$TestChar$TestChar$TestChar  $s
				|
			""".trimMargin(),
		)
	}

	@Test fun offsetVerticalModifiableNegativeBeyondBorders() {
		val size = 6
		val actual = renderMosaic {
			Box(modifier = Modifier.size(size).offset { IntOffset(0, -1) }) {
				TestFiller(modifier = Modifier.size(3))
			}
		}
		assertThat(actual).isEqualTo(
			"""
				|$TestChar$TestChar$TestChar  $s
				|$TestChar$TestChar$TestChar  $s
				|     $s
				|     $s
				|     $s
				|     $s
				|
			""".trimMargin(),
		)
	}

	@Test fun offsetModifiable() {
		val actual = renderMosaic {
			Box(modifier = Modifier.size(6).offset { IntOffset(3, 4) }) {
				TestFiller(modifier = Modifier.size(1))
			}
		}
		assertThat(actual).isEqualTo(
			"""
			|     $s
			|     $s
			|     $s
			|     $s
			|   $TestChar $s
			|     $s
			|
			""".trimMargin(),
		)
	}

	@Test fun offsetModifiableFarBeyondBorders() {
		val size = 6
		val actual = renderMosaic {
			Box(modifier = Modifier.size(size).offset { IntOffset(30, 40) }) {
				TestFiller(modifier = Modifier.size(1))
			}
		}
		assertThat(actual).isEqualTo(getBlankStringBlock(size))
	}

	@Test fun offsetModifiableNegativeFarBeyondBorders() {
		val size = 6
		val actual = renderMosaic {
			Box(modifier = Modifier.size(size).offset { IntOffset(-30, -40) }) {
				TestFiller(modifier = Modifier.size(1))
			}
		}
		assertThat(actual).isEqualTo(getBlankStringBlock(size))
	}

	@Test fun offsetModifiableBeyondBorders() {
		val size = 6
		val actual = renderMosaic {
			Box(modifier = Modifier.size(size).offset { IntOffset(4, 5) }) {
				TestFiller(modifier = Modifier.size(3))
			}
		}
		assertThat(actual).isEqualTo(
			"""
				|     $s
				|     $s
				|     $s
				|     $s
				|     $s
				|    $TestChar$TestChar
				|
			""".trimMargin(),
		)
	}

	@Test fun offsetModifiableNegativeBeyondBorders() {
		val size = 6
		val actual = renderMosaic {
			Box(modifier = Modifier.size(size).offset { IntOffset(-1, -2) }) {
				TestFiller(modifier = Modifier.size(3))
			}
		}
		assertThat(actual).isEqualTo(
			"""
				|$TestChar$TestChar   $s
				|     $s
				|     $s
				|     $s
				|     $s
				|     $s
				|
			""".trimMargin(),
		)
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
		}
	}
}
