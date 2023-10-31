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
}
