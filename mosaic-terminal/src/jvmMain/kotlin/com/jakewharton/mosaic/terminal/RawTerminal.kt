package com.jakewharton.mosaic.terminal

public actual class RawTerminal(
	private val ptr: Long,
) : AutoCloseable {
	public actual fun read(bytes: ByteArray, offset: Int, count: Int): Int = TODO()
	public actual fun read(bytes: ByteArray, offset: Int, count: Int, timeoutMillis: Int): Int = TODO()
	public actual fun interruptRead(): Unit = TODO()

	public actual fun writeOutput(bytes: ByteArray, offset: Int, count: Int): Int = TODO()
	public actual fun writeError(bytes: ByteArray, offset: Int, count: Int): Int = TODO()

	public actual fun enableRawMode(): Unit = TODO()

	// public actual fun enableStandardStreamRedirection(): Unit = TODO()

	public actual fun enableNativeResizeEvents(): Unit = TODO()
	public actual fun currentSize(): IntArray = TODO()

	actual override fun close(): Unit = TODO()

	public actual interface EventCallback {
		public actual fun onFocus(focused: Boolean)
		public actual fun onKey()
		public actual fun onMouse()
		public actual fun onResize(columns: Int, rows: Int, width: Int, height: Int)
		// public actual fun onStandardOutput(bytes: ByteArray)
		// public actual fun onStandardError(bytes: ByteArray)
	}

	public actual companion object {
		@JvmStatic
		public actual fun install(callback: EventCallback): RawTerminal = TODO()
	}
}
