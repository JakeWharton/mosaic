package com.jakewharton.mosaic.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import assertk.assertThat
import assertk.assertions.isEqualTo
import com.jakewharton.mosaic.fillWith
import com.jakewharton.mosaic.layout.height
import com.jakewharton.mosaic.layout.size
import com.jakewharton.mosaic.layout.width
import com.jakewharton.mosaic.layout.wrapContentHeight
import com.jakewharton.mosaic.layout.wrapContentWidth
import com.jakewharton.mosaic.modifier.Modifier
import com.jakewharton.mosaic.testing.runMosaicTest
import kotlin.test.Test
import kotlinx.coroutines.test.runTest

class RowColumnModifierTest {
	@Test fun rowUpdatesOnAlignmentChange() = runTest {
		runMosaicTest {
			val count = 5
			var alignment by mutableStateOf(Alignment.Top)

			setContent {
				Box(Modifier.size(20)) {
					Row(Modifier.wrapContentHeight()) {
						repeat(count) { index ->
							Box(Modifier.width(4).height(if (index == count - 1) 2 else 4).align(alignment).fillWith('A' + index))
						}
					}
				}
			}

			assertThat(awaitSnapshot()).isEqualTo(
				"""
				|AAAABBBBCCCCDDDDEEEE
				|AAAABBBBCCCCDDDDEEEE
				|AAAABBBBCCCCDDDD
				|AAAABBBBCCCCDDDD
				|
				|
				|
				|
				|
				|
				|
				|
				|
				|
				|
				|
				|
				|
				|
				|
				""".trimMargin(),
			)

			alignment = Alignment.CenterVertically

			assertThat(awaitSnapshot()).isEqualTo(
				"""
				|AAAABBBBCCCCDDDD
				|AAAABBBBCCCCDDDDEEEE
				|AAAABBBBCCCCDDDDEEEE
				|AAAABBBBCCCCDDDD
				|
				|
				|
				|
				|
				|
				|
				|
				|
				|
				|
				|
				|
				|
				|
				|
				""".trimMargin(),
			)
		}
	}

	@Test fun rowUpdatesOnWeightChange() = runTest {
		runMosaicTest {
			val count = 5
			var fill by mutableStateOf(false)

			setContent {
				Box(Modifier.size(20)) {
					Row(Modifier.wrapContentHeight()) {
						repeat(count) { index ->
							Box(Modifier.size(2).weight(1f, fill).fillWith('A' + index))
						}
					}
				}
			}

			assertThat(awaitSnapshot()).isEqualTo(
				"""
				|AABBCCDDEE
				|AABBCCDDEE
				|
				|
				|
				|
				|
				|
				|
				|
				|
				|
				|
				|
				|
				|
				|
				|
				|
				|
				""".trimMargin(),
			)

			fill = true

			assertThat(awaitSnapshot()).isEqualTo(
				"""
				|AAAABBBBCCCCDDDDEEEE
				|AAAABBBBCCCCDDDDEEEE
				|
				|
				|
				|
				|
				|
				|
				|
				|
				|
				|
				|
				|
				|
				|
				|
				|
				|
				""".trimMargin(),
			)
		}
	}

	@Test fun rowUpdatesOnWeightAndAlignmentChange() = runTest {
		runMosaicTest {
			val count = 5
			var fill by mutableStateOf(false)
			var alignment by mutableStateOf(Alignment.Top)

			setContent {
				Box(Modifier.size(20)) {
					Row(Modifier.wrapContentHeight()) {
						repeat(count) { index ->
							Box(
								Modifier.width(2).height(if (index == count - 1) 2 else 4)
									.weight(1f, fill)
									.align(alignment)
									.fillWith('A' + index),
							)
						}
					}
				}
			}

			assertThat(awaitSnapshot()).isEqualTo(
				"""
				|AABBCCDDEE
				|AABBCCDDEE
				|AABBCCDD
				|AABBCCDD
				|
				|
				|
				|
				|
				|
				|
				|
				|
				|
				|
				|
				|
				|
				|
				|
				""".trimMargin(),
			)

			alignment = Alignment.CenterVertically
			fill = true

			assertThat(awaitSnapshot()).isEqualTo(
				"""
				|AAAABBBBCCCCDDDD
				|AAAABBBBCCCCDDDDEEEE
				|AAAABBBBCCCCDDDDEEEE
				|AAAABBBBCCCCDDDD
				|
				|
				|
				|
				|
				|
				|
				|
				|
				|
				|
				|
				|
				|
				|
				|
				""".trimMargin(),
			)
		}
	}

