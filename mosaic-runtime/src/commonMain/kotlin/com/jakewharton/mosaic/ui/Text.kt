@file:JvmName("Text")

package com.jakewharton.mosaic.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.jakewharton.mosaic.layout.drawBehind
import com.jakewharton.mosaic.modifier.Modifier
import com.jakewharton.mosaic.text.AnnotatedString
import com.jakewharton.mosaic.text.AnnotatedStringTextLayout
import com.jakewharton.mosaic.text.StringTextLayout
import kotlin.jvm.JvmName

@Composable
@MosaicComposable
public fun Text(
	value: String,
	modifier: Modifier = Modifier,
	color: Color? = null,
	background: Color? = null,
	style: TextStyle? = null,
) {
	val layout = remember { StringTextLayout() }
	layout.value = value

	Layout(
		debugInfo = {
			"""Text("$value")"""
		},
		measurePolicy = {
			layout.measure()
			layout(layout.width, layout.height)
		},
		modifier = modifier.drawBehind {
			layout.lines.forEachIndexed { row, line ->
				drawText(row, 0, line, color, background, style)
			}
		},
	)
}

@Composable
@MosaicComposable
public fun Text(
	value: AnnotatedString,
	modifier: Modifier = Modifier,
	color: Color? = null,
	background: Color? = null,
	style: TextStyle? = null,
) {
	val layout = remember { AnnotatedStringTextLayout() }
	layout.value = value

	Layout(
		debugInfo = {
			"""Text("$value")"""
		},
		measurePolicy = {
			layout.measure()
			layout(layout.width, layout.height)
		},
		modifier = modifier.drawBehind {
			layout.lines.forEachIndexed { row, line ->
				drawText(row, 0, line, color, background, style)
			}
		},
	)
}
