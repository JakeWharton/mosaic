package com.jakewharton.mosaic.ui

import assertk.assertThat
import assertk.assertions.isEqualTo
import kotlin.test.Test

class ArrangementTest {
	@Test fun arrangementStart() {
		testHorizontalArrangement(
			actualArrangement = Arrangement.Start,
			actualTotalSize = 140,
			actualSizes = intArrayOf(20),
			actualOutPositions = intArrayOf(0),
			expectedOutPositions = intArrayOf(0),
		)
		testHorizontalArrangement(
			actualArrangement = Arrangement.Start,
			actualTotalSize = 140,
			actualSizes = intArrayOf(20, 30),
			actualOutPositions = intArrayOf(0, 0),
			expectedOutPositions = intArrayOf(0, 20),
		)
		testHorizontalArrangement(
			actualArrangement = Arrangement.Start,
			actualTotalSize = 140,
			actualSizes = intArrayOf(20, 30, 40),
			actualOutPositions = intArrayOf(0, 0, 0),
			expectedOutPositions = intArrayOf(0, 20, 50),
		)
		testHorizontalArrangement(
			actualArrangement = Arrangement.Start,
			actualTotalSize = 140,
			actualSizes = intArrayOf(20, 30, 40),
			actualOutPositions = intArrayOf(50, 20, 0),
			expectedOutPositions = intArrayOf(0, 20, 50),
		)
	}

	@Test fun arrangementEnd() {
		testHorizontalArrangement(
			actualArrangement = Arrangement.End,
			actualTotalSize = 140,
			actualSizes = intArrayOf(20),
			actualOutPositions = intArrayOf(0),
			expectedOutPositions = intArrayOf(120),
		)
		testHorizontalArrangement(
			actualArrangement = Arrangement.End,
			actualTotalSize = 140,
			actualSizes = intArrayOf(20, 30),
			actualOutPositions = intArrayOf(0, 0),
			expectedOutPositions = intArrayOf(90, 110),
		)
		testHorizontalArrangement(
			actualArrangement = Arrangement.End,
			actualTotalSize = 140,
			actualSizes = intArrayOf(20, 30, 40),
			actualOutPositions = intArrayOf(0, 0, 0),
			expectedOutPositions = intArrayOf(50, 70, 100),
		)
		testHorizontalArrangement(
			actualArrangement = Arrangement.End,
			actualTotalSize = 140,
			actualSizes = intArrayOf(20, 30, 40),
			actualOutPositions = intArrayOf(100, 70, 50),
			expectedOutPositions = intArrayOf(50, 70, 100),
		)
	}

	@Test fun arrangementTop() {
		testVerticalArrangement(
			actualArrangement = Arrangement.Top,
			actualTotalSize = 140,
			actualSizes = intArrayOf(20),
			actualOutPositions = intArrayOf(0),
			expectedOutPositions = intArrayOf(0),
		)
		testVerticalArrangement(
			actualArrangement = Arrangement.Top,
			actualTotalSize = 140,
			actualSizes = intArrayOf(20, 30),
			actualOutPositions = intArrayOf(0, 0),
			expectedOutPositions = intArrayOf(0, 20),
		)
		testVerticalArrangement(
			actualArrangement = Arrangement.Top,
			actualTotalSize = 140,
			actualSizes = intArrayOf(20, 30, 40),
			actualOutPositions = intArrayOf(0, 0, 0),
			expectedOutPositions = intArrayOf(0, 20, 50),
		)
		testVerticalArrangement(
			actualArrangement = Arrangement.Top,
			actualTotalSize = 140,
			actualSizes = intArrayOf(20, 30, 40),
			actualOutPositions = intArrayOf(50, 20, 0),
			expectedOutPositions = intArrayOf(0, 20, 50),
		)
	}

	@Test fun arrangementBottom() {
		testVerticalArrangement(
			actualArrangement = Arrangement.Bottom,
			actualTotalSize = 140,
			actualSizes = intArrayOf(20),
			actualOutPositions = intArrayOf(0),
			expectedOutPositions = intArrayOf(120),
		)
		testVerticalArrangement(
			actualArrangement = Arrangement.Bottom,
			actualTotalSize = 140,
			actualSizes = intArrayOf(20, 30),
			actualOutPositions = intArrayOf(0, 0),
			expectedOutPositions = intArrayOf(90, 110),
		)
		testVerticalArrangement(
			actualArrangement = Arrangement.Bottom,
			actualTotalSize = 140,
			actualSizes = intArrayOf(20, 30, 40),
			actualOutPositions = intArrayOf(0, 0, 0),
			expectedOutPositions = intArrayOf(50, 70, 100),
		)
		testVerticalArrangement(
			actualArrangement = Arrangement.Bottom,
			actualTotalSize = 140,
			actualSizes = intArrayOf(20, 30, 40),
			actualOutPositions = intArrayOf(100, 70, 50),
			expectedOutPositions = intArrayOf(50, 70, 100),
		)
	}

