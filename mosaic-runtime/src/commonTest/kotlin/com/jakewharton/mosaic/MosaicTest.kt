package com.jakewharton.mosaic

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.lifecycle.compose.LocalLifecycleOwner
import assertk.all
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isLessThan
import assertk.assertions.isPositive
import com.jakewharton.mosaic.layout.drawBehind
import com.jakewharton.mosaic.layout.offset
import com.jakewharton.mosaic.layout.size
import com.jakewharton.mosaic.layout.width
import com.jakewharton.mosaic.modifier.Modifier
import com.jakewharton.mosaic.testing.runMosaicTest
import com.jakewharton.mosaic.ui.Box
import com.jakewharton.mosaic.ui.Filler
import com.jakewharton.mosaic.ui.Spacer
import com.jakewharton.mosaic.ui.Text
import com.jakewharton.mosaic.ui.unit.IntOffset
import kotlin.test.Test
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.runTest

class MosaicTest {
	@Test fun changeInCompositionPhase() = runTest {
		runMosaicTest {
			setContent {
				var offsetX by remember { mutableIntStateOf(0) }

				Box(modifier = Modifier.width(10)) {
					Filler(TestChar, modifier = Modifier.size(1).offset(offsetX, 0))
				}

				LaunchedEffect(Unit) {
					delay(100L)
					offsetX = 5
				}
			}

			assertThat(awaitSnapshot()).isEqualTo("$TestChar         ")
			assertThat(awaitSnapshot()).isEqualTo("     $TestChar    ")
		}
	}

	@Test fun changeInLayoutPhase() = runTest {
		runMosaicTest {
			setContent {
				var offsetX by remember { mutableIntStateOf(0) }

				Box(modifier = Modifier.width(10)) {
					Filler(TestChar, modifier = Modifier.size(1).offset { IntOffset(offsetX, 0) })
				}

				LaunchedEffect(Unit) {
					delay(100L)
					offsetX = 5
				}
			}

			assertThat(awaitSnapshot()).isEqualTo("$TestChar         ")
			assertThat(awaitSnapshot()).isEqualTo("     $TestChar    ")
		}
	}

	@Test fun changeInDrawPhase() = runTest {
		runMosaicTest {
			setContent {
				var drawAnother by remember { mutableStateOf(false) }

				Box(modifier = Modifier.width(10)) {
					Spacer(
						modifier = Modifier
							.size(1)
							.drawBehind {
								if (drawAnother) {
									drawText(0, 0, "${TestChar + 1}")
								} else {
									drawText(0, 0, "$TestChar")
								}
							},
					)
				}

				LaunchedEffect(Unit) {
					delay(100L)
					drawAnother = true
				}
			}

			assertThat(awaitSnapshot()).isEqualTo("$TestChar         ")
			assertThat(awaitSnapshot()).isEqualTo("${TestChar + 1}         ")
		}
	}

	@Test fun frameTimeChanges() = runTest {
		var frameTimeA = 0L
		var frameTimeB = 0L

		runMosaicComposition(
			rendering = AnsiRendering(),
			keyEvents = Channel(),
			terminalState = mutableStateOf(Terminal.Default),
		) {
			LaunchedEffect(Unit) {
				withFrameNanos { frameTimeNanos ->
					frameTimeA = frameTimeNanos
				}
				withFrameNanos { frameTimeNanos ->
					frameTimeB = frameTimeNanos
				}
			}
		}

		assertThat(frameTimeA).all {
			isPositive()
			isLessThan(frameTimeB)
		}
	}

	@Test fun lifecycleUpdatesWithTerminal() = runTest {
		runMosaicTest {
			setContent {
				val terminal = LocalTerminal.current
				val lifecycle = LocalLifecycleOwner.current.lifecycle
				Text("${terminal.focused} ${lifecycle.currentState}")
			}
			assertThat(awaitSnapshot()).isEqualTo("true RESUMED")

			terminalState.update { copy(focused = false) }
			assertThat(awaitSnapshot()).isEqualTo("false STARTED")
		}
	}
}
