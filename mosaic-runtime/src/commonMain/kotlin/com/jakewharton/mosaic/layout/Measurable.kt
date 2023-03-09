package com.jakewharton.mosaic.layout

import com.jakewharton.mosaic.layout.Placeable.PlacementScope

public interface Measurable {
	public fun measure(): Placeable

	public sealed class MeasureScope {
		public fun layout(
			width: Int,
			height: Int,
			placementBlock: PlacementScope.() -> Unit,
		): MeasureResult {
			return LayoutResult(width, height, placementBlock)
		}

		internal companion object : MeasureScope()
	}
}

private class LayoutResult(
	override val width: Int,
	override val height: Int,
	private val placementBlock: PlacementScope.() -> Unit,
) : MeasureResult {
	override fun placeChildren() = PlacementScope.placementBlock()
}
