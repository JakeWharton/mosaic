package com.jakewharton.mosaic.tty

import kotlinx.cinterop.CPointer
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.useContents
import kotlinx.cinterop.usePinned

public actual class TestTerminal private constructor(
	private var ptr: CPointer<MosaicTestTty>?,
	public actual val streams: StandardStreams,
	public actual val tty: Tty,
) : AutoCloseable {
	public actual companion object {
		public actual fun bind(
			stdinIsTty: Boolean,
			stdoutIsTty: Boolean,
			stderrIsTty: Boolean,
		): TestTerminal {
			val ptr = mosaic_test_init(stdinIsTty, stdoutIsTty, stderrIsTty).useContents {
				test_tty?.let { return@useContents it }

				if (already_bound) {
					throw IllegalStateException("TestTerminal or Tty already bound")
				}
				if (error != 0U) {
					throwIoe(error)
				}
				throw OutOfMemoryError()
			}

			val streamsPtr = mosaic_test_get_streams(ptr)!!
			val streams = StandardStreams(streamsPtr)
			val ttyPtr = mosaic_test_get_tty(ptr)!!
			val tty = Tty(ttyPtr)
			return TestTerminal(ptr, streams, tty)
		}
	}

	public actual fun writeTty(buffer: ByteArray, offset: Int, count: Int): Int {
		buffer.asUByteArray().usePinned {
			mosaic_test_write_tty(ptr, it.addressOf(offset), count).useContents {
				if (error == 0U) {
					return this.count
				}
				throwIoe(error)
			}
		}
	}

	public actual fun readTty(buffer: ByteArray, offset: Int, count: Int): Int {
		buffer.asUByteArray().usePinned {
			mosaic_test_read_tty(ptr, it.addressOf(offset), count).useContents {
				if (error == 0U) {
					return this.count
				}
				throwIoe(error)
			}
		}
	}

	public actual fun readTtyWithTimeout(buffer: ByteArray, offset: Int, count: Int, timeoutMillis: Int): Int {
		buffer.asUByteArray().usePinned {
			mosaic_test_read_tty_with_timeout(ptr, it.addressOf(offset), count, timeoutMillis).useContents {
				if (error == 0U) {
					return this.count
				}
				throwIoe(error)
			}
		}
	}

	public actual fun interruptTtyRead() {
		val error = mosaic_test_interrupt_tty_read(ptr)
		if (error != 0U) {
			throwIoe(error)
		}
	}

	public actual fun writeStandardInput(buffer: ByteArray, offset: Int, count: Int): Int {
		buffer.asUByteArray().usePinned {
			mosaic_test_write_input(ptr, it.addressOf(offset), count).useContents {
				if (error == 0U) {
					return this.count
				}
				throwIoe(error)
			}
		}
	}

	public actual fun readStandardOutput(buffer: ByteArray, offset: Int, count: Int): Int {
		buffer.asUByteArray().usePinned {
			mosaic_test_read_output(ptr, it.addressOf(offset), count).useContents {
				if (error == 0U) {
					return this.count
				}
				throwIoe(error)
			}
		}
	}

	public actual fun readStandardOutputWithTimeout(buffer: ByteArray, offset: Int, count: Int, timeoutMillis: Int): Int {
		buffer.asUByteArray().usePinned {
			mosaic_test_read_output_with_timeout(ptr, it.addressOf(offset), count, timeoutMillis).useContents {
				if (error == 0U) {
					return this.count
				}
				throwIoe(error)
			}
		}
	}

	public actual fun interruptStandardOutputRead() {
		val error = mosaic_test_interrupt_output_read(ptr)
		if (error != 0U) {
			throwIoe(error)
		}
	}

	public actual fun readStandardError(buffer: ByteArray, offset: Int, count: Int): Int {
		buffer.asUByteArray().usePinned {
			mosaic_test_read_error(ptr, it.addressOf(offset), count).useContents {
				if (error == 0U) {
					return this.count
				}
				throwIoe(error)
			}
		}
	}

	public actual fun readStandardErrorWithTimeout(buffer: ByteArray, offset: Int, count: Int, timeoutMillis: Int): Int {
		buffer.asUByteArray().usePinned {
			mosaic_test_read_error_with_timeout(ptr, it.addressOf(offset), count, timeoutMillis).useContents {
				if (error == 0U) {
					return this.count
				}
				throwIoe(error)
			}
		}
	}

	public actual fun interruptStandardErrorRead() {
		val error = mosaic_test_interrupt_error_read(ptr)
		if (error != 0U) {
			throwIoe(error)
		}
	}

	public actual fun resize(columns: Int, rows: Int, width: Int, height: Int) {
		val error = mosaic_test_resize(ptr, columns, rows, width, height)
		if (error != 0U) {
			throwIoe(error)
		}
	}

	public actual fun sendFocusEvent(focused: Boolean) {
		val error = mosaic_test_send_focus_event(ptr, focused)
		if (error != 0U) {
			throwIoe(error)
		}
	}

	public actual fun sendKeyEvent() {
		val error = mosaic_test_send_key_event(ptr)
		if (error != 0U) {
			throwIoe(error)
		}
	}

	public actual fun sendMouseEvent() {
		val error = mosaic_test_send_mouse_event(ptr)
		if (error != 0U) {
			throwIoe(error)
		}
	}

	actual override fun close() {
		ptr?.let { ref ->
			this.ptr = null

			tty.close()

			val error = mosaic_test_free(ref)

			if (error == 0U) return
			throwIoe(error)
		}
	}
}
