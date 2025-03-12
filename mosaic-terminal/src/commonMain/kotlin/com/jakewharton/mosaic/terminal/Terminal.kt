package com.jakewharton.mosaic.terminal

import com.jakewharton.finalization.withFinalizationHook
import com.jakewharton.mosaic.terminal.event.CapabilityQueryEvent
import com.jakewharton.mosaic.terminal.event.DecModeReportEvent
import com.jakewharton.mosaic.terminal.event.DecModeReportEvent.Setting
import com.jakewharton.mosaic.terminal.event.Event
import com.jakewharton.mosaic.terminal.event.FocusEvent
import com.jakewharton.mosaic.terminal.event.KeyboardEvent
import com.jakewharton.mosaic.terminal.event.KittyGraphicsEvent
import com.jakewharton.mosaic.terminal.event.KittyKeyboardQueryEvent
import com.jakewharton.mosaic.terminal.event.KittyNotificationEvent
import com.jakewharton.mosaic.terminal.event.KittyPointerQueryEvent
import com.jakewharton.mosaic.terminal.event.OperatingStatusResponseEvent
import com.jakewharton.mosaic.terminal.event.PrimaryDeviceAttributesEvent
import com.jakewharton.mosaic.terminal.event.ResizeEvent
import com.jakewharton.mosaic.terminal.event.SystemThemeEvent
import com.jakewharton.mosaic.tty.Tty
import dev.drewhamilton.poko.Poko
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineStart.UNDISPATCHED
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.channels.BufferOverflow.DROP_OLDEST
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.Channel.Factory.UNLIMITED
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull

public interface Terminal {
	public val state: State
	public val capabilities: Capabilities
	public val keyEvents: Channel<KeyboardEvent>

	public interface State {
		public val focused: StateFlow<Boolean>
		public val systemTheme: StateFlow<Boolean>
		public val size: StateFlow<Size>
	}

