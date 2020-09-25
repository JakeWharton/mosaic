package com.jakewharton.mosaic

import androidx.compose.runtime.Composable
import androidx.compose.runtime.EmbeddingContext
import androidx.compose.runtime.Recomposer
import androidx.compose.runtime.compositionFor
import androidx.compose.runtime.dispatch.BroadcastFrameClock
import androidx.compose.runtime.yoloGlobalEmbeddingContext
import com.facebook.yoga.YogaConstants.UNDEFINED
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart.UNDISPATCHED
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.fusesource.jansi.Ansi.ansi
import org.fusesource.jansi.AnsiConsole
import java.util.concurrent.TimeUnit.NANOSECONDS
import kotlin.coroutines.CoroutineContext

interface Mosaic {
	/** Returns true if there are pending changes that need applied with a call to [applyChanges]. */
	val hasPendingChanges: Boolean

	/**
	 * Send a frame event which will apply any pending changes to the output.
	 * This will reset [hasPendingChanges] to false.
	 */
	fun applyChanges()

	fun cancel()

	fun setContent(content: @Composable () -> Unit)

	/** Measure, layout, and render the content for display. */
	override fun toString(): String
}

fun CoroutineScope.createMosaic(): Mosaic {
	var pendingChanges = false
	val clock = BroadcastFrameClock {
		pendingChanges = true
	}

	val job = Job(coroutineContext[Job])
	val composeContext = coroutineContext + clock + job

	val mainThread = Thread.currentThread()
	val embeddingContext = object : EmbeddingContext {
		override fun isMainThread(): Boolean {
			return Thread.currentThread() === mainThread
		}

		override fun mainThreadCompositionContext(): CoroutineContext {
			return composeContext
		}
	}
	yoloGlobalEmbeddingContext = embeddingContext

	val rootNode = BoxNode()
	val applier = MosaicNodeApplier(rootNode)

	val recomposer = Recomposer(embeddingContext)
	val composition = compositionFor(Any(), applier, recomposer)

	// Start undispatched to ensure we can use suspending things inside the content.
	launch(start = UNDISPATCHED, context = composeContext) {
		recomposer.runRecomposeAndApplyChanges()
	}

	return object : Mosaic {
		override val hasPendingChanges get() = pendingChanges

		override fun applyChanges() {
			clock.sendFrame(0L) // Frame time value is not used by Compose runtime.
			pendingChanges = false
		}

		override fun cancel() {
			job.cancel()
			composition.dispose()
		}

		override fun setContent(content: @Composable () -> Unit) {
			composition.setContent(content)
			pendingChanges = true
		}

		override fun toString(): String {
			rootNode.yoga.calculateLayout(UNDEFINED, UNDEFINED)
			return rootNode.renderToString()
		}
	}
}

interface MosaicHandle {
	suspend fun awaitRenderThenCancel()
	fun cancel()
}

/**
 * True when using ANSI control sequences to overwrite output.
 * False for a debug-like output that renders each "frame" on its own with a timestamp delta.
 */
private const val ansiConsole = true

fun Mosaic.renderIn(scope: CoroutineScope): MosaicHandle {
	if (ansiConsole) {
		AnsiConsole.systemInstall()
	}

	var lastHeight = 0
	var lastRenderNanos = 0L
	fun render(output: String) {
		if (ansiConsole) {
			repeat(lastHeight) {
				print(ansi().cursorUpLine().eraseLine())
			}
			lastHeight = 1 + output.count { it == '\n' }
		} else {
			val renderNanos = System.nanoTime()

			if (lastRenderNanos != 0L) {
				println(buildString(60) {
					repeat(50) { append('~') }
					append(" +")
					val nanoDiff = renderNanos - lastRenderNanos
					append(NANOSECONDS.toMillis(nanoDiff))
					append("ms")
				})
			}
			lastRenderNanos = renderNanos
		}

		println(output)
	}

	var renderSignal: CompletableDeferred<Unit>? = null
	val job = scope.launch {
		while (true) {
			if (hasPendingChanges) {
				applyChanges()
				render(this@renderIn.toString())
				renderSignal?.complete(Unit)
			}
			delay(50)
		}
	}
	job.invokeOnCompletion {
		cancel()
		if (ansiConsole) {
			AnsiConsole.systemUninstall()
		}
	}

	return object : MosaicHandle {
		override suspend fun awaitRenderThenCancel() {
			if (hasPendingChanges) {
				CompletableDeferred<Unit>().also {
					renderSignal = it
					it.await()
				}
			}
			cancel()
		}

		override fun cancel() {
			job.cancel()
		}
	}
}

fun CoroutineScope.launchMosaic(
	content: @Composable () -> Unit,
): MosaicHandle {
	val mosaic = createMosaic()
	mosaic.setContent(content)
	return mosaic.renderIn(this)
}
