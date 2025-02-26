package com.jakewharton.mosaic

import androidx.collection.MutableObjectList
import androidx.collection.mutableObjectListOf
import androidx.collection.mutableScatterSetOf
import androidx.compose.runtime.AbstractApplier
import androidx.compose.runtime.BroadcastFrameClock
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Composition
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.MonotonicFrameClock
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.Recomposer
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.snapshots.ObserverHandle
import androidx.compose.runtime.snapshots.Snapshot
import androidx.compose.runtime.withFrameNanos
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.jakewharton.finalization.withFinalizationHook
import com.jakewharton.mosaic.layout.KeyEvent
import com.jakewharton.mosaic.layout.MosaicNode
import com.jakewharton.mosaic.terminal.TerminalReader
import com.jakewharton.mosaic.terminal.event.CapabilityQueryEvent
import com.jakewharton.mosaic.terminal.event.DecModeReportEvent
import com.jakewharton.mosaic.terminal.event.DecModeReportEvent.Setting
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
import com.jakewharton.mosaic.ui.AnsiLevel
import com.jakewharton.mosaic.ui.BoxMeasurePolicy
import com.jakewharton.mosaic.ui.unit.IntSize
import kotlin.concurrent.Volatile
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.coroutineContext
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart.UNDISPATCHED
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.Channel.Factory.UNLIMITED
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeoutOrNull

public fun renderMosaic(content: @Composable () -> Unit): String {
	val mosaicComposition = MosaicComposition(
		coroutineContext = BroadcastFrameClock(),
		onDraw = {},
		keyEvents = Channel(),
		terminalState = mutableStateOf(Terminal.Default),
	)
	mosaicComposition.setContent(content)
	mosaicComposition.cancel()
	return createRendering().render(mosaicComposition).toString()
}

public fun runMosaicBlocking(content: @Composable () -> Unit) {
	runBlocking {
		runMosaic(content)
	}
}

private const val DebugBootstrap = false
private const val StageDeviceAttributes = 3
private const val StageCapabilityQueries = 2
private const val StageDefaultQueries = 1
private const val StageNormalOperation = 0

public suspend fun runMosaic(content: @Composable () -> Unit) {
	val reader = TerminalReader()

	// Entering raw mode can fail, so perform it before any additional control sequences which change
	// settings. We also need to be in character mode to query capabilities with control sequences.
	if (env("MOSAIC_RAW_MODE") != "false") {
		reader.enableRawMode()
	}

	// Each of these will become true when their respective feature is recognized by the terminal
	// and was not already configured to our desired setting. Revert each toggled setting on exit.
	var toggleCursor = false
	var toggleFocus = false
	var toggleInBandResize = false
	var toggleSystemTheme = false

	withFinalizationHook(
		hook = {
			if (toggleSystemTheme) print(systemThemeDisable)
			if (toggleInBandResize) print(inBandResizeDisable)
			if (toggleFocus) print(focusDisable)
			if (toggleCursor) print(cursorEnable)
			reader.close()
		},
		block = {
			launch(Dispatchers.IO) {
				reader.runParseLoop()
			}

			val keyEvents = Channel<KeyEvent>(UNLIMITED)
			val terminalState = mutableStateOf(Terminal.Default)

			print("${CSI}0c")
			var stage = StageDeviceAttributes

			var supportsSynchronizedRendering = false
			// TODO Use these to alter the capabilities of the corresponding nodes.
			var supportsKittyKeyboard = false
			var supportsKittyGraphics = false
			var supportsKittyNotifications = false
			var supportsKittyPointerShape = false
			var supportsKittyUnderlines = false

			val bootstrapDone = CompletableDeferred<Unit>()
			val eventJob = launch(start = UNDISPATCHED) {
				try {
					for (event in reader.events) {
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
									stage = StageNormalOperation
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
								terminalState.update { copy(focused = event.focused) }
							}
							is KeyboardEvent -> {
								event.toKeyEventOrNull()?.let {
									keyEvents.trySend(it)
								}
							}
							is ResizeEvent -> {
								terminalState.update { copy(size = IntSize(event.columns, event.rows)) }
							}
							is SystemThemeEvent -> {
								terminalState.update { copy(darkTheme = event.isDark) }
							}

							else -> {}
						}
					}
				} finally {
					// When cancelled (from signal or normally), wake up the reader parse loop so it can exit.
					reader.interrupt()
				}
			}

			// Spend at most 1 second bootstrapping capabilities and defaults. In theory, there could
			// exist a terminal which does not respond to DA1 or DSR. Does that terminal actually work?
			// Who knows, but we don't want to hang forever waiting. Take whatever we got so far
			// (if anything) and move on with rendering.
			withTimeoutOrNull(1.seconds) {
				bootstrapDone.await()
			}
			if (DebugBootstrap) {
				print("\r\n")
			}

			if (!toggleInBandResize) {
				terminalState.update {
					copy(
						size = reader.currentSize().let { size ->
							IntSize(size.columns, size.rows)
						},
					)
				}
				reader.enableWindowResizeEvents()
			}

			val rendering = createRendering(
				ansiLevel = detectAnsiLevel(),
				synchronizedRendering = supportsSynchronizedRendering,
			)

			runMosaicComposition(rendering, keyEvents, terminalState, content)

			eventJob.cancel()
		},
	)
}

