package com.jakewharton.mosaic

import androidx.collection.mutableScatterSetOf
import androidx.compose.runtime.AbstractApplier
import androidx.compose.runtime.BroadcastFrameClock
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Composition
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.MonotonicFrameClock
import androidx.compose.runtime.Recomposer
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshots.ObserverHandle
import androidx.compose.runtime.snapshots.Snapshot
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.jakewharton.mosaic.NonInteractivePolicy.AssumeAndIgnore
import com.jakewharton.mosaic.NonInteractivePolicy.Exit
import com.jakewharton.mosaic.NonInteractivePolicy.Ignore
import com.jakewharton.mosaic.NonInteractivePolicy.Return
import com.jakewharton.mosaic.NonInteractivePolicy.Throw
import com.jakewharton.mosaic.layout.KeyEvent
import com.jakewharton.mosaic.layout.MosaicNode
import com.jakewharton.mosaic.terminal.KeyboardEvent
import com.jakewharton.mosaic.terminal.Terminal
import com.jakewharton.mosaic.tty.Tty
import com.jakewharton.mosaic.tty.terminal.asTerminalIn
import com.jakewharton.mosaic.ui.BoxMeasurePolicy
import kotlin.concurrent.Volatile
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.coroutineContext
import kotlin.time.TimeSource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart.UNDISPATCHED
import kotlinx.coroutines.Dispatchers.Unconfined
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.Channel.Factory.UNLIMITED
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

public fun runMosaicMain(
	content: @Composable () -> Unit,
) {
	runMosaicBlocking(content = content)
}

public fun runMosaicBlocking(
	onNonInteractive: NonInteractivePolicy = Exit,
	content: @Composable () -> Unit,
): Boolean {
	return runBlocking {
		runMosaic(onNonInteractive, content)
	}
}

public suspend fun runMosaic(
	onNonInteractive: NonInteractivePolicy = Exit,
	content: @Composable () -> Unit,
): Boolean = coroutineScope {
	val terminal = if (onNonInteractive != AssumeAndIgnore) {
		Tty.tryBind()
			?.asTerminalIn(this)
			?: when (onNonInteractive) {
				Exit -> nonInteractiveExit()
				Throw -> throw IllegalStateException(NonInteractiveMessage)
				Return -> return@coroutineScope false
				Ignore -> NonInteractiveTerminal
				AssumeAndIgnore -> throw AssertionError()
			}
	} else {
		NonInteractiveTerminal
	}

	terminal.use { terminal ->
		val rendering = if (env("MOSAIC_DEBUG_RENDERING") == "true") {
			DebugRendering(
				ansiLevel = terminal.capabilities.ansiLevel,
				supportsKittyUnderlines = terminal.capabilities.kittyUnderline,
				systemClock = TimeSource.Monotonic,
			)
		} else {
			AnsiRendering(
				ansiLevel = terminal.capabilities.ansiLevel,
				synchronizedOutput = terminal.capabilities.synchronizedOutput,
				supportsKittyUnderlines = terminal.capabilities.kittyUnderline,
			)
		}

		runMosaicComposition(terminal, rendering, content)
	}

	true
}

internal suspend fun runMosaicComposition(
	terminal: Terminal,
	rendering: Rendering,
	content: @Composable () -> Unit,
) {
	val clock = BroadcastFrameClock()
	val mosaicComposition = MosaicComposition(
		coroutineContext = coroutineContext + clock,
		onDraw = { rootNode ->
			print(rendering.render(rootNode).toString())
		},
		terminal = terminal,
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

public interface Mosaic {
	public fun setContent(content: @Composable () -> Unit)

	public fun draw(): TextCanvas
	public fun static(): String?
	public fun dumpNodes(): String

	public suspend fun awaitComplete()
	public fun cancel()
}

public fun Mosaic(
	coroutineContext: CoroutineContext,
	onDraw: (Mosaic) -> Unit,
	terminal: Terminal,
): Mosaic {
	return MosaicComposition(coroutineContext, onDraw, terminal)
}

internal class MosaicComposition(
	coroutineContext: CoroutineContext,
	private val onDraw: (Mosaic) -> Unit,
	private val terminal: Terminal,
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

	private val staticLogs = Channel<Any>(UNLIMITED)
	private val staticLogger = StaticLogger(staticLogs) { needDraw = true }

	override val lifecycle = LifecycleRegistry.createUnsafe(this)

	private var terminalState = mutableStateOf(
		TerminalState(
			terminal.state.focused.value,
			terminal.state.theme.value,
			terminal.state.size.value,
		),
	).also { state ->
		scope.launch(Unconfined, start = UNDISPATCHED) {
			terminal.state.focused.collect { focused ->
				state.value = state.value.copy(focused = focused)

				lifecycle.handleLifecycleEvent(
					if (focused) Lifecycle.Event.ON_RESUME else Lifecycle.Event.ON_PAUSE,
				)
			}
		}
		scope.launch(Unconfined, start = UNDISPATCHED) {
			terminal.state.theme.collect { theme ->
				state.value = state.value.copy(theme = theme)
			}
		}
		scope.launch(Unconfined, start = UNDISPATCHED) {
			terminal.state.size.collect { size ->
				state.value = state.value.copy(size = size)
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

	override fun draw(): TextCanvas {
		return Snapshot.observe(readObserver = drawBlockStateReadObserver) {
			rootNode.draw()
		}
	}

	override fun static(): String? {
		var static = staticLogs.tryReceive().getOrNull()
		if (static == null) {
			return null
		}
		return buildString {
			do {
				when (static) {
					is String -> {
						append(static)
						append("\r\n")
					}
					is TextCanvas -> {
						for (row in 0 until static.height) {
							static.appendRowTo(
								appendable = this,
								row = row,
								ansiLevel = terminal.capabilities.ansiLevel,
								supportsKittyUnderlines = terminal.capabilities.kittyUnderline,
							)
							append("\r\n")
						}
					}
				}

				static = staticLogs.tryReceive().getOrNull()
			} while (static != null)

			// Remove trailing "\r\n".
			setLength(length - 2)
		}
	}

	override fun dumpNodes(): String {
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
						val event = terminal.events.tryReceive().getOrNull() ?: break
						if (event !is KeyboardEvent) continue
						val keyEvent = event.toKeyEventOrNull() ?: continue
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
				LocalTerminalState provides terminalState.value,
				LocalStaticLogger provides staticLogger,
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
				// TODO Re-evaluate this for correctness.
				scope.launch { internalClock.withFrameNanos { } }.join()
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
}

internal class MosaicNodeApplier(
	private val onChanges: () -> Unit = {},
) : AbstractApplier<MosaicNode>(
	root = MosaicNode(
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
