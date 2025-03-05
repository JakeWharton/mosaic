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

public actual class Tty internal constructor(
	ptr: CPointer<MosaicTty>,
) : AutoCloseable {
	public actual companion object {
		public actual fun bind(): Tty {
			tty_init().useContents {
				tty?.let { ttyPtr ->
					return Tty(ttyPtr)
				}
				if (already_bound) {
					throw IllegalStateException("Tty already bound")
				}
				if (error != 0U) {
					throwIse(error)
				}
				throw OutOfMemoryError()
			}
		}
	}

	private var ptr: CPointer<MosaicTty>? = ptr
	private var callbackPtrAndRef: Pair<CPointer<MosaicTtyCallback>, StableRef<Callback>>? = null

	public actual fun setCallback(callback: Callback?) {
		callbackPtrAndRef?.let { (callbackPtr, callbackRef) ->
			nativeHeap.free(callbackPtr)
			callbackRef.dispose()
			callbackPtrAndRef = null
		}

		val callbackPtr = callback?.let { callback ->
			val callbackRef = StableRef.create(callback)
			val callbackPtr = callbackRef.toNativeAllocationIn(nativeHeap).ptr

			callbackPtrAndRef = callbackPtr to callbackRef
			callbackPtr
		}

		tty_setCallback(ptr, callbackPtr)
	}

	public actual fun readInput(buffer: ByteArray, offset: Int, count: Int): Int {
		buffer.asUByteArray().usePinned {
			tty_readInput(ptr, it.addressOf(offset), count).useContents {
				if (error == 0U) return this.count
				throwIse(error)
			}
		}
	}

	public actual fun readInputWithTimeout(buffer: ByteArray, offset: Int, count: Int, timeoutMillis: Int): Int {
		buffer.asUByteArray().usePinned {
			tty_readInputWithTimeout(ptr, it.addressOf(offset), count, timeoutMillis).useContents {
				if (error == 0U) return this.count
				throwIse(error)
			}
		}
	}

	public actual fun interruptRead() {
		val error = tty_interruptRead(ptr)
		if (error == 0U) return
		throwIse(error)
	}

	public actual fun writeOutput(buffer: ByteArray, offset: Int, count: Int): Int {
		buffer.asUByteArray().usePinned {
			tty_writeOutput(ptr, it.addressOf(offset), count).useContents {
				if (error == 0U) return this.count
				throwIse(error)
			}
		}
	}

	public actual fun writeError(buffer: ByteArray, offset: Int, count: Int): Int {
		buffer.asUByteArray().usePinned {
			tty_writeError(ptr, it.addressOf(offset), count).useContents {
				if (error == 0U) return this.count
				throwIse(error)
			}
		}
	}

	public actual fun enableRawMode() {
		val error = tty_enableRawMode(ptr)
		if (error == 0U) return
		throwIse(error)
	}

	public actual fun enableWindowResizeEvents() {
		val error = tty_enableWindowResizeEvents(ptr)
		if (error == 0U) return
		throwIse(error)
	}

	public actual fun currentSize(): IntArray {
		tty_currentTerminalSize(ptr).useContents {
			if (error == 0U) {
				return intArrayOf(columns, rows, width, height)
			}
			throwIse(error)
		}
	}

	actual override fun close() {
		ptr?.let { ptr ->
			this.ptr = null

			val error = tty_free(ptr)

			callbackPtrAndRef?.let { (callbackPtr, callbackRef) ->
				nativeHeap.free(callbackPtr)
				callbackRef.dispose()
				callbackPtrAndRef = null
			}

			if (error == 0U) return
			throwIse(error)
		}
	}

	public actual interface Callback {
		public actual fun onFocus(focused: Boolean)
		public actual fun onKey()
		public actual fun onMouse()
		public actual fun onResize(columns: Int, rows: Int, width: Int, height: Int)
	}
}

internal fun throwIse(error: UInt): Nothing {
	throw IllegalStateException(error.toString())
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
