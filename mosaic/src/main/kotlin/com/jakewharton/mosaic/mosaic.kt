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
import kotlin.coroutines.CoroutineContext

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

	AnsiConsole.systemInstall()
	job.invokeOnCompletion {
		AnsiConsole.systemUninstall()
		composition.dispose()
	}

	composition.setContent(content)

	var lastHeight = 0
	fun render() {
		val root = rootNode.yoga
		root.calculateLayout(UNDEFINED, UNDEFINED)

		val rendered = rootNode.renderToString()
		if (lastHeight == 0) {
			// Special case 0 to avoid https://github.com/fusesource/jansi/issues/172.
			println(rendered)
		} else {
			println(ansi().cursorUpLine(lastHeight).a(rendered))
		}

		lastHeight = root.layoutHeight.toInt()
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
