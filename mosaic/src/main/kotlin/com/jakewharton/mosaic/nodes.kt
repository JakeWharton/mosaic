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

	var style: TextStyle? = null

	override fun render(canvas: TextCanvas) {
		value.split('\n').forEachIndexed { index, line ->
			val write = buildString {
				if (Bold in style) {
					append("\u001B[1m")
				}
				if (Dim in style) {
					append("\u001B[2m")
				}
				if (Italic in style) {
					append("\u001B[3m")
				}
				if (Underline in style) {
					append("\u001B[4m")
				}
				if (Invert in style) {
					append("\u001B[7m")
				}
				if (Strikethrough in style) {
					append("\u001B[9m")
				}

				append(line)

				if (Strikethrough in style) {
					append("\u001B[29m")
				}
				if (Invert in style) {
					append("\u001B[27m")
				}
				if (Underline in style) {
					append("\u001B[24m")
				}
				if (Italic in style) {
					append("\u001B[23m")
				}
				if (Bold in style || Dim in style) {
					// 22 clears both 1 (bold) and 2 (dim).
					append("\u001B[22m")
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
