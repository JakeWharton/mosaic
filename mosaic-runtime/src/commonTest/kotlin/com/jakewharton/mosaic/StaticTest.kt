package com.jakewharton.mosaic

import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import assertk.assertAll
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isNull
import assertk.assertions.isTrue
import com.jakewharton.mosaic.testing.MosaicSnapshots
import com.jakewharton.mosaic.testing.runMosaicTest
import com.jakewharton.mosaic.ui.Text
import kotlin.test.Test
import kotlin.test.assertFailsWith
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.runTest

class StaticTest {
	@Test fun renderingDoesNotCauseAnotherFrame() = runTest {
		runMosaicTest(MosaicSnapshots) {
			setContent {
				StaticEffect { Text("static") }
				Text("content")
			}

			assertThat(awaitSnapshot().static()).isEqualTo("static")
			assertFailsWith<TimeoutCancellationException> { awaitSnapshot() }
		}
	}

	@Test fun staticOnlyRendersOnce() = runTest {
		runMosaicTest(MosaicSnapshots) {
			var count by mutableIntStateOf(1)
			setContent {
				StaticEffect { Text("static: $count") }
				Text("content: $count")
			}

			val one = awaitSnapshot()
			assertThat(one.draw().render()).isEqualTo("content: 1")
			assertThat(one.static()).isEqualTo("static: 1")

			count = 2

			val two = awaitSnapshot()
			assertThat(two.draw().render()).isEqualTo("content: 2")
			assertThat(two.static()).isNull()
		}
	}

	@Test fun sideEffectsRun() = runTest {
		runMosaicTest {
			var ran = false
			setContent {
				StaticEffect {
					SideEffect {
						ran = true
					}
				}
			}
			assertThat(ran).isTrue()
		}
	}

	@Test fun launchedEffectsDoNotRun() = runTest {
		runMosaicTest {
			var ran = false
			setContent {
				StaticEffect {
					LaunchedEffect(Unit) {
						ran = true
					}
				}
			}

			// Allow the effect to run, if it were going to.
			delay(10.milliseconds)

			assertThat(ran).isFalse()
		}
	}

	@Test fun disposableEffectsRunAndDispose() = runTest {
		runMosaicTest {
			var effectRan = false
			var disposeRan = false
			setContent {
				StaticEffect {
					DisposableEffect(Unit) {
						effectRan = true
						onDispose {
							disposeRan = true
						}
					}
				}
			}
			assertAll {
				assertThat(effectRan, "effect").isTrue()
				assertThat(disposeRan, "dispose").isTrue()
			}
		}
	}

	@Test fun noRecomposition() = runTest {
		runMosaicTest {
			var staticRecompositions = 0
			var normalRecompositions = 0
			var count by mutableIntStateOf(0)
			setContentAndSnapshot {
				StaticEffect {
					staticRecompositions++
					Text("count: $count")
				}

				normalRecompositions++
				Text("count: $count")
			}
			assertThat(staticRecompositions).isEqualTo(1)
			assertThat(normalRecompositions).isEqualTo(1)

			count = 1
			awaitSnapshot()
			assertThat(staticRecompositions).isEqualTo(1)
			assertThat(normalRecompositions).isEqualTo(2)
		}
	}

	@Test fun loggingCausesFrameWithoutRecomposition() = runTest {
		runMosaicTest(MosaicSnapshots) {
			var recompositionCount = 0
			lateinit var staticLogger: StaticLogger
			val initial = setContentAndSnapshot {
				staticLogger = LocalStaticLogger.current
				Text("Count: ${++recompositionCount}")
			}
			assertThat(initial.draw().render()).isEqualTo("Count: 1")
			assertThat(initial.static()).isNull()

			staticLogger += "sup"
			val snapshot = awaitSnapshot()
			assertThat(snapshot.draw().render()).isEqualTo("Count: 1")
			assertThat(snapshot.static()).isEqualTo("sup")
		}
	}
}
