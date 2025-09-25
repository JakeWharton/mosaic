package com.jakewharton.mosaic.tty

import com.jakewharton.mosaic.tty.Libmosaic.mosaic_tty_current_terminal_size
import com.jakewharton.mosaic.tty.Libmosaic.mosaic_tty_enable_raw_mode
import com.jakewharton.mosaic.tty.Libmosaic.mosaic_tty_enable_window_resize_events
import com.jakewharton.mosaic.tty.Libmosaic.mosaic_tty_free
import com.jakewharton.mosaic.tty.Libmosaic.mosaic_tty_init
import com.jakewharton.mosaic.tty.Libmosaic.mosaic_tty_interrupt_read
import com.jakewharton.mosaic.tty.Libmosaic.mosaic_tty_read
import com.jakewharton.mosaic.tty.Libmosaic.mosaic_tty_read_with_timeout
import com.jakewharton.mosaic.tty.Libmosaic.mosaic_tty_reset
import com.jakewharton.mosaic.tty.Libmosaic.mosaic_tty_set_callback
import com.jakewharton.mosaic.tty.Libmosaic.mosaic_tty_write
import java.io.InputStream
import java.io.OutputStream
import java.lang.foreign.Arena
import java.lang.foreign.MemorySegment
import java.lang.foreign.ValueLayout

public class Tty internal constructor(
	private var ttyPtr: MemorySegment,
) : AutoCloseable {
	public companion object {
		@JvmStatic
		public fun tryBind(): Tty? {
			NativeLibrary.ensureLoaded()

			val result = mosaic_tty_init.makeInvoker().apply(Arena.global())
			val ttyPtr = MosaicTtyInitResult.tty(result)
			if (ttyPtr != MemorySegment.NULL) {
				return Tty(ttyPtr)
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
	}

	public fun asInputStream(): InputStream = TtyInputStream(this)

	public fun asOutputStream(): OutputStream = TtyOutputStream(this)

	private var callbackArena: Arena? = null

	public fun setCallback(callback: Callback?) {
		callbackArena?.let { arena ->
			arena.close()
			callbackArena = null
		}

		val ttyCallback = if (callback == null) {
			MemorySegment.NULL
		} else {
			val arena = Arena.ofShared()
			callbackArena = arena

			MosaicTtyCallback.allocate(arena).also { ttyCallback ->
				MosaicTtyCallback.onFocus(
					ttyCallback,
					MosaicTtyCallbackOnFocus.allocate({ _, focused ->
						callback.onFocus(focused)
					}, arena),
				)
				MosaicTtyCallback.onKey(
					ttyCallback,
					MosaicTtyCallbackOnKey.allocate({
						callback.onKey()
					}, arena),
				)
				MosaicTtyCallback.onMouse(
					ttyCallback,
					MosaicTtyCallbackOnMouse.allocate({
						callback.onMouse()
					}, arena),
				)
				MosaicTtyCallback.onResize(
					ttyCallback,
					MosaicTtyCallbackOnResize.allocate({ _, columns, rows, width, height ->
						callback.onResize(columns, rows, width, height)
					}, arena),
				)
			}
		}

		mosaic_tty_set_callback(ttyPtr, ttyCallback)
	}

	@Throws(IOException::class)
	public fun read(buffer: ByteArray, offset: Int, count: Int): Int {
		val segment = Libmosaic.LIBRARY_ARENA.allocate(count.toLong())
		val result = mosaic_tty_read(Arena.global(), ttyPtr, segment, count)
		val error = MosaicIoResult.error(result)
		if (error == 0) {
			val read = MosaicIoResult.count(result)
			MemorySegment.copy(segment, ValueLayout.JAVA_BYTE, 0L, buffer, offset, read)
			return read
		}
		throwIoe(error)
	}

	@Throws(IOException::class)
	public fun readWithTimeout(buffer: ByteArray, offset: Int, count: Int, timeoutMillis: Int): Int {
		val segment = Libmosaic.LIBRARY_ARENA.allocate(count.toLong())
		val result = mosaic_tty_read_with_timeout(Arena.global(), ttyPtr, segment, count, timeoutMillis)
		val error = MosaicIoResult.error(result)
		if (error == 0) {
			val read = MosaicIoResult.count(result)
			MemorySegment.copy(segment, ValueLayout.JAVA_BYTE, 0L, buffer, offset, read)
			return read
		}
		throwIoe(error)
	}

	@Throws(IOException::class)
	public fun interruptRead() {
		val error = mosaic_tty_interrupt_read(ttyPtr)
		if (error == 0) return
		throwIoe(error)
	}

	@Throws(IOException::class)
	public fun write(buffer: ByteArray, offset: Int, count: Int): Int {
		val segment = Libmosaic.LIBRARY_ARENA.allocate(count.toLong())
		MemorySegment.copy(buffer, offset, segment, ValueLayout.JAVA_BYTE, 0, count)
		val result = mosaic_tty_write(Arena.global(), ttyPtr, segment, count)
		val error = MosaicIoResult.error(result)
		if (error == 0) {
			return MosaicIoResult.count(result)
		}
		throwIoe(error)
	}

	@Throws(IOException::class)
	public fun enableRawMode() {
		val error = mosaic_tty_enable_raw_mode(ttyPtr)
		if (error == 0) return
		throwIoe(error)
	}

	@Throws(IOException::class)
	public fun enableWindowResizeEvents() {
		val error = mosaic_tty_enable_window_resize_events(ttyPtr)
		if (error == 0) return
		throwIoe(error)
	}

	@Throws(IOException::class)
	public fun currentSize(): IntArray {
		val result = mosaic_tty_current_terminal_size(Arena.global(), ttyPtr)
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
		val error = mosaic_tty_reset(ttyPtr)
		if (error == 0) return
		throwIoe(error)
	}

	@Throws(IOException::class)
	override fun close() {
		val ttyPtr = ttyPtr
		if (ttyPtr != MemorySegment.NULL) {
			this.ttyPtr = MemorySegment.NULL

			callbackArena?.close()
			callbackArena = null

			val error = mosaic_tty_free(ttyPtr)
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

internal fun throwIoe(error: Int): Nothing {
	throw IOException(error.toString())
}
