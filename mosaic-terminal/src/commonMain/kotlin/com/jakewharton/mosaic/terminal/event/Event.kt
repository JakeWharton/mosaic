package com.jakewharton.mosaic.terminal.event

import dev.drewhamilton.poko.ArrayContentBased
import dev.drewhamilton.poko.Poko

public sealed interface Event

@Poko
public class UnknownEvent(
	// TODO ByteString once it moves into the stdlib.
	@ArrayContentBased public val bytes: ByteArray,
) : Event {
	override fun toString(): String = buildString {
		append("UnknownEvent(")
		for (byte in bytes) {
			append(byte.toUByte().toString(16).padStart(2, '0'))
		}
		append(')')
	}
}

internal data object KeyEscape : Event

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
		append(codepoint.toString(16).uppercase().padStart(2, '0'))
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

@Poko
public class FocusEvent(
	public val focused: Boolean,
) : Event

@Poko
public class BracketedPasteEvent(
	public val start: Boolean,
) : Event

@Poko
public class PrimaryDeviceAttributesEvent(
	public val data: String,
) : Event

@Poko
public class OperatingStatusResponseEvent(
	public val ok: Boolean,
) : Event

@Poko
public class KittyKeyboardQueryEvent(
	public val flags: Int,
) : Event {
	public val disambiguateEscapeCodes: Boolean get() = (flags and 0b1) != 0
	public val reportEventTypes: Boolean get() = (flags and 0b10) != 0
	public val reportAlternateKeys: Boolean get() = (flags and 0b100) != 0
	public val reportAllKeysAsEscapeCodes: Boolean get() = (flags and 0b1000) != 0
	public val reportAssociatedText: Boolean get() = (flags and 0b10000) != 0
}

@Poko
public class TerminalVersionEvent(
	public val data: String,
) : Event

@Poko
public class SystemThemeEvent(
	public val isDark: Boolean,
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

@Poko
public class DecModeReportEvent(
	public val mode: Int,
	public val setting: Setting,
) : Event {
	public enum class Setting {
		NotRecognized,
		Set,
		Reset,
		PermanentlySet,
		PermanentlyReset,
	}
}

@Poko
public class TerminalColorEvent(
	public val color: Color,
	public val value: String,
) : Event {
	public enum class Color {
		Foreground,
		Background,
		Cursor,
	}
}

@Poko
public class PaletteColorEvent(
	public val color: Int,
	public val value: String,
) : Event

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
