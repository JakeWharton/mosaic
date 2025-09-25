package com.jakewharton.mosaic.tty

import kotlinx.cinterop.CPointer
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.useContents
import kotlinx.cinterop.usePinned

public actual class TestTty private constructor(
	private var ptr: CPointer<MosaicTestTty>?,
	public actual val streams: StandardStreams,
	public actual val tty: Tty,
) : AutoCloseable {
	public actual companion object {
		public actual fun bind(
			stdinIsTty: Boolean,
			stdoutIsTty: Boolean,
			stderrIsTty: Boolean,
		): TestTty {
			val testTtyPtr = mosaic_test_init(stdinIsTty, stdoutIsTty, stderrIsTty).useContents {
				testTty?.let { return@useContents it }

				if (already_bound) {
					throw IllegalStateException("TestTty or Tty already bound")
				}
				if (error != 0U) {
					throwIoe(error)
				}
				throw OutOfMemoryError()
			}

			val streamsPtr = mosaic_test_get_streams(testTtyPtr)!!
			val streams = StandardStreams(streamsPtr)
			val ttyPtr = mosaic_test_get_tty(testTtyPtr)!!
			val tty = Tty(ttyPtr)
			return TestTty(testTtyPtr, streams, tty)
		}
	}

	public actual fun write(buffer: ByteArray, offset: Int, count: Int): Int {
		buffer.asUByteArray().usePinned {
			mosaic_test_write(ptr, it.addressOf(offset), count).useContents {
				if (error == 0U) {
					return this.count
				}
				throwIoe(error)
			}
		}
	}

	public actual fun read(buffer: ByteArray, offset: Int, count: Int): Int {
		buffer.asUByteArray().usePinned {
			mosaic_test_read(ptr, it.addressOf(offset), count).useContents {
				if (error == 0U) {
					return this.count
				}
				throwIoe(error)
			}
		}
	}

	public actual fun readWithTimeout(buffer: ByteArray, offset: Int, count: Int, timeoutMillis: Int): Int {
		buffer.asUByteArray().usePinned {
			mosaic_test_read_with_timeout(ptr, it.addressOf(offset), count, timeoutMillis).useContents {
				if (error == 0U) {
					return this.count
				}
				throwIoe(error)
			}
		}
	}

	public actual fun interruptRead() {
		val error = mosaic_test_interrupt_read(ptr)
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
