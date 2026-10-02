package com.jakewharton.mosaic.animation

import assertk.assertThat
import assertk.assertions.isEqualTo
import kotlin.test.Test

class ArcSplineTest {
	private val array = floatArrayOf(0f, 0.25f, 0.5f, 1f)

	@Test fun binarySearchFound() {
		assertThat(binarySearch(array, 0f)).isEqualTo(0)
		assertThat(binarySearch(array, 0.5f)).isEqualTo(2)
		assertThat(binarySearch(array, 1f)).isEqualTo(3)
	}

	@Test fun binarySearchMissingReturnsInsertionPoint() {
		assertThat(binarySearch(array, -1f)).isEqualTo(-1)
		assertThat(binarySearch(array, 0.3f)).isEqualTo(-3)
		assertThat(binarySearch(array, 2f)).isEqualTo(-5)
	}
}
