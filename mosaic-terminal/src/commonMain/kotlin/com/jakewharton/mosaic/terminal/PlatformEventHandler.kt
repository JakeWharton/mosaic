package com.jakewharton.mosaic.terminal

import com.jakewharton.mosaic.terminal.event.DebugEvent
import com.jakewharton.mosaic.terminal.event.Event
import com.jakewharton.mosaic.terminal.event.FocusEvent
import com.jakewharton.mosaic.terminal.event.ResizeEvent
import com.jakewharton.mosaic.tty.Tty
import kotlinx.coroutines.channels.SendChannel

internal class PlatformEventHandler(
	private val events: SendChannel<Event>,
	private val emitDebugEvents: Boolean,
) : Tty.Callback {
	override fun onFocus(focused: Boolean) {
		sendEvent(FocusEvent(focused))
	}

	override fun onKey() {
		return
	}

	override fun onMouse() {
		return
	}

	override fun onResize(columns: Int, rows: Int, width: Int, height: Int) {
		sendEvent(ResizeEvent(columns, rows, width, height))
	}

	private fun sendEvent(event: Event) {
		events.trySend(event)
		if (emitDebugEvents) {
			events.trySend(DebugEvent(event, byteArrayOf()))
		}
	}
}
