package com.jakewharton.mosaic

import androidx.compose.runtime.AbstractApplier
import androidx.compose.runtime.BroadcastFrameClock
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Composition
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.Recomposer
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshots.Snapshot
import com.github.ajalt.mordant.terminal.Terminal as MordantTerminal
import com.jakewharton.mosaic.layout.MosaicNode
import com.jakewharton.mosaic.ui.BoxMeasurePolicy
import com.jakewharton.mosaic.ui.unit.IntSize
import kotlin.coroutines.CoroutineContext
import kotlin.time.ExperimentalTime
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * True for a debug-like output that renders each "frame" on its own with a timestamp delta.
 * False when using ANSI control sequences to overwrite output.
 */
private const val debugOutput = false

internal fun mosaicNodes(content: @Composable () -> Unit): MosaicNode {
	val clock = BroadcastFrameClock()
	val job = Job()
	val composeContext = clock + job

	val rootNode = createRootNode()
	val recomposer = Recomposer(composeContext)
	val composition = Composition(MosaicNodeApplier(rootNode), recomposer)

	composition.setContent(content)

	job.cancel()
	composition.dispose()

	return rootNode
}

public fun renderMosaic(content: @Composable () -> Unit): String {
	val rootNode = mosaicNodes(content)
	val render = AnsiRendering().render(rootNode)
	return render.toString()
}

public suspend fun runMosaic(content: @Composable () -> Unit) {
	coroutineScope {
		val mosaicComposition = MosaicComposition(coroutineScope = this, onRender = ::platformDisplay)
		mosaicComposition.setContent(content)
		mosaicComposition.awaitComplete()
	}
}

internal class MosaicComposition(
	coroutineScope: CoroutineScope,
	override val onRender: (CharSequence) -> Unit,
) : BaseMosaicComposition(coroutineScope) {
	private val terminal = MordantTerminal()

	override val rendering: Rendering = if (debugOutput) {
		@OptIn(ExperimentalTime::class) // Not used in production.
		DebugRendering(ansiLevel = terminal.info.ansiLevel.toMosaicAnsiLevel())
	} else {
		AnsiRendering(ansiLevel = terminal.info.ansiLevel.toMosaicAnsiLevel())
	}

	override val terminalInfo: MutableState<Terminal> =
		mutableStateOf(Terminal(size = IntSize(terminal.info.width, terminal.info.height)))

	init {
		coroutineScope.sendFrames()
		coroutineScope.updateTerminalInfo()
	}

	private fun CoroutineScope.updateTerminalInfo() {
		launch(composeContext) {
			while (true) {
				val currentTerminalInfo = terminalInfo.value
				if (terminal.info.updateTerminalSize() &&
					(
						currentTerminalInfo.size.width != terminal.info.width ||
							currentTerminalInfo.size.height != terminal.info.height
						)
				) {
					terminalInfo.value = Terminal(size = IntSize(terminal.info.width, terminal.info.height))
				}
				delay(50L)
			}
		}
	}
}

internal abstract class BaseMosaicComposition(coroutineScope: CoroutineScope) {
	protected abstract val onRender: (CharSequence) -> Unit
	protected abstract val rendering: Rendering

	private val job = Job(coroutineScope.coroutineContext[Job])
	private val clock = BroadcastFrameClock()
	protected val composeContext: CoroutineContext = coroutineScope.coroutineContext + job + clock

	private val rootNode = createRootNode()
	private val applier = MosaicNodeApplier(rootNode) { onRender(rendering.render(rootNode)) }
	private val recomposer = Recomposer(composeContext)
	private val composition = Composition(applier, recomposer)

	protected abstract val terminalInfo: MutableState<Terminal>

	init {
		startGlobalSnapshotManager()
		coroutineScope.startRecomposer()
	}

	private fun startGlobalSnapshotManager() {
		GlobalSnapshotManager().ensureStarted(composeContext)
	}

	private fun CoroutineScope.startRecomposer() {
		launch(composeContext, start = CoroutineStart.UNDISPATCHED) {
			recomposer.runRecomposeAndApplyChanges()
		}
	}

	protected fun CoroutineScope.sendFrames(): Job {
		return launch(composeContext) {
			while (true) {
				clock.sendFrame(0L) // Frame time value is not used by Compose runtime.
				delay(50L)
			}
		}
	}

	fun setContent(content: @Composable () -> Unit) {
		composition.setContent {
			CompositionLocalProvider(LocalTerminal provides terminalInfo.value) {
				content()
			}
		}
	}

	suspend fun awaitComplete() {
		try {
			val effectJob = checkNotNull(recomposer.effectCoroutineContext[Job]) {
				"No Job in effectCoroutineContext of recomposer"
			}
			effectJob.children.forEach { it.join() }
			recomposer.awaitIdle()

			recomposer.close()
			recomposer.join()
		} finally {
			job.cancel()
		}
	}
}

internal fun createRootNode(): MosaicNode {
	return MosaicNode(
		measurePolicy = BoxMeasurePolicy(),
		debugPolicy = { children.joinToString(separator = "\n") },
		onStaticDraw = null,
	)
}

internal class MosaicNodeApplier(
	root: MosaicNode,
	private val onEndChanges: () -> Unit = {},
) : AbstractApplier<MosaicNode>(root) {
	override fun onEndChanges() {
		onEndChanges.invoke()
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
	private val started = AtomicBoolean(false)
	private val sent = AtomicBoolean(false)

	fun ensureStarted(coroutineContext: CoroutineContext) {
		if (started.compareAndSet(expect = false, update = true)) {
			val channel = Channel<Unit>(1)
			CoroutineScope(coroutineContext).launch {
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
