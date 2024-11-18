package com.jakewharton.mosaic.terminal.event

internal sealed interface Event

// Some temporary events while we spin up parsing...

internal class UnknownEvent(
	val context: String,
	val bytes: ByteArray,
) : Event {
	@OptIn(ExperimentalStdlibApi::class)
	override fun toString(): String {
		return buildString {
			append("UnknownEvent(")
			append(context)
			append(' ')
			append(bytes.toHexString())
			append(')')
		}
	}
}

internal object KeyEscape : Event
