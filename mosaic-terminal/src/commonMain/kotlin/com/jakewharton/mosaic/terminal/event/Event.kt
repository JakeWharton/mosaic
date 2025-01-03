package com.jakewharton.mosaic.terminal.event

import com.jakewharton.cite.__TYPE__
import dev.drewhamilton.poko.Poko
import dev.drewhamilton.poko.Poko.ReadArrayContent

public sealed interface Event

@Poko
public class UnknownEvent(
	// TODO ByteString once it moves into the stdlib.
	@ReadArrayContent public val bytes: ByteArray,
) : Event {
	override fun toString(): String = buildString {
		append(__TYPE__)
		append('(')
		for (byte in bytes) {
			append(byte.toUByte().toString(16).padStart(2, '0'))
		}
		append(')')
	}
}

@Poko
public class KeyboardEvent(
	public val codepoint: Int,
	public val shiftedCodepoint: Int = -1,
	public val baseLayoutCodepoint: Int = -1,
	public val modifiers: Int = 0,
	public val eventType: Int = EventTypePress,
	public val text: String? = null,
) : Event {
	public val shift: Boolean get() = (modifiers and ModifierShift) != 0
	public val alt: Boolean get() = (modifiers and ModifierAlt) != 0
	public val ctrl: Boolean get() = (modifiers and ModifierCtrl) != 0
	public val `super`: Boolean get() = (modifiers and ModifierSuper) != 0
	public val hyper: Boolean get() = (modifiers and ModifierHyper) != 0
	public val meta: Boolean get() = (modifiers and ModifierMeta) != 0
	public val capsLock: Boolean get() = (modifiers and ModifierCapsLock) != 0
	public val numLock: Boolean get() = (modifiers and ModifierNumLock) != 0

	public companion object {
		public const val ModifierShift: Int = 0b1
		public const val ModifierAlt: Int = 0b10
		public const val ModifierCtrl: Int = 0b100
		public const val ModifierSuper: Int = 0b1000
		public const val ModifierHyper: Int = 0b10000
		public const val ModifierMeta: Int = 0b100000
		public const val ModifierCapsLock: Int = 0b1000000
		public const val ModifierNumLock: Int = 0b10000000

		public const val EventTypePress: Int = 1
		public const val EventTypeRepeat: Int = 2
		public const val EventTypeRelease: Int = 3

		// These codepoints are defined by Kitty in the Unicode private space.
		internal const val Insert = 57348
		internal const val Delete = 57349
		internal const val PageUp = 57354
		internal const val PageDown = 57355
		internal const val Up = 57352
		internal const val Down = 57353
		internal const val Right = 57351
		internal const val Left = 57350
		internal const val KpBegin = 57427
		internal const val End = 57357
		internal const val Home = 57356
		internal const val F1 = 57364
		internal const val F2 = 57365
		internal const val F3 = 57366
		internal const val F4 = 57367
		internal const val F5 = 57368
		internal const val F6 = 57369
		internal const val F7 = 57370
		internal const val F8 = 57371
		internal const val F9 = 57372
		internal const val F10 = 57373
		internal const val F11 = 57374
		internal const val F12 = 57375
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

@Poko
public class ResizeEvent(
	public val rows: Int,
	public val columns: Int,
	public val height: Int,
	public val width: Int,
) : Event

@Poko
public class XtermPixelSizeEvent(
	public val height: Int,
	public val width: Int,
) : Event

@Poko
public class XtermCharacterSizeEvent(
	public val rows: Int,
	public val columns: Int,
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
	val button: Button = Button.None,
	val shift: Boolean = false,
	val alt: Boolean = false,
	val ctrl: Boolean = false,
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
