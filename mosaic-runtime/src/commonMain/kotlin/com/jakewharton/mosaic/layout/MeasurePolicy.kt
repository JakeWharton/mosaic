package com.jakewharton.mosaic.layout

import com.jakewharton.mosaic.layout.Measurable.MeasureScope

public fun interface MeasurePolicy {
	public fun MeasureScope.measure(measurables: List<Measurable>): MeasureResult
}
