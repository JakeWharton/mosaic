package com.jakewharton.mosaic

import androidx.compose.runtime.Composable
import androidx.compose.runtime.EmbeddingContext
import androidx.compose.runtime.Recomposer
import androidx.compose.runtime.compositionFor
import androidx.compose.runtime.dispatch.BroadcastFrameClock
import androidx.compose.runtime.yoloGlobalEmbeddingContext
import com.facebook.yoga.YogaConstants.UNDEFINED
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
	/**
	 * Trigger a frame which will apply any pending changes to the output.
	 *
	 * @return True if the string representation should be re-rendered as it has probably changed.
	 */
	fun sendFrame(): Boolean

	fun cancel()

	/** Measure, layout, and render the content for display. */
	override fun toString(): String
}

fun CoroutineScope.createMosaic(
	content: @Composable () -> Unit,
): Mosaic {
	var dirty = true
	val clock = BroadcastFrameClock {
		dirty = true
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

	composition.setContent(content)

	return object : Mosaic {
		override fun sendFrame(): Boolean {
			clock.sendFrame(0L) // Frame time value is not used by Compose runtime.

			val wasDirty = dirty
			dirty = false
			return wasDirty
		}

		override fun cancel() {
			job.cancel()
			composition.dispose()
		}

		override fun toString(): String {
			rootNode.yoga.calculateLayout(UNDEFINED, UNDEFINED)
			return rootNode.renderToString()
		}
	}
}

/**
 * True when using ANSI control sequences to overwrite output.
 * False for a debug-like output that renders each "frame" on its own with a timestamp delta.
 */
private const val ansiConsole = true

fun CoroutineScope.launchMosaic(
	content: @Composable () -> Unit,
): Job {
	val mosaic = createMosaic(content)

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

	val job = launch {
		while (true) {
			if (mosaic.sendFrame()) {
				render(mosaic.toString())
			}
			delay(100)
		}
	}
	job.invokeOnCompletion {
		mosaic.cancel()
		if (ansiConsole) {
			AnsiConsole.systemUninstall()
		}
	}

	return job
}
