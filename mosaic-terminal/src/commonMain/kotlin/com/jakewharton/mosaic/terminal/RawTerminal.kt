package com.jakewharton.mosaic.terminal

public expect class RawTerminal : AutoCloseable {
	public fun read(bytes: ByteArray, offset: Int, count: Int): Int
	public fun read(bytes: ByteArray, offset: Int, count: Int, timeoutMillis: Int): Int
	public fun interruptRead()

	public fun writeOutput(bytes: ByteArray, offset: Int, count: Int): Int
	public fun writeError(bytes: ByteArray, offset: Int, count: Int): Int

	public fun enableRawMode()

	// public fun enableStandardStreamRedirection()

	public fun enableNativeResizeEvents()
	public fun currentSize(): IntArray

	override fun close()

	public interface EventCallback {
		public fun onFocus(focused: Boolean)
		public fun onKey() // TODO
		public fun onMouse() // TODO
		public fun onResize(columns: Int, rows: Int, width: Int, height: Int)
		// public actual fun onStandardOutput(bytes: ByteArray)
		// public actual fun onStandardError(bytes: ByteArray)
	}

	public companion object {
		public fun initialize(callback: EventCallback): RawTerminal
	}
}
