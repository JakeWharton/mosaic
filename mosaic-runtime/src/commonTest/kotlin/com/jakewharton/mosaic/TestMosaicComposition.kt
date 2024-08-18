package com.jakewharton.mosaic

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import com.jakewharton.mosaic.ui.AnsiLevel
import com.jakewharton.mosaic.ui.unit.IntSize
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withTimeout

private val DefaultTestTerminalSize = IntSize(80, 20)

internal suspend fun runMosaicTest(
	withAnsi: Boolean = false,
	terminalSize: IntSize = DefaultTestTerminalSize,
	block: suspend TestMosaicComposition.() -> Unit,
) {
	coroutineScope {
		val mosaicComposition = TestMosaicComposition(this, withAnsi, terminalSize)
		block.invoke(mosaicComposition)
		mosaicComposition.awaitComplete()
	}
}

internal class TestMosaicComposition(
	coroutineScope: CoroutineScope,
	withAnsi: Boolean = false,
	terminalSize: IntSize = DefaultTestTerminalSize,
) : BaseMosaicComposition(coroutineScope) {

	/** Channel with the most recent snapshot, if any. */
	private val snapshots = Channel<String>(Channel.CONFLATED)

	override val onRender: (CharSequence) -> Unit = { render ->
		val stringRender = if (withAnsi) {
			render.toString()
		} else {
			render.toString()
				.replace(ansiBeginSynchronizedUpdate, "")
				.replace(ansiEndSynchronizedUpdate, "")
				.replace(clearLine, "")
				.replace(cursorUp, "")
				.removeSuffix("\r\n")
		}
		snapshots.trySend(stringRender)
	}

	override val terminalInfo: MutableState<Terminal> = mutableStateOf(Terminal(size = terminalSize))

	override val rendering: Rendering = AnsiRendering(
		ansiLevel = if (withAnsi) AnsiLevel.TRUECOLOR else AnsiLevel.NONE,
	)

	fun changeTerminalSize(width: Int, height: Int) {
		terminalInfo.value = Terminal(size = IntSize(width, height))
	}

	suspend fun awaitSnapshot(): String {
		// Await at least one change, sending frames while we wait.
		return withTimeout(1000L) {
			val sendFramesJob = sendFrames()
			try {
				snapshots.receive()
			} finally {
				sendFramesJob.cancel()
			}
		}
	}
}
