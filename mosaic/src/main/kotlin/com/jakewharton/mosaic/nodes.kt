package com.jakewharton.mosaic

import androidx.compose.runtime.AbstractApplier
import com.facebook.yoga.YogaMeasureOutput
import com.facebook.yoga.YogaNode
import com.facebook.yoga.YogaNodeFactory
import com.jakewharton.crossword.TextCanvas
import com.jakewharton.crossword.visualCodePointCount
import com.jakewharton.mosaic.TextStyle.Companion.Bold
import com.jakewharton.mosaic.TextStyle.Companion.Dim
import com.jakewharton.mosaic.TextStyle.Companion.Invert
import com.jakewharton.mosaic.TextStyle.Companion.Italic
import com.jakewharton.mosaic.TextStyle.Companion.Strikethrough
import com.jakewharton.mosaic.TextStyle.Companion.Underline

internal sealed class MosaicNode {
	val yoga: YogaNode = YogaNodeFactory.create()

	abstract fun render(canvas: TextCanvas)
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

	var color: Color? = null
	var background: Color? = null
	var style: TextStyle? = null

	override fun render(canvas: TextCanvas) {
		value.split('\n').forEachIndexed { index, line ->
			val write = buildString {
				val attributes = mutableListOf<Int>()
				if (Bold in style) {
					attributes += 1
				}
				if (Dim in style) {
					attributes += 2
				}
				if (Italic in style) {
					attributes += 3
				}
				if (Underline in style) {
					attributes += 4
				}
				if (Invert in style) {
					attributes += 7
				}
				if (Strikethrough in style) {
					attributes += 9
				}
				color?.let { color ->
					attributes += color.fg
				}
				background?.let { background ->
					attributes += background.bg
				}
				if (attributes.isNotEmpty()) {
					attributes.joinTo(this, separator = ";", prefix = "\u001B[", postfix = "m")
					attributes.clear()
				}

				append(line)

				if (color != null) {
					attributes += 39
				}
				if (background != null) {
					attributes += 49
				}
				if (Strikethrough in style) {
					attributes += 29
				}
				if (Invert in style) {
					attributes += 27
				}
				if (Underline in style) {
					attributes += 24
				}
				if (Italic in style) {
					attributes += 23
				}
				if (Bold in style || Dim in style) {
					// 22 clears both 1 (bold) and 2 (dim).
					attributes += 22
				}
				if (attributes.isNotEmpty()) {
					attributes.joinTo(this, separator = ";", prefix = "\u001B[", postfix = "m")
				}
			}

			canvas.write(index, 0, write)
		}
	}

	override fun toString() = "Text($value)"
}

internal class BoxNode : MosaicNode() {
	val children = mutableListOf<MosaicNode>()

	override fun render(canvas: TextCanvas) {
		for (child in children) {
			val childYoga = child.yoga
			val left = childYoga.layoutX.toInt()
			val top = childYoga.layoutY.toInt()
			val right = left + childYoga.layoutWidth.toInt()
			val bottom = top + childYoga.layoutHeight.toInt()
			val clipped = canvas.clip(left, top, right, bottom)
			child.render(clipped)
		}
	}

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
