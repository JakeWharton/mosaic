package com.jakewharton.mosaic

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import assertk.assertThat
import assertk.assertions.isEqualTo
import com.jakewharton.mosaic.ui.Column
import com.jakewharton.mosaic.ui.Text
import kotlin.test.Test
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.runTest

class MosaicTest {
	@Test fun render() {
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
			""".trimMargin().replaceLineEndingsWithCRLF(),
		)
	}

	@Test fun counter() = runTest {
		val strings = mutableListOf<CharSequence>()

		runMosaic(
			content = {
				var count by remember { mutableIntStateOf(0) }

				Text("The count is: $count")

				LaunchedEffect(Unit) {
					for (i in 1..20) {
						delay(250)
						count = i
					}
				}
			},
			display = {
				strings.add(it)
			},
		)

		assertThat(strings.size).isEqualTo(21)
		assertThat(strings[0]).isEqualTo(
			"""
			|${ansiBeginSynchronizedUpdate}The count is: 0
			|$ansiEndSynchronizedUpdate
			""".trimMargin().replaceLineEndingsWithCRLF(),
		)
		for (i in 1..20) {
			assertThat(strings[i]).isEqualTo(
				"""
				|${ansiBeginSynchronizedUpdate}${cursorUp}The count is: ${i}$clearLine
				|$ansiEndSynchronizedUpdate
				""".trimMargin().replaceLineEndingsWithCRLF(),
			)
		}
	}
}
