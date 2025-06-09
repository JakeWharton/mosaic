package com.jakewharton.mosaic.layout

import com.jakewharton.mosaic.focus.FocusState
import com.jakewharton.mosaic.modifier.Modifier

public interface FocusModifier : Modifier.Element {

	public fun onFocusStateChanged(state: FocusState)
}

public fun Modifier.focusable(
	onFocusStateChanged: ((FocusState) -> Unit)? = null,
): Modifier = this then FocusElement(onFocusStateChanged)

internal class FocusElement(
	private val onFocusStateChanged: ((FocusState) -> Unit)? = null,
) : FocusModifier {

	override fun onFocusStateChanged(state: FocusState) {
		onFocusStateChanged?.invoke(state)
	}
}
