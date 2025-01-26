package com.jakewharton.mosaic.ui

import androidx.compose.runtime.mutableStateListOf
import assertk.assertThat
import assertk.assertions.hasSize
import com.jakewharton.mosaic.testing.MosaicSnapshots
import com.jakewharton.mosaic.testing.runMosaicTest
import kotlin.test.Test
import kotlin.test.assertFailsWith
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.test.runTest

class StaticTest {
	@Test fun renderingDoesNotCauseAnotherFrame() = runTest {
		val statics = mutableStateListOf("static")
		runMosaicTest(MosaicSnapshots) {
			setContent {
				Static(statics) { Text(it) }
				Text("content")
			}

			assertThat(awaitSnapshot().paintStatics()).hasSize(1)
			assertFailsWith<TimeoutCancellationException> { awaitSnapshot() }
		}
	}
}
