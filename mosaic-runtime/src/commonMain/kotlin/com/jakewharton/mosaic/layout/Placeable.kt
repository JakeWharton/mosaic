package com.jakewharton.mosaic.layout

public abstract class Placeable {
	public abstract val width: Int
	public abstract val height: Int

	protected abstract fun placeAt(x: Int, y: Int)

	public sealed class PlacementScope {
		public fun Placeable.place(x: Int, y: Int) {
			placeAt(x, y)
		}

		internal companion object : PlacementScope()
	}
}
