package com.jakewharton.mosaic

import androidx.compose.runtime.AbstractApplier
import androidx.compose.runtime.Applier
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReusableComposeNode

internal fun interface MeasurePolicy {
	fun MosaicNode.performMeasure()
}

internal fun interface LayoutPolicy {
	fun MosaicNode.performLayout()
}

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
				} else {
					child.drawTo(canvas.empty())
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
	var layoutPolicy: LayoutPolicy,
	var drawPolicy: DrawPolicy,
	var staticDrawPolicy: StaticDrawPolicy,
	var debugPolicy: DebugPolicy,
) {
	val children: MutableList<MosaicNode> = mutableListOf()

	// These two values are set by a call to `measurePolicy`.
	var width = 0
	var height = 0

	// These two values are set by a call to `layoutPolicy` on the parent node.
	/** Pixels right relative to parent at which this node will draw. */
	var x = 0
	/** Pixels down relative to parent at which this node will draw. */
	var y = 0

	/** Measure this node (and any children) and update [width] and [height]. */
	fun measure() = measurePolicy.run { performMeasure() }
	/** Layout any children nodes and update their [x] and [y] relative to this node. */
	fun layout() = layoutPolicy.run { performLayout() }

	fun drawTo(canvas: TextCanvas) = drawPolicy.run { performDraw(canvas) }

	fun draw(): TextCanvas {
		measure()
		layout()
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
				layoutPolicy = ThrowingPolicy,
				drawPolicy = ThrowingPolicy,
				staticDrawPolicy = ThrowingPolicy,
				debugPolicy = ThrowingPolicy,
			)
		}

		fun root(): MosaicNode {
			return MosaicNode(
				measurePolicy = {
					var width = 0
					var height = 0
					for (child in children) {
						child.measure()
						width = maxOf(width, child.width)
						height = maxOf(height, child.height)
					}
					this.width = width
					this.height = height
				},
				layoutPolicy = {
					for (child in children) {
						child.layout()
					}
				},
				drawPolicy = DrawPolicy.Children,
				staticDrawPolicy = StaticDrawPolicy.Children,
				debugPolicy = {
					children.joinToString(separator = "\n")
				}
			)
		}

		private val ThrowingPolicy = object : MeasurePolicy, LayoutPolicy, DrawPolicy, StaticDrawPolicy, DebugPolicy {
			override fun MosaicNode.performMeasure() = throw AssertionError()
			override fun MosaicNode.performLayout() = throw AssertionError()
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
	layoutPolicy: LayoutPolicy,
	drawPolicy: DrawPolicy,
	staticDrawPolicy: StaticDrawPolicy,
	debugPolicy: DebugPolicy,
) {
	ReusableComposeNode<MosaicNode, Applier<Any>>(
		factory = MosaicNode.Factory,
		update = {
			set(measurePolicy) { this.measurePolicy = measurePolicy }
			set(layoutPolicy) { this.layoutPolicy = layoutPolicy }
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
