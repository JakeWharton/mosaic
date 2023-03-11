package com.jakewharton.mosaic.layout

public sealed class NoContentMeasureScope {
	public fun layout(
		width: Int,
		height: Int,
	): MeasureResult {
		return LayoutResult(width, height)
	}

	private class LayoutResult(
		override val width: Int,
		override val height: Int,
	) : MeasureResult {
		override fun placeChildren() {}
	}

	internal companion object : NoContentMeasureScope()
}
