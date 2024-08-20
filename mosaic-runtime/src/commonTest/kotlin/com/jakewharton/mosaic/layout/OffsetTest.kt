package com.jakewharton.mosaic.layout

import assertk.assertThat
import assertk.assertions.isEqualTo
import com.jakewharton.mosaic.TestChar
import com.jakewharton.mosaic.TestFiller
import com.jakewharton.mosaic.modifier.Modifier
import com.jakewharton.mosaic.runMosaicTest
import com.jakewharton.mosaic.ui.Box
import com.jakewharton.mosaic.ui.unit.IntOffset
import kotlin.test.Test
import kotlin.test.assertFails
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
				|   $TestChar
				|
				|
				|
				|
				|
				""".trimMargin(),
			)
		}
	}

	@Test fun offsetHorizontalFixedBeyondBorders() = runTest {
		assertFails {
			runMosaicTest {
				setContent {
					Box(modifier = Modifier.size(6).offset(30, 0)) {
						TestFiller(modifier = Modifier.size(1))
					}
				}
			}
		}
	}

	@Test fun offsetHorizontalFixedNegativeBeyondBorders() = runTest {
		assertFails {
			runMosaicTest {
				setContent {
					Box(modifier = Modifier.size(6).offset(-3, 0)) {
						TestFiller(modifier = Modifier.size(1))
					}
				}
			}
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
				|
				|
				|
				|
				|$TestChar
				|
				""".trimMargin(),
			)
		}
	}

	@Test fun offsetVerticalFixedBeyondBorders() = runTest {
		assertFails {
			runMosaicTest {
				setContent {
					Box(modifier = Modifier.size(6).offset(0, 40)) {
						TestFiller(modifier = Modifier.size(1))
					}
				}
			}
		}
	}

	@Test fun offsetVerticalFixedNegativeBeyondBorders() = runTest {
		assertFails {
			runMosaicTest {
				setContent {
					Box(modifier = Modifier.size(6).offset(0, -4)) {
						TestFiller(modifier = Modifier.size(1))
					}
				}
			}
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
				|
				|
				|
				|
				|   $TestChar
				|
				""".trimMargin(),
			)
		}
	}

	@Test fun offsetFixedBeyondBorders() = runTest {
		assertFails {
			runMosaicTest {
				setContent {
					Box(modifier = Modifier.size(6).offset(30, 40)) {
						TestFiller(modifier = Modifier.size(1))
					}
				}
			}
		}
	}

	@Test fun offsetFixedNegativeBeyondBorders() = runTest {
		assertFails {
			runMosaicTest {
				setContent {
					Box(modifier = Modifier.size(6).offset(-3, -4)) {
						TestFiller(modifier = Modifier.size(1))
					}
				}
			}
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
				|   $TestChar
				|
				|
				|
				|
				|
				""".trimMargin(),
			)
		}
	}

	@Test fun offsetHorizontalModifiableBeyondBorders() = runTest {
		assertFails {
			runMosaicTest {
				setContent {
					Box(modifier = Modifier.size(6).offset { IntOffset(30, 0) }) {
						TestFiller(modifier = Modifier.size(1))
					}
				}
			}
		}
	}

	@Test fun offsetHorizontalModifiableNegativeBeyondBorders() = runTest {
		assertFails {
			runMosaicTest {
				setContent {
					Box(modifier = Modifier.size(6).offset { IntOffset(-3, 0) }) {
						TestFiller(modifier = Modifier.size(1))
					}
				}
			}
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
				|
				|
				|
				|
				|$TestChar
				|
				""".trimMargin(),
			)
		}
	}

	@Test fun offsetVerticalModifiableBeyondBorders() = runTest {
		assertFails {
			runMosaicTest {
				setContent {
					Box(modifier = Modifier.size(6).offset { IntOffset(0, 40) }) {
						TestFiller(modifier = Modifier.size(1))
					}
				}
			}
		}
	}

	@Test fun offsetVerticalModifiableNegativeBeyondBorders() = runTest {
		assertFails {
			runMosaicTest {
				setContent {
					Box(modifier = Modifier.size(6).offset { IntOffset(0, -4) }) {
						TestFiller(modifier = Modifier.size(1))
					}
				}
			}
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
				|
				|
				|
				|
				|   $TestChar
				|
				""".trimMargin(),
			)
		}
	}

	@Test fun offsetModifiableBeyondBorders() = runTest {
		assertFails {
			runMosaicTest {
				setContent {
					Box(modifier = Modifier.size(6).offset { IntOffset(30, 40) }) {
						TestFiller(modifier = Modifier.size(1))
					}
				}
			}
		}
	}

	@Test fun offsetModifiableNegativeBeyondBorders() = runTest {
		assertFails {
			runMosaicTest {
				setContent {
					Box(modifier = Modifier.size(6).offset { IntOffset(-3, -4) }) {
						TestFiller(modifier = Modifier.size(1))
					}
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
