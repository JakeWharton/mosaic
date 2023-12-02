package com.jakewharton.mosaic.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.NonRestartableComposable
import com.jakewharton.mosaic.layout.Measurable
import com.jakewharton.mosaic.layout.MeasurePolicy
import com.jakewharton.mosaic.layout.MeasureResult
import com.jakewharton.mosaic.layout.MeasureScope
import com.jakewharton.mosaic.modifier.Modifier
import com.jakewharton.mosaic.ui.unit.Constraints

/**
 * Component that represents an empty space layout, whose size can be defined using
 * [Modifier.width], [Modifier.height] and [Modifier.size] modifiers.
 *
 * @param modifier modifiers to set to this spacer
 */
@Composable
@NonRestartableComposable
public fun Spacer(modifier: Modifier = Modifier) {
	Layout(
		content = EmptySpacerContent,
		measurePolicy = SpacerMeasurePolicy,
		modifier = modifier,
		debugInfo = { "Spacer()" },
	)
}

private val EmptySpacerContent: @Composable () -> Unit = {}

private object SpacerMeasurePolicy : MeasurePolicy {
	override fun MeasureScope.measure(
		measurables: List<Measurable>,
		constraints: Constraints,
	): MeasureResult {
		return with(constraints) {
			val width = if (hasFixedWidth) maxWidth else 0
			val height = if (hasFixedHeight) maxHeight else 0
			layout(width, height) {}
		}
	}
}
