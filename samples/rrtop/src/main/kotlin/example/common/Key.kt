package example.common

@JvmInline
value class Key(val keyCode: Int) {
	companion object {
		/** Unknown key. */
		val Unknown: Key = Key(0)

		/** Special key for detecting text input. */
		val Type: Key = Key(1)

		/**
		 * Up Arrow Key / Directional Pad Up key.
		 */
		val DirectionUp: Key = Key(2)

		/**
		 * Down Arrow Key / Directional Pad Down key.
		 */
		val DirectionDown: Key = Key(3)

		/**
		 * Left Arrow Key / Directional Pad Left key.
		 */
		val DirectionLeft: Key = Key(4)

		/**
		 * Right Arrow Key / Directional Pad Right key.
		 */
		val DirectionRight: Key = Key(5)

		/**
		 * Home Movement key.
		 *
		 * Used for scrolling or moving the cursor around to the start of a line
		 * or to the top of a list.
		 */
		val MoveHome: Key = Key(6)

		/**
		 * End Movement key.
		 *
		 * Used for scrolling or moving the cursor around to the end of a line
		 * or to the bottom of a list.
		 */
		val MoveEnd: Key = Key(7)

		/**
		 * Insert key.
		 *
		 * Toggles insert / overwrite edit mode.
		 */
		val Insert: Key = Key(8)

		/**
		 * Backspace key.
		 *
		 * Deletes characters before the insertion point, unlike [Delete].
		 */
		val Backspace: Key = Key(9)

		/**
		 * Delete key.
		 *
		 * Deletes characters ahead of the insertion point, unlike [Backspace].
		 */
		val Delete: Key = Key(10)

		/** Page Up key. */
		val PageUp: Key = Key(11)

		/** Page Down key. */
		val PageDown: Key = Key(12)

		/** Tab key. */
		val Tab: Key = Key(13)

		/** Enter key. */
		val Enter: Key = Key(14)

		/** F1 key. */
		val F1: Key = Key(15)

		/** F2 key. */
		val F2: Key = Key(16)

		/** F3 key. */
		val F3: Key = Key(17)

		/** F4 key. */
		val F4: Key = Key(18)

		/** F5 key. */
		val F5: Key = Key(19)

		/** F6 key. */
		val F6: Key = Key(20)

		/** F7 key. */
		val F7: Key = Key(21)

		/** F8 key. */
		val F8: Key = Key(22)

		/** F9 key. */
		val F9: Key = Key(23)

		/** F10 key. */
		val F10: Key = Key(24)

		/** F11 key. */
		val F11: Key = Key(25)

		/** F12 key. */
		val F12: Key = Key(26)
	}
}
