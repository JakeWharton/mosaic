package com.jakewharton.mosaic

import androidx.compose.runtime.BroadcastFrameClock
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Composition
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.Recomposer
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshots.Snapshot
import com.jakewharton.mosaic.TerminalInfo.Size
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart.UNDISPATCHED
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.yield
import org.jline.terminal.Terminal
import org.jline.terminal.Terminal.Signal.WINCH
import org.jline.terminal.TerminalBuilder

/**
 * True when using ANSI control sequences to overwrite output.
 * False for a debug-like output that renders each "frame" on its own with a timestamp delta.
 */
private const val ansiConsole = true

interface MosaicScope : CoroutineScope {
	fun setContent(content: @Composable () -> Unit)
}

fun runMosaic(body: suspend MosaicScope.() -> Unit) = runBlocking {
	val output = if (ansiConsole) AnsiOutput else DebugOutput

	var hasFrameWaiters = false
	val clock = BroadcastFrameClock {
		hasFrameWaiters = true
	}

	val job = Job(coroutineContext[Job])
	val composeContext = coroutineContext + clock + job

	val rootNode = BoxNode()
	val recomposer = Recomposer(composeContext)
	val composition = Composition(MosaicNodeApplier(rootNode), recomposer)

	// Start undispatched to ensure we can use suspending things inside the content.
	launch(start = UNDISPATCHED, context = composeContext) {
		recomposer.runRecomposeAndApplyChanges()
	}

	var displaySignal: CompletableDeferred<Unit>? = null
	launch(context = composeContext) {
		while (true) {
			if (hasFrameWaiters) {
				hasFrameWaiters = false
				clock.sendFrame(0L) // Frame time value is not used by Compose runtime.

				output.display(rootNode.render())
				displaySignal?.complete(Unit)
			}
			delay(50)
		}
	}

	lateinit var terminal: Terminal
	lateinit var terminalInfo: MutableState<TerminalInfo>
	terminal = TerminalBuilder.builder()
		.nativeSignals(true)
		.signalHandler {
			when (it) {
				WINCH -> {
					terminalInfo.value = TerminalInfo(
						Size(
							width = terminal.width,
							height = terminal.height,
						)
					)
				}
				else -> {}
			}
		}
		.build()

	terminalInfo = mutableStateOf(
		TerminalInfo(
			Size(
				width = terminal.width,
				height = terminal.height,
			)
		)
	)

	terminal.enterRawMode()
	val terminalReader = terminal.reader()
	val keyHandlers = mutableListOf<(KeyEvent) -> Unit>()
	launch(context = composeContext + Dispatchers.IO) {
		while (isActive) {
			val keyEvent = when (terminalReader.read()) {
				'q'.code -> KeyEvent.Q
				27 -> {
					when (terminalReader.read()) {
						91 -> {
							when (terminalReader.read()) {
								65 -> KeyEvent.UP
								66 -> KeyEvent.DOWN
								67 -> KeyEvent.RIGHT
								68 -> KeyEvent.LEFT
								else -> null
							}
						}
						else -> null
					}
				}
				else -> null
			}
			if (keyEvent != null) {
				for (keyHandler in keyHandlers) {
					keyHandler(keyEvent)
				}
			}
		}
	}

	coroutineScope {
		val scope = object : MosaicScope, CoroutineScope by this {
			override fun setContent(content: @Composable () -> Unit) {
				composition.setContent {
					CompositionLocalProvider(
						Terminal provides terminalInfo.value,
						KeyHandlers provides keyHandlers,
					) {
						content()
					}
				}
				hasFrameWaiters = true
			}
		}

		var snapshotNotificationsPending = false
		val snapshotObserverHandle = Snapshot.registerGlobalWriteObserver {
			if (!snapshotNotificationsPending) {
				snapshotNotificationsPending = true
				launch {
					snapshotNotificationsPending = false
					Snapshot.sendApplyNotifications()
				}
			}
		}
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
