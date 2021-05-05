package com.jakewharton.mosaic

import androidx.compose.runtime.AbstractApplier
import com.facebook.yoga.YogaConstants.UNDEFINED
import com.facebook.yoga.YogaMeasureOutput
import com.facebook.yoga.YogaNode
import com.facebook.yoga.YogaNodeFactory

internal sealed class MosaicNode {
	val yoga: YogaNode = YogaNodeFactory.create()

	abstract fun renderTo(canvas: TextCanvas)

	fun render(): String {
		val canvas = with(yoga) {
			calculateLayout(UNDEFINED, UNDEFINED)
			TextSurface(layoutWidth.toInt(), layoutHeight.toInt())
		}
		renderTo(canvas)
		return canvas.toString()
	}
}

internal class TextNode(initialValue: String = "") : MosaicNode() {
	init {
		yoga.setMeasureFunction { _, _, _, _, _ ->
			val lines = value.split('\n')
			val measuredWidth = lines.maxOf { it.codePointCount(0, it.length) }
			val measuredHeight = lines.size
			YogaMeasureOutput.make(measuredWidth, measuredHeight)
		}
	}

	var value: String = initialValue
		set(value) {
			field = value
			yoga.dirty()
		}

	var foreground: Color? = null
	var background: Color? = null
	var style: TextStyle? = null

	override fun renderTo(canvas: TextCanvas) {
		value.split('\n').forEachIndexed { index, line ->
			canvas.write(index, 0, line, foreground, background, style)
		}
	}

	override fun toString() = "Text($value)"
}

internal class BoxNode : MosaicNode() {
	val children = mutableListOf<MosaicNode>()

	override fun renderTo(canvas: TextCanvas) {
		for (child in children) {
			val childYoga = child.yoga
			val left = childYoga.layoutX.toInt()
			val top = childYoga.layoutY.toInt()
			val right = left + childYoga.layoutWidth.toInt() - 1
			val bottom = top + childYoga.layoutHeight.toInt() - 1
			child.renderTo(canvas[top..bottom, left..right])
		}
	}

	override fun toString() = children.joinToString(prefix = "Box(", postfix = ")")
}

internal class MosaicNodeApplier(root: BoxNode) : AbstractApplier<MosaicNode>(root) {
	override fun insertTopDown(index: Int, instance: MosaicNode) {
		// Ignored, we insert bottom-up.
	}

	override fun insertBottomUp(index: Int, instance: MosaicNode) {
		val boxNode = current as BoxNode
		boxNode.children.add(index, instance)
		boxNode.yoga.addChildAt(instance.yoga, index)
	}

	override fun remove(index: Int, count: Int) {
		val boxNode = current as BoxNode
		boxNode.children.remove(index, count)
		repeat(count) {
			boxNode.yoga.removeChildAt(index)
		}
	}

	override fun move(from: Int, to: Int, count: Int) {
		val boxNode = current as BoxNode
		boxNode.children.move(from, to, count)

		val yoga = boxNode.yoga
		val newIndex = if (to > from) to - count else to
		if (count == 1) {
			val node = yoga.removeChildAt(from)
			yoga.addChildAt(node, newIndex)
		} else {
			val nodes = Array(count) {
				yoga.removeChildAt(from)
			}
			nodes.forEachIndexed { offset, node ->
				yoga.addChildAt(node, newIndex + offset)
			}
		}
	}

	override fun onClear() {
		val boxNode = root as BoxNode
		// Remove in reverse to avoid internal list copies.
		for (i in boxNode.yoga.childCount - 1 downTo 0) {
			boxNode.yoga.removeChildAt(i)
		}
	}
}
