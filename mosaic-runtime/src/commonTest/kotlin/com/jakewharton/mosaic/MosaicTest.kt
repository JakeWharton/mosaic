package com.jakewharton.mosaic

import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
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
import com.jakewharton.mosaic.ui.Column
import com.jakewharton.mosaic.ui.Filler
import com.jakewharton.mosaic.ui.Spacer
import com.jakewharton.mosaic.ui.Text
import com.jakewharton.mosaic.ui.unit.IntOffset
import kotlin.test.Test
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withContext

class MosaicTest {
	@Test fun renderMosaicSimple() {
		val actual = renderMosaic {
			Column {
				Text("One")
				Text("Two")
				Text("Three")
			}
		}
		assertThat(actual).isEqualTo(
			"""
			|One $s
			|Two $s
			|Three
			|
			""".trimMargin().wrapWithAnsiSynchronizedUpdate().replaceLineEndingsWithCRLF(),
		)
	}

	@Test fun renderMosaicIgnoreLaunchedEffect() {
		val actual = renderMosaic {
			var count by remember { mutableIntStateOf(0) }

			Column {
				Text("One")
				Text("Two")
				Text("Three")
				repeat(count) {
					Text("Any number")
				}
			}

			LaunchedEffect(Unit) {
				while (true) {
					count++
					delay(50L)
				}
			}
		}
		assertThat(actual).isEqualTo(
			"""
			|One $s
			|Two $s
			|Three
			|
			""".trimMargin().wrapWithAnsiSynchronizedUpdate().replaceLineEndingsWithCRLF(),
		)
	}

	@Test fun renderMosaicIgnoreDisposableEffect() {
		val actual = renderMosaic {
			var count by remember { mutableIntStateOf(0) }

			Column {
				Text("One")
				Text("Two")
				Text("Three")
				repeat(count) {
					Text("Any number")
				}
			}

			DisposableEffect(Unit) {
				count++
				onDispose {
					count++
				}
			}
		}
		assertThat(actual).isEqualTo(
			"""
			|One $s
			|Two $s
			|Three
			|
			""".trimMargin().wrapWithAnsiSynchronizedUpdate().replaceLineEndingsWithCRLF(),
		)
	}

	@Test fun renderMosaicIgnoreMultipleEffects() {
		val actual = renderMosaic {
			var count by remember { mutableIntStateOf(0) }

			DisposableEffect(Unit) {
				count = 1
				onDispose {
					count = 2
				}
			}

			LaunchedEffect(Unit) {
				count = 3
			}

			SideEffect {
				count = 4
			}

			Column {
				Text("One")
				Text("Two")
				Text("Three")
				repeat(count) {
					Text("Any number")
				}
			}

			LaunchedEffect(Unit) {
				count = 5
			}
		}
		assertThat(actual).isEqualTo(
			"""
			|One $s
			|Two $s
			|Three
			|
			""".trimMargin().wrapWithAnsiSynchronizedUpdate().replaceLineEndingsWithCRLF(),
		)
	}

	@Test fun renderMosaicInDefaultCoroutineDispatcher() = runTest {
		val actual = withContext(Dispatchers.Default) {
			renderMosaic {
				Column {
					Text("One")
					Text("Two")
					Text("Three")
				}
			}
		}
		assertThat(actual).isEqualTo(
			"""
			|One $s
			|Two $s
			|Three
			|
			""".trimMargin().wrapWithAnsiSynchronizedUpdate().replaceLineEndingsWithCRLF(),
		)
	}

	@Test fun renderMosaicConcurrently() = runTest {
		val actuals = List(100) {
			async(Dispatchers.Default, start = CoroutineStart.LAZY) {
				renderMosaic {
					Column {
						Text("One")
						Text("Two")
						Text("Three")
					}
				}
			}
		}.awaitAll()

		actuals.forEach { actual ->
			assertThat(actual).isEqualTo(
				"""
				|One $s
				|Two $s
				|Three
				|
				""".trimMargin().wrapWithAnsiSynchronizedUpdate().replaceLineEndingsWithCRLF(),
			)
		}
	}

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

		runMosaic(enterRawMode = false) {
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
}
