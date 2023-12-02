package com.jakewharton.mosaic.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.NonRestartableComposable
import com.jakewharton.mosaic.layout.Measurable
import com.jakewharton.mosaic.layout.MeasurePolicy
import com.jakewharton.mosaic.layout.MeasureResult
import com.jakewharton.mosaic.layout.MeasureScope
import com.jakewharton.mosaic.layout.drawBehind
import com.jakewharton.mosaic.modifier.Modifier
import com.jakewharton.mosaic.ui.unit.Constraints

/**
 * Component that represents an layout of identical characters, whose size can be
 * defined using [Modifier.width], [Modifier.height] and [Modifier.size] modifiers.
 *
 * This layout is similar to [Spacer].
 *
 * @param char character to fill in
 * @param modifier modifiers to set to this spacer
 */
@Composable
@NonRestartableComposable
public fun Filler(
	char: Char,
	modifier: Modifier = Modifier,
	foreground: Color? = null,
	background: Color? = null,
	style: TextStyle? = null,
) {
	Layout(
		content = EmptyFillerContent,
		measurePolicy = FillerMeasurePolicy,
		debugInfo = { "Filler('$char')" },
		modifiers = modifier.drawBehind {
			val line = char.toString().repeat(width)
			repeat(height) { row ->
				drawText(row, 0, line, foreground, background, style)
			}
		},
	)
}

private val EmptyFillerContent: @Composable () -> Unit = {}

private object FillerMeasurePolicy : MeasurePolicy {
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
