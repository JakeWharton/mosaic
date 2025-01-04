package com.jakewharton.mosaic.layout

import androidx.compose.runtime.Stable
import com.jakewharton.mosaic.modifier.Modifier

/**
 * Tag the element with [layoutId] to identify the element within its parent.
 */
@Stable
public fun Modifier.layoutId(layoutId: Any): Modifier {
	return this then LayoutIdModifier(layoutId)
}

/**
 * A [ParentDataModifier] which tags the target with the given [id][layoutId]. The provided tag
 * will act as parent data, and can be used for example by parent layouts to associate composable
 * children to [Measurable]s when doing layout, as shown below.
 */
private class LayoutIdModifier(
	override val layoutId: Any,
) : ParentDataModifier,
	LayoutIdParentData {

	override fun modifyParentData(parentData: Any?): Any {
		return this@LayoutIdModifier
	}
}

/**
 * Can be implemented by values used as parent data to make them usable as tags. If a parent data
 * value implements this interface, it can then be returned when querying [Measurable.layoutId] for
 * the corresponding child.
 */
internal interface LayoutIdParentData {
	val layoutId: Any
}

/**
 * Retrieves the tag associated to a composable with the [Modifier.layoutId] modifier. For a parent
 * data value to be returned by this property when not using the [Modifier.layoutId] modifier, the
 * parent data value should implement the [LayoutIdParentData] interface.
 */
public val Measurable.layoutId: Any?
	get() = (parentData as? LayoutIdParentData)?.layoutId
