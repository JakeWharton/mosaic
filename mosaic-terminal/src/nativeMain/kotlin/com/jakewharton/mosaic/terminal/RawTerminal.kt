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

public actual class RawTerminal internal constructor(
	private var ptr: CPointer<MosaicTerminal>?,
	private var callbackPtr: CPointer<*>?,
	private var callbackRef: StableRef<*>?,
) : AutoCloseable {
	public actual fun read(bytes: ByteArray, offset: Int, count: Int): Int {
		bytes.asUByteArray().usePinned {
			MosaicTerminalRead(ptr, it.addressOf(offset), count).useContents {
				if (error == 0U) return this.count
				Tty.throwError(error)
			}
		}
	}

	public actual fun read(bytes: ByteArray, offset: Int, count: Int, timeoutMillis: Int): Int {
		bytes.asUByteArray().usePinned {
			MosaicTerminalReadWithTimeout(ptr, it.addressOf(offset), count, timeoutMillis.toUInt()).useContents {
				if (error == 0U) return this.count
				Tty.throwError(error)
			}
		}
	}

	public actual fun interruptRead() {
		val error = MosaicTerminalInterrupt(ptr)
		if (error != 0U) {
			Tty.throwError(error)
		}
	}

	public actual fun writeOutput(bytes: ByteArray, offset: Int, count: Int): Int {
		bytes.asUByteArray().usePinned {
			MosaicTerminalWriteOutput(ptr, it.addressOf(offset), count).useContents {
				if (error == 0U) return this.count
				Tty.throwError(error)
			}
		}
	}

	public actual fun writeError(bytes: ByteArray, offset: Int, count: Int): Int {
		bytes.asUByteArray().usePinned {
			MosaicTerminalWriteError(ptr, it.addressOf(offset), count).useContents {
				if (error == 0U) return this.count
				Tty.throwError(error)
			}
		}
	}

	public actual fun enableRawMode() {
		val error = MosaicTerminalEnableRawMode(ptr)
		if (error != 0U) {
			Tty.throwError(error)
		}
	}

	// public actual fun enableStandardStreamRedirection() {
	// 	val error = MosaicTerminalEnableOutputRedirection(ptr)
	// 	if (error != 0U) {
	// 		Tty.throwError(error)
	// 	}
	// }

	public actual fun enableNativeResizeEvents() {
		val error = MosaicTerminalEnableResizeEvents(ptr)
		if (error != 0U) {
			Tty.throwError(error)
		}
	}

	public actual fun currentSize(): IntArray {
		MosaicTerminalCurrentSize(ptr).useContents {
			if (error == 0U) {
				return intArrayOf(columns.toInt(), rows.toInt(), width.toInt(), height.toInt())
			}
			Tty.throwError(error)
		}
	}

	actual override fun close() {
		ptr?.let { ptr ->
			this.ptr = null

			val error = MosaicTerminalFree(ptr)
			callbackPtr?.let(nativeHeap::free)
			callbackRef?.dispose()

			if (error != 0U) {
				Tty.throwError(error)
			}
		}
	}

	public actual interface EventCallback {
		public actual fun onFocus(focused: Boolean)
		public actual fun onKey()
		public actual fun onMouse()
		public actual fun onResize(columns: Int, rows: Int, width: Int, height: Int)
		// public actual fun onStandardOutput(bytes: ByteArray)
		// public actual fun onStandardError(bytes: ByteArray)
	}

	public actual companion object {
		public actual fun initialize(callback: EventCallback): RawTerminal {
			val callbackRef = StableRef.create(callback)
			val callbackPtr = callbackRef.toAllocation(nativeHeap)

			val error = MosaicTerminalInit(callbackPtr).useContents {
				terminal?.let { ptr ->
					return RawTerminal(ptr, callbackPtr, callbackRef)
				}
				error
			}

			nativeHeap.free(callbackPtr)
			callbackRef.dispose()

			if (error != 0U) {
				Tty.throwError(error)
			}
			throw OutOfMemoryError()
		}

		internal fun StableRef<EventCallback>.toAllocation(placement: NativePlacement): CPointer<MosaicTerminalEventCallback> {
			return placement.alloc<MosaicTerminalEventCallback> {
				opaque = asCPointer()
				onFocus = staticCFunction(::onFocusCallback)
				onKey = staticCFunction(::onKeyCallback)
				onMouse = staticCFunction(::onMouseCallback)
				onResize = staticCFunction(::onResizeCallback)
			}.ptr
		}
	}
}

private fun onFocusCallback(opaque: COpaquePointer?, focused: Boolean) {
	val handler = opaque!!.asStableRef<RawTerminal.EventCallback>().get()
	handler.onFocus(focused)
}

private fun onKeyCallback(opaque: COpaquePointer?) {
	val handler = opaque!!.asStableRef<RawTerminal.EventCallback>().get()
	handler.onKey()
}

private fun onMouseCallback(opaque: COpaquePointer?) {
	val handler = opaque!!.asStableRef<RawTerminal.EventCallback>().get()
	handler.onMouse()
}

private fun onResizeCallback(opaque: COpaquePointer?, columns: UShort, rows: UShort, width: UShort, height: UShort) {
	val handler = opaque!!.asStableRef<RawTerminal.EventCallback>().get()
	handler.onResize(columns.toInt(), rows.toInt(), width.toInt(), height.toInt())
}
