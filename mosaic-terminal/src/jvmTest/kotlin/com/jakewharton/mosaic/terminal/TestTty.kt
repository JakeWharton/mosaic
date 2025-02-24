package com.jakewharton.mosaic.terminal

import com.jakewharton.mosaic.terminal.Jni.testTtyGetTty
import com.jakewharton.mosaic.terminal.Jni.testTtyInit
import com.jakewharton.mosaic.terminal.Jni.ttyCallbackFree
import com.jakewharton.mosaic.terminal.Jni.ttyCallbackInit
import com.jakewharton.mosaic.terminal.event.Event
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.Channel.Factory.UNLIMITED

internal actual class TestTty(
	private var testTtyPtr: Long,
	private val events: Channel<Event>,
	actual val tty: Tty,
) : AutoCloseable {
	actual companion object {
		actual fun create(callback: Tty.Callback): TestTty {
			val events = Channel<Event>(UNLIMITED)
			val callbackPtr = ttyCallbackInit(PlatformEventHandler(events, emitDebugEvents = false))
			if (callbackPtr != 0L) {
				val testTtyPtr = testTtyInit(callbackPtr)
				if (testTtyPtr != 0L) {
					val ttyPtr = testTtyGetTty(testTtyPtr)
					val tty = Tty(ttyPtr, callbackPtr)
					return TestTty(testTtyPtr, events, tty)
				}
				ttyCallbackFree(callbackPtr)
			}
			throw OutOfMemoryError()
		}
	}

	actual fun write(buffer: ByteArray) {
		Jni.testTtyWrite(testTtyPtr, buffer)
	}

	actual fun focusEvent(focused: Boolean) {
		Jni.testTtyFocusEvent(testTtyPtr, focused)
	}

	actual fun keyEvent() {
		Jni.testTtyKeyEvent(testTtyPtr)
	}

	actual fun mouseEvent() {
		Jni.testTtyMouseEvent(testTtyPtr)
	}

	actual fun resizeEvent(columns: Int, rows: Int, width: Int, height: Int) {
		Jni.testTtyResizeEvent(testTtyPtr, columns, rows, width, height)
	}

	actual override fun close() {
		if (testTtyPtr != 0L) {
			tty.close()
			Jni.testTtyFree(testTtyPtr)
			testTtyPtr = 0
		}
	}
}