	@Test fun columnUpdatesOnAlignmentChange() = runTest {
		runMosaicTest {
			val count = 5
			var alignment by mutableStateOf(Alignment.Start)

			setContent {
				Box(Modifier.size(20)) {
					Column(Modifier.wrapContentWidth().wrapContentHeight()) {
						repeat(count) { index ->
							Box(Modifier.height(4).width(if (index == count - 1) 2 else 4).align(alignment).fillWith('A' + index))
						}
					}
				}
			}

			assertThat(awaitSnapshot()).isEqualTo(
				"""
				|AAAA
				|AAAA
				|AAAA
				|AAAA
				|BBBB
				|BBBB
				|BBBB
				|BBBB
				|CCCC
				|CCCC
				|CCCC
				|CCCC
				|DDDD
				|DDDD
				|DDDD
				|DDDD
				|EE
				|EE
				|EE
				|EE
				""".trimMargin(),
			)

			alignment = Alignment.CenterHorizontally

			assertThat(awaitSnapshot()).isEqualTo(
				"""
				|AAAA
				|AAAA
				|AAAA
				|AAAA
				|BBBB
				|BBBB
				|BBBB
				|BBBB
				|CCCC
				|CCCC
				|CCCC
				|CCCC
				|DDDD
				|DDDD
				|DDDD
				|DDDD
				| EE
				| EE
				| EE
				| EE
				""".trimMargin(),
			)
		}
	}

	@Test fun columnUpdatesOnWeightChange() = runTest {
		runMosaicTest {
			val count = 5
			var fill by mutableStateOf(false)

			setContent {
				Box(Modifier.size(20)) {
					Column(Modifier.wrapContentHeight()) {
						repeat(count) { index ->
							Box(Modifier.size(2).weight(1f, fill).fillWith('A' + index))
						}
					}
				}
			}

			assertThat(awaitSnapshot()).isEqualTo(
				"""
				|AA
				|AA
				|BB
				|BB
				|CC
				|CC
				|DD
				|DD
				|EE
				|EE
				|
				|
				|
				|
				|
				|
				|
				|
				|
				|
				""".trimMargin(),
			)

			fill = true

			assertThat(awaitSnapshot()).isEqualTo(
				"""
				|AA
				|AA
				|AA
				|AA
				|BB
				|BB
				|BB
				|BB
				|CC
				|CC
				|CC
				|CC
				|DD
				|DD
				|DD
				|DD
				|EE
				|EE
				|EE
				|EE
				""".trimMargin(),
			)
		}
	}

	@Test fun columnUpdatesOnWeightAndAlignmentChange() = runTest {
		runMosaicTest {
			val count = 5
			var fill by mutableStateOf(false)
			var alignment by mutableStateOf(Alignment.Start)

			setContent {
				Box(Modifier.size(20)) {
					Column(Modifier.wrapContentHeight()) {
						repeat(count) { index ->
							Box(
								Modifier.height(2).width(if (index == count - 1) 2 else 4).weight(1f, fill)
									.align(alignment)
									.fillWith('A' + index),
							)
						}
					}
				}
			}

			assertThat(awaitSnapshot()).isEqualTo(
				"""
				|AAAA
				|AAAA
				|BBBB
				|BBBB
				|CCCC
				|CCCC
				|DDDD
				|DDDD
				|EE
				|EE
				|
				|
				|
				|
				|
				|
				|
				|
				|
				|
				""".trimMargin(),
			)

			alignment = Alignment.CenterHorizontally
			fill = true

			assertThat(awaitSnapshot()).isEqualTo(
				"""
				|AAAA
				|AAAA
				|AAAA
				|AAAA
				|BBBB
				|BBBB
				|BBBB
				|BBBB
				|CCCC
				|CCCC
				|CCCC
				|CCCC
				|DDDD
				|DDDD
				|DDDD
				|DDDD
				| EE
				| EE
				| EE
				| EE
				""".trimMargin(),
			)
		}
	}
}
