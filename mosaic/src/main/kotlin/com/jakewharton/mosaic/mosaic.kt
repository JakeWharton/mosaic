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

private fun CoroutineScope.launchMosaic(
	content: @Composable () -> Unit,
	output: (String) -> Unit,
): Job {
	val mainThread = Thread.currentThread()

	var dirty = true
	val clock = BroadcastFrameClock {
		dirty = true
	}

	val job = Job(coroutineContext[Job])
	val composeContext = coroutineContext + clock + job

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

	job.invokeOnCompletion {
		composition.dispose()
	}

	// Start undispatched to ensure we can use suspending things inside the content.
	launch(start = UNDISPATCHED, context = composeContext) {
		recomposer.runRecomposeAndApplyChanges()
	}

	composition.setContent(content)

	launch(context = composeContext) {
		while (true) {
			clock.sendFrame(System.nanoTime())
			if (dirty) {
				dirty = false

				val root = rootNode.yoga
				root.calculateLayout(UNDEFINED, UNDEFINED)
				output(rootNode.renderToString())
			}
			delay(100)
		}
	}

	return job
}

/**
 * True when using ANSI control sequences to overwrite output.
 * False for a debug-like output that renders each "frame" on its own with a timestamp delta.
 */
private const val ansiConsole = true

fun CoroutineScope.launchMosaic(
	content: @Composable () -> Unit,
): Job {
	if (ansiConsole) {
		AnsiConsole.systemInstall()
	}

	var lastHeight = 0
	var lastRenderNanos = 0L
	val job = launchMosaic(content) { output ->
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

	if (ansiConsole) {
		job.invokeOnCompletion {
			AnsiConsole.systemUninstall()
		}
	}

	return job
}
