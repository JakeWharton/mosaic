package com.jakewharton.mosaic

import com.jakewharton.mosaic.terminal.AnsiLevel
import com.jakewharton.mosaic.terminal.Event
import com.jakewharton.mosaic.terminal.Terminal
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/** Behaviors when there is no interactive TTY. */
public enum class NonInteractivePolicy {
	/** Print a non-interactive message to stderr and exit the process with a non-zero code. */
	Exit,

	/** Throw an [IllegalStateException] with a non-interactive message. */
	Throw,

	/** Immediately return false. */
	Return,

	/** Continue running with a fake [Terminal] with default state, no capabilities, and no events. */
	Ignore,

	/** Assume the absence of a TTY without checking, and proceed using [Ignore]. */
	AssumeAndIgnore,
}

internal object NonInteractiveTerminal : Terminal, Terminal.State, Terminal.Capabilities {
	override val name: String? get() = null
	override val state: Terminal.State get() = this
	override val capabilities: Terminal.Capabilities get() = this
	override val events: ReceiveChannel<Event> = Channel<Event>().apply { close() }

	override val focused: StateFlow<Boolean> = MutableStateFlow(true)
	override val theme: StateFlow<Terminal.Theme> = MutableStateFlow(Terminal.Theme.Unknown)
	override val size: StateFlow<Terminal.Size> = MutableStateFlow(Terminal.Size.Default)

	override val interactive: Boolean get() = false
	override val ansiLevel: AnsiLevel get() = AnsiLevel.NONE
	override val kittyGraphics get() = false
	override val kittyKeyboard get() = false
	override val kittyNotifications get() = false
	override val kittyPointerShape get() = false
	override val kittyTextSizingScale get() = false
	override val kittyTextSizingWidth get() = false
	override val kittyUnderline get() = false
	override val synchronizedRendering get() = false

	override fun close() {}
}

internal const val NonInteractiveMessage = "Unable to run in non-interactive mode."
