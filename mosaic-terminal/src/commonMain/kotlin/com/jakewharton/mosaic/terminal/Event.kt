package com.jakewharton.mosaic.terminal

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
public class DebugEvent(
	public val event: Event,
	// TODO ByteString once it moves into the stdlib.
	/**
	 * The bytes which produced [event]. This will be empty if the event was produced from something
	 * other than bytes, such as direct platform integration.
	 */
	@ReadArrayContent public val bytes: ByteArray,
) : Event

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
		public const val Insert: Int = 57348
		public const val Delete: Int = 57349
		public const val PageUp: Int = 57354
		public const val PageDown: Int = 57355
		public const val Up: Int = 57352
		public const val Down: Int = 57353
		public const val Right: Int = 57351
		public const val Left: Int = 57350
		public const val KpBegin: Int = 57427
		public const val End: Int = 57357
		public const val Home: Int = 57356
		public const val F1: Int = 57364
		public const val F2: Int = 57365
		public const val F3: Int = 57366
		public const val F4: Int = 57367
		public const val F5: Int = 57368
		public const val F6: Int = 57369
		public const val F7: Int = 57370
		public const val F8: Int = 57371
		public const val F9: Int = 57372
		public const val F10: Int = 57373
		public const val F11: Int = 57374
		public const val F12: Int = 57375
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
	public val id: Int,
	public val data: String,
) : Event

@Poko
public class SecondaryDeviceAttributesEvent(
	/**
	 * Possible values (non-exhaustive):
	 * - 0: VT100
	 * - 1: VT220
	 * - 2: VT240 or VT241
	 * - 18: VT330
	 * - 19: VT340
	 * - 24: VT320
	 * - 32: VT382
	 * - 41: VT420
	 * - 61: VT510
	 * - 64: VT520
	 * - 65: VT525
	 */
	public val type: Int,
	public val firmwareVersion: Int,
	public val registrationNumber: Int,
) : Event

@Poko
public class TertiaryDeviceAttributesEvent(
	public val manufacturingSite: Int,
	public val terminalId: Int,
) : Event

@Poko
public class OperatingStatusResponseEvent(
	public val ok: Boolean,
) : Event

@Poko
public class CursorPositionEvent(
	public val row: Int,
	public val column: Int,
) : Event

@Poko
public class CapabilityQueryEvent(
	public val success: Boolean,
	public val data: Map<String, String?>,
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
public class KittyNotificationEvent(
	public val raw: String,
) : Event

public sealed interface KittyPointerQueryEvent : Event

@Poko
public class KittyPointerQueryNameEvent(
	public val name: String,
) : KittyPointerQueryEvent

@Poko
public class KittyPointerQuerySupportEvent(
	@ReadArrayContent public val values: BooleanArray,
) : KittyPointerQueryEvent

@Poko
public class TerminalVersionEvent(
	public val data: String,
) : Event

@Poko
public class SystemThemeEvent(
	public val isDark: Boolean,
) : Event

@Poko
public class KittyGraphicsEvent(
	public val id: Int,
	public val message: String,
) : Event

@Poko
public class ResizeEvent(
	public val columns: Int,
	public val rows: Int,
	public val width: Int,
	public val height: Int,
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

@Poko
public class MouseEvent(
	public val x: Int,
	public val y: Int,
	public val type: Type,
	public val button: Button = Button.None,
	public val shift: Boolean = false,
	public val alt: Boolean = false,
	public val ctrl: Boolean = false,
) : Event {
	public enum class Type {
		Drag,
		Motion,
		Release,
		Press,
	}
	public enum class Button {
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
