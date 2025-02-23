package com.jakewharton.mosaic.terminal

import kotlinx.cinterop.COpaquePointer
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.NativePlacement
import kotlinx.cinterop.StableRef
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.alloc
import kotlinx.cinterop.asStableRef
import kotlinx.cinterop.free
import kotlinx.cinterop.nativeHeap
import kotlinx.cinterop.staticCFunction
import kotlinx.cinterop.useContents
import kotlinx.cinterop.usePinned

internal actual class PlatformInput internal constructor(
	ptr: CPointer<platformInput>,
	private val handlerPtr: CPointer<platformEventHandler>?,
	private val handlerRef: StableRef<PlatformEventHandler>?,
) : AutoCloseable {
	private var ptr: CPointer<platformInput>? = ptr

	actual fun read(buffer: ByteArray, offset: Int, count: Int): Int {
		buffer.usePinned {
			platformInput_read(ptr, it.addressOf(offset), count).useContents {
				if (error == 0U) return this.count
				throwError(error)
			}
		}
	}

	actual fun readWithTimeout(buffer: ByteArray, offset: Int, count: Int, timeoutMillis: Int): Int {
		buffer.usePinned {
			platformInput_readWithTimeout(ptr, it.addressOf(offset), count, timeoutMillis).useContents {
				if (error == 0U) return this.count
				throwError(error)
			}
		}
	}

	actual fun interrupt() {
		val error = platformInput_interrupt(ptr)
		if (error == 0U) return
		throwError(error)
	}

	actual fun enableRawMode() {
		val error = platformInput_enableRawMode(ptr)
		if (error == 0U) return
		throwError(error)
	}

	actual fun enableWindowResizeEvents() {
		val error = platformInput_enableWindowResizeEvents(ptr)
		if (error == 0U) return
		throwError(error)
	}

	actual fun currentSize(): IntArray {
		platformInput_currentTerminalSize(ptr).useContents {
			if (error == 0U) {
				return intArrayOf(columns, rows, width, height)
			}
			throwError(error)
		}
	}

	actual override fun close() {
		ptr?.let { ptr ->
			this.ptr = null

			val error = platformInput_free(ptr)
			handlerPtr?.let(nativeHeap::free)
			handlerRef?.dispose()

			if (error == 0U) return
			throwError(error)
		}
	}
}

internal fun StableRef<PlatformEventHandler>.toNativeAllocationIn(memory: NativePlacement): platformEventHandler {
	return memory.alloc<platformEventHandler> {
		opaque = asCPointer()
		onFocus = staticCFunction(::onFocusCallback)
		onKey = staticCFunction(::onKeyCallback)
		onMouse = staticCFunction(::onMouseCallback)
		onResize = staticCFunction(::onResizeCallback)
	}
}

private fun onFocusCallback(opaque: COpaquePointer?, focused: Boolean) {
	val handler = opaque!!.asStableRef<PlatformEventHandler>().get()
	handler.onFocus(focused)
}

private fun onKeyCallback(opaque: COpaquePointer?) {
	val handler = opaque!!.asStableRef<PlatformEventHandler>().get()
	handler.onKey()
}

private fun onMouseCallback(opaque: COpaquePointer?) {
	val handler = opaque!!.asStableRef<PlatformEventHandler>().get()
	handler.onMouse()
}

private fun onResizeCallback(opaque: COpaquePointer?, columns: Int, rows: Int, width: Int, height: Int) {
	val handler = opaque!!.asStableRef<PlatformEventHandler>().get()
	handler.onResize(columns, rows, width, height)
}
