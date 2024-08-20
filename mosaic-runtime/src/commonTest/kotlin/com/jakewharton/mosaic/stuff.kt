@file:Suppress("NOTHING_TO_INLINE")

package com.jakewharton.mosaic

import androidx.compose.runtime.Composable
import androidx.compose.runtime.snapshots.SnapshotStateList
import com.jakewharton.mosaic.layout.IntrinsicMeasurable
import com.jakewharton.mosaic.layout.Measurable
import com.jakewharton.mosaic.layout.MeasurePolicy
import com.jakewharton.mosaic.layout.MeasureResult
import com.jakewharton.mosaic.layout.MeasureScope
import com.jakewharton.mosaic.layout.MosaicNode
import com.jakewharton.mosaic.layout.Placeable
import com.jakewharton.mosaic.modifier.Modifier
import com.jakewharton.mosaic.ui.Alignment
import com.jakewharton.mosaic.ui.Filler
import com.jakewharton.mosaic.ui.Layout
import com.jakewharton.mosaic.ui.unit.Constraints
import com.jakewharton.mosaic.ui.unit.IntOffset
import com.jakewharton.mosaic.ui.unit.IntSize
import com.jakewharton.mosaic.ui.unit.constrain
import com.jakewharton.mosaic.ui.unit.constrainHeight
import com.jakewharton.mosaic.ui.unit.constrainWidth
import kotlin.math.max
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.first

const val s = " "

const val TestChar = 'X'

fun String.replaceLineEndingsWithCRLF(): String {
	return this.replace("\n", "\r\n")
}

fun String.wrapWithAnsiSynchronizedUpdate(): String {
	return "$ansiBeginSynchronizedUpdate$this$ansiEndSynchronizedUpdate"
}

fun <T> snapshotStateListOf(vararg values: T): SnapshotStateList<T> {
	return SnapshotStateList<T>().apply { addAll(values) }
}

internal val MosaicNode.size: IntSize
	get() = IntSize(width, height)

internal val MosaicNode.position: IntOffset
	get() = IntOffset(x, y)

internal suspend fun mosaicNodesWithMeasureAndPlace(content: @Composable () -> Unit): MosaicNode {
	return callbackFlow {
		// if rendering is enabled, the measureAndPlace is implicitly called
		runMosaicTest(withRenderSnapshots = false) {
			setContent(content)
			send(awaitNodeSnapshot().also { it.measureAndPlace() })
		}
	}.first()
}

class Holder<T>(var value: T)

@Composable
inline fun TestFiller(modifier: Modifier = Modifier) {
	Filler(TestChar, modifier = modifier)
}

@Composable
fun Container(
	modifier: Modifier = Modifier,
	alignment: Alignment = Alignment.Center,
	expanded: Boolean = false,
	constraints: Constraints = Constraints(),
	width: Int? = null,
	height: Int? = null,
	content: @Composable () -> Unit = {},
) {
	Layout(content, modifier, debugInfo = { "Container()" }) { measurables, incomingConstraints ->
		val containerConstraints = incomingConstraints.constrain(
			Constraints(constraints.value)
				.copy(
					width ?: constraints.minWidth,
					width ?: constraints.maxWidth,
					height ?: constraints.minHeight,
					height ?: constraints.maxHeight,
				),
		)
		val childConstraints = containerConstraints.copy(minWidth = 0, minHeight = 0)
		var placeable: Placeable? = null
		val containerWidth = if ((containerConstraints.hasFixedWidth || expanded) &&
			containerConstraints.hasBoundedWidth
		) {
			containerConstraints.maxWidth
		} else {
			placeable = measurables.firstOrNull()?.measure(childConstraints)
			max(placeable?.width ?: 0, containerConstraints.minWidth)
		}
		val containerHeight = if ((containerConstraints.hasFixedHeight || expanded) &&
			containerConstraints.hasBoundedHeight
		) {
			containerConstraints.maxHeight
		} else {
			if (placeable == null) {
				placeable = measurables.firstOrNull()?.measure(childConstraints)
			}
			max(placeable?.height ?: 0, containerConstraints.minHeight)
		}
		layout(containerWidth, containerHeight) {
			val p = placeable ?: measurables.firstOrNull()?.measure(childConstraints)
			p?.let {
				val position = alignment.align(
					IntSize(it.width, it.height),
					IntSize(containerWidth, containerHeight),
				)
				it.place(position.x, position.y)
			}
		}
	}
}

