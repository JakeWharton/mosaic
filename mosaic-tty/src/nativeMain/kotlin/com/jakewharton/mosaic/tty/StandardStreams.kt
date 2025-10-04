package com.jakewharton.mosaic.tty

import kotlinx.cinterop.CValue
import kotlinx.cinterop.CValuesRef
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.useContents
import kotlinx.cinterop.usePinned

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

	public actual fun readInput(buffer: ByteArray, offset: Int, count: Int): Int {
		buffer.asUByteArray().usePinned {
			mosaic_streams_read_input(ptr, it.addressOf(offset), count).useContents {
				if (error == 0U) return this.count
				throwIoe(error)
			}
		}
	}

	public actual fun readInputWithTimeout(buffer: ByteArray, offset: Int, count: Int, timeoutMillis: Int): Int {
		buffer.asUByteArray().usePinned {
			mosaic_streams_read_input_with_timeout(ptr, it.addressOf(offset), count, timeoutMillis).useContents {
				if (error == 0U) return this.count
				throwIoe(error)
			}
		}
	}

	public actual fun interruptInputRead() {
		val error = mosaic_streams_interrupt_input_read(ptr)
		if (error == 0U) return
		throwIoe(error)
	}

	public actual fun writeOutput(buffer: ByteArray, offset: Int, count: Int): Int {
		buffer.asUByteArray().usePinned {
			mosaic_streams_write_output(ptr, it.addressOf(offset), count).useContents {
				if (error == 0U) return this.count
				throwIoe(error)
			}
		}
	}

	public actual fun writeError(buffer: ByteArray, offset: Int, count: Int): Int {
		buffer.asUByteArray().usePinned {
			mosaic_streams_write_error(ptr, it.addressOf(offset), count).useContents {
				if (error == 0U) return this.count
				throwIoe(error)
			}
		}
	}

	public actual fun interceptOtherWrites(): InterceptedStreams {
		mosaic_streams_intercept_start(ptr).useContents {
			if (is_test) {
				throw IllegalStateException("Cannot intercept test streams")
			}
			if (already_bound) {
				throw IllegalStateException("Standard streams already intercepted")
			}
			if (error != 0U) {
				throwIoe(error)
			}
			return InterceptedStreams(ptr)
		}
	}

	actual override fun close() {
		ptr?.let {
			mosaic_streams_free(it)
			ptr = null
		}
	}

	public actual class InterceptedStreams internal constructor(
		private val ptr: CValuesRef<MosaicStreams>?,
	) : AutoCloseable {
		public actual fun readOutput(buffer: ByteArray, offset: Int, count: Int): Int {
			buffer.asUByteArray().usePinned {
				mosaic_streams_read_intercepted_output(ptr, it.addressOf(offset), count).useContents {
					if (error == 0U) return this.count
					throwIoe(error)
				}
			}
		}

		public actual fun readOutputWithTimeout(buffer: ByteArray, offset: Int, count: Int, timeoutMillis: Int): Int {
			buffer.asUByteArray().usePinned {
				mosaic_streams_read_intercepted_output_with_timeout(ptr, it.addressOf(offset), count, timeoutMillis).useContents {
					if (error == 0U) return this.count
					throwIoe(error)
				}
			}
		}

		public actual fun interruptOutputRead() {
			val error = mosaic_streams_interrupt_intercepted_output_read(ptr)
			if (error == 0U) return
			throwIoe(error)
		}

		public actual fun readError(buffer: ByteArray, offset: Int, count: Int): Int {
			buffer.asUByteArray().usePinned {
				mosaic_streams_read_intercepted_error(ptr, it.addressOf(offset), count).useContents {
					if (error == 0U) return this.count
					throwIoe(error)
				}
			}
		}

		public actual fun readErrorWithTimeout(buffer: ByteArray, offset: Int, count: Int, timeoutMillis: Int): Int {
			buffer.asUByteArray().usePinned {
				mosaic_streams_read_intercepted_error_with_timeout(ptr, it.addressOf(offset), count, timeoutMillis).useContents {
					if (error == 0U) return this.count
					throwIoe(error)
				}
			}
		}

		public actual fun interruptErrorRead() {
			val error = mosaic_streams_interrupt_intercepted_error_read(ptr)
			if (error == 0U) return
			throwIoe(error)
		}

		actual override fun close() {
			val error = mosaic_streams_intercept_stop(ptr)
			if (error == 0U) return
			throwIoe(error)
		}
	}
}
