package com.jakewharton.mosaic.testing

import androidx.collection.MutableObjectList
import androidx.compose.runtime.BroadcastFrameClock
import androidx.compose.runtime.Composable
import com.jakewharton.mosaic.Mosaic
import com.jakewharton.mosaic.TextCanvas
import com.jakewharton.mosaic.layout.KeyEvent
import com.jakewharton.mosaic.ui.AnsiLevel
import kotlin.coroutines.CoroutineContext
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.withTimeout

public suspend fun runMosaicTest(block: suspend TestMosaic<String>.() -> Unit) {
	runMosaicTest(PlainTextSnapshots, block)
}

public fun interface SnapshotStrategy<T> {
	public fun create(mosaic: Mosaic): T
}

public object MosaicSnapshots : SnapshotStrategy<Mosaic> {
	override fun create(mosaic: Mosaic): Mosaic = mosaic
}

public suspend fun <T, R> runMosaicTest(
	snapshotStrategy: SnapshotStrategy<T>,
	block: suspend TestMosaic<T>.() -> R,
): R {
	val tester = RealTestMosaic(
		coroutineContext = currentCoroutineContext(),
		snapshotStrategy = snapshotStrategy,
	)
	val result = block.invoke(tester)
	tester.cancel()
	return result
}

public interface TestMosaic<T> : Mosaic {
	public suspend fun awaitSnapshot(duration: Duration = 1.seconds): T
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
	val mosaic = Mosaic(
		coroutineContext = coroutineContext + clock,
		onDraw = { hasChanges = true },
	)

	override fun setContent(content: @Composable () -> Unit) {
		contentSet = true
		mosaic.setContent(content)
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
		return snapshotStrategy.create(mosaic)
	}

	override fun sendKeyEvent(keyEvent: KeyEvent) {
		mosaic.sendKeyEvent(keyEvent)
	}

	override val terminalState get() = mosaic.terminalState

	override fun paint() = mosaic.paint()

	override fun paintStaticsTo(list: MutableObjectList<TextCanvas>) {
		mosaic.paintStaticsTo(list)
	}

	override fun dump() = mosaic.dump()

	override suspend fun awaitComplete() {
		mosaic.awaitComplete()
	}

	override fun cancel() {
		mosaic.cancel()
	}
}

internal object PlainTextSnapshots : SnapshotStrategy<String> {
	override fun create(mosaic: Mosaic): String {
		return mosaic.paint().render(AnsiLevel.NONE)
	}
}
