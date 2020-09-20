package com.jakewharton.mosaic

import com.jakewharton.crossword.TextCanvas

internal fun MosaicNode.renderToString(): String {
	val surface = TextCanvas(yoga.layoutWidth.toInt(), yoga.layoutHeight.toInt())
	render(surface)
	return surface.toString()
}

private fun MosaicNode.render(canvas: TextCanvas) {
	when (this) {
		is TextNode -> {
			value.split('\n').forEachIndexed { index, line ->
				canvas.write(index, 0, line)
			}
		}
		is BoxNode -> {
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
	}
}
