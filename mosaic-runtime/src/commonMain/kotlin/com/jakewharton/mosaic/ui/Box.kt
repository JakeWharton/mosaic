@file:JvmName("Box")

package com.jakewharton.mosaic.ui

import androidx.compose.runtime.Composable
import com.jakewharton.mosaic.layout.Measurable
import com.jakewharton.mosaic.layout.MeasurePolicy
import com.jakewharton.mosaic.layout.MeasureResult
import com.jakewharton.mosaic.layout.MeasureScope
import com.jakewharton.mosaic.modifier.Modifier
import kotlin.jvm.JvmName

@Composable
public fun Box(
	modifier: Modifier = Modifier,
	content: @Composable () -> Unit,
) {
	Layout(content, modifier, { "Box()" }, BoxMeasurePolicy())
}

internal class BoxMeasurePolicy : MeasurePolicy {
	override fun MeasureScope.measure(measurables: List<Measurable>): MeasureResult {
		var width = 0
		var height = 0
		val placeables = measurables.map { measurable ->
			measurable.measure().also {
				width = maxOf(width, it.width)
				height = maxOf(height, it.height)
			}
		}
		return layout(width, height) {
			for (placeable in placeables) {
				placeable.place(0, 0)
			}
		}
	}
}
