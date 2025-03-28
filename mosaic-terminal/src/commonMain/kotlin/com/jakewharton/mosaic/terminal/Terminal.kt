package com.jakewharton.mosaic.terminal

import dev.drewhamilton.poko.Poko
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.flow.StateFlow

public interface Terminal : AutoCloseable {
	public val name: String?
	public val interactive: Boolean

	public val state: State
	public val capabilities: Capabilities
	public val events: ReceiveChannel<Event>

	public interface State {
		/**
		 * True when this terminal surface has focus, false when it does not.
		 * The default is true, and will only change if [Capabilities.focusEvents] is true.
		 *
		 */
		public val focused: StateFlow<Boolean>

		/**
		 * Indicates whether the color palette ("theme") of this terminal surface is light, dark, or
		 * unknown. The default is unknown, and will only change if [Capabilities.themeEvents] is true.
		 *
		 * This is not the theme of the operating system (OS) / window manager (WM). While a terminal
		 * emulator may match the OS/WM theme, it is not required to do so. The value only reflects
		 * the terminal surface of the running application.
		 */
		public val theme: StateFlow<Theme>

		/**
		 * The size of the terminal surface in both monospace cells and, optionally, pixels.
		 * Updates to the size can be sourced from multiple locations depending on whether
		 * [Capabilities.inBandResizeEvents] is true and native operating system API support.
		 *
		 * If [interactive] is false, the default is [Size.Default].
		 */
		public val size: StateFlow<Size>
	}

	public interface Capabilities {
		public val ansiLevel: AnsiLevel

		/**
		 * True when cursor visibility control (mode 25) is supported by this terminal.
		 *
		 * See: https://vt100.net/docs/vt510-rm/DECTCEM.html
		 */
		public val cursorVisibility: Boolean

		/**
		 * True when focus events (mode 1004) are supported by this terminal.
		 * These are enabled automatically when supported, and will cause [State.focused] to change.
		 *
		 * See: https://invisible-island.net/xterm/ctlseqs/ctlseqs.html#h3-FocusIn_FocusOut
		 */
		public val focusEvents: Boolean

		/**
		 * True when in-band resize events (mode 2048) are supported by this terminal. These are enabled
		 * automatically when supported to power [State.size].
		 *
		 * See: https://gist.github.com/rockorager/e695fb2924d36b2bcf1fff4a3704bd83
		 */
		public val inBandResizeEvents: Boolean

		/**
		 * True when the Kitty image protocol is recognized by this terminal.
		 *
		 * Note: The terminal is not guaranteed to share a file system with the application when this is
		 * true. Support for loading from the file system needs to be queried separately (and manually).
		 *
		 * See: https://sw.kovidgoyal.net/kitty/graphics-protocol/
		 */
		public val kittyGraphics: Boolean

		/**
		 * True when the Kitty keyboard protocol is supported by this terminal.
		 *
		 * See: https://sw.kovidgoyal.net/kitty/keyboard-protocol/
		 */
		public val kittyKeyboard: Boolean

		/**
		 * True when the Kitty notification protocol is supported by this terminal. Not all features
		 * are guaranteed to be supported by the terminal emulator and/or the operating system / window
		 * manager.
		 *
		 * See: https://sw.kovidgoyal.net/kitty/desktop-notifications/
		 */
		public val kittyNotifications: Boolean

		/**
		 * True when the Kitty pointer shape protocol is supported by this terminal.
		 *
		 * See: https://sw.kovidgoyal.net/kitty/pointer-shapes/
		 */
		public val kittyPointerShape: Boolean

		/**
		 * True when the scale portion of the Kitty text-sizing protocol is supported by this terminal.
		 *
		 * See: https://sw.kovidgoyal.net/kitty/text-sizing-protocol/
		 */
		public val kittyTextSizingScale: Boolean

		/**
		 * True when the width portion of the Kitty text-sizing protocol is supported by this terminal.
		 *
		 * See: https://sw.kovidgoyal.net/kitty/text-sizing-protocol/
		 */
		public val kittyTextSizingWidth: Boolean

		/**
		 * True when the Kitty underline protocol is supported by this terminal.
		 *
		 * See: https://sw.kovidgoyal.net/kitty/underlines/
		 */
		public val kittyUnderline: Boolean

		/**
		 * True when synchronized output (mode 2026) is supported by this terminal. This ensures a set
		 * of commands (usually output-related) are atomically reflected in the rendering. Without this,
		 * updates may be partially rendered and cause a tearing effect.
		 *
		 * See: https://contour-terminal.org/vt-extensions/synchronized-output/
		 */
		public val synchronizedOutput: Boolean

		/**
		 * True when theme-change events (mode 2031) are supported by this terminal.
		 * These are enabled automatically when supported, and will cause [State.theme] to change.
		 *
		 * See: https://contour-terminal.org/vt-extensions/color-palette-update-notifications/
		 */
		public val themeEvents: Boolean
	}

	@Poko
	public class Size(
		public val columns: Int,
		public val rows: Int,
		public val width: Int = 0,
		public val height: Int = 0,
	) {
		public companion object {
			public val Default: Size = Size(80, 24)
		}
	}

	public enum class Theme {
		/** No color palette information is known. */
		Unknown,

		/** Dark text on a light background. */
		Light,

		/** Light text on a dark background. */
		Dark,
	}
}
