package com.jakewharton.mosaic.terminal

import com.jakewharton.mosaic.terminal.event.Event
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.StableRef
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.free
import kotlinx.cinterop.nativeHeap
import kotlinx.cinterop.ptr
import kotlinx.cinterop.useContents
import kotlinx.cinterop.usePinned
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.Channel.Factory.UNLIMITED

internal actual class TestTty(
	private var ptr: CPointer<MosaicTestTty>?,
	private val events: Channel<Event>,
	actual val tty: Tty,
) : AutoCloseable {
	actual companion object {
		actual fun create(callback: Tty.Callback): TestTty {
			val events = Channel<Event>(UNLIMITED)

			val callback = PlatformEventHandler(events, emitDebugEvents = false)
			val callbackRef = StableRef.create(callback)
			val callbackPtr = callbackRef.toNativeAllocationIn(nativeHeap).ptr

			val testTtyPtr = testTty_init(callbackPtr).useContents {
				testTty?.let { return@useContents it }

				nativeHeap.free(callbackPtr)
				callbackRef.dispose()

				check(error == 0U) { "Unable to create test tty: $error" }
				throw OutOfMemoryError()
			}

			val ttyPtr = testTty_getTty(testTtyPtr)!!
			val tty = Tty(ttyPtr, callbackPtr, callbackRef)
			return TestTty(testTtyPtr, events, tty)
		}
	}

	actual fun write(buffer: ByteArray) {
		val error = buffer.usePinned {
			testTty_write(ptr, it.addressOf(0), buffer.size)
		}
		if (error == 0U) return
		throwError(error)
	}

	actual fun focusEvent(focused: Boolean) {
		testTty_focusEvent(ptr, focused)
	}

	actual fun keyEvent() {
		testTty_keyEvent(ptr)
	}

	actual fun mouseEvent() {
		testTty_mouseEvent(ptr)
	}

	actual fun resizeEvent(columns: Int, rows: Int, width: Int, height: Int) {
		testTty_resizeEvent(ptr, columns, rows, width, height)
	}

	actual override fun close() {
		ptr?.let { ref ->
			this.ptr = null

			tty.close()

			val error = testTty_free(ref)

			if (error == 0U) return
			throwError(error)
		}
	}
}
