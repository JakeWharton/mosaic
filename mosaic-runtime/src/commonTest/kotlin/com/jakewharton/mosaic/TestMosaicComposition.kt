package com.jakewharton.mosaic

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import com.jakewharton.mosaic.layout.MosaicNode
import com.jakewharton.mosaic.ui.AnsiLevel
import com.jakewharton.mosaic.ui.unit.IntSize
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
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
			coroutineScope = this,
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

	suspend fun awaitNodeSnapshot(duration: Duration = 1.seconds): MosaicNode

	suspend fun awaitRenderSnapshot(duration: Duration = 1.seconds): String

	suspend fun awaitNodeRenderSnapshot(duration: Duration = 1.seconds): NodeRenderSnapshot

	data class NodeRenderSnapshot(val node: MosaicNode, val render: String)
}

private class RealTestMosaicComposition(
	coroutineScope: CoroutineScope,
	withAnsi: Boolean,
	initialTerminalSize: IntSize,
) : TestMosaicComposition {

	private var contentSet = false

	/** Channel with the most recent snapshot, if any. */
	private val nodeSnapshots = Channel<MosaicNode>(Channel.CONFLATED)

	/** Channel with the most recent snapshot, if any. */
	private val renderSnapshots = Channel<String>(Channel.CONFLATED)

	private val rendering: Rendering = AnsiRendering(
		ansiLevel = if (withAnsi) AnsiLevel.TRUECOLOR else AnsiLevel.NONE,
	)

	private val terminalState: MutableState<Terminal> = mutableStateOf(
		Terminal(size = initialTerminalSize),
	)

	val mosaicComposition = MosaicComposition(coroutineScope, terminalState) { rootNode ->
		nodeSnapshots.trySend(rootNode)
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
		renderSnapshots.trySend(stringRender)
	}

	override fun setContent(content: @Composable () -> Unit) {
		contentSet = true
		mosaicComposition.setContent(content)
	}

	override fun changeTerminalSize(width: Int, height: Int) {
		terminalState.value = Terminal(size = IntSize(width, height))
	}

	override suspend fun awaitNodeSnapshot(duration: Duration): MosaicNode {
		return awaitSnapshot(duration) { nodeSnapshots.receive() }
	}

	override suspend fun awaitRenderSnapshot(duration: Duration): String {
		return awaitSnapshot(duration) { renderSnapshots.receive() }
	}

	override suspend fun awaitNodeRenderSnapshot(duration: Duration): TestMosaicComposition.NodeRenderSnapshot {
		return awaitSnapshot(duration) {
			TestMosaicComposition.NodeRenderSnapshot(nodeSnapshots.receive(), renderSnapshots.receive())
		}
	}

	private suspend fun <T> awaitSnapshot(duration: Duration, block: suspend () -> T): T {
		check(contentSet) { "setContent must be called first!" }

		// Await at least one change, sending frames while we wait.
		return withTimeout(duration) {
			val sendFramesJob = with(mosaicComposition) { sendFrames() }
			try {
				block()
			} finally {
				sendFramesJob.cancel()
			}
		}
	}
}
