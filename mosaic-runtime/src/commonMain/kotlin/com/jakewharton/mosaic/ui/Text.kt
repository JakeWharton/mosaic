@file:JvmName("Text")

package com.jakewharton.mosaic.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.jakewharton.mosaic.layout.drawBehind
import com.jakewharton.mosaic.modifier.Modifier
import com.jakewharton.mosaic.text.TextLayout
import kotlin.jvm.JvmName

@Composable
public fun Text(
	value: String,
	modifier: Modifier = Modifier,
	color: Color? = null,
	background: Color? = null,
	style: TextStyle? = null,
) {
	val layout = remember { TextLayout() }
	layout.value = value

	Layout(
		debugInfo = {
			"""Text("$value")"""
		},
		measurePolicy = {
			layout.measure()
			layout(layout.width, layout.height)
		},
		modifiers = modifier.drawBehind {
			layout.lines.forEachIndexed { row, line ->
				drawText(row, 0, line, color, background, style)
			}
		},
	)
}
