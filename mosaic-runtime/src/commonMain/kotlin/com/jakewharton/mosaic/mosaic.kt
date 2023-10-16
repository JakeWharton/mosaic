package com.jakewharton.mosaic

import androidx.compose.runtime.AbstractApplier
import androidx.compose.runtime.BroadcastFrameClock
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Composition
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Recomposer
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshots.Snapshot
import com.github.ajalt.mordant.terminal.Terminal
import com.jakewharton.mosaic.layout.MosaicNode
import com.jakewharton.mosaic.ui.BoxMeasurePolicy
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart.UNDISPATCHED
import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.yield
import kotlin.time.ExperimentalTime

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

public interface MosaicScope : CoroutineScope {
	public fun setContent(content: @Composable () -> Unit)
}

public suspend fun runMosaic(body: suspend MosaicScope.() -> Unit): Unit = coroutineScope {
	val rendering = if (debugOutput) {
		@OptIn(ExperimentalTime::class) // Not used in production.
		DebugRendering()
	} else {
		AnsiRendering()
	}

	var hasFrameWaiters = false
	val clock = BroadcastFrameClock {
		hasFrameWaiters = true
	}

	val job = Job(coroutineContext[Job])
	val composeContext = coroutineContext + clock + job

	val rootNode = createRootNode()
	var displaySignal: CompletableDeferred<Unit>? = null
	val applier = MosaicNodeApplier(rootNode) {
		val render = rendering.render(rootNode)
		platformDisplay(render)

		displaySignal?.complete(Unit)
		hasFrameWaiters = false
	}

	val recomposer = Recomposer(composeContext)
	val composition = Composition(applier, recomposer)

	// Start undispatched to ensure we can use suspending things inside the content.
	launch(start = UNDISPATCHED, context = composeContext) {
		recomposer.runRecomposeAndApplyChanges()
	}

	launch(context = composeContext) {
		while (true) {
			clock.sendFrame(0L) // Frame time value is not used by Compose runtime.
			delay(50)
		}
	}

	val terminal = Terminal()
	val terminalInfo = mutableStateOf(
		TerminalInfo(
			size = TerminalInfo.Size(terminal.info.width, terminal.info.height)
		)
	)

	launch(context = composeContext) {
		while (true) {
			if (terminal.info.updateTerminalSize()
				&& (terminalInfo.value.size.width != terminal.info.width
					|| terminalInfo.value.size.height != terminal.info.height)
			) {
				terminalInfo.value = terminalInfo.value.copy(
					size = TerminalInfo.Size(terminal.info.width, terminal.info.height)
				)
			}
			delay(50)
		}
	}

	coroutineScope {
		val scope = object : MosaicScope, CoroutineScope by this {
			override fun setContent(content: @Composable () -> Unit) {
				composition.setContent {
					CompositionLocalProvider(Terminal provides terminalInfo.value) {
						content()
					}
				}
			}
		}

		var snapshotNotificationsPending = false
		val observer: (state: Any) -> Unit = {
			if (!snapshotNotificationsPending) {
				snapshotNotificationsPending = true
				launch {
					snapshotNotificationsPending = false
					Snapshot.sendApplyNotifications()
				}
			}
		}
		val snapshotObserverHandle = Snapshot.registerGlobalWriteObserver(observer)
		try {
			scope.body()
		} finally {
			snapshotObserverHandle.dispose()
		}
	}

	// Ensure the final state modification is discovered. We need to ensure that the coroutine
	// which is running the recomposition loop wakes up, notices the changes, and waits for the
	// next frame. If you are using snapshots this only requires a single yield. If you are not
	// then it requires two yields. THIS IS NOT GREAT! But at least it's implementation detail...
	// TODO https://issuetracker.google.com/issues/169425431
	yield()
	yield()
	Snapshot.sendApplyNotifications()
	yield()
	yield()

	if (hasFrameWaiters) {
		CompletableDeferred<Unit>().also {
			displaySignal = it
			it.await()
		}
	}

	job.cancel()
	composition.dispose()
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
