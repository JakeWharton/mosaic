package com.jakewharton.mosaic

import androidx.compose.runtime.AbstractApplier
import androidx.compose.runtime.Applier
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReusableComposeNode
import com.jakewharton.mosaic.Measurable.MeasureScope

internal fun interface DrawPolicy {
	fun MosaicNode.performDraw(canvas: TextCanvas)

	companion object {
		val Children = DrawPolicy { canvas ->
			for (child in children) {
				if (child.width != 0 && child.height != 0) {
					val left = child.x
					val top = child.y
					val right = left + child.width - 1
					val bottom = top + child.height - 1
					child.drawTo(canvas[top..bottom, left..right])
				}
			}
		}
	}
}

internal fun interface StaticDrawPolicy {
	fun MosaicNode.performDrawStatics(): List<TextCanvas>

	companion object {
		val Children = StaticDrawPolicy {
			var statics: MutableList<TextCanvas>? = null
			for (child in children) {
				val childStatics = child.drawStatics()
				if (childStatics.isNotEmpty()) {
					if (statics == null) {
						statics = mutableListOf()
					}
					statics += childStatics
				}
			}
			statics ?: emptyList()
		}
	}
}

internal fun interface DebugPolicy {
	fun MosaicNode.renderDebug(): String
}

internal class MosaicNode(
	var measurePolicy: MeasurePolicy,
	var drawPolicy: DrawPolicy,
	var staticDrawPolicy: StaticDrawPolicy,
	var debugPolicy: DebugPolicy,
) : Placeable(), Measurable {
	val children: MutableList<MosaicNode> = mutableListOf()

	// These two values are set by a call to `measure`.
	override var width = 0
		private set
	override var height = 0
		private set

	/** Measure this node (and any children) and update [width] and [height]. */
	override fun measure(): Placeable = apply {
		val result = measurePolicy.run { MeasureScope.run { measure(children) } }
		width = result.width
		height = result.height
		result.placeChildren()
	}

	// These two values are set by a call to `placeAt`.
	/** Pixels right relative to parent at which this node will draw. */
	var x = 0
		private set
	/** Pixels down relative to parent at which this node will draw. */
	var y = 0
		private set

	override fun placeAt(x: Int, y: Int) {
		this.x = x
		this.y = y
	}

	fun drawTo(canvas: TextCanvas) = drawPolicy.run { performDraw(canvas) }

	fun draw(): TextCanvas {
		measure()
		val surface = TextSurface(width, height)
		drawTo(surface)
		return surface
	}

	fun drawStatics() = staticDrawPolicy.run { performDrawStatics() }

	override fun toString() = debugPolicy.run { renderDebug() }

	companion object {
		val Factory: () -> MosaicNode = {
			MosaicNode(
				measurePolicy = ThrowingPolicy,
				drawPolicy = ThrowingPolicy,
				staticDrawPolicy = ThrowingPolicy,
				debugPolicy = ThrowingPolicy,
			)
		}

		fun root(): MosaicNode {
			return MosaicNode(
				measurePolicy = { measurables ->
					var width = 0
					var height = 0
					for (measurable in measurables) {
						measurable.measure().also {
							width = maxOf(width, it.width)
							height = maxOf(height, it.height)
						}
					}
					layout(width, height) {
						// Nothing to do. Everything renders at (0,0).
					}
				},
				drawPolicy = DrawPolicy.Children,
				staticDrawPolicy = StaticDrawPolicy.Children,
				debugPolicy = {
					children.joinToString(separator = "\n")
				}
			)
		}

		private val ThrowingPolicy = object : MeasurePolicy, DrawPolicy, StaticDrawPolicy, DebugPolicy {
			override fun MeasureScope.measure(measurables: List<Measurable>) = throw AssertionError()
			override fun MosaicNode.performDraw(canvas: TextCanvas) = throw AssertionError()
			override fun MosaicNode.performDrawStatics() = throw AssertionError()
			override fun MosaicNode.renderDebug() = throw AssertionError()
		}
	}
}

@Composable
internal inline fun Node(
	content: @Composable () -> Unit = {},
	measurePolicy: MeasurePolicy,
	drawPolicy: DrawPolicy,
	staticDrawPolicy: StaticDrawPolicy,
	debugPolicy: DebugPolicy,
) {
	ReusableComposeNode<MosaicNode, Applier<Any>>(
		factory = MosaicNode.Factory,
		update = {
			set(measurePolicy) { this.measurePolicy = measurePolicy }
			set(drawPolicy) { this.drawPolicy = drawPolicy }
			set(staticDrawPolicy) { this.staticDrawPolicy = staticDrawPolicy }
			set(debugPolicy) { this.debugPolicy = debugPolicy }
		},
		content = content,
	)
}

internal class MosaicNodeApplier(root: MosaicNode) : AbstractApplier<MosaicNode>(root) {
	override fun insertTopDown(index: Int, instance: MosaicNode) {
		// Ignored, we insert bottom-up.
	}

	override fun insertBottomUp(index: Int, instance: MosaicNode) {
		current.children.add(index, instance)
	}

	override fun remove(index: Int, count: Int) {
		current.children.remove(index, count)
	}

	override fun move(from: Int, to: Int, count: Int) {
		current.children.move(from, to, count)
	}

	override fun onClear() {}
}
