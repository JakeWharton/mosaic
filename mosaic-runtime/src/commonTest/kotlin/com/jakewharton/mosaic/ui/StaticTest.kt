package com.jakewharton.mosaic.ui

import androidx.collection.mutableObjectListOf
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import assertk.assertAll
import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.hasSize
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isTrue
import com.jakewharton.mosaic.NodeSnapshots
import com.jakewharton.mosaic.assertFailure
import com.jakewharton.mosaic.render
import com.jakewharton.mosaic.testing.MosaicSnapshots
import com.jakewharton.mosaic.testing.runMosaicTest
import kotlin.test.Test
import kotlin.test.assertFailsWith
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.test.runTest

class StaticTest {
	@Test fun renderingDoesNotCauseAnotherFrame() = runTest {
		runMosaicTest(MosaicSnapshots) {
			setContent {
				Static { Text("static") }
				Text("content")
			}

			assertThat(awaitSnapshot().paintStatics()).hasSize(1)
			assertFailsWith<TimeoutCancellationException> { awaitSnapshot() }
		}
	}

	@Test fun staticOnlyRendersOnce() = runTest {
		runMosaicTest(MosaicSnapshots) {
			var count by mutableIntStateOf(1)
			setContent {
				Static { Text("static: $count") }
				Text("content: $count")
			}

			val one = awaitSnapshot()
			assertThat(one.paint().render()).isEqualTo("content: 1")
			assertThat(one.paintStatics().render()).containsExactly("static: 1")

			count = 2

			val two = awaitSnapshot()
			assertThat(two.paint().render()).isEqualTo("content: 2")
			assertThat(two.paintStatics().render()).isEmpty()
		}
	}

	@Test fun staticNodesRemovedAfterRenderWithoutRecomposition() = runTest {
		runMosaicTest(NodeSnapshots) {
			var count by mutableIntStateOf(1)
			setContent {
				Static { Text("static: $count") }
				Text("content: $count")
			}

			val one = awaitSnapshot()
			assertThat(one.toString()).isEqualTo(
				"""
				|Static()
				|  Text("static: 1") x=0 y=0 w=9 h=1 DrawBehind
				|Text("content: 1") x=0 y=0 w=10 h=1 DrawBehind
				""".trimMargin(),
			)

			one.paintStaticsTo(mutableObjectListOf())
			assertThat(one.toString()).isEqualTo(
				"""
				|Static()
				|Text("content: 1") x=0 y=0 w=10 h=1 DrawBehind
				""".trimMargin(),
			)

			assertFailure<TimeoutCancellationException> { awaitSnapshot() }

			count = 2
			val two = awaitSnapshot()
			assertThat(two.toString()).isEqualTo(
				"""
				|Static()
				|Text("content: 2") x=0 y=0 w=10 h=1 DrawBehind
				""".trimMargin(),
			)
		}
	}

	@Test fun sideEffectsRun() = runTest {
		runMosaicTest {
			var ran = false
			setContent {
				Static {
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
				Static {
					LaunchedEffect(Unit) {
						ran = true
					}
				}
			}
			assertThat(ran).isFalse()
		}
	}

	@Test fun disposableEffectsRunAndDispose() = runTest {
		runMosaicTest {
			var effectRan = false
			var disposeRan = false
			setContent {
				Static {
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
				Static {
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
}
