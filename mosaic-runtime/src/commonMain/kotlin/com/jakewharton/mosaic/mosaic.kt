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
import com.jakewharton.mosaic.ui.AnsiLevel
import com.jakewharton.mosaic.ui.BoxMeasurePolicy
import com.jakewharton.mosaic.ui.unit.IntSize
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
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

internal fun renderMosaicNode(content: @Composable () -> Unit): MosaicNode {
	val mosaicComposition = object : MordantTerminalMosaicComposition(
		coroutineScope = CoroutineScope(EmptyCoroutineContext),
	) {
		override val onEndChanges: (MosaicNode) -> Unit = {}
	}
	mosaicComposition.setContent(content)
	mosaicComposition.cancel()
	return mosaicComposition.applier.root
}

public fun renderMosaic(content: @Composable () -> Unit): String {
	return createRendering().render(renderMosaicNode(content)).toString()
}

public suspend fun runMosaic(content: @Composable () -> Unit) {
	coroutineScope {
		val mosaicComposition = RenderingMosaicComposition(
			coroutineScope = this,
			onRender = ::platformDisplay,
		)
		mosaicComposition.setContent(content)
		mosaicComposition.awaitComplete()
	}
}

private class RenderingMosaicComposition(
	coroutineScope: CoroutineScope,
	onRender: (CharSequence) -> Unit,
) : MordantTerminalMosaicComposition(coroutineScope) {

	private val rendering: Rendering = createRendering(terminal.info.ansiLevel.toMosaicAnsiLevel())

	override val onEndChanges: (MosaicNode) -> Unit = { rootNode ->
		onRender(rendering.render(rootNode))
	}

	init {
		coroutineScope.sendFrames()
	}
}

private fun createRendering(ansiLevel: AnsiLevel = AnsiLevel.TRUECOLOR): Rendering {
	return if (debugOutput) {
		@OptIn(ExperimentalTime::class) // Not used in production.
		DebugRendering(ansiLevel = ansiLevel)
	} else {
		AnsiRendering(ansiLevel = ansiLevel)
	}
}

private abstract class MordantTerminalMosaicComposition(
	coroutineScope: CoroutineScope,
) : MosaicComposition(coroutineScope) {

	protected val terminal = MordantTerminal()

	override val terminalInfo: MutableState<Terminal> = mutableStateOf(
		Terminal(size = IntSize(terminal.info.width, terminal.info.height)),
	)

	init {
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

internal abstract class MosaicComposition(coroutineScope: CoroutineScope) {
	protected abstract val onEndChanges: (MosaicNode) -> Unit

	private val job = Job(coroutineScope.coroutineContext[Job])
	private val clock = BroadcastFrameClock()
	protected val composeContext: CoroutineContext = coroutineScope.coroutineContext + job + clock

	val applier = MosaicNodeApplier { rootNode -> onEndChanges(rootNode) }
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

	open fun setContent(content: @Composable () -> Unit) {
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

	fun cancel() {
		recomposer.cancel()
		job.cancel()
	}
}

internal class MosaicNodeApplier(
	private val onEndChanges: (MosaicNode) -> Unit = {},
) : AbstractApplier<MosaicNode>(
	root = MosaicNode(
		measurePolicy = BoxMeasurePolicy(),
		debugPolicy = { children.joinToString(separator = "\n") },
		onStaticDraw = null,
	),
) {
	override fun onEndChanges() {
		onEndChanges.invoke(root)
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

// TestMosaicComposition is located here, not in the commonTest, because task
// ':mosaic-runtime:compileTestKotlinLinuxArm64' fails with the message:
// Refined declaration 'fun setContent(content: @Composable() ComposableFunction0<Unit>): Unit'
// overrides declarations with different or no refinement from: interface TestMosaicComposition : Any
internal interface TestMosaicComposition {

	fun setContent(content: @Composable () -> Unit)

	fun changeTerminalSize(width: Int, height: Int)

	suspend fun awaitNodeSnapshot(duration: Duration = 1.seconds): MosaicNode

	suspend fun awaitRenderSnapshot(duration: Duration = 1.seconds): String

	suspend fun awaitNodeRenderSnapshot(duration: Duration = 1.seconds): NodeRenderSnapshot

	data class NodeRenderSnapshot(val node: MosaicNode, val render: String)
}
