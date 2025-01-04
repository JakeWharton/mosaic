package com.jakewharton.mosaic.layout

import assertk.assertThat
import assertk.assertions.hasSize
import assertk.assertions.isEqualTo
import com.jakewharton.mosaic.AtLeastSize
import com.jakewharton.mosaic.modifier.Modifier
import com.jakewharton.mosaic.testing.runMosaicTest
import com.jakewharton.mosaic.ui.Box
import com.jakewharton.mosaic.ui.Layout
import kotlin.test.Test
import kotlinx.coroutines.test.runTest

class LayoutIdTest {

	@Test fun testTags() = runTest {
		runMosaicTest {
			setContent {
				Layout({
					AtLeastSize(0, Modifier.layoutId("first"), content = {})
					Box(Modifier.layoutId("second")) { AtLeastSize(0, content = {}) }
					Box(Modifier.layoutId("third")) { AtLeastSize(0, content = {}) }
				}) { measurables, _ ->
					assertThat(measurables).hasSize(3)
					assertThat(measurables[0].layoutId).isEqualTo("first")
					assertThat(measurables[1].layoutId).isEqualTo("second")
					assertThat(measurables[2].layoutId).isEqualTo("third")
					layout(0, 0) {}
				}
			}
			awaitSnapshot()
		}
	}
}
