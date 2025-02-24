package com.jakewharton.mosaic.tty

public expect class TestTty : AutoCloseable {
	public companion object {
		public fun create(callback: Tty.Callback): TestTty
	}

	public val tty: Tty

	// TODO Take ByteString once it migrates to stdlib,
	//  or if Sink/RawSink migrates expose that as a val.
	//  https://github.com/Kotlin/kotlinx-io/issues/354
	public fun write(buffer: ByteArray)

	public fun focusEvent(focused: Boolean)
	public fun keyEvent()
	public fun mouseEvent()
	public fun resizeEvent(columns: Int, rows: Int, width: Int, height: Int)

	override fun close()
}
