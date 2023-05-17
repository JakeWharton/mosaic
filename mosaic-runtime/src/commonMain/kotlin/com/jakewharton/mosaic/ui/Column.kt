@file:JvmName("Column")

package com.jakewharton.mosaic.ui

import androidx.compose.runtime.Composable
import com.jakewharton.mosaic.layout.Measurable
import com.jakewharton.mosaic.layout.MeasurePolicy
import com.jakewharton.mosaic.layout.MeasureResult
import com.jakewharton.mosaic.layout.MeasureScope
import com.jakewharton.mosaic.modifier.Modifier
import kotlin.jvm.JvmName

@Composable
public fun Column(
	modifier: Modifier = Modifier,
	content: @Composable () -> Unit
) {
	Layout(content, modifier, { "Column()" }, ColumnMeasurePolicy())
}

private class ColumnMeasurePolicy : MeasurePolicy {
	override fun MeasureScope.measure(measurables: List<Measurable>): MeasureResult {
		var width = 0
		var height = 0
		val placeables = measurables.map { measurable ->
			measurable.measure().also { placeable ->
				width = maxOf(width, placeable.width)
				height += placeable.height
			}
		}
		return layout(width, height) {
			var y = 0
			for (placeable in placeables) {
				placeable.place(0, y)
				y += placeable.height
			}
		}
	}
}
