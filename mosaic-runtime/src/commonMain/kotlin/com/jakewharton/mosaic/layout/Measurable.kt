package com.jakewharton.mosaic.layout

import com.jakewharton.mosaic.ui.unit.Constraints

public interface Measurable : IntrinsicMeasurable {
	public fun measure(constraints: Constraints): Placeable
}
