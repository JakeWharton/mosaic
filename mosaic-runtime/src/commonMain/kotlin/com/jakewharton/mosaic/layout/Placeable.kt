package com.jakewharton.mosaic.layout

import com.jakewharton.mosaic.ui.unit.IntOffset

public abstract class Placeable {
	public abstract val width: Int
	public abstract val height: Int

	protected abstract fun placeAt(x: Int, y: Int)

	public interface PlacementScope {
		public val x: Int
		public val y: Int

		public fun Placeable.place(x: Int, y: Int) {
			placeAt(this@PlacementScope.x + x, this@PlacementScope.y + y)
		}

		public fun Placeable.place(position: IntOffset) {
			placeAt(this@PlacementScope.x + position.x, this@PlacementScope.y + position.y)
		}
	}
}
