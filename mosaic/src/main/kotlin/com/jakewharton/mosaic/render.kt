package com.jakewharton.mosaic

import com.jakewharton.crossword.TextCanvas

internal fun MosaicNode.renderToString(): String {
	val surface = TextCanvas(yoga.layoutWidth.toInt(), yoga.layoutHeight.toInt())
	render(surface)
	return surface.toString()
}
