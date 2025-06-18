package com.jakewharton.mosaic.focus

/**
 * Use [com.jakewharton.mosaic.layout.focusable] modifier to access [FocusState].
 */
public interface FocusState {
	/**
	 * Whether the component is focused or not.
	 *
	 * @return true if the component is focused, false otherwise.
	 */
	public val isFocused: Boolean

	/**
	 * Whether the focus modifier associated with this [FocusState] has a child that is focused.
	 *
	 * @return true if a child is focused, false otherwise.
	 */
	public val hasFocus: Boolean
}

internal enum class FocusStateImpl : FocusState {
	/** The focusable component is currently active (i.e. it receives key events). */
	Active,

	/** One of the descendants of the focusable component is Active. */
	ActiveParent,

	/**
	 * The focusable component does not receive any key events. (ie it is not active, nor are any of
	 * its descendants active).
	 */
	Inactive,

	;

	override val isFocused: Boolean
		get() =
			when (this) {
				Active -> true
				ActiveParent,
				Inactive,
				-> false
			}

	override val hasFocus: Boolean
		get() =
			when (this) {
				Active,
				ActiveParent,
				-> true
				Inactive -> false
			}
}
