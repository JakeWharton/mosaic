package com.jakewharton.mosaic.tty

import com.jakewharton.mosaic.tty.Libmosaic.mosaic_streams_free
import com.jakewharton.mosaic.tty.Libmosaic.mosaic_streams_init
import com.jakewharton.mosaic.tty.Libmosaic.mosaic_streams_intercept_start
import com.jakewharton.mosaic.tty.Libmosaic.mosaic_streams_intercept_stop
import com.jakewharton.mosaic.tty.Libmosaic.mosaic_streams_interrupt_input_read
import com.jakewharton.mosaic.tty.Libmosaic.mosaic_streams_interrupt_intercepted_error_read
import com.jakewharton.mosaic.tty.Libmosaic.mosaic_streams_interrupt_intercepted_output_read
import com.jakewharton.mosaic.tty.Libmosaic.mosaic_streams_is_stderr_tty
import com.jakewharton.mosaic.tty.Libmosaic.mosaic_streams_is_stdin_tty
import com.jakewharton.mosaic.tty.Libmosaic.mosaic_streams_is_stdout_tty
import com.jakewharton.mosaic.tty.Libmosaic.mosaic_streams_read_input
import com.jakewharton.mosaic.tty.Libmosaic.mosaic_streams_read_input_with_timeout
import com.jakewharton.mosaic.tty.Libmosaic.mosaic_streams_read_intercepted_error
import com.jakewharton.mosaic.tty.Libmosaic.mosaic_streams_read_intercepted_error_with_timeout
import com.jakewharton.mosaic.tty.Libmosaic.mosaic_streams_read_intercepted_output
import com.jakewharton.mosaic.tty.Libmosaic.mosaic_streams_read_intercepted_output_with_timeout
import com.jakewharton.mosaic.tty.Libmosaic.mosaic_streams_write_error
import com.jakewharton.mosaic.tty.Libmosaic.mosaic_streams_write_output
import java.lang.foreign.Arena
import java.lang.foreign.MemorySegment
import java.lang.foreign.ValueLayout

