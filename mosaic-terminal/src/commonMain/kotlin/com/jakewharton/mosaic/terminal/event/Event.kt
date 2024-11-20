package com.jakewharton.mosaic.terminal.event

public sealed interface Event

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

internal data class CodepointEvent(
	val codepoint: Int,
	val shift: Boolean = false,
	val alt: Boolean = false,
	val ctrl: Boolean = false,
) : Event {
	override fun toString() = buildString {
		append("CodepointEvent(")
		if (shift) append("Shift+")
		if (ctrl) append("Ctrl+")
		if (alt) append("Alt+")
		append("0x")
		append(codepoint.toString(16).uppercase())
		append(')')
	}

	companion object {
		// These codepoints are defined by Kitty in the Unicode private space.
		const val Insert = 57348
		const val Delete = 57349
		const val PageUp = 57354
		const val PageDown = 57355
		const val Up = 57352
		const val Down = 57353
		const val Right = 57351
		const val Left = 57350
		const val KpBegin = 57427
		const val End = 57357
		const val Home = 57356
		const val F1 = 57364
		const val F2 = 57365
		const val F3 = 57366
		const val F4 = 57367
		const val F5 = 57368
		const val F6 = 57369
		const val F7 = 57370
		const val F8 = 57371
		const val F9 = 57372
		const val F10 = 57373
		const val F11 = 57374
		const val F12 = 57375
	}
}

internal data class FocusEvent(
	val focused: Boolean,
) : Event

internal data class PasteEvent(
	val start: Boolean,
) : Event

internal data class PrimaryDeviceAttributes(
	val data: String,
) : Event

internal data class DeviceStatusReportString(
	val data: String,
) : Event

internal data class KittyGraphicsEvent(
	val id: Int,
	val message: String,
) : Event

internal data class ResizeEvent(
	val rows: Int,
	val cols: Int,
	val height: Int,
	val width: Int,
) : Event

internal data class DecModeReport(
	val mode: Int,
	val setting: Setting,
) : Event {
	enum class Setting {
		NotRecognized,
		Set,
		Reset,
		PermanentlySet,
		PermanentlyReset,
	}
}

internal data class MouseEvent(
	val x: Int,
	val y: Int,
	val type: Type,
	val button: Button,
	val shift: Boolean,
	val alt: Boolean,
	val ctrl: Boolean,
) : Event {
	enum class Type {
		Drag,
		Motion,
		Release,
		Press,
	}
	enum class Button {
		Left,
		Middle,
		Right,
		None,
		WheelUp,
		WheelDown,
		Button8,
		Button9,
		Button10,
		Button11,
	}
}

internal data class LinePositionAbsolute(
	val row: Int,
) : Event

internal data class LinePositionRelative(
	val rows: Int,
) : Event
