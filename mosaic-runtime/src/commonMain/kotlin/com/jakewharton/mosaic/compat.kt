package com.jakewharton.mosaic

import com.jakewharton.mosaic.layout.KeyEvent
import com.jakewharton.mosaic.terminal.event.KeyboardEvent

internal fun KeyboardEvent.toKeyEventOrNull(): KeyEvent? {
	if (eventType != KeyboardEvent.EventTypePress) {
		return null
	}

	return KeyEvent(
		key = when (val codepoint = codepoint) {
			9 -> "Tab"
			13 -> "Enter"
			in 32..126 -> codepoint.toChar().toString()
			127 -> "Backspace"
			57350 -> "ArrowLeft"
			57351 -> "ArrowRight"
			57352 -> "ArrowUp"
			57353 -> "ArrowDown"
			57348 -> "Insert"
			57349 -> "Delete"
			in 57364..57364 -> "F" + (codepoint - 57363)
			else -> throw UnsupportedOperationException(toString())
		},
		alt = alt,
		ctrl = ctrl,
		shift = shift,
	)
}
