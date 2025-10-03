package com.jakewharton.mosaic.tty

import com.jakewharton.mosaic.tty.Libmosaic.mosaic_test_free
import com.jakewharton.mosaic.tty.Libmosaic.mosaic_test_get_streams
import com.jakewharton.mosaic.tty.Libmosaic.mosaic_test_get_tty
import com.jakewharton.mosaic.tty.Libmosaic.mosaic_test_init
import com.jakewharton.mosaic.tty.Libmosaic.mosaic_test_interrupt_error_read
import com.jakewharton.mosaic.tty.Libmosaic.mosaic_test_interrupt_output_read
import com.jakewharton.mosaic.tty.Libmosaic.mosaic_test_interrupt_tty_read
import com.jakewharton.mosaic.tty.Libmosaic.mosaic_test_read_error
import com.jakewharton.mosaic.tty.Libmosaic.mosaic_test_read_error_with_timeout
import com.jakewharton.mosaic.tty.Libmosaic.mosaic_test_read_output
import com.jakewharton.mosaic.tty.Libmosaic.mosaic_test_read_output_with_timeout
import com.jakewharton.mosaic.tty.Libmosaic.mosaic_test_read_tty
import com.jakewharton.mosaic.tty.Libmosaic.mosaic_test_read_tty_with_timeout
import com.jakewharton.mosaic.tty.Libmosaic.mosaic_test_resize
import com.jakewharton.mosaic.tty.Libmosaic.mosaic_test_send_focus_event
import com.jakewharton.mosaic.tty.Libmosaic.mosaic_test_send_key_event
import com.jakewharton.mosaic.tty.Libmosaic.mosaic_test_send_mouse_event
import com.jakewharton.mosaic.tty.Libmosaic.mosaic_test_write_input
import com.jakewharton.mosaic.tty.Libmosaic.mosaic_test_write_tty
import java.lang.foreign.Arena
import java.lang.foreign.MemorySegment
import java.lang.foreign.ValueLayout

