package com.jakewharton.mosaic.layout

public fun interface MeasurePolicy {
	public fun MeasureScope.measure(measurables: List<Measurable>): MeasureResult
}
