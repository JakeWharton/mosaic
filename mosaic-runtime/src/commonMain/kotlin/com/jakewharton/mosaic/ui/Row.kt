@file:JvmName("Row")

package com.jakewharton.mosaic.ui

import androidx.compose.runtime.Composable
import kotlin.jvm.JvmName

@Composable
public fun Row(content: @Composable () -> Unit) {
	Layout(content, { "Row()" }) { measurables ->
		var width = 0
		var height = 0
		val placeables = measurables.map { measurable ->
			measurable.measure().also { placeable ->
				width += placeable.width
				height = maxOf(height, placeable.height)
			}
		}
		layout(width, height) {
			var x = 0
			for (placeable in placeables) {
				placeable.place(x, 0)
				x += placeable.width
			}
		}
	}
}
