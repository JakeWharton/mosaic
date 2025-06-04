package com.jakewharton.mosaic.tty

import java.lang.foreign.Arena
import java.lang.foreign.FunctionDescriptor
import java.lang.foreign.Linker
import java.lang.foreign.MemorySegment
import java.lang.foreign.ValueLayout
import java.lang.invoke.MethodHandles
import java.lang.invoke.MethodType

public class Tty internal constructor(
	ttyPtr: MemorySegment,
) : AutoCloseable {
	public companion object {
		@JvmStatic
		public fun tryBind(): Tty? {
			val result = Libmosaic.tty_init.makeInvoker().apply(Arena.global())
			MosaicTtyInitResult.tty(result)?.let { tty ->
				return Tty(tty)
			}
			if (MosaicTtyInitResult.no_tty(result)) {
				return null
			}
			if (MosaicTtyInitResult.already_bound(result)) {
				throw IllegalStateException("Tty already bound")
			}
			val error = MosaicTtyInitResult.error(result)
			if (error != 0) {
				throwIoe(error)
			}
			throw OutOfMemoryError()
		}

		private fun throwIoe(error: Int): Nothing {
			throw IOException(error.toString())
		}
	}

	private var ttyPtr: MemorySegment? = ttyPtr
	private var callbackPtr: MemorySegment? = null

	public fun setCallback(callback: Callback?) {
		val arena = Arena.global()
		val linker = Linker.nativeLinker()
		val callback = MosaicTtyCallback.allocate(arena)
		MosaicTtyCallback.onFocus(callback, run {
			val descriptor = FunctionDescriptor.ofVoid(
				ValueLayout.JAVA_BOOLEAN,
			)
			val handle = MethodHandles.lookup()
				.findVirtual(
					Callback::class.java,
					"onFocus",
				MethodType.methodType(
					Void.TYPE,
					Boolean::class.javaPrimitiveType,
				))
			linker.upcallStub(handle, descriptor, arena)
		})
		MosaicTtyCallback.onKey(callback, run {
			val descriptor = FunctionDescriptor.ofVoid()
			val handle = MethodHandles.lookup()
				.findVirtual(
					Callback::class.java,
					"onKey",
					MethodType.methodType(Void.TYPE))
			linker.upcallStub(handle, descriptor, arena)
		})
		MosaicTtyCallback.onMouse(callback, run {
			val descriptor = FunctionDescriptor.ofVoid()
			val handle = MethodHandles.lookup()
				.findVirtual(
					Callback::class.java,
					"onMouse",
					MethodType.methodType(Void.TYPE))
			linker.upcallStub(handle, descriptor, arena)
		})
		MosaicTtyCallback.onMouse(callback, run {
			val descriptor = FunctionDescriptor.ofVoid(
				ValueLayout.JAVA_INT,
				ValueLayout.JAVA_INT,
				ValueLayout.JAVA_INT,
				ValueLayout.JAVA_INT,
			)
			val handle = MethodHandles.lookup()
				.findVirtual(
					Callback::class.java,
					"onResize",
					MethodType.methodType(
						Void.TYPE,
						Int::class.javaPrimitiveType,
						Int::class.javaPrimitiveType,
						Int::class.javaPrimitiveType,
						Int::class.javaPrimitiveType,
					))
			linker.upcallStub(handle, descriptor, arena)
		})

		callbackPtr = callback
		Libmosaic.tty_setCallback(ttyPtr, callback)
	}

	@Throws(IOException::class)
	public fun read(buffer: ByteArray, offset: Int, count: Int): Int {
		val segment = MemorySegment.ofArray(buffer).asSlice(offset.toLong())
		val result = Libmosaic.tty_read(Arena.global(), ttyPtr, segment, count)
		val error = MosaicTtyIoResult.error(result)
		if (error == 0) return MosaicTtyIoResult.count(result)
		throwIoe(error)
	}

	@Throws(IOException::class)
	public fun readWithTimeout(buffer: ByteArray, offset: Int, count: Int, timeoutMillis: Int): Int {
		val segment = MemorySegment.ofArray(buffer).asSlice(offset.toLong())
		val result = Libmosaic.tty_readWithTimeout(Arena.global(), ttyPtr, segment, count, timeoutMillis)
		val error = MosaicTtyIoResult.error(result)
		if (error == 0) return MosaicTtyIoResult.count(result)
		throwIoe(error)
	}

	@Throws(IOException::class)
	public fun interruptRead() {
		val error = Libmosaic.tty_interruptRead(ttyPtr)
		if (error == 0) return
		throwIoe(error)
	}

	@Throws(IOException::class)
	public fun write(buffer: ByteArray, offset: Int, count: Int): Int {
		val segment = MemorySegment.ofArray(buffer).asSlice(offset.toLong())
		val result = Libmosaic.tty_write(Arena.global(), ttyPtr, segment, count)
		val error = MosaicTtyIoResult.error(result)
		if (error == 0) return MosaicTtyIoResult.count(result)
		throwIoe(error)
	}

	@Throws(IOException::class)
	public fun enableRawMode() {
		val error = Libmosaic.tty_enableRawMode(ttyPtr)
		if (error == 0) return
		throwIoe(error)
	}

	@Throws(IOException::class)
	public fun enableWindowResizeEvents() {
		val error = Libmosaic.tty_enableWindowResizeEvents(ttyPtr)
		if (error == 0) return
		throwIoe(error)
	}

	@Throws(IOException::class)
	public fun currentSize(): IntArray {
		val result = Libmosaic.tty_currentTerminalSize(Arena.global(), ttyPtr)
		val error = MosaicTtyTerminalSizeResult.error(result)
		if (error == 0) {
			return intArrayOf(
				MosaicTtyTerminalSizeResult.columns(result),
				MosaicTtyTerminalSizeResult.rows(result),
				MosaicTtyTerminalSizeResult.width(result),
				MosaicTtyTerminalSizeResult.height(result),
			)
		}
		throwIoe(error)
	}

	@Throws(IOException::class)
	public fun reset() {
		val error = Libmosaic.tty_reset(ttyPtr)
		if (error == 0) return
		throwIoe(error)
	}

	@Throws(IOException::class)
	override fun close() {
		ttyPtr?.let { ttyPtr ->
			this.ttyPtr = null
			callbackPtr = null

			val error = Libmosaic.tty_free(ttyPtr)
			if (error == 0) return
			throwIoe(error)
		}
	}

	public interface Callback {
		public fun onFocus(focused: Boolean)
		public fun onKey()
		public fun onMouse()
		public fun onResize(columns: Int, rows: Int, width: Int, height: Int)
	}
}
