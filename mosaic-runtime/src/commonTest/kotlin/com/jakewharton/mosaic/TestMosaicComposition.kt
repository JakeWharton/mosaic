package com.jakewharton.mosaic

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

	private var contentSet = false

	/** Channel with the most recent snapshot, if any. */
	private val snapshots = Channel<NodeRenderSnapshot>(CONFLATED)

	private val rendering: Rendering = AnsiRendering(
		ansiLevel = if (withAnsi) AnsiLevel.TRUECOLOR else AnsiLevel.NONE,
	)

	private val terminalState: MutableState<Terminal> = mutableStateOf(
		Terminal(size = initialTerminalSize),
	)

	private val keyEvents = Channel<KeyEvent>(UNLIMITED)

	val mosaicComposition = MosaicComposition(coroutineContext, terminalState, keyEvents) { rootNode ->
		val stringRender = if (withAnsi) {
			rendering.render(rootNode).toString()
		} else {
			rendering.render(rootNode).toString()
				.removeSurrounding(ansiBeginSynchronizedUpdate, ansiEndSynchronizedUpdate)
				.removeSuffix("\r\n") // without last line break for simplicity
				.replace(clearLine, "")
				.replace(cursorUp, "")
				.replace("\r\n", "\n") // CRLF to LF for simplicity
		}
		snapshots.trySend(NodeRenderSnapshot(rootNode, stringRender))
	}

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

		// Await at least one change, sending frames while we wait.
		return withTimeout(duration) {
			val sendFramesJob = with(mosaicComposition) { sendFrames() }
			try {
				snapshots.receive()
			} finally {
				sendFramesJob.cancel()
			}
		}
	}
}
