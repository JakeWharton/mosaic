package com.jakewharton.mosaic.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.hasSize
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
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
}
