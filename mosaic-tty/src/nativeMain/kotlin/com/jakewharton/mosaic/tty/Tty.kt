package com.jakewharton.mosaic.tty

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

public actual class Tty(
	ptr: CPointer<MosaicTty>,
	private val handlerPtr: CPointer<MosaicTtyCallback>,
	private val handlerRef: StableRef<Callback>,
) : AutoCloseable {
	public actual companion object {
		public actual fun create(callback: Callback): Tty {
			val callbackRef = StableRef.create(callback)
			val callbackPtr = callbackRef.toNativeAllocationIn(nativeHeap).ptr

			tty_init(callbackPtr).useContents {
				tty?.let { ttyPtr ->
					return Tty(ttyPtr, callbackPtr, callbackRef)
				}

				nativeHeap.free(callbackPtr)
				callbackRef.dispose()

				check(error == 0U) { "Unable to create stdin reader: $error" }
				throw OutOfMemoryError()
			}
		}
	}

	private var ptr: CPointer<MosaicTty>? = ptr

	public actual fun read(buffer: ByteArray, offset: Int, count: Int): Int {
		buffer.usePinned {
			tty_read(ptr, it.addressOf(offset), count).useContents {
				if (error == 0U) return this.count
				throwError(error)
			}
		}
	}

	public actual fun readWithTimeout(buffer: ByteArray, offset: Int, count: Int, timeoutMillis: Int): Int {
		buffer.usePinned {
			tty_readWithTimeout(ptr, it.addressOf(offset), count, timeoutMillis).useContents {
				if (error == 0U) return this.count
				throwError(error)
			}
		}
	}

	public actual fun interrupt() {
		val error = tty_interrupt(ptr)
		if (error == 0U) return
		throwError(error)
	}

	public actual fun enableRawMode() {
		val error = tty_enableRawMode(ptr)
		if (error == 0U) return
		throwError(error)
	}

	public actual fun enableWindowResizeEvents() {
		val error = tty_enableWindowResizeEvents(ptr)
		if (error == 0U) return
		throwError(error)
	}

	public actual fun currentSize(): IntArray {
		tty_currentTerminalSize(ptr).useContents {
			if (error == 0U) {
				return intArrayOf(columns, rows, width, height)
			}
			throwError(error)
		}
	}

	actual override fun close() {
		ptr?.let { ptr ->
			this.ptr = null

			val error = tty_free(ptr)
			nativeHeap.free(handlerPtr)
			handlerRef.dispose()

			if (error == 0U) return
			throwError(error)
		}
	}

	public actual interface Callback {
		public actual fun onFocus(focused: Boolean)
		public actual fun onKey()
		public actual fun onMouse()
		public actual fun onResize(columns: Int, rows: Int, width: Int, height: Int)
	}
}

internal fun throwError(error: UInt): Nothing {
	throw RuntimeException(error.toString())
}

internal fun StableRef<Tty.Callback>.toNativeAllocationIn(memory: NativePlacement): MosaicTtyCallback {
	return memory.alloc<MosaicTtyCallback> {
		opaque = asCPointer()
		onFocus = staticCFunction(::onFocusCallback)
		onKey = staticCFunction(::onKeyCallback)
		onMouse = staticCFunction(::onMouseCallback)
		onResize = staticCFunction(::onResizeCallback)
	}
}

private fun onFocusCallback(opaque: COpaquePointer?, focused: Boolean) {
	val callback = opaque!!.asStableRef<Tty.Callback>().get()
	callback.onFocus(focused)
}

private fun onKeyCallback(opaque: COpaquePointer?) {
	val callback = opaque!!.asStableRef<Tty.Callback>().get()
	callback.onKey()
}

private fun onMouseCallback(opaque: COpaquePointer?) {
	val callback = opaque!!.asStableRef<Tty.Callback>().get()
	callback.onMouse()
}

private fun onResizeCallback(opaque: COpaquePointer?, columns: Int, rows: Int, width: Int, height: Int) {
	val callback = opaque!!.asStableRef<Tty.Callback>().get()
	callback.onResize(columns, rows, width, height)
}
