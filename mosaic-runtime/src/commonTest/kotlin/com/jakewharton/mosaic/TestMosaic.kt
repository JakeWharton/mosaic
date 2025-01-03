package com.jakewharton.mosaic

import androidx.compose.runtime.BroadcastFrameClock
import androidx.compose.runtime.Composable
import com.jakewharton.mosaic.layout.KeyEvent
import com.jakewharton.mosaic.layout.MosaicNode
import com.jakewharton.mosaic.ui.AnsiLevel
import com.jakewharton.mosaic.ui.unit.IntSize
import kotlin.coroutines.CoroutineContext
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.withTimeout

internal fun interface SnapshotStrategy<T> {
	fun create(mosaic: Mosaic): T
}

internal suspend fun runMosaicTest(block: suspend TestMosaic<String>.() -> Unit) {
	runMosaicTest(PlainTextSnapshots, block)
}

internal suspend fun <T, R> runMosaicTest(
	snapshotStrategy: SnapshotStrategy<T>,
	block: suspend TestMosaic<T>.() -> R,
): R {
	return coroutineScope {
		val testMosaicComposition = RealTestMosaic<T>(
			coroutineContext = coroutineContext,
			snapshotStrategy = snapshotStrategy,
		)
		val result = block.invoke(testMosaicComposition)
		testMosaicComposition.mosaicComposition.cancel()
		result
	}
}

internal interface TestMosaic<T> {
	fun setContent(content: @Composable () -> Unit)

	fun setTerminalSize(width: Int, height: Int)

	fun sendKeyEvent(keyEvent: KeyEvent)

	suspend fun awaitSnapshot(duration: Duration = 1.seconds): T
}

private class RealTestMosaic<T>(
	coroutineContext: CoroutineContext,
	private val snapshotStrategy: SnapshotStrategy<T>,
) : TestMosaic<T> {

	private var timeNanos = 0L
	private val frameDelay = 1.seconds / 60
	private var contentSet = false
	private var hasChanges = false

	private val clock = BroadcastFrameClock()
	val mosaicComposition = MosaicComposition(
		coroutineContext = coroutineContext + clock,
		onDraw = { hasChanges = true },
	)

	override fun setContent(content: @Composable () -> Unit) {
		contentSet = true
		mosaicComposition.setContent(content)
	}

	override fun setTerminalSize(width: Int, height: Int) {
		mosaicComposition.terminalState.value = Terminal(size = IntSize(width, height))
	}

	override fun sendKeyEvent(keyEvent: KeyEvent) {
		mosaicComposition.sendKeyEvent(keyEvent)
	}

	override suspend fun awaitSnapshot(duration: Duration): T {
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
		return snapshotStrategy.create(mosaicComposition)
	}
}

internal object PlainTextSnapshots : SnapshotStrategy<String> {
	override fun create(mosaic: Mosaic): String {
		return mosaic.paint().render(AnsiLevel.NONE)
	}
}

internal object DumpSnapshots : SnapshotStrategy<String> {
	override fun create(mosaic: Mosaic): String {
		return mosaic.dump()
	}
}

internal object NodeSnapshots : SnapshotStrategy<MosaicNode> {
	override fun create(mosaic: Mosaic): MosaicNode {
		return (mosaic as MosaicComposition).rootNode
	}
}

internal class RenderingSnapshots(
	private val rendering: Rendering,
) : SnapshotStrategy<String> {
	override fun create(mosaic: Mosaic): String {
		return rendering.render(mosaic).toString()
	}
}
