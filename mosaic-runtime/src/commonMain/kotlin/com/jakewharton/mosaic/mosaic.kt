package com.jakewharton.mosaic

import androidx.collection.mutableScatterSetOf
import androidx.compose.runtime.AbstractApplier
import androidx.compose.runtime.BroadcastFrameClock
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Composition
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.Recomposer
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshots.ObserverHandle
import androidx.compose.runtime.snapshots.Snapshot
import androidx.compose.runtime.withFrameNanos
import com.github.ajalt.mordant.input.RawModeScope
import com.github.ajalt.mordant.input.enterRawMode
import com.github.ajalt.mordant.platform.MultiplatformSystem
import com.github.ajalt.mordant.terminal.Terminal as MordantTerminal
import com.jakewharton.finalization.withFinalizationHook
import com.jakewharton.mosaic.layout.KeyEvent
import com.jakewharton.mosaic.layout.MosaicNode
import com.jakewharton.mosaic.ui.AnsiLevel
import com.jakewharton.mosaic.ui.BoxMeasurePolicy
import com.jakewharton.mosaic.ui.unit.IntSize
import kotlin.concurrent.Volatile
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.ExperimentalTime
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.Channel.Factory.UNLIMITED
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

/**
 * True for a debug-like output that renders each "frame" on its own with a timestamp delta.
 * False when using ANSI control sequences to overwrite output.
 */
private const val debugOutput = false

internal fun renderMosaicNode(content: @Composable () -> Unit): MosaicNode {
	val mosaicComposition = MosaicComposition(
		coroutineScope = CoroutineScope(EmptyCoroutineContext),
		terminalState = MordantTerminal().toMutableState(),
		keyEvents = Channel(),
		onDraw = {},
	)
	mosaicComposition.setContent(content)
	mosaicComposition.cancel()
	return mosaicComposition.rootNode
}

public fun renderMosaic(content: @Composable () -> Unit): String {
	return createRendering().render(renderMosaicNode(content)).toString()
}

public fun runMosaicBlocking(content: @Composable () -> Unit) {
	runBlocking {
		runMosaic(content)
	}
}

public suspend fun runMosaic(content: @Composable () -> Unit) {
	runMosaic(enterRawMode = true, content)
}

internal suspend fun runMosaic(enterRawMode: Boolean, content: @Composable () -> Unit) {
	val mordantTerminal = MordantTerminal()
	val rendering = createRendering(mordantTerminal.terminalInfo.ansiLevel.toMosaicAnsiLevel())
	val terminalState = mordantTerminal.toMutableState()
	val keyEvents = Channel<KeyEvent>(UNLIMITED)

	val rawMode = if (enterRawMode && MultiplatformSystem.readEnvironmentVariable("MOSAIC_RAW_MODE") != "false") {
		// In theory this call could fail, so perform it before any additional control sequences.
		mordantTerminal.enterRawMode()
	} else {
		null
	}

	platformDisplay(cursorHide)

	withFinalizationHook(
		hook = {
			platformDisplay(cursorShow)
			rawMode?.close()
		},
		block = {
			val mosaicComposition = MosaicComposition(
				coroutineScope = this,
				terminalState = terminalState,
				keyEvents = keyEvents,
				onDraw = { rootNode ->
					platformDisplay(rendering.render(rootNode))
				},
			)
			mosaicComposition.sendFrames()
			mosaicComposition.scope.updateTerminalInfo(mordantTerminal, terminalState)
			rawMode?.let { rawMode ->
				mosaicComposition.scope.readRawModeKeys(rawMode, keyEvents)
			}
			mosaicComposition.setContent(content)
			mosaicComposition.awaitComplete()
		},
	)
}

private fun MordantTerminal.toMutableState(): MutableState<Terminal> {
	return mutableStateOf(
		Terminal(size = IntSize(size.width, size.height)),
	)
}

private fun createRendering(ansiLevel: AnsiLevel = AnsiLevel.TRUECOLOR): Rendering {
	return if (debugOutput) {
		@OptIn(ExperimentalTime::class) // Not used in production.
		DebugRendering(ansiLevel = ansiLevel)
	} else {
		AnsiRendering(ansiLevel = ansiLevel)
	}
}

