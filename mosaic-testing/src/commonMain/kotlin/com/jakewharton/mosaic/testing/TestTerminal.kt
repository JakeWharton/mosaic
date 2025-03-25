package com.jakewharton.mosaic.testing

import com.jakewharton.mosaic.terminal.AnsiLevel
import com.jakewharton.mosaic.terminal.Event
import com.jakewharton.mosaic.terminal.Terminal
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.Channel.Factory.UNLIMITED
import kotlinx.coroutines.flow.MutableStateFlow

public class TestTerminal(
	override val name: String? = "Mosaic Test Terminal",
	override val capabilities: Terminal.Capabilities = Capabilities(),
) : Terminal {
	override val state: State = State()
	override val events: Channel<Event> = Channel(UNLIMITED)

	override fun close() {
		events.close()
	}

	public class State : Terminal.State {
		override val focused: MutableStateFlow<Boolean> = MutableStateFlow(true)
		override val theme: MutableStateFlow<Terminal.Theme> = MutableStateFlow(Terminal.Theme.Unknown)
		override val size: MutableStateFlow<Terminal.Size> = MutableStateFlow(Terminal.Size.Default)
	}

	public class Capabilities(
		override val interactive: Boolean = true,
		override val ansiLevel: AnsiLevel = AnsiLevel.TRUECOLOR,
		override val kittyKeyboard: Boolean = true,
		override val kittyUnderline: Boolean = true,
		override val kittyGraphics: Boolean = true,
		override val kittyNotifications: Boolean = true,
		override val kittyPointerShape: Boolean = true,
		override val synchronizedRendering: Boolean = true,
	) : Terminal.Capabilities
}
