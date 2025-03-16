package com.jakewharton.mosaic.tty.terminal

import com.jakewharton.mosaic.terminal.DebugEvent
import com.jakewharton.mosaic.terminal.Event
import com.jakewharton.mosaic.terminal.FocusEvent
import com.jakewharton.mosaic.terminal.ResizeEvent
import com.jakewharton.mosaic.terminal.Terminal
import com.jakewharton.mosaic.tty.Tty
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.flow.MutableStateFlow

internal class EventParserTtyCallback(
	private val focused: MutableStateFlow<Boolean>,
	private val size: MutableStateFlow<Terminal.Size>,
	private val events: SendChannel<Event>,
	private val emitDebugEvents: Boolean,
) : Tty.Callback {
	override fun onFocus(focused: Boolean) {
		this.focused.value = focused
		sendEvent(FocusEvent(focused))
	}

	override fun onKey() {
		return
	}

	override fun onMouse() {
		return
	}

	override fun onResize(columns: Int, rows: Int, width: Int, height: Int) {
		size.value = Terminal.Size(columns, rows, width, height)
		sendEvent(ResizeEvent(columns, rows, width, height))
	}

	private fun sendEvent(event: Event) {
		events.trySend(event)
		if (emitDebugEvents) {
			events.trySend(DebugEvent(event, byteArrayOf()))
		}
	}
}
