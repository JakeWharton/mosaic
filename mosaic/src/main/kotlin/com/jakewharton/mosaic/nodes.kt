package com.jakewharton.mosaic

import androidx.compose.runtime.AbstractApplier
import com.facebook.yoga.YogaMeasureOutput
import com.facebook.yoga.YogaNode
import com.facebook.yoga.YogaNodeFactory
import com.jakewharton.crossword.visualCodePointCount

internal sealed class MosaicNode {
	val yoga: YogaNode = YogaNodeFactory.create()
}

internal class TextNode(initialValue: String = "") : MosaicNode() {
	init {
		yoga.setMeasureFunction { _, _, _, _, _ ->
			val lines = value.split('\n')
			val measuredWidth = lines.maxOf { it.visualCodePointCount }
			val measuredHeight = lines.size
			YogaMeasureOutput.make(measuredWidth, measuredHeight)
		}
	}

	var value: String = initialValue
		set(value) {
			field = value
			yoga.dirty()
		}

	override fun toString() = "Text($value)"
}

internal class BoxNode : MosaicNode() {
	val children = mutableListOf<MosaicNode>()

	override fun toString() = children.joinToString(prefix = "Box(", postfix = ")")
}

internal class MosaicNodeApplier(root: BoxNode) : AbstractApplier<MosaicNode>(root) {
	override fun insert(index: Int, instance: MosaicNode) {
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
