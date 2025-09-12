package com.jakewharton.mosaic.tty

import com.jakewharton.mosaic.tty.Libmosaic.testTty_free
import com.jakewharton.mosaic.tty.Libmosaic.testTty_getStreams
import com.jakewharton.mosaic.tty.Libmosaic.testTty_getTty
import com.jakewharton.mosaic.tty.Libmosaic.testTty_init
import com.jakewharton.mosaic.tty.Libmosaic.testTty_interruptRead
import com.jakewharton.mosaic.tty.Libmosaic.testTty_read
import com.jakewharton.mosaic.tty.Libmosaic.testTty_resize
import com.jakewharton.mosaic.tty.Libmosaic.testTty_sendFocusEvent
import com.jakewharton.mosaic.tty.Libmosaic.testTty_sendKeyEvent
import com.jakewharton.mosaic.tty.Libmosaic.testTty_sendMouseEvent
import com.jakewharton.mosaic.tty.Libmosaic.testTty_write
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

			val result = testTty_init(Arena.global(), stdinIsTty, stdoutIsTty, stderrIsTty)
			val testTtyPtr = MosaicTestTtyInitResult.testTty(result)
			if (testTtyPtr != MemorySegment.NULL) {
				val streamsPtr = testTty_getStreams(testTtyPtr)
				val streams = StandardStreams(streamsPtr)
				val ttyPtr = testTty_getTty(testTtyPtr)
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

	public fun write(buffer: ByteArray, offset: Int, count: Int): Int {
		val segment = Libmosaic.LIBRARY_ARENA.allocate(count.toLong())
		MemorySegment.copy(buffer, offset, segment, ValueLayout.JAVA_BYTE, 0, count)
		val result = testTty_write(Arena.global(), ptr, segment, count)
		val error = MosaicIoResult.error(result)
		if (error == 0) {
			return MosaicIoResult.count(result)
		}
		throwIoe(error)
	}

	public fun read(buffer: ByteArray, offset: Int, count: Int): Int {
		val segment = Libmosaic.LIBRARY_ARENA.allocate(count.toLong())
		val result = testTty_read(Arena.global(), ptr, segment, count)
		val error = MosaicIoResult.error(result)
		if (error == 0) {
			val read = MosaicIoResult.count(result)
			MemorySegment.copy(segment, ValueLayout.JAVA_BYTE, 0L, buffer, offset, read)
			return read
		}
		throwIoe(error)
	}

	public fun interruptRead() {
		val error = testTty_interruptRead(ptr)
		if (error == 0) return
		throwIoe(error)
	}

	public fun resize(columns: Int, rows: Int, width: Int, height: Int) {
		val error = testTty_resize(ptr, columns, rows, width, height)
		if (error == 0) return
		throwIoe(error)
	}

	public fun sendFocusEvent(focused: Boolean) {
		val error = testTty_sendFocusEvent(ptr, focused)
		if (error == 0) return
		throwIoe(error)
	}

	public fun sendKeyEvent() {
		val error = testTty_sendKeyEvent(ptr)
		if (error == 0) return
		throwIoe(error)
	}

	public fun sendMouseEvent() {
		val error = testTty_sendMouseEvent(ptr)
		if (error == 0) return
		throwIoe(error)
	}

	override fun close() {
		val ptr = ptr
		if (ptr != MemorySegment.NULL) {
			this.ptr = MemorySegment.NULL

			tty.close()

			val error = testTty_free(ptr)
			if (error == 0) return
			throwIoe(error)
		}
	}
}
