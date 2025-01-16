package com.jakewharton.mosaic.terminal

import com.jakewharton.mosaic.terminal.event.Event
import com.jakewharton.mosaic.terminal.event.FocusEvent
import com.jakewharton.mosaic.terminal.event.ResizeEvent
import kotlinx.coroutines.channels.SendChannel

internal class PlatformEventHandler(
	private val events: SendChannel<Event>,
) {
	fun onFocus(focused: Boolean) {
		events.trySend(FocusEvent(focused))
	}

	fun onKey() {
		return
	}

	fun onMouse() {
		return
	}

	fun onResize(columns: Int, rows: Int, width: Int, height: Int) {
		events.trySend(ResizeEvent(columns, rows, width, height))
	}
}
