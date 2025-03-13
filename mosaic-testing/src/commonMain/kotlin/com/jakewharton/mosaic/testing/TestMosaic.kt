package com.jakewharton.mosaic.testing

import androidx.compose.runtime.BroadcastFrameClock
import androidx.compose.runtime.Composable
import com.jakewharton.mosaic.Mosaic
import com.jakewharton.mosaic.terminal.AnsiLevel
import com.jakewharton.mosaic.terminal.event.KeyboardEvent
import kotlin.coroutines.CoroutineContext
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.Channel.Factory.UNLIMITED
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.withTimeout

public suspend fun runMosaicTest(
	capabilities: TestCapabilities = TestCapabilities(),
	block: suspend TestMosaic<String>.() -> Unit,
) {
	runMosaicTest(PlainTextSnapshots, capabilities, block)
}

public fun interface SnapshotStrategy<T> {
	public fun create(mosaic: Mosaic): T
}

public object MosaicSnapshots : SnapshotStrategy<Mosaic> {
	override fun create(mosaic: Mosaic): Mosaic = mosaic
}

public suspend fun <T, R> runMosaicTest(
	snapshotStrategy: SnapshotStrategy<T>,
	capabilities: TestCapabilities = TestCapabilities(),
	block: suspend TestMosaic<T>.() -> R,
): R {
	val tester = RealTestMosaic(
		coroutineContext = currentCoroutineContext(),
		snapshotStrategy = snapshotStrategy,
		capabilities = capabilities,
	)
	val result = block.invoke(tester)
	tester.cancel()
	return result
}

public interface TestMosaic<T> : Mosaic {
	public fun setContentAndSnapshot(content: @Composable () -> Unit): T
	public suspend fun awaitSnapshot(duration: Duration = 1.seconds): T

	public fun sendKeyEvent(keyEvent: KeyboardEvent)
	public val state: TestState
}

private class RealTestMosaic<T>(
	coroutineContext: CoroutineContext,
	private val snapshotStrategy: SnapshotStrategy<T>,
	capabilities: TestCapabilities,
) : TestMosaic<T> {
	private val keyEvents = Channel<KeyboardEvent>(UNLIMITED)
	override val state: TestState = TestTerminal.State()

	private var timeNanos = 0L
	private val frameDelay = 1.seconds / 60
	private var contentSet = false
	private var hasChanges = false

	private val clock = BroadcastFrameClock()
	val mosaic = Mosaic(
		coroutineContext = coroutineContext + clock,
		onDraw = { hasChanges = true },
		terminal = TestTerminal(
			state = state,
			capabilities = capabilities,
			keyEvents = keyEvents,
		),
	)

	override fun setContent(content: @Composable () -> Unit) {
		contentSet = true
		mosaic.setContent(content)
	}

	override fun setContentAndSnapshot(content: @Composable () -> Unit): T {
		setContent(content)
		check(hasChanges)
		hasChanges = false
		return snapshotStrategy.create(mosaic)
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

	override fun sendKeyEvent(keyEvent: KeyboardEvent) {
		keyEvents.trySend(keyEvent)
	}

	override fun draw() = mosaic.draw()
	override fun static() = mosaic.static()
	override fun dumpNodes() = mosaic.dumpNodes()

	override suspend fun awaitComplete() = mosaic.awaitComplete()
	override fun cancel() = mosaic.cancel()
}

internal object PlainTextSnapshots : SnapshotStrategy<String> {
	override fun create(mosaic: Mosaic): String {
		return mosaic.draw().render(AnsiLevel.NONE, false)
	}
}