private fun CoroutineScope.updateTerminalInfo(terminal: MordantTerminal, terminalInfo: MutableState<Terminal>) {
	launch {
		while (true) {
			val currentTerminalInfo = terminalInfo.value
			val newSize = terminal.updateSize()
			if (currentTerminalInfo.size.width != newSize.width ||
				currentTerminalInfo.size.height != newSize.height
			) {
				terminalInfo.value = Terminal(size = IntSize(newSize.width, newSize.height))
			}
			delay(50L)
		}
	}
}

private fun CoroutineScope.readRawModeKeys(rawMode: RawModeScope, keyEvents: Channel<KeyEvent>) {
	launch(Dispatchers.IO) {
		while (isActive) {
			val keyboardEvent = rawMode.readKeyOrNull(10.milliseconds) ?: continue
			val keyEvent = KeyEvent(
				key = keyboardEvent.key,
				alt = keyboardEvent.alt,
				ctrl = keyboardEvent.ctrl,
				shift = keyboardEvent.shift,
			)
			keyEvents.trySend(keyEvent)
		}
	}
}

internal class MosaicComposition(
	coroutineScope: CoroutineScope,
	private val terminalState: State<Terminal>,
	private val keyEvents: ReceiveChannel<KeyEvent>,
	private val onDraw: (MosaicNode) -> Unit,
) {
	private val job = Job(coroutineScope.coroutineContext[Job])
	private val clock = BroadcastFrameClock()
	private val composeContext: CoroutineContext = coroutineScope.coroutineContext + job + clock
	val scope = CoroutineScope(composeContext)

	private val applier = MosaicNodeApplier { needLayout = true }
	val rootNode = applier.root
	private val recomposer = Recomposer(composeContext)
	private val composition = Composition(applier, recomposer)

	private val applyObserverHandle: ObserverHandle

	private val readingStatesOnLayout = mutableScatterSetOf<Any>()
	private val readingStatesOnDraw = mutableScatterSetOf<Any>()

	private val layoutBlockStateReadObserver: (Any) -> Unit = { readingStatesOnLayout.add(it) }
	private val drawBlockStateReadObserver: (Any) -> Unit = { readingStatesOnDraw.add(it) }

	@Volatile
	private var needLayout = false

	@Volatile
	private var needDraw = false

	init {
		GlobalSnapshotManager().ensureStarted(scope)
		startRecomposer()
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
		Snapshot.observe(readObserver = drawBlockStateReadObserver) {
			onDraw(rootNode)
		}
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
		scope.launch(start = CoroutineStart.UNDISPATCHED) {
			recomposer.runRecomposeAndApplyChanges()
		}
	}

	fun sendFrames(): Job {
		return scope.launch {
			val ctrlC = KeyEvent("c", ctrl = true)

			while (true) {
				// Drain any pending key events before triggering the frame.
				keyEvents.tryReceive().getOrNull()?.let { keyEvent ->
					val keyHandled = rootNode.sendKeyEvent(keyEvent)
					if (!keyHandled && keyEvent == ctrlC) {
						cancel()
					}
					continue
				}

				clock.sendFrame(nanoTime())

				if (needLayout) {
					performLayout()
				} else if (needDraw) {
					performDraw()
				}

				// "1000 FPS should be enough for anybody"
				// We need to yield in order for other coroutines on this dispatcher to run, otherwise
				// this is effectively a spin loop. We do a delay instead of a yield since dispatchers
				// are not required to support yield, but reasonable delay support is almost a guarantee.
				delay(1)
			}
		}
	}

	fun setContent(content: @Composable () -> Unit) {
		composition.setContent {
			CompositionLocalProvider(LocalTerminal provides terminalState.value) {
				content()
			}
		}
		performLayout()
	}

	suspend fun awaitComplete() {
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

	fun cancel() {
		applyObserverHandle.dispose()
		recomposer.cancel()
		job.cancel()
	}

	private suspend fun awaitFrame() {
		scope.launch { withFrameNanos { } }.join()
	}
}

internal class MosaicNodeApplier(
	private val onChanges: () -> Unit = {},
) : AbstractApplier<MosaicNode>(
	root = MosaicNode(
		measurePolicy = BoxMeasurePolicy(),
		debugPolicy = { children.joinToString(separator = "\n") },
		onStaticDraw = null,
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

	override fun onClear() {}
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
