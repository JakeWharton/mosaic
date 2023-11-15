package com.jakewharton.mosaic.ui

import com.jakewharton.mosaic.layout.Measurable
import com.jakewharton.mosaic.layout.Placeable
import com.jakewharton.mosaic.ui.unit.Constraints
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.math.sign

/**
 * This is a data class that holds the determined width, height of a row,
 * and information on how to retrieve main axis and cross axis positions.
 */
internal class RowColumnMeasureHelperResult(
	val crossAxisSize: Int,
	val mainAxisSize: Int,
	val startIndex: Int,
	val endIndex: Int,
	val mainAxisPositions: IntArray,
)

/**
 * RowColumnMeasurementHelper
 * Measures the row and column without placing, useful for reusing row/column logic
 */
internal class RowColumnMeasurementHelper(
	private val orientation: LayoutOrientation,
	private val horizontalArrangement: Arrangement.Horizontal?,
	private val verticalArrangement: Arrangement.Vertical?,
	private val arrangementSpacing: Int,
	private val crossAxisSize: SizeMode,
	private val crossAxisAlignment: CrossAxisAlignment,
	private val measurables: List<Measurable>,
	private val placeables: Array<Placeable?>,
) {

	private val rowColumnParentData = Array(measurables.size) {
		measurables[it].rowColumnParentData
	}

	private fun Placeable.mainAxisSize() =
		if (orientation == LayoutOrientation.Horizontal) width else height

	private fun Placeable.crossAxisSize() =
		if (orientation == LayoutOrientation.Horizontal) height else width

	/**
	 * Measures the row and column without placing, useful for reusing row/column logic
	 *
	 * @param constraints The desired constraints for the startIndex and endIndex
	 * can hold null items if not measured.
	 * @param startIndex The startIndex (inclusive) when examining measurables, placeable
	 * and parentData
	 * @param endIndex The ending index (exclusive) when examinning measurable, placeable
	 * and parentData
	 */
	fun measureWithoutPlacing(
		constraints: Constraints,
		startIndex: Int,
		endIndex: Int,
	): RowColumnMeasureHelperResult {
		@Suppress("NAME_SHADOWING")
		val constraints = OrientationIndependentConstraints(constraints, orientation)

		var totalWeight = 0f
		var fixedSpace = 0L
		var crossAxisSpace = 0
		var weightChildrenCount = 0

		val subSize = endIndex - startIndex

		// First measure children with zero weight.
		var spaceAfterLastNoWeight = 0
		for (i in startIndex until endIndex) {
			val child = measurables[i]
			val parentData = rowColumnParentData[i]
			val weight = parentData.weight

			if (weight > 0f) {
				totalWeight += weight
				++weightChildrenCount
			} else {
				val mainAxisMax = constraints.mainAxisMax
				val placeable = placeables[i] ?: child.measure(
					// Ask for preferred main axis size.
					constraints.copy(
						mainAxisMin = 0,
						mainAxisMax = if (mainAxisMax == Constraints.Infinity) {
							Constraints.Infinity
						} else {
							(mainAxisMax - fixedSpace).coerceAtLeast(0).toInt()
						},
						crossAxisMin = 0,
					).toBoxConstraints(orientation),
				)
				spaceAfterLastNoWeight = min(
					arrangementSpacing,
					(mainAxisMax - fixedSpace - placeable.mainAxisSize())
						.coerceAtLeast(0).toInt(),
				)
				fixedSpace += placeable.mainAxisSize() + spaceAfterLastNoWeight
				crossAxisSpace = max(crossAxisSpace, placeable.crossAxisSize())
				placeables[i] = placeable
			}
		}

		var weightedSpace = 0
		if (weightChildrenCount == 0) {
			// fixedSpace contains an extra spacing after the last non-weight child.
			fixedSpace -= spaceAfterLastNoWeight
		} else {
			// Measure the rest according to their weights in the remaining main axis space.
			val targetSpace =
				if (totalWeight > 0f && constraints.mainAxisMax != Constraints.Infinity) {
					constraints.mainAxisMax
				} else {
					constraints.mainAxisMin
				}
			val arrangementSpacingTotal = arrangementSpacing.toLong() * (weightChildrenCount - 1)
			val remainingToTarget =
				(targetSpace - fixedSpace - arrangementSpacingTotal).coerceAtLeast(0)

			val weightUnitSpace = if (totalWeight > 0) remainingToTarget / totalWeight else 0f
			var remainder = remainingToTarget - (startIndex until endIndex).sumOf {
				(weightUnitSpace * rowColumnParentData[it].weight).roundToInt()
			}

			for (i in startIndex until endIndex) {
				if (placeables[i] == null) {
					val child = measurables[i]
					val parentData = rowColumnParentData[i]
					val weight = parentData.weight
					check(weight > 0) { "All weights <= 0 should have placeables" }
					// After the weightUnitSpace rounding, the total space going to be occupied
					// can be smaller or larger than remainingToTarget. Here we distribute the
					// loss or gain remainder evenly to the first children.
					val remainderUnit = remainder.sign
					remainder -= remainderUnit
					val childMainAxisSize = max(
						0,
						(weightUnitSpace * weight).roundToInt() + remainderUnit,
					)
					val placeable = child.measure(
						OrientationIndependentConstraints(
							if (parentData.fill && childMainAxisSize != Constraints.Infinity) {
								childMainAxisSize
							} else {
								0
							},
							childMainAxisSize,
							0,
							constraints.crossAxisMax,
						).toBoxConstraints(orientation),
					)
					weightedSpace += placeable.mainAxisSize()
					crossAxisSpace = max(crossAxisSpace, placeable.crossAxisSize())
					placeables[i] = placeable
				}
			}
			weightedSpace = (weightedSpace + arrangementSpacingTotal)
				.coerceIn(0, constraints.mainAxisMax - fixedSpace)
				.toInt()
		}

		// Compute the Row or Column size and position the children.
		val mainAxisLayoutSize = max(
			(fixedSpace + weightedSpace).coerceAtLeast(0).toInt(),
			constraints.mainAxisMin,
		)
		val crossAxisLayoutSize = if (constraints.crossAxisMax != Constraints.Infinity &&
			crossAxisSize == SizeMode.Expand
		) {
			constraints.crossAxisMax
		} else {
			max(crossAxisSpace, constraints.crossAxisMin)
		}
		val mainAxisPositions = IntArray(subSize) { 0 }
		val childrenMainAxisSize = IntArray(subSize) { index ->
			placeables[index + startIndex]!!.mainAxisSize()
		}

		return RowColumnMeasureHelperResult(
			mainAxisSize = mainAxisLayoutSize,
			crossAxisSize = crossAxisLayoutSize,
			startIndex = startIndex,
			endIndex = endIndex,
			mainAxisPositions = mainAxisPositions(
				mainAxisLayoutSize,
				childrenMainAxisSize,
				mainAxisPositions,
			),
		)
	}

	private fun mainAxisPositions(
		mainAxisLayoutSize: Int,
		childrenMainAxisSize: IntArray,
		mainAxisPositions: IntArray,
	): IntArray {
		if (orientation == LayoutOrientation.Vertical) {
			with(requireNotNull(verticalArrangement) { "null verticalArrangement in Column" }) {
				arrange(
					mainAxisLayoutSize,
					childrenMainAxisSize,
					mainAxisPositions,
				)
			}
		} else {
			with(requireNotNull(horizontalArrangement) { "null horizontalArrangement in Row" }) {
				arrange(
					mainAxisLayoutSize,
					childrenMainAxisSize,
					mainAxisPositions,
				)
			}
		}
		return mainAxisPositions
	}

	private fun getCrossAxisPosition(
		placeable: Placeable,
		parentData: RowColumnParentData?,
		crossAxisLayoutSize: Int,
	): Int {
		val childCrossAlignment = parentData?.crossAxisAlignment ?: crossAxisAlignment
		return childCrossAlignment.align(
			size = crossAxisLayoutSize - placeable.crossAxisSize(),
			placeable = placeable,
		)
	}

	fun placeHelper(
		placeableScope: Placeable.PlacementScope,
		measureResult: RowColumnMeasureHelperResult,
		crossAxisOffset: Int,
	) {
		with(placeableScope) {
			for (i in measureResult.startIndex until measureResult.endIndex) {
				val placeable = placeables[i]
				placeable!!
				val mainAxisPositions = measureResult.mainAxisPositions
				val crossAxisPosition = getCrossAxisPosition(
					placeable,
					(measurables[i].parentData as? RowColumnParentData),
					measureResult.crossAxisSize,
				) + crossAxisOffset
				if (orientation == LayoutOrientation.Horizontal) {
					placeable.place(
						mainAxisPositions[i - measureResult.startIndex],
						crossAxisPosition,
					)
				} else {
					placeable.place(
						crossAxisPosition,
						mainAxisPositions[i - measureResult.startIndex],
					)
				}
			}
		}
	}
}
