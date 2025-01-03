package com.jakewharton.mosaic

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import assertk.assertThat
import assertk.assertions.isEqualTo
import com.jakewharton.mosaic.layout.width
import com.jakewharton.mosaic.modifier.Modifier
import com.jakewharton.mosaic.ui.Alignment
import com.jakewharton.mosaic.ui.Box
import com.jakewharton.mosaic.ui.Column
import com.jakewharton.mosaic.ui.Text
import com.jakewharton.mosaic.ui.unit.IntSize
import kotlin.test.Test
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.runTest

class CounterTest {
	@Test fun counter() = runTest {
		runMosaicTest {
			setCounter()
			for (count in 0..20) {
				assertThat(awaitSnapshot()).isEqualTo("The count is: $count")
			}
		}
	}

	@Test fun counterInTerminalCenter() = runTest {
		runMosaicTest {
			terminalState.value = Terminal(size = IntSize(width = 30, height = 1))
			setCounterInTerminalCenter()
			for (count in 0..9) {
				assertThat(awaitSnapshot()).isEqualTo("        The count is: $count       ")
			}

			terminalState.value = Terminal(size = IntSize(width = 20, height = 1))

			// After changing the terminal size, we wait for the counter to increase before getting a
			// new snapshot, otherwise there will be the previous value (9) and a different output size.
			delay(250L)

			for (count in 10..20) {
				assertThat(awaitSnapshot()).isEqualTo("  The count is: $count  ")
			}
		}
	}

	@Test fun counterWithContentChanges() = runTest {
		runMosaicTest {
			setCounter()
			for (count in 0..9) {
				assertThat(awaitSnapshot()).isEqualTo("The count is: $count")
			}
			setChangedCounter()
			for (count in 0..20) {
				assertThat(awaitSnapshot()).isEqualTo(
					"""
					|The count is: $count      $s
					|The second count is: $count
					""".trimMargin(),
				)
			}
		}
	}

	private fun TestMosaic<*>.setCounter() {
		setContent {
			var count by remember { mutableIntStateOf(0) }

			Text("The count is: $count")

			LaunchedEffect(Unit) {
				for (i in 1..20) {
					delay(250L)
					count = i
				}
			}
		}
	}

	private fun TestMosaic<*>.setCounterInTerminalCenter() {
		setContent {
			var count by remember { mutableIntStateOf(0) }

			Box(
				modifier = Modifier.width(LocalTerminal.current.size.width),
				contentAlignment = Alignment.Center,
			) {
				Text("The count is: $count")
			}

			LaunchedEffect(Unit) {
				for (i in 1..20) {
					delay(250L)
					count = i
				}
			}
		}
	}

	private fun TestMosaic<*>.setChangedCounter() {
		setContent {
			var count by remember { mutableIntStateOf(0) }
			var secondCount by remember { mutableIntStateOf(0) }

			Column {
				Text("The count is: $count")
				Text("The second count is: $secondCount")
			}

			LaunchedEffect(Unit) {
				for (i in 1..20) {
					delay(250L)
					count = i
				}
			}
			LaunchedEffect(Unit) {
				for (i in 1..20) {
					delay(250L)
					secondCount = i
				}
			}
		}
	}
}
