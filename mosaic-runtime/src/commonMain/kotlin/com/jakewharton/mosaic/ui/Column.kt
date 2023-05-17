@file:JvmName("Column")

package com.jakewharton.mosaic.ui

import androidx.compose.runtime.Composable
import com.jakewharton.mosaic.modifier.Modifier
import kotlin.jvm.JvmName

@Composable
public fun Column(
	modifier: Modifier = Modifier,
	content: @Composable () -> Unit
) {
	Layout(content, modifier, debugInfo = { "Column()" }) { measurables ->
		var width = 0
		var height = 0
		val placeables = measurables.map { measurable ->
			measurable.measure().also { placeable ->
				width = maxOf(width, placeable.width)
				height += placeable.height
			}
		}
		layout(width, height) {
			var y = 0
			for (placeable in placeables) {
				placeable.place(0, y)
				y += placeable.height
			}
		}
	}
}
