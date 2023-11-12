package com.jakewharton.mosaic.layout

/**
 * A part of the composition that can be measured. This represents a layout.
 * The instance should never be stored.
 */
public interface IntrinsicMeasurable {
	/**
	 * Data provided by the `ParentData`
	 */
	public val parentData: Any?

	/**
	 * Calculates the minimum width that the layout can be such that
	 * the content of the layout will be painted correctly.
	 */
	public fun minIntrinsicWidth(height: Int): Int

	/**
	 * Calculates the smallest width beyond which increasing the width never
	 * decreases the height.
	 */
	public fun maxIntrinsicWidth(height: Int): Int

	/**
	 * Calculates the minimum height that the layout can be such that
	 * the content of the layout will be painted correctly.
	 */
	public fun minIntrinsicHeight(width: Int): Int

	/**
	 * Calculates the smallest height beyond which increasing the height never
	 * decreases the width.
	 */
	public fun maxIntrinsicHeight(width: Int): Int
}
