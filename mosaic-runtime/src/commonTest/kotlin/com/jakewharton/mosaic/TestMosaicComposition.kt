package com.jakewharton.mosaic

import androidx.compose.runtime.BroadcastFrameClock
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import com.jakewharton.mosaic.TestMosaicComposition.NodeRenderSnapshot
import com.jakewharton.mosaic.layout.KeyEvent
import com.jakewharton.mosaic.layout.MosaicNode
import com.jakewharton.mosaic.ui.AnsiLevel
import com.jakewharton.mosaic.ui.unit.IntSize
import kotlin.coroutines.CoroutineContext
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.Channel.Factory.CONFLATED
import kotlinx.coroutines.channels.Channel.Factory.UNLIMITED
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.withTimeout

private val DefaultTestTerminalSize = IntSize(80, 20)

internal suspend fun runMosaicTest(
	withAnsi: Boolean = false,
	initialTerminalSize: IntSize = DefaultTestTerminalSize,
	block: suspend TestMosaicComposition.() -> Unit,
) {
	coroutineScope {
		val testMosaicComposition = RealTestMosaicComposition(
			coroutineContext = coroutineContext,
			withAnsi = withAnsi,
			initialTerminalSize = initialTerminalSize,
		)
		block.invoke(testMosaicComposition)
		testMosaicComposition.mosaicComposition.cancel()
	}
}

internal interface TestMosaicComposition {
	fun setContent(content: @Composable () -> Unit)

	fun changeTerminalSize(width: Int, height: Int)

	fun sendKeyEvent(keyEvent: KeyEvent)

	suspend fun awaitNodeSnapshot(duration: Duration = 1.seconds): MosaicNode

	suspend fun awaitRenderSnapshot(duration: Duration = 1.seconds): String

	suspend fun awaitNodeRenderSnapshot(duration: Duration = 1.seconds): NodeRenderSnapshot

	data class NodeRenderSnapshot(val node: MosaicNode, val render: String)
}

private class RealTestMosaicComposition(
	coroutineContext: CoroutineContext,
	withAnsi: Boolean,
	initialTerminalSize: IntSize,
) : TestMosaicComposition {

	private var timeNanos = 0L
	private val frameDelay = 1.seconds / 60
	private var contentSet = false
	private var hasChanges = false

	/** Channel with the most recent snapshot, if any. */
	private val snapshots = Channel<NodeRenderSnapshot>(CONFLATED)

	private val terminalState: MutableState<Terminal> = mutableStateOf(
		Terminal(size = initialTerminalSize),
	)

	private val keyEvents = Channel<KeyEvent>(UNLIMITED)

	private val clock = BroadcastFrameClock()
	val mosaicComposition = MosaicComposition(
		coroutineContext = coroutineContext + clock,
		terminalState = terminalState,
		keyEvents = keyEvents,
		onDraw = { rootNode ->
			val ansiLevel = if (withAnsi) AnsiLevel.TRUECOLOR else AnsiLevel.NONE
			val stringRender = rootNode.paint().render(ansiLevel)
			snapshots.trySend(NodeRenderSnapshot(rootNode, stringRender))
			hasChanges = true
		},
	)

	override fun setContent(content: @Composable () -> Unit) {
		contentSet = true
		mosaicComposition.setContent(content)
	}

	override fun changeTerminalSize(width: Int, height: Int) {
		terminalState.value = Terminal(size = IntSize(width, height))
	}

	override fun sendKeyEvent(keyEvent: KeyEvent) {
		keyEvents.trySend(keyEvent)
	}

	override suspend fun awaitNodeSnapshot(duration: Duration): MosaicNode {
		return awaitNodeRenderSnapshot(duration).node
	}

	override suspend fun awaitRenderSnapshot(duration: Duration): String {
		return awaitNodeRenderSnapshot(duration).render
	}

	override suspend fun awaitNodeRenderSnapshot(duration: Duration): NodeRenderSnapshot {
		check(contentSet) { "setContent must be called first!" }

		// Await changes, sending at least one frame while we wait.
		withTimeout(duration) {
			while (true) {
				clock.sendFrame(timeNanos)
				if (hasChanges) break

				timeNanos += frameDelay.inWholeNanoseconds
				delay(frameDelay)
			}
		}

		hasChanges = false
		return snapshots.receive()
	}
}
