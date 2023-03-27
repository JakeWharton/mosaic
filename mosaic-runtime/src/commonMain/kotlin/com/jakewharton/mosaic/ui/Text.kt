@file:JvmName("Text")

package com.jakewharton.mosaic.ui

import androidx.compose.runtime.Composable
import com.jakewharton.mosaic.layout.Layout
import de.cketti.codepoints.codePointCount
import kotlin.jvm.JvmName

@Composable
public fun Text(
	value: String,
	color: Color? = null,
	background: Color? = null,
	style: TextStyle? = null,
) {
	Layout(
		debugInfo = {
			"""Text("$value")"""
		},
		measurePolicy = {
			val lines = value.split('\n')
			val width = lines.maxOf { it.codePointCount(0, it.length) }
			val height = lines.size
			layout(width, height)
		},
		drawPolicy = { canvas ->
			value.split('\n').forEachIndexed { row, line ->
				canvas.write(row, 0, line, color, background, style)
			}
		},
	)
}
