package com.jakewharton.mosaic.testing

import com.jakewharton.mosaic.terminal.AnsiLevel
import com.jakewharton.mosaic.terminal.Terminal
import com.jakewharton.mosaic.terminal.event.KeyboardEvent
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow

internal class TestTerminal(
	override val state: Terminal.State,
	override val capabilities: Terminal.Capabilities,
	override val keyEvents: Channel<KeyboardEvent>,
) : Terminal {
	class State : TestState {
		override val focused = MutableStateFlow(true)
		override val systemTheme = MutableStateFlow(false)
		override val size = MutableStateFlow(Terminal.Size.Default)
	}
}

public class TestCapabilities(
	override val ansiLevel: AnsiLevel = AnsiLevel.TRUECOLOR,
	override val kittyKeyboard: Boolean = true,
	override val kittyUnderline: Boolean = true,
	override val kittyGraphics: Boolean = true,
	override val kittyNotifications: Boolean = true,
	override val kittyPointerShape: Boolean = true,
	override val synchronizedRendering: Boolean = true,
) : Terminal.Capabilities

public interface TestState : Terminal.State {
	override val focused: MutableStateFlow<Boolean>
	override val systemTheme: MutableStateFlow<Boolean>
	override val size: MutableStateFlow<Terminal.Size>
}