public class StandardStreams internal constructor(
	private var ptr: MemorySegment,
) : AutoCloseable {
	public companion object {
		@JvmStatic
		public fun bind(): StandardStreams {
			NativeLibrary.ensureLoaded()

			val result = mosaic_streams_init.makeInvoker().apply(Arena.global())
			val ptr = MosaicStreamsInitResult.streams(result)
			if (ptr != MemorySegment.NULL) {
				return StandardStreams(ptr)
			}
			val error = MosaicStreamsInitResult.error(result)
			if (error != 0) {
				throwIoe(error)
			}
			throw OutOfMemoryError()
		}
	}

	private val MemorySegment.isTty: Boolean
		get() {
			val error = MosaicStreamsTtyResult.error(this)
			if (error == 0) {
				return MosaicStreamsTtyResult.is_tty(this)
			}
			throwIoe(error)
		}

	@Throws(IOException::class)
	public fun isInputTty(): Boolean {
		Arena.ofConfined().use { arena ->
			return mosaic_streams_is_stdin_tty(arena, ptr).isTty
		}
	}

	@Throws(IOException::class)
	public fun isOutputTty(): Boolean {
		Arena.ofConfined().use { arena ->
			return mosaic_streams_is_stdout_tty(arena, ptr).isTty
		}
	}

	@Throws(IOException::class)
	public fun isErrorTty(): Boolean {
		Arena.ofConfined().use { arena ->
			return mosaic_streams_is_stderr_tty(arena, ptr).isTty
		}
	}

	@Throws(IOException::class)
	public fun readInput(buffer: ByteArray, offset: Int, count: Int): Int {
		val segment = Libmosaic.LIBRARY_ARENA.allocate(count.toLong())
		val result = mosaic_streams_read_input(Arena.global(), ptr, segment, count)
		val error = MosaicIoResult.error(result)
		if (error == 0) {
			val read = MosaicIoResult.count(result)
			MemorySegment.copy(segment, ValueLayout.JAVA_BYTE, 0L, buffer, offset, read)
			return read
		}
		throwIoe(error)
	}

	@Throws(IOException::class)
	public fun readInputWithTimeout(buffer: ByteArray, offset: Int, count: Int, timeoutMillis: Int): Int {
		val segment = Libmosaic.LIBRARY_ARENA.allocate(count.toLong())
		val result = mosaic_streams_read_input_with_timeout(Arena.global(), ptr, segment, count, timeoutMillis)
		val error = MosaicIoResult.error(result)
		if (error == 0) {
			val read = MosaicIoResult.count(result)
			MemorySegment.copy(segment, ValueLayout.JAVA_BYTE, 0L, buffer, offset, read)
			return read
		}
		throwIoe(error)
	}

	@Throws(IOException::class)
	public fun interruptInputRead() {
		val error = mosaic_streams_interrupt_input_read(ptr)
		if (error == 0) return
		throwIoe(error)
	}

	@Throws(IOException::class)
	public fun writeOutput(buffer: ByteArray, offset: Int, count: Int): Int {
		val segment = Libmosaic.LIBRARY_ARENA.allocate(count.toLong())
		MemorySegment.copy(buffer, offset, segment, ValueLayout.JAVA_BYTE, 0, count)
		val result = mosaic_streams_write_output(Arena.global(), ptr, segment, count)
		val error = MosaicIoResult.error(result)
		if (error == 0) {
			return MosaicIoResult.count(result)
		}
		throwIoe(error)
	}

	@Throws(IOException::class)
	public fun writeError(buffer: ByteArray, offset: Int, count: Int): Int {
		val segment = Libmosaic.LIBRARY_ARENA.allocate(count.toLong())
		MemorySegment.copy(buffer, offset, segment, ValueLayout.JAVA_BYTE, 0, count)
		val result = mosaic_streams_write_error(Arena.global(), ptr, segment, count)
		val error = MosaicIoResult.error(result)
		if (error == 0) {
			return MosaicIoResult.count(result)
		}
		throwIoe(error)
	}

	public fun interceptOtherWrites(): InterceptedStreams {
		Arena.ofConfined().use { arena ->
			val result = mosaic_streams_intercept_start(arena, ptr)
			if (MosaicStreamsInterceptResult.is_test(result)) {
				throw IllegalStateException("Cannot intercept test streams")
			}
			if (MosaicStreamsInterceptResult.already_bound(result)) {
				throw IllegalStateException("Standard streams already intercepted")
			}
			val error = MosaicStreamsInterceptResult.error(result)
			if (error != 0) {
				throwIoe(error)
			}
		}
		return InterceptedStreams(ptr)
	}

	override fun close() {
		val ptr = ptr
		if (ptr != MemorySegment.NULL) {
			this.ptr = MemorySegment.NULL
			val error = mosaic_streams_free(ptr)
			if (error != 0) throwIoe(error)
		}
	}

	public class InterceptedStreams internal constructor(
		private val ptr: MemorySegment,
	) : AutoCloseable {
		public fun readOutput(buffer: ByteArray, offset: Int, count: Int): Int {
			val segment = Libmosaic.LIBRARY_ARENA.allocate(count.toLong())
			val result = mosaic_streams_read_intercepted_output(Arena.global(), ptr, segment, count)
			val error = MosaicIoResult.error(result)
			if (error == 0) {
				val read = MosaicIoResult.count(result)
				MemorySegment.copy(segment, ValueLayout.JAVA_BYTE, 0L, buffer, offset, read)
				return read
			}
			throwIoe(error)
		}

		public fun readOutputWithTimeout(buffer: ByteArray, offset: Int, count: Int, timeoutMillis: Int): Int {
			val segment = Libmosaic.LIBRARY_ARENA.allocate(count.toLong())
			val result = mosaic_streams_read_intercepted_output_with_timeout(Arena.global(), ptr, segment, count, timeoutMillis)
			val error = MosaicIoResult.error(result)
			if (error == 0) {
				val read = MosaicIoResult.count(result)
				MemorySegment.copy(segment, ValueLayout.JAVA_BYTE, 0L, buffer, offset, read)
				return read
			}
			throwIoe(error)
		}

		public fun interruptOutputRead() {
			val error = mosaic_streams_interrupt_intercepted_output_read(ptr)
			if (error == 0) return
			throwIoe(error)
		}

		public fun readError(buffer: ByteArray, offset: Int, count: Int): Int {
			val segment = Libmosaic.LIBRARY_ARENA.allocate(count.toLong())
			val result = mosaic_streams_read_intercepted_error(Arena.global(), ptr, segment, count)
			val error = MosaicIoResult.error(result)
			if (error == 0) {
				val read = MosaicIoResult.count(result)
				MemorySegment.copy(segment, ValueLayout.JAVA_BYTE, 0L, buffer, offset, read)
				return read
			}
			throwIoe(error)
		}

		public fun readErrorWithTimeout(buffer: ByteArray, offset: Int, count: Int, timeoutMillis: Int): Int {
			val segment = Libmosaic.LIBRARY_ARENA.allocate(count.toLong())
			val result = mosaic_streams_read_intercepted_error_with_timeout(Arena.global(), ptr, segment, count, timeoutMillis)
			val error = MosaicIoResult.error(result)
			if (error == 0) {
				val read = MosaicIoResult.count(result)
				MemorySegment.copy(segment, ValueLayout.JAVA_BYTE, 0L, buffer, offset, read)
				return read
			}
			throwIoe(error)
		}

		public fun interruptErrorRead() {
			val error = mosaic_streams_interrupt_intercepted_error_read(ptr)
			if (error == 0) return
			throwIoe(error)
		}

		override fun close() {
			val error = mosaic_streams_intercept_stop(ptr)
			if (error == 0) return
			throwIoe(error)
		}
	}
}
