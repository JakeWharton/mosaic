package com.jakewharton.mosaic.tty

import kotlinx.cinterop.CPointer
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.useContents
import kotlinx.cinterop.usePinned

public actual class TestTty private constructor(
	private var ptr: CPointer<MosaicTestTty>?,
	public actual val tty: Tty,
) : AutoCloseable {
	public actual companion object {
		public actual fun create(): TestTty {
			val testTtyPtr = testTty_init().useContents {
				testTty?.let { return@useContents it }

				if (error != 0U) {
					throwIoe(error)
				}
				throw OutOfMemoryError()
			}

			val ttyPtr = testTty_getTty(testTtyPtr)!!
			val tty = Tty(ttyPtr)
			return TestTty(testTtyPtr, tty)
		}
	}

	public actual fun write(buffer: ByteArray, offset: Int, count: Int): Int {
		buffer.asUByteArray().usePinned {
			testTty_write(ptr, it.addressOf(offset), count).useContents {
				if (error == 0U) {
					return this.count
				}
				throwIoe(error)
			}
		}
	}

	public actual fun read(buffer: ByteArray, offset: Int, count: Int): Int {
		buffer.asUByteArray().usePinned {
			testTty_read(ptr, it.addressOf(offset), count).useContents {
				if (error == 0U) {
					return this.count
				}
				throwIoe(error)
			}
		}
	}

	public actual fun interruptRead() {
		val error = testTty_interruptRead(ptr)
		if (error != 0U) {
			throwIoe(error)
		}
	}

	public actual fun focusEvent(focused: Boolean) {
		val error = testTty_focusEvent(ptr, focused)
		if (error != 0U) {
			throwIoe(error)
		}
	}

	public actual fun keyEvent() {
		val error = testTty_keyEvent(ptr)
		if (error != 0U) {
			throwIoe(error)
		}
	}

	public actual fun mouseEvent() {
		val error = testTty_mouseEvent(ptr)
		if (error != 0U) {
			throwIoe(error)
		}
	}

	public actual fun resizeEvent(columns: Int, rows: Int, width: Int, height: Int) {
		val error = testTty_resizeEvent(ptr, columns, rows, width, height)
		if (error != 0U) {
			throwIoe(error)
		}
	}

	actual override fun close() {
		ptr?.let { ref ->
			this.ptr = null

			tty.close()

			val error = testTty_free(ref)

			if (error == 0U) return
			throwIoe(error)
		}
	}
}
