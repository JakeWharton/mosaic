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
import kotlinx.cinterop.ptr
import kotlinx.cinterop.staticCFunction
import kotlinx.cinterop.useContents
import kotlinx.cinterop.usePinned

internal actual class PlatformInput(
	ptr: CPointer<platformInput>,
	private val handlerPtr: CPointer<platformInputCallback>,
	private val handlerRef: StableRef<Callback>,
) : AutoCloseable {
	actual companion object {
		actual fun create(callback: Callback): PlatformInput {
			val callbackRef = StableRef.create(callback)
			val callbackPtr = callbackRef.toNativeAllocationIn(nativeHeap).ptr

			platformInput_init(callbackPtr).useContents {
				input?.let { inputPtr ->
					return PlatformInput(inputPtr, callbackPtr, callbackRef)
				}

				nativeHeap.free(callbackPtr)
				callbackRef.dispose()

				check(error == 0U) { "Unable to create stdin reader: $error" }
				throw OutOfMemoryError()
			}
		}
	}

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
			nativeHeap.free(handlerPtr)
			handlerRef.dispose()

			if (error == 0U) return
			throwError(error)
		}
	}

	actual interface Callback {
		actual fun onFocus(focused: Boolean)
		actual fun onKey()
		actual fun onMouse()
		actual fun onResize(columns: Int, rows: Int, width: Int, height: Int)
	}
}

internal fun throwError(error: UInt): Nothing {
	throw RuntimeException(error.toString())
}

internal fun StableRef<PlatformInput.Callback>.toNativeAllocationIn(memory: NativePlacement): platformInputCallback {
	return memory.alloc<platformInputCallback> {
		opaque = asCPointer()
		onFocus = staticCFunction(::onFocusCallback)
		onKey = staticCFunction(::onKeyCallback)
		onMouse = staticCFunction(::onMouseCallback)
		onResize = staticCFunction(::onResizeCallback)
	}
}

private fun onFocusCallback(opaque: COpaquePointer?, focused: Boolean) {
	val handler = opaque!!.asStableRef<PlatformInput.Callback>().get()
	handler.onFocus(focused)
}

private fun onKeyCallback(opaque: COpaquePointer?) {
	val handler = opaque!!.asStableRef<PlatformInput.Callback>().get()
	handler.onKey()
}

private fun onMouseCallback(opaque: COpaquePointer?) {
	val handler = opaque!!.asStableRef<PlatformInput.Callback>().get()
	handler.onMouse()
}

private fun onResizeCallback(opaque: COpaquePointer?, columns: Int, rows: Int, width: Int, height: Int) {
	val handler = opaque!!.asStableRef<PlatformInput.Callback>().get()
	handler.onResize(columns, rows, width, height)
}
