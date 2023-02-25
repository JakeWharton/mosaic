package com.jakewharton.mosaic

import androidx.compose.runtime.AbstractApplier
import de.cketti.codepoints.codePointCount

internal sealed class MosaicNode {
	// These two values are set by a call to `measure`.
	var width = 0
	var height = 0

	// These two values are set by a call to `layout` on the parent node.
	/** Pixels right relative to parent at which this node will draw. */
	var x = 0
	/** Pixels down relative to parent at which this node will draw. */
	var y = 0

	/** Measure this node (and any children) and update [width] and [height]. */
	abstract fun measure()
	/** Layout any children nodes and update their [x] and [y] relative to this node. */
	abstract fun layout()
	abstract fun drawTo(canvas: TextCanvas)

	fun draw(): TextCanvas {
		measure()
		layout()
		val canvas = TextSurface(width, height)
		drawTo(canvas)
		return canvas
	}

	abstract fun drawStatics(): List<TextCanvas>
}

internal class TextNode(initialValue: String = "") : MosaicNode() {
	private var sizeInvalidated = true
	var value: String = initialValue
		set(value) {
			field = value
			sizeInvalidated = true
		}

	var foreground: Color? = null
	var background: Color? = null
	var style: TextStyle? = null

	override fun measure() {
		if (sizeInvalidated) {
			val lines = value.split('\n')
			width = lines.maxOf { it.codePointCount(0, it.length) }
			height = lines.size
			sizeInvalidated = false
		}
	}

	override fun layout() {
		// No children.
	}

	override fun drawTo(canvas: TextCanvas) {
		value.split('\n').forEachIndexed { index, line ->
			canvas.write(index, 0, line, foreground, background, style)
		}
	}

	override fun drawStatics() = emptyList<TextCanvas>()

	override fun toString() = "Text(\"$value\", x=$x, y=$y, width=$width, height=$height)"
}

internal sealed class ContainerNode : MosaicNode() {
	abstract val children: MutableList<MosaicNode>
}

internal class BoxNode : ContainerNode() {
	override val children = mutableListOf<MosaicNode>()

	override fun measure() {
		var width = 0
		var height = 0
		for (child in children) {
			child.measure()
			width = maxOf(width, child.width)
			height = maxOf(height, child.height)
		}
		this.width = width
		this.height = height
	}

	override fun layout() {
		for (child in children) {
			child.layout()
		}
	}

	override fun drawTo(canvas: TextCanvas) {
		for (child in children) {
			if (child.width != 0 && child.height != 0) {
				child.drawTo(canvas[0 until child.height, 0 until child.width])
			} else {
				child.drawTo(canvas.empty())
			}
		}
	}

	override fun drawStatics(): List<TextCanvas> {
		return children.flatMap(MosaicNode::drawStatics)
	}

	override fun toString() = children.joinToString(prefix = "Box(", postfix = ")")
}

internal class LinearNode(var isRow: Boolean = true) : ContainerNode() {
	override val children = mutableListOf<MosaicNode>()

	override fun measure() {
		if (isRow) {
			measureRow()
		} else {
			measureColumn()
		}
	}

	private fun measureRow() {
		var width = 0
		var height = 0
		for (child in children) {
			child.measure()
			width += child.width
			height = maxOf(height, child.height)
		}
		this.width = width
		this.height = height
	}

	private fun measureColumn() {
		var width = 0
		var height = 0
		for (child in children) {
			child.measure()
			width = maxOf(width, child.width)
			height += child.height
		}
		this.width = width
		this.height = height
	}

	override fun layout() {
		if (isRow) {
			layoutRow()
		} else {
			layoutColumn()
		}
	}

	private fun layoutRow() {
		var childX = 0
		for (child in children) {
			child.x = childX
			child.y = 0
			child.layout()
			childX += child.width
		}
	}

	private fun layoutColumn() {
		var childY = 0
		for (child in children) {
			child.x = 0
			child.y = childY
			child.layout()
			childY += child.height
		}
	}

	override fun drawTo(canvas: TextCanvas) {
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

	override fun drawStatics(): List<TextCanvas> {
		return children.flatMap(MosaicNode::drawStatics)
	}

	override fun toString() = buildString {
		append(if (isRow) "Row" else "Column")
		children.joinTo(this, prefix = "(", postfix = ")")
	}
}

internal class StaticNode(
	private val onPostDraw: () -> Unit,
) : ContainerNode() {
	// Delegate container column for static content.
	private val box = LinearNode(isRow = false)

	override val children: MutableList<MosaicNode>
		get() = box.children

	override fun measure() {
		// Not visible.
	}

	override fun layout() {
		// Not visible.
	}

	override fun drawTo(canvas: TextCanvas) {
		// No content.
	}

	override fun drawStatics(): List<TextCanvas> {
		val statics = mutableListOf<TextCanvas>()

		// Draw contents of static node to a separate node hierarchy.
		val static = box.draw()

		// Add display canvas to static canvases if it is not empty.
		if (static.width > 0 && static.height > 0) {
			statics.add(static)
		}

		// Propagate any static content of this static node.
		statics.addAll(box.drawStatics())

		onPostDraw()

		return statics
	}

	override fun toString() = box.children.joinToString(prefix = "Static(", postfix = ")")
}

internal class MosaicNodeApplier(root: MosaicNode) : AbstractApplier<MosaicNode>(root) {
	override fun insertTopDown(index: Int, instance: MosaicNode) {
		// Ignored, we insert bottom-up.
	}

	override fun insertBottomUp(index: Int, instance: MosaicNode) {
		val boxNode = current as ContainerNode
		boxNode.children.add(index, instance)
	}

	override fun remove(index: Int, count: Int) {
		val boxNode = current as ContainerNode
		boxNode.children.remove(index, count)
	}

	override fun move(from: Int, to: Int, count: Int) {
		val boxNode = current as ContainerNode
		boxNode.children.move(from, to, count)
	}

	override fun onClear() {}
}