	public interface Capabilities {
		public val ansiLevel: AnsiLevel
		public val kittyKeyboard: Boolean
		public val kittyUnderline: Boolean
		public val kittyGraphics: Boolean
		public val kittyNotifications: Boolean
		public val kittyPointerShape: Boolean
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
}

private class TtyTerminal(
	override val state: Terminal.State,
	override val capabilities: Terminal.Capabilities,
	override val keyEvents: Channel<KeyboardEvent>,
) : Terminal {
	class State(
		override val focused: StateFlow<Boolean>,
		override val systemTheme: StateFlow<Boolean>,
		override val size: StateFlow<Terminal.Size>,
	) : Terminal.State

	class Capabilities(
		override val ansiLevel: AnsiLevel,
		override val kittyKeyboard: Boolean,
		override val kittyUnderline: Boolean,
		override val kittyGraphics: Boolean,
		override val kittyNotifications: Boolean,
		override val kittyPointerShape: Boolean,
		override val synchronizedRendering: Boolean,
	) : Terminal.Capabilities
}

private const val DebugBootstrap = false
private const val StageDeviceAttributes = 3
private const val StageCapabilityQueries = 2
private const val StageDefaultQueries = 1
private const val StageNormalOperation = 0

public suspend fun Tty.asTerminal(block: suspend (Terminal) -> Unit) {
	val events = Channel<Event>(UNLIMITED)

	setCallback(EventChannelTtyCallback(events, false))

	// Each of these will become true when their respective feature is recognized by the terminal
	// and was not already configured to our desired setting. Revert each toggled setting on exit.
	var toggleCursor = false
	var toggleFocus = false
	var toggleInBandResize = false
	var toggleSystemTheme = false

	// Entering raw mode can fail, so perform it before any additional control sequences which change
	// settings. We also need to be in character mode to query capabilities with control sequences.
	// TODO This should also be a parameter.
	if (env("MOSAIC_RAW_MODE") != "false") {
		enableRawMode()
	}

	withFinalizationHook(
		hook = {
			if (toggleSystemTheme) print(systemThemeDisable)
			if (toggleInBandResize) print(inBandResizeDisable)
			if (toggleFocus) print(focusDisable)
			if (toggleCursor) print(cursorEnable)

			close() // TODO We should have a way to reset the Tty without closing.
		},
		block = {
			launch(Dispatchers.IO) {
				val parser = TerminalParser(this@asTerminal)
				while (true) {
					val event = parser.next() ?: break
					events.trySend(event)
				}
			}

			print("${CSI}0c")
			var stage = StageDeviceAttributes

			var supportsSynchronizedRendering = false
			var supportsKittyKeyboard = false
			var supportsKittyGraphics = false
			var supportsKittyNotifications = false
			var supportsKittyPointerShape = false
			var supportsKittyUnderlines = false

			val focused = MutableStateFlow(true)
			val systemTheme = MutableStateFlow(false)
			val size = MutableStateFlow(Terminal.Size.Default)

			val keyEvents = Channel<KeyboardEvent>(64, onBufferOverflow = DROP_OLDEST)

			val bootstrapDone = CompletableDeferred<Unit>()
			val eventJob = launch(start = UNDISPATCHED) {
				try {
					for (event in events) {
						if (DebugBootstrap) {
							if (stage != StageNormalOperation) {
								print("$event\r\n")
							}
						}
						when (event) {
							is PrimaryDeviceAttributesEvent -> {
								if (stage == StageNormalOperation) continue

								if (event.id == 1) {
									// VT100 terminals can't handle most of the other queries so just bail.
									stage = StageNormalOperation
									bootstrapDone.complete(Unit)
									continue
								}

								stage = StageCapabilityQueries
								print(
									"$CSI?${cursorMode}\$p" +
										"$CSI?${focusMode}\$p" +
										"$CSI?${synchronizedRenderingMode}\$p" +
										"$CSI?${systemThemeMode}\$p" +
										"$CSI?${inBandResizeMode}\$p" +
										"$CSI?u" + // Kitty keyboard
										"${APC}Gi=31,s=1,v=1,a=q,t=d,f=24;AAAA$ST" + // Kitty graphics
										"${OSC}99;i=1:p=?$ST" + // Kitty notifications
										"${OSC}22;?__current__$ST" + // Kitty pointer shape
										"$DCS+q5375$ST" + // Kitty underline ("Su")
										"${CSI}5n", // DSR (end marker)
								)
							}
							is DecModeReportEvent -> {
								if (stage != StageCapabilityQueries) continue

								when (event.mode) {
									cursorMode -> {
										if (event.setting == Setting.Set) {
											toggleCursor = true
											print(cursorDisable)
										}
									}
									focusMode -> {
										if (event.setting == Setting.Reset) {
											toggleFocus = true
											// Enabling focus notification _might_ trigger an initial event. There is
											// otherwise no explicit way to request the initial value.
											print(focusEnable)
										}
									}
									synchronizedRenderingMode -> {
										if (event.setting == Setting.Reset) {
											supportsSynchronizedRendering = true
										}
									}
									systemThemeMode -> {
										if (event.setting == Setting.Reset) {
											toggleSystemTheme = true
											print(
												systemThemeEnable +
													"$CSI?996n", // Current system theme query.
											)
										}
									}
									inBandResizeMode -> {
										if (event.setting == Setting.Reset) {
											toggleInBandResize = true
											// Enabling in-band resize will trigger an initial event.
											print(inBandResizeEnable)
										}
									}
								}
							}
							is OperatingStatusResponseEvent -> {
								if (stage == StageCapabilityQueries) {
									if (toggleFocus or toggleInBandResize or toggleSystemTheme) {
										// By enabling these modes (or by sending an explicit default value query after
										// enabling the mode) wait for a reply about the default with a second DSR.
										stage = StageDefaultQueries
										print("${CSI}5n")
									} else {
										stage = StageNormalOperation
										bootstrapDone.complete(Unit)
									}
								} else if (stage == StageDefaultQueries) {
									bootstrapDone.complete(Unit)
								}
							}
							is KittyKeyboardQueryEvent -> {
								if (stage == StageCapabilityQueries) {
									supportsKittyKeyboard = true
								}
							}
							is KittyGraphicsEvent -> {
								if (stage == StageCapabilityQueries) {
									supportsKittyGraphics = true
								}
							}
							is KittyPointerQueryEvent -> {
								if (stage == StageCapabilityQueries) {
									supportsKittyPointerShape = true
								}
							}
							is KittyNotificationEvent -> {
								if (stage == StageCapabilityQueries) {
									supportsKittyNotifications = true
								}
							}
							is CapabilityQueryEvent -> {
								if (stage == StageCapabilityQueries && event.success) {
									if ("Su" in event.data) {
										supportsKittyUnderlines = true
									}
								}
							}

							is FocusEvent -> {
								focused.value = event.focused
							}
							is KeyboardEvent -> {
								keyEvents.trySend(event)
							}
							is ResizeEvent -> {
								size.value = Terminal.Size(event.columns, event.rows, event.width, event.height)
							}
							is SystemThemeEvent -> {
								systemTheme.value = event.isDark
							}

							else -> {}
						}
					}
				} finally {
					// When cancelled (from signal or normally), wake up the reader parse loop so it can exit.
					interruptRead()
				}
			}

			// Spend at most 1 second bootstrapping capabilities and defaults. In theory, there could
			// exist a terminal which does not respond to DA1 or DSR. Does that terminal actually work?
			// Who knows, but we don't want to hang forever waiting. Take whatever we got so far
			// (if anything) and move on with rendering.
			withTimeoutOrNull(1.seconds) {
				bootstrapDone.await()
			}
			stage = StageNormalOperation

			if (DebugBootstrap) {
				print("\r\n")
			}

			if (!toggleInBandResize) {
				currentSize().let { (columns, rows) ->
					size.value = Terminal.Size(columns, rows)
				}
				enableWindowResizeEvents()
			}

			val ansiLevel = detectAnsiLevel()

			val terminal = TtyTerminal(
				state = TtyTerminal.State(
					focused = focused,
					systemTheme = systemTheme,
					size = size,
				),
				capabilities = TtyTerminal.Capabilities(
					ansiLevel = ansiLevel,
					kittyKeyboard = supportsKittyKeyboard,
					kittyUnderline = supportsKittyUnderlines,
					kittyGraphics = supportsKittyGraphics,
					kittyNotifications = supportsKittyNotifications,
					kittyPointerShape = supportsKittyPointerShape,
					synchronizedRendering = supportsSynchronizedRendering,
				),
				keyEvents = keyEvents,
			)

			block(terminal)

			eventJob.cancel()
		},
	)
}

internal fun detectAnsiLevel(): AnsiLevel {
	if (!env("NO_COLOR").isNullOrEmpty()) {
		return AnsiLevel.NONE
	}
	val term = env("COLORTERM") ?: env("TERM") ?: "dumb"
	if (term.contains("24bit", ignoreCase = true) || term.contains("truecolor", ignoreCase = true)) {
		return AnsiLevel.TRUECOLOR
	}
	if (term.contains("256")) {
		return AnsiLevel.ANSI256
	}
	if (term != "dumb") {
		return AnsiLevel.ANSI16
	}
	return AnsiLevel.NONE
}
