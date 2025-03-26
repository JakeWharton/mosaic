package com.jakewharton.mosaic.terminal

import dev.drewhamilton.poko.Poko
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.flow.StateFlow

public interface Terminal : AutoCloseable {
	public val name: String?
	public val state: State
	public val capabilities: Capabilities
	public val events: ReceiveChannel<Event>

	public interface State {
		public val focused: StateFlow<Boolean>
		public val theme: StateFlow<Theme>
		public val size: StateFlow<Size>
	}

	public interface Capabilities {
		public val interactive: Boolean
		public val ansiLevel: AnsiLevel
		public val kittyGraphics: Boolean
		public val kittyKeyboard: Boolean
		public val kittyNotifications: Boolean
		public val kittyPointerShape: Boolean
		public val kittyTextSizingScale: Boolean
		public val kittyTextSizingWidth: Boolean
		public val kittyUnderline: Boolean
		public val synchronizedRendering: Boolean
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
		Unknown,
		Light,
		Dark,
	}
}