suspend fun testIntrinsics(
	vararg layouts: @Composable () -> Unit,
	test: ((Int) -> Int, (Int) -> Int, (Int) -> Int, (Int) -> Int) -> Unit,
) {
	layouts.forEach { layout ->
		runMosaicTest {
			setContent {
				val measurePolicy = object : MeasurePolicy {
					override fun MeasureScope.measure(
						measurables: List<Measurable>,
						constraints: Constraints,
					): MeasureResult {
						val measurable = measurables.first()
						test(
							{ h -> measurable.minIntrinsicWidth(h) },
							{ w -> measurable.minIntrinsicHeight(w) },
							{ h -> measurable.maxIntrinsicWidth(h) },
							{ w -> measurable.maxIntrinsicHeight(w) },
						)

						return layout(0, 0) {}
					}

					override fun minIntrinsicWidth(
						measurables: List<IntrinsicMeasurable>,
						height: Int,
					) = 0

					override fun minIntrinsicHeight(
						measurables: List<IntrinsicMeasurable>,
						width: Int,
					) = 0

					override fun maxIntrinsicWidth(
						measurables: List<IntrinsicMeasurable>,
						height: Int,
					) = 0

					override fun maxIntrinsicHeight(
						measurables: List<IntrinsicMeasurable>,
						width: Int,
					) = 0
				}
				Layout(
					content = layout,
					measurePolicy = measurePolicy,
				)
			}
		}
	}
}

@Composable
internal fun ConstrainedBox(
	constraints: Constraints,
	modifier: Modifier = Modifier,
	content: @Composable () -> Unit = {},
) {
	@Suppress("PARAMETER_NAME_CHANGED_ON_OVERRIDE")
	val measurePolicy = object : MeasurePolicy {
		override fun MeasureScope.measure(
			measurables: List<Measurable>,
			incomingConstraints: Constraints,
		): MeasureResult {
			val measurable = measurables.firstOrNull()
			val childConstraints = incomingConstraints.constrain(constraints)
			val placeable = measurable?.measure(childConstraints)

			val layoutWidth = placeable?.width ?: childConstraints.minWidth
			val layoutHeight = placeable?.height ?: childConstraints.minHeight
			return layout(layoutWidth, layoutHeight) {
				placeable?.place(0, 0)
			}
		}

		override fun minIntrinsicWidth(
			measurables: List<IntrinsicMeasurable>,
			height: Int,
		): Int {
			val width = measurables.firstOrNull()?.minIntrinsicWidth(height) ?: 0
			return constraints.constrainWidth(width)
		}

		override fun minIntrinsicHeight(
			measurables: List<IntrinsicMeasurable>,
			width: Int,
		): Int {
			val height = measurables.firstOrNull()?.minIntrinsicHeight(width) ?: 0
			return constraints.constrainHeight(height)
		}

		override fun maxIntrinsicWidth(
			measurables: List<IntrinsicMeasurable>,
			height: Int,
		): Int {
			val width = measurables.firstOrNull()?.maxIntrinsicWidth(height) ?: 0
			return constraints.constrainWidth(width)
		}

		override fun maxIntrinsicHeight(
			measurables: List<IntrinsicMeasurable>,
			width: Int,
		): Int {
			val height = measurables.firstOrNull()?.maxIntrinsicHeight(width) ?: 0
			return constraints.constrainHeight(height)
		}
	}
	Layout(
		content = content,
		modifier = modifier,
		measurePolicy = measurePolicy,
	)
}