internal suspend fun runMosaicComposition(
	rendering: Rendering,
	keyEvents: Channel<KeyEvent>,
	terminalState: MutableState<Terminal>,
	content: @Composable (() -> Unit),
) {
	val clock = BroadcastFrameClock()
	val mosaicComposition = MosaicComposition(
		coroutineContext = coroutineContext + clock,
		onDraw = { rootNode ->
			print(rendering.render(rootNode).toString())
		},
		keyEvents = keyEvents,
		terminalState = terminalState,
	)

	mosaicComposition.setContent(content)

	mosaicComposition.scope.launch {
		while (true) {
			clock.sendFrame(nanoTime())

			// "1000 FPS should be enough for anybody"
			// We need to yield in order for other coroutines on this dispatcher to run, otherwise
			// this is effectively a spin loop. We do a delay instead of a yield since dispatchers
			// are not required to support yield, but reasonable delay support is almost a guarantee.
			delay(1)
		}
	}

	mosaicComposition.awaitComplete()
}

internal inline fun <T> MutableState<T>.update(updater: T.() -> T) {
	value = value.updater()
}

private fun createRendering(
	ansiLevel: AnsiLevel = AnsiLevel.TRUECOLOR,
	synchronizedRendering: Boolean = false,
): Rendering {
	return if (env("MOSAIC_DEBUG_RENDERING") == "true") {
		DebugRendering(ansiLevel = ansiLevel)
	} else {
		AnsiRendering(ansiLevel, synchronizedRendering)
	}
}

public interface Mosaic {
	public fun setContent(content: @Composable () -> Unit)

	public fun paint(): TextCanvas
	public fun paintStaticsTo(list: MutableObjectList<TextCanvas>)
	public fun paintStatics(): List<TextCanvas> {
		return mutableObjectListOf<TextCanvas>()
			.apply(::paintStaticsTo)
			.asList()
	}

	public fun dump(): String

	public suspend fun awaitComplete()
	public fun cancel()
}

// TODO This function signature is all kinds of broken for structured concurrency!
public fun Mosaic(
	coroutineContext: CoroutineContext,
	onDraw: (Mosaic) -> Unit,
	keyEvents: Channel<KeyEvent>,
	terminalState: State<Terminal>,
): Mosaic {
	return MosaicComposition(coroutineContext, onDraw, keyEvents, terminalState)
}

