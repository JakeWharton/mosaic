package com.jakewharton.mosaic.tty

import kotlinx.cinterop.CValue
import kotlinx.cinterop.CValuesRef
import kotlinx.cinterop.useContents

public actual class StandardStreams internal constructor(
	ptr: CValuesRef<MosaicStreams>,
) : AutoCloseable {
	public actual companion object {
		public actual fun bind(): StandardStreams {
			mosaic_streams_init().useContents {
				streams?.let {
					return StandardStreams(it)
				}
				if (error != 0U) throwIoe(error)
				throw OutOfMemoryError()
			}
		}
	}

	private var ptr: CValuesRef<MosaicStreams>? = ptr

	private val CValue<MosaicStreamsTtyResult>.isTty: Boolean get() {
		useContents {
			if (error == 0U) return is_tty
			throwIoe(error)
		}
	}

	public actual fun isInputTty(): Boolean = mosaic_streams_is_stdin_tty(ptr).isTty
	public actual fun isOutputTty(): Boolean = mosaic_streams_is_stdout_tty(ptr).isTty
	public actual fun isErrorTty(): Boolean = mosaic_streams_is_stderr_tty(ptr).isTty

	actual override fun close() {
		ptr?.let {
			mosaic_streams_free(it)
			ptr = null
		}
	}
}
