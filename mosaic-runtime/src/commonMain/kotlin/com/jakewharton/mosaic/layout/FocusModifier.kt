package com.jakewharton.mosaic.layout

import com.jakewharton.mosaic.focus.FocusState
import com.jakewharton.mosaic.modifier.Modifier

public interface FocusModifier : Modifier.Element {

	/**
	 * This function is called when the focus switches from this element or to this element
	 */
	public fun onFocusStateChanged(state: FocusState)
}

/**
 * Adding this [modifier][Modifier] to the [modifier][Modifier] parameter of a component will allow
 * it to react on focus events.
 *
 * @param onFocusStateChange This callback is invoked when the focus switches from this element or
 *   to this element.
 */
public fun Modifier.focusable(
	onFocusStateChange: ((FocusState) -> Unit)? = null,
): Modifier = this then FocusElement(onFocusStateChange)

internal class FocusElement(
	private val onFocusStateChanged: ((FocusState) -> Unit)? = null,
) : FocusModifier {

	override fun onFocusStateChanged(state: FocusState) {
		onFocusStateChanged?.invoke(state)
	}
}
