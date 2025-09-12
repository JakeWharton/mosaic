package com.jakewharton.mosaic.tty

import com.jakewharton.mosaic.tty.Libmosaic.mosaic_streams_free
import com.jakewharton.mosaic.tty.Libmosaic.mosaic_streams_init
import com.jakewharton.mosaic.tty.Libmosaic.mosaic_streams_is_stderr_tty
import com.jakewharton.mosaic.tty.Libmosaic.mosaic_streams_is_stdin_tty
import com.jakewharton.mosaic.tty.Libmosaic.mosaic_streams_is_stdout_tty
import java.lang.foreign.Arena
import java.lang.foreign.MemorySegment

public class StandardStreams internal constructor(
	private var ptr: MemorySegment,
) : AutoCloseable {
	public companion object {
		@JvmStatic
		public fun bind(): StandardStreams {
			val result = mosaic_streams_init.makeInvoker().apply(Arena.global())
			val streams = MosaicStreamsInitResult.streams(result)
			if (streams != MemorySegment.NULL) {
				return StandardStreams(streams)
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

	override fun close() {
		val ptr = ptr
		if (ptr != MemorySegment.NULL) {
			this.ptr = MemorySegment.NULL
			val error = mosaic_streams_free(ptr)
			if (error != 0) throwIoe(error)
		}
	}
}
