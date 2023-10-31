package com.jakewharton.mosaic.layout

import com.jakewharton.mosaic.modifier.Modifier

/**
 * A [Modifier] that provides data to the parent [Layout]. This can be read from within the
 * the [Layout] during measurement and positioning, via [IntrinsicMeasurable.parentData].
 * The parent data is commonly used to inform the parent how the child [Layout] should be measured
 * and positioned.
 */
public interface ParentDataModifier : Modifier.Element {
	/**
	 * Provides a parentData, given the [parentData] already provided through the modifier's chain.
	 */
	public fun modifyParentData(parentData: Any?): Any?
}
