package com.jakewharton.mosaic.focus

import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.staticCompositionLocalOf

public interface FocusManager {
	/**
	 * Call this function to clear focus from the currently focused component, and set the focus to
	 * the root focus modifier.
	 */
	public fun clearFocus()

	/**
	 * Moves focus in the specified [direction][FocusDirection].
	 *
	 * @return true if focus was moved successfully. false if the focused item is unchanged.
	 */
	public fun moveFocus(focusDirection: FocusDirection): Boolean
}

public val LocalFocusManager: ProvidableCompositionLocal<FocusManager> =
	staticCompositionLocalOf { error("No FocusManager provider") }
