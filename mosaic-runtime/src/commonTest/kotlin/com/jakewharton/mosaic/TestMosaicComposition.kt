package com.jakewharton.mosaic

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import com.jakewharton.mosaic.layout.MosaicNode
import com.jakewharton.mosaic.ui.AnsiLevel
import com.jakewharton.mosaic.ui.unit.IntSize
import kotlin.time.Duration
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withTimeout

private val DefaultTestTerminalSize = IntSize(80, 20)

internal suspend fun runMosaicTest(
	withAnsi: Boolean = false,
	initialTerminalSize: IntSize = DefaultTestTerminalSize,
	withRenderSnapshots: Boolean = true,
	block: suspend TestMosaicComposition.() -> Unit,
) {
	coroutineScope {
		val mosaicComposition = RealTestMosaicComposition(
			coroutineScope = this,
			withAnsi = withAnsi,
			initialTerminalSize = initialTerminalSize,
			withRenderSnapshots = withRenderSnapshots,
		)
		block.invoke(mosaicComposition)
		mosaicComposition.cancel()
	}
}

private class RealTestMosaicComposition(
	coroutineScope: CoroutineScope,
	withAnsi: Boolean,
	initialTerminalSize: IntSize,
	withRenderSnapshots: Boolean,
) : MosaicComposition(coroutineScope),
	TestMosaicComposition {

	private var contentSet = false

	/** Channel with the most recent snapshot, if any. */
	private val nodeSnapshots = Channel<MosaicNode>(Channel.CONFLATED)

	/** Channel with the most recent snapshot, if any. */
	private val renderSnapshots = Channel<String>(Channel.CONFLATED)

	private val rendering: Rendering = if (withRenderSnapshots) {
		AnsiRendering(ansiLevel = if (withAnsi) AnsiLevel.TRUECOLOR else AnsiLevel.NONE)
	} else {
		object : Rendering {
			override fun render(node: MosaicNode): CharSequence {
				throw UnsupportedOperationException("Rendering disabled by `withRenderSnapshots`")
			}
		}
	}

	override val onEndChanges: (MosaicNode) -> Unit = { rootNode ->
		nodeSnapshots.trySend(rootNode)
		if (withRenderSnapshots) {
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
	}

	override val terminalInfo: MutableState<Terminal> = mutableStateOf(
		Terminal(size = initialTerminalSize),
	)

	override fun setContent(content: @Composable () -> Unit) {
		contentSet = true
		super.setContent(content)
	}

	override fun changeTerminalSize(width: Int, height: Int) {
		terminalInfo.value = Terminal(size = IntSize(width, height))
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
			val sendFramesJob = sendFrames()
			try {
				block()
			} finally {
				sendFramesJob.cancel()
			}
		}
	}
}