public class TestTty private constructor(
	private var ptr: MemorySegment,
	public val streams: StandardStreams,
	public val tty: Tty,
) : AutoCloseable {
	public companion object {
		@JvmStatic
		public fun bind(
			stdinIsTty: Boolean = false,
			stdoutIsTty: Boolean = false,
			stderrIsTty: Boolean = false,
		): TestTty {
			NativeLibrary.ensureLoaded()

			val result = mosaic_test_init(Arena.global(), stdinIsTty, stdoutIsTty, stderrIsTty)
			val testTtyPtr = MosaicTestTtyInitResult.testTty(result)
			if (testTtyPtr != MemorySegment.NULL) {
				val streamsPtr = mosaic_test_get_streams(testTtyPtr)
				val streams = StandardStreams(streamsPtr)
				val ttyPtr = mosaic_test_get_tty(testTtyPtr)
				val tty = Tty(ttyPtr)
				return TestTty(testTtyPtr, streams, tty)
			}
			if (MosaicTestTtyInitResult.already_bound(result)) {
				throw IllegalStateException("TestTty or Tty already bound")
			}
			val error = MosaicTestTtyInitResult.error(result)
			if (error != 0) {
				throwIoe(error)
			}
			throw OutOfMemoryError()
		}
	}

	public fun writeTty(buffer: ByteArray, offset: Int, count: Int): Int {
		val segment = Libmosaic.LIBRARY_ARENA.allocate(count.toLong())
		MemorySegment.copy(buffer, offset, segment, ValueLayout.JAVA_BYTE, 0, count)
		val result = mosaic_test_write_tty(Arena.global(), ptr, segment, count)
		val error = MosaicIoResult.error(result)
		if (error == 0) {
			return MosaicIoResult.count(result)
		}
		throwIoe(error)
	}

	public fun readTty(buffer: ByteArray, offset: Int, count: Int): Int {
		val segment = Libmosaic.LIBRARY_ARENA.allocate(count.toLong())
		val result = mosaic_test_read_tty(Arena.global(), ptr, segment, count)
		val error = MosaicIoResult.error(result)
		if (error == 0) {
			val read = MosaicIoResult.count(result)
			MemorySegment.copy(segment, ValueLayout.JAVA_BYTE, 0L, buffer, offset, read)
			return read
		}
		throwIoe(error)
	}

	public fun readTtyWithTimeout(buffer: ByteArray, offset: Int, count: Int, timeoutMillis: Int): Int {
		val segment = Libmosaic.LIBRARY_ARENA.allocate(count.toLong())
		val result = mosaic_test_read_tty_with_timeout(Arena.global(), ptr, segment, count, timeoutMillis)
		val error = MosaicIoResult.error(result)
		if (error == 0) {
			val read = MosaicIoResult.count(result)
			MemorySegment.copy(segment, ValueLayout.JAVA_BYTE, 0L, buffer, offset, read)
			return read
		}
		throwIoe(error)
	}

	public fun interruptTtyRead() {
		val error = mosaic_test_interrupt_tty_read(ptr)
		if (error == 0) return
		throwIoe(error)
	}

	public fun writeStandardInput(buffer: ByteArray, offset: Int, count: Int): Int {
		val segment = Libmosaic.LIBRARY_ARENA.allocate(count.toLong())
		MemorySegment.copy(buffer, offset, segment, ValueLayout.JAVA_BYTE, 0, count)
		val result = mosaic_test_write_input(Arena.global(), ptr, segment, count)
		val error = MosaicIoResult.error(result)
		if (error == 0) {
			return MosaicIoResult.count(result)
		}
		throwIoe(error)
	}

	public fun readStandardOutput(buffer: ByteArray, offset: Int, count: Int): Int {
		val segment = Libmosaic.LIBRARY_ARENA.allocate(count.toLong())
		val result = mosaic_test_read_output(Arena.global(), ptr, segment, count)
		val error = MosaicIoResult.error(result)
		if (error == 0) {
			val read = MosaicIoResult.count(result)
			MemorySegment.copy(segment, ValueLayout.JAVA_BYTE, 0L, buffer, offset, read)
			return read
		}
		throwIoe(error)
	}

	public fun readStandardOutputWithTimeout(buffer: ByteArray, offset: Int, count: Int, timeoutMillis: Int): Int {
		val segment = Libmosaic.LIBRARY_ARENA.allocate(count.toLong())
		val result = mosaic_test_read_output_with_timeout(Arena.global(), ptr, segment, count, timeoutMillis)
		val error = MosaicIoResult.error(result)
		if (error == 0) {
			val read = MosaicIoResult.count(result)
			MemorySegment.copy(segment, ValueLayout.JAVA_BYTE, 0L, buffer, offset, read)
			return read
		}
		throwIoe(error)
	}

	public fun interruptStandardOutputRead() {
		val error = mosaic_test_interrupt_output_read(ptr)
		if (error == 0) return
		throwIoe(error)
	}

	public fun readStandardError(buffer: ByteArray, offset: Int, count: Int): Int {
		val segment = Libmosaic.LIBRARY_ARENA.allocate(count.toLong())
		val result = mosaic_test_read_error(Arena.global(), ptr, segment, count)
		val error = MosaicIoResult.error(result)
		if (error == 0) {
			val read = MosaicIoResult.count(result)
			MemorySegment.copy(segment, ValueLayout.JAVA_BYTE, 0L, buffer, offset, read)
			return read
		}
		throwIoe(error)
	}

	public fun readStandardErrorWithTimeout(buffer: ByteArray, offset: Int, count: Int, timeoutMillis: Int): Int {
		val segment = Libmosaic.LIBRARY_ARENA.allocate(count.toLong())
		val result = mosaic_test_read_error_with_timeout(Arena.global(), ptr, segment, count, timeoutMillis)
		val error = MosaicIoResult.error(result)
		if (error == 0) {
			val read = MosaicIoResult.count(result)
			MemorySegment.copy(segment, ValueLayout.JAVA_BYTE, 0L, buffer, offset, read)
			return read
		}
		throwIoe(error)
	}

	public fun interruptStandardErrorRead() {
		val error = mosaic_test_interrupt_error_read(ptr)
		if (error == 0) return
		throwIoe(error)
	}

	public fun resize(columns: Int, rows: Int, width: Int, height: Int) {
		val error = mosaic_test_resize(ptr, columns, rows, width, height)
		if (error == 0) return
		throwIoe(error)
	}

	public fun sendFocusEvent(focused: Boolean) {
		val error = mosaic_test_send_focus_event(ptr, focused)
		if (error == 0) return
		throwIoe(error)
	}

	public fun sendKeyEvent() {
		val error = mosaic_test_send_key_event(ptr)
		if (error == 0) return
		throwIoe(error)
	}

	public fun sendMouseEvent() {
		val error = mosaic_test_send_mouse_event(ptr)
		if (error == 0) return
		throwIoe(error)
	}

	override fun close() {
		val ptr = ptr
		if (ptr != MemorySegment.NULL) {
			this.ptr = MemorySegment.NULL

			tty.close()

			val error = mosaic_test_free(ptr)
			if (error == 0) return
			throwIoe(error)
		}
	}
}
