package com.jakewharton.mosaic.terminal

internal expect fun PlatformInputWriter(): PlatformInputWriter

internal expect class PlatformInputWriter : AutoCloseable {
	val input: PlatformInput

	fun terminalReader(emitDebugEvents: Boolean = false): TerminalReader

	// TODO Take ByteString once it migrates to stdlib,
	//  or if Sink/RawSink migrates expose that as a val.
	//  https://github.com/Kotlin/kotlinx-io/issues/354
	fun write(buffer: ByteArray)

	fun focusEvent(focused: Boolean)
	fun keyEvent()
	fun mouseEvent()
	fun resizeEvent(columns: Int, rows: Int, width: Int, height: Int)

	override fun close()
}