internal class MosaicComposition(
	coroutineContext: CoroutineContext,
	private val onDraw: (Mosaic) -> Unit,
	private val keyEvents: Channel<KeyEvent>,
	private val terminalState: State<Terminal>,
) : Mosaic,
	LifecycleOwner {
	private val externalClock = checkNotNull(coroutineContext[MonotonicFrameClock]) {
		"Mosaic requires an external MonotonicFrameClock in its coroutine context"
	}
	private val internalClock = BroadcastFrameClock()

	private val job = Job(coroutineContext[Job])
	private val composeContext = coroutineContext + job + internalClock
	val scope = CoroutineScope(composeContext)

	private val applier = MosaicNodeApplier { needLayout = true }
	val rootNode = applier.root
	private val recomposer = Recomposer(composeContext)
	private val composition = Composition(applier, recomposer)

	override val lifecycle = LifecycleRegistry.createUnsafe(this).also { lifecycle ->
		scope.launch(start = UNDISPATCHED) {
			snapshotFlow { terminalState.value.focused }.collect { focused ->
				lifecycle.handleLifecycleEvent(
					if (focused) Lifecycle.Event.ON_RESUME else Lifecycle.Event.ON_PAUSE,
				)
			}
		}
	}

	private val applyObserverHandle: ObserverHandle

	private val readingStatesOnLayout = mutableScatterSetOf<Any>()
	private val readingStatesOnDraw = mutableScatterSetOf<Any>()

	private val layoutBlockStateReadObserver: (Any) -> Unit = readingStatesOnLayout::add
	private val drawBlockStateReadObserver: (Any) -> Unit = readingStatesOnDraw::add

	@Volatile
	private var needLayout = false

	@Volatile
	private var needDraw = false

	init {
		GlobalSnapshotManager().ensureStarted(scope)
		startRecomposer()
		startFrameListener()
		applyObserverHandle = registerSnapshotApplyObserver()
	}

	private fun performLayout() {
		needLayout = false
		Snapshot.observe(readObserver = layoutBlockStateReadObserver) {
			rootNode.measureAndPlace()
		}
		performDraw()
	}

	private fun performDraw() {
		needDraw = false
		onDraw(this)
	}

	override fun paint(): TextCanvas {
		return Snapshot.observe(readObserver = drawBlockStateReadObserver) {
			rootNode.paint()
		}
	}

	override fun paintStaticsTo(list: MutableObjectList<TextCanvas>) {
		rootNode.paintStaticsTo(list)
	}

	override fun dump(): String {
		return rootNode.toString()
	}

	private fun registerSnapshotApplyObserver(): ObserverHandle {
		return Snapshot.registerApplyObserver { changedStates, _ ->
			if (!needLayout) {
				var setDraw = needDraw
				for (state in changedStates) {
					if (state in readingStatesOnLayout) {
						needLayout = true
						break
					}
					if (setDraw) {
						continue
					}
					if (state in readingStatesOnDraw) {
						setDraw = true
						needDraw = true
					}
				}
			}
		}
	}

	private fun startRecomposer() {
		scope.launch(start = UNDISPATCHED) {
			recomposer.runRecomposeAndApplyChanges()
		}
	}

	private fun startFrameListener() {
		scope.launch(start = UNDISPATCHED) {
			val ctrlC = KeyEvent("c", ctrl = true)

			do {
				externalClock.withFrameNanos { nanos ->
					// Drain any pending key events before triggering the frame.
					while (true) {
						val keyEvent = keyEvents.tryReceive().getOrNull() ?: break
						val keyHandled = rootNode.sendKeyEvent(keyEvent)
						if (!keyHandled && keyEvent == ctrlC) {
							job.cancel()
							return@withFrameNanos
						}
					}

					internalClock.sendFrame(nanos)

					if (needLayout) {
						performLayout()
					} else if (needDraw) {
						performDraw()
					}
				}
			} while (job.isActive)
		}
	}

	override fun setContent(content: @Composable () -> Unit) {
		composition.setContent {
			CompositionLocalProvider(
				LocalTerminal provides terminalState.value,
				LocalLifecycleOwner provides this,
				content = content,
			)
		}
		performLayout()
	}

	override suspend fun awaitComplete() {
		try {
			val effectJob = checkNotNull(recomposer.effectCoroutineContext[Job]) {
				"No Job in effectCoroutineContext of recomposer"
			}
			effectJob.children.forEach { it.join() }
			recomposer.awaitIdle()

			applyObserverHandle.dispose()
			if (needLayout || needDraw) {
				awaitFrame()
			}

			recomposer.close()
			recomposer.join()
		} finally {
			applyObserverHandle.dispose() // if canceled before dispose in the try block
			job.cancel()
		}
	}

	override fun cancel() {
		applyObserverHandle.dispose()
		recomposer.cancel()
		job.cancel()
	}

	private suspend fun awaitFrame() {
		scope.launch { withFrameNanos { } }.join()
	}
}

internal class MosaicNodeApplier(
	root: MosaicNode? = null,
	private val onChanges: () -> Unit = {},
) : AbstractApplier<MosaicNode>(
	root = root ?: MosaicNode(
		measurePolicy = BoxMeasurePolicy(),
		debugPolicy = { children.joinToString(separator = "\n") },
		isStatic = false,
	),
) {
	override fun onBeginChanges() {
		super.onBeginChanges()
		// We invoke this here rather than in the end change callback to try and ensure
		// no one relies on it to signal the end of changes.
		onChanges.invoke()
	}

	override fun insertTopDown(index: Int, instance: MosaicNode) {
		// Ignored, we insert bottom-up.
	}

	override fun insertBottomUp(index: Int, instance: MosaicNode) {
		current.children.add(index, instance)
	}

	override fun remove(index: Int, count: Int) {
		current.children.remove(index, count)
	}

	override fun move(from: Int, to: Int, count: Int) {
		current.children.move(from, to, count)
	}

	override fun onClear() {
	}
}

internal class GlobalSnapshotManager {
	private val started = atomicBooleanOf(false)
	private val sent = atomicBooleanOf(false)

	fun ensureStarted(scope: CoroutineScope) {
		if (started.compareAndSet(expect = false, update = true)) {
			val channel = Channel<Unit>(1)
			scope.launch {
				channel.consumeEach {
					sent.set(false)
					Snapshot.sendApplyNotifications()
				}
			}
			Snapshot.registerGlobalWriteObserver {
				if (sent.compareAndSet(expect = false, update = true)) {
					channel.trySend(Unit)
				}
			}
		}
	}
}