	@Test fun arrangementCenter() {
		testVerticalArrangement(
			actualArrangement = Arrangement.Center,
			actualTotalSize = 140,
			actualSizes = intArrayOf(20),
			actualOutPositions = intArrayOf(0),
			expectedOutPositions = intArrayOf(60),
		)
		testVerticalArrangement(
			actualArrangement = Arrangement.Center,
			actualTotalSize = 140,
			actualSizes = intArrayOf(20, 30),
			actualOutPositions = intArrayOf(0, 0),
			expectedOutPositions = intArrayOf(45, 65),
		)
		testVerticalArrangement(
			actualArrangement = Arrangement.Center,
			actualTotalSize = 140,
			actualSizes = intArrayOf(20, 30, 40),
			actualOutPositions = intArrayOf(0, 0, 0),
			expectedOutPositions = intArrayOf(25, 45, 75),
		)
		testVerticalArrangement(
			actualArrangement = Arrangement.Center,
			actualTotalSize = 140,
			actualSizes = intArrayOf(20, 30, 40),
			actualOutPositions = intArrayOf(75, 45, 25),
			expectedOutPositions = intArrayOf(25, 45, 75),
		)
	}

	@Test fun arrangementSpaceEvenly() {
		testVerticalArrangement(
			actualArrangement = Arrangement.SpaceEvenly,
			actualTotalSize = 140,
			actualSizes = intArrayOf(20),
			actualOutPositions = intArrayOf(0),
			expectedOutPositions = intArrayOf(60),
		)
		testVerticalArrangement(
			actualArrangement = Arrangement.SpaceEvenly,
			actualTotalSize = 140,
			actualSizes = intArrayOf(20, 30),
			actualOutPositions = intArrayOf(0, 0),
			expectedOutPositions = intArrayOf(30, 80),
		)
		testVerticalArrangement(
			actualArrangement = Arrangement.SpaceEvenly,
			actualTotalSize = 150,
			actualSizes = intArrayOf(20, 30, 40),
			actualOutPositions = intArrayOf(0, 0, 0),
			expectedOutPositions = intArrayOf(15, 50, 95),
		)
		testVerticalArrangement(
			actualArrangement = Arrangement.SpaceEvenly,
			actualTotalSize = 150,
			actualSizes = intArrayOf(20, 30, 40),
			actualOutPositions = intArrayOf(95, 50, 15),
			expectedOutPositions = intArrayOf(15, 50, 95),
		)
	}

	@Test fun arrangementSpaceBetween() {
		testVerticalArrangement(
			actualArrangement = Arrangement.SpaceBetween,
			actualTotalSize = 140,
			actualSizes = intArrayOf(20),
			actualOutPositions = intArrayOf(0),
			expectedOutPositions = intArrayOf(0),
		)
		testVerticalArrangement(
			actualArrangement = Arrangement.SpaceBetween,
			actualTotalSize = 140,
			actualSizes = intArrayOf(20, 30),
			actualOutPositions = intArrayOf(0, 0),
			expectedOutPositions = intArrayOf(0, 110),
		)
		testVerticalArrangement(
			actualArrangement = Arrangement.SpaceBetween,
			actualTotalSize = 140,
			actualSizes = intArrayOf(20, 30, 40),
			actualOutPositions = intArrayOf(0, 0, 0),
			expectedOutPositions = intArrayOf(0, 45, 100),
		)
		testVerticalArrangement(
			actualArrangement = Arrangement.SpaceBetween,
			actualTotalSize = 140,
			actualSizes = intArrayOf(20, 30, 40),
			actualOutPositions = intArrayOf(100, 45, 0),
			expectedOutPositions = intArrayOf(0, 45, 100),
		)
	}

	@Test fun arrangementSpaceAroung() {
		testVerticalArrangement(
			actualArrangement = Arrangement.SpaceAround,
			actualTotalSize = 140,
			actualSizes = intArrayOf(20),
			actualOutPositions = intArrayOf(0),
			expectedOutPositions = intArrayOf(60),
		)
		testVerticalArrangement(
			actualArrangement = Arrangement.SpaceAround,
			actualTotalSize = 150,
			actualSizes = intArrayOf(20, 30),
			actualOutPositions = intArrayOf(0, 0),
			expectedOutPositions = intArrayOf(25, 95),
		)
		testVerticalArrangement(
			actualArrangement = Arrangement.SpaceAround,
			actualTotalSize = 150,
			actualSizes = intArrayOf(20, 30, 40),
			actualOutPositions = intArrayOf(0, 0, 0),
			expectedOutPositions = intArrayOf(10, 50, 100),
		)
		testVerticalArrangement(
			actualArrangement = Arrangement.SpaceAround,
			actualTotalSize = 150,
			actualSizes = intArrayOf(20, 30, 40),
			actualOutPositions = intArrayOf(100, 50, 10),
			expectedOutPositions = intArrayOf(10, 50, 100),
		)
	}

	private fun testVerticalArrangement(
		actualArrangement: Arrangement.Vertical,
		actualTotalSize: Int,
		actualSizes: IntArray,
		actualOutPositions: IntArray,
		expectedOutPositions: IntArray,
	) {
		actualArrangement.arrange(actualTotalSize, actualSizes, actualOutPositions)
		assertThat(actualOutPositions).isEqualTo(expectedOutPositions)
	}

	private fun testHorizontalArrangement(
		actualArrangement: Arrangement.Horizontal,
		actualTotalSize: Int,
		actualSizes: IntArray,
		actualOutPositions: IntArray,
		expectedOutPositions: IntArray,
	) {
		actualArrangement.arrange(actualTotalSize, actualSizes, actualOutPositions)
		assertThat(actualOutPositions).isEqualTo(expectedOutPositions)
	}
}
