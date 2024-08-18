package com.jakewharton.mosaic.layout

import assertk.assertThat
import assertk.assertions.isEqualTo
import com.jakewharton.mosaic.TestChar
import com.jakewharton.mosaic.TestFiller
import com.jakewharton.mosaic.modifier.Modifier
import com.jakewharton.mosaic.renderMosaic
import com.jakewharton.mosaic.replaceLineEndingsWithCRLF
import com.jakewharton.mosaic.s
import com.jakewharton.mosaic.ui.Box
import com.jakewharton.mosaic.ui.unit.IntOffset
import com.jakewharton.mosaic.wrapWithAnsiSynchronizedUpdate
import kotlin.test.Test
import kotlin.test.assertFails

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
			""".trimMargin().wrapWithAnsiSynchronizedUpdate().replaceLineEndingsWithCRLF(),
		)
	}

	@Test fun offsetHorizontalFixedBeyondBorders() {
		assertFails {
			renderMosaic {
				Box(modifier = Modifier.size(6).offset(30, 0)) {
					TestFiller(modifier = Modifier.size(1))
				}
			}
		}
	}

	@Test fun offsetHorizontalFixedNegativeBeyondBorders() {
		assertFails {
			renderMosaic {
				Box(modifier = Modifier.size(6).offset(-3, 0)) {
					TestFiller(modifier = Modifier.size(1))
				}
			}
		}
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
			""".trimMargin().wrapWithAnsiSynchronizedUpdate().replaceLineEndingsWithCRLF(),
		)
	}

	@Test fun offsetVerticalFixedBeyondBorders() {
		assertFails {
			renderMosaic {
				Box(modifier = Modifier.size(6).offset(0, 40)) {
					TestFiller(modifier = Modifier.size(1))
				}
			}
		}
	}

	@Test fun offsetVerticalFixedNegativeBeyondBorders() {
		assertFails {
			renderMosaic {
				Box(modifier = Modifier.size(6).offset(0, -4)) {
					TestFiller(modifier = Modifier.size(1))
				}
			}
		}
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
			""".trimMargin().wrapWithAnsiSynchronizedUpdate().replaceLineEndingsWithCRLF(),
		)
	}

	@Test fun offsetFixedBeyondBorders() {
		assertFails {
			renderMosaic {
				Box(modifier = Modifier.size(6).offset(30, 40)) {
					TestFiller(modifier = Modifier.size(1))
				}
			}
		}
	}

	@Test fun offsetFixedNegativeBeyondBorders() {
		assertFails {
			renderMosaic {
				Box(modifier = Modifier.size(6).offset(-3, -4)) {
					TestFiller(modifier = Modifier.size(1))
				}
			}
		}
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
			""".trimMargin().wrapWithAnsiSynchronizedUpdate().replaceLineEndingsWithCRLF(),
		)
	}

	@Test fun offsetHorizontalModifiableBeyondBorders() {
		assertFails {
			renderMosaic {
				Box(modifier = Modifier.size(6).offset { IntOffset(30, 0) }) {
					TestFiller(modifier = Modifier.size(1))
				}
			}
		}
	}

	@Test fun offsetHorizontalModifiableNegativeBeyondBorders() {
		assertFails {
			renderMosaic {
				Box(modifier = Modifier.size(6).offset { IntOffset(-3, 0) }) {
					TestFiller(modifier = Modifier.size(1))
				}
			}
		}
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
			""".trimMargin().wrapWithAnsiSynchronizedUpdate().replaceLineEndingsWithCRLF(),
		)
	}

	@Test fun offsetVerticalModifiableBeyondBorders() {
		assertFails {
			renderMosaic {
				Box(modifier = Modifier.size(6).offset { IntOffset(0, 40) }) {
					TestFiller(modifier = Modifier.size(1))
				}
			}
		}
	}

	@Test fun offsetVerticalModifiableNegativeBeyondBorders() {
		assertFails {
			renderMosaic {
				Box(modifier = Modifier.size(6).offset { IntOffset(0, -4) }) {
					TestFiller(modifier = Modifier.size(1))
				}
			}
		}
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
			""".trimMargin().wrapWithAnsiSynchronizedUpdate().replaceLineEndingsWithCRLF(),
		)
	}

	@Test fun offsetModifiableBeyondBorders() {
		assertFails {
			renderMosaic {
				Box(modifier = Modifier.size(6).offset { IntOffset(30, 40) }) {
					TestFiller(modifier = Modifier.size(1))
				}
			}
		}
	}

	@Test fun offsetModifiableNegativeBeyondBorders() {
		assertFails {
			renderMosaic {
				Box(modifier = Modifier.size(6).offset { IntOffset(-3, -4) }) {
					TestFiller(modifier = Modifier.size(1))
				}
			}
		}
	}

	@Test fun offsetModifiableDebug() {
		val offsetLambda = { IntOffset(-3, -4) }
		val actual = Modifier.offset(offsetLambda).toString()
		assertThat(actual).isEqualTo("ChangeableOffset(offset=$offsetLambda)")
	}
}
