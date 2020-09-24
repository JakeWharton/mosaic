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

private const val ansiConsole = true

fun CoroutineScope.launchMosaic(
	content: @Composable () -> Unit,
): Job {
	val mainThread = Thread.currentThread()

	val clock = BroadcastFrameClock()
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

	if (ansiConsole) {
		AnsiConsole.systemInstall()
	}
	job.invokeOnCompletion {
		if (ansiConsole) {
			AnsiConsole.systemUninstall()
		}
		composition.dispose()
	}

	composition.setContent(content)

	var lastHeight = 0
	var lastRenderNanos = 0L
	fun render() {
		val root = rootNode.yoga
		root.calculateLayout(UNDEFINED, UNDEFINED)

		if (ansiConsole) {
			repeat(lastHeight) {
				print(ansi().cursorUpLine().eraseLine())
			}
			lastHeight = root.layoutHeight.toInt()
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

		println(rootNode.renderToString())
	}

	launch(start = UNDISPATCHED, context = composeContext) {
		render()
		while (true) {
			recomposer.recomposeAndApplyChanges(1L)
			render()
		}
	}

	launch(start = UNDISPATCHED, context = composeContext) {
		while (true) {
			clock.sendFrame(System.nanoTime())
			delay(100)
		}
	}

	return job
}
