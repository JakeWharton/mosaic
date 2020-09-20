package com.jakewharton.mosaic

import androidx.compose.runtime.AbstractApplier
import com.facebook.yoga.YogaNode
import com.facebook.yoga.YogaNodeFactory

internal sealed class MosaicNode {
	val yoga: YogaNode = YogaNodeFactory.create()
}

internal class TextNode : MosaicNode() {
	var value: String = ""
		set(value) {
			yoga.setWidth(value.length.toFloat())
			yoga.setHeight(value.split('\n').size.toFloat())
			field = value
		}
}

internal class BoxNode : MosaicNode() {
	val children = mutableListOf<MosaicNode>()
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
		TODO("Cannot move children in Yoga yet")
	}

	override fun onClear() {
		val boxNode = root as BoxNode
		// Remove in reverse to avoid internal list copies.
		for (i in boxNode.yoga.childCount - 1 downTo 0) {
			boxNode.yoga.removeChildAt(i)
		}
	}
}
