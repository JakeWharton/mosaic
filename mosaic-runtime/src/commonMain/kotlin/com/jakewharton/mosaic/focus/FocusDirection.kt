package com.jakewharton.mosaic.focus

import kotlin.jvm.JvmInline

/**
 * The [FocusDirection] is used to specify the direction for a [FocusManager.moveFocus] request.
 */
@JvmInline
public value class FocusDirection internal constructor(private val value: Int) {

	override fun toString(): String {
		return when (this) {
			Next -> "Next"
			Previous -> "Previous"
			else -> "Invalid FocusDirection"
		}
	}

	public companion object {
		/**
		 * Direction used in [FocusManager.moveFocus] to indicate that you are searching for the
		 * next focusable item.
		 */
		public val Next: FocusDirection = FocusDirection(1)

		/**
		 * Direction used in [FocusManager.moveFocus] to indicate that you are searching for the
		 * previous focusable item.
		 */
		public val Previous: FocusDirection = FocusDirection(2)
	}
}
