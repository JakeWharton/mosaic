@file:JvmName("Column")

package com.jakewharton.mosaic.ui

import androidx.compose.runtime.Composable
import com.jakewharton.mosaic.layout.Layout
import kotlin.jvm.JvmName

@Composable
public fun Column(content: @Composable () -> Unit) {
	Layout(content, { "Column()" }) { measurables ->
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
