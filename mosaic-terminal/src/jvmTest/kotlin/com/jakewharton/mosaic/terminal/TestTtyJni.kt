package com.jakewharton.mosaic.terminal

import com.jakewharton.mosaic.terminal.TtyJni.loadNativeLibrary

internal object TestTtyJni {
	init {
		// Ensure the main library is loaded first.
		TtyJni

		loadNativeLibrary("mosaic-test-tty")
	}

	@JvmStatic
	external fun platformInputWriterInit(handlerPtr: Long): Long

	@JvmStatic
	external fun platformInputWriterGetPlatformInput(writerPtr: Long): Long

	@JvmStatic
	external fun platformInputWriterWrite(writerPtr: Long, buffer: ByteArray)

	@JvmStatic
	external fun platformInputWriterFocusEvent(writerPtr: Long, focused: Boolean)

	@JvmStatic
	external fun platformInputWriterKeyEvent(writerPtr: Long)

	@JvmStatic
	external fun platformInputWriterMouseEvent(writerPtr: Long)

	@JvmStatic
	external fun platformInputWriterResizeEvent(
		writerPtr: Long,
		columns: Int,
		rows: Int,
		width: Int,
		height: Int,
	)

	@JvmStatic
	external fun platformInputWriterFree(writerPtr: Long)
}
