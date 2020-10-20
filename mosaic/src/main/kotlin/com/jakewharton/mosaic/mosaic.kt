package com.jakewharton.mosaic

import androidx.compose.runtime.Composable
import androidx.compose.runtime.EmbeddingContext
import androidx.compose.runtime.Recomposer
import androidx.compose.runtime.compositionFor
import androidx.compose.runtime.dispatch.BroadcastFrameClock
import androidx.compose.runtime.yoloGlobalEmbeddingContext
import com.facebook.yoga.YogaConstants.UNDEFINED
import com.jakewharton.crossword.TextCanvas
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart.UNDISPATCHED
import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.fusesource.jansi.AnsiConsole
import java.util.concurrent.TimeUnit.NANOSECONDS
import kotlin.coroutines.CoroutineContext

interface MosaicScope : CoroutineScope {
	fun setContent(content: @Composable () -> Unit)
}

fun runMosaic(body: suspend MosaicScope.() -> Unit) {
	runBlocking {
		val mosaic = createMosaic()
		val handle = mosaic.renderIn(this)

		coroutineScope {
			val scope = object : MosaicScope, CoroutineScope by this {
				override fun setContent(content: @Composable () -> Unit) {
					mosaic.setContent(content)
				}
			}
			scope.body()
		}

		handle.awaitRenderThenCancel()
	}
}

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
			val canvas = with(rootNode.yoga) {
				calculateLayout(UNDEFINED, UNDEFINED)
				TextCanvas(layoutWidth.toInt(), layoutHeight.toInt())
			}
			rootNode.render(canvas)
			return canvas.toString()
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
		print(buildString {
			if (ansiConsole) {
				val lines = output.split("\n")

				repeat(lastHeight) {
					append("\u001B[F") // Cursor up line.
				}

				for (line in lines) {
					append(line)
					append("\u001B[K") // Clear rest of line.
					append('\n')
				}

				// If the new output contains fewer lines than the last output, clear those old lines.
				for (unused in 0 until lastHeight - lines.size) {
					append("\u001B[K") // Clear line.
					append('\n')
				}

				lastHeight = lines.size
			} else {
				val renderNanos = System.nanoTime()

				if (lastRenderNanos != 0L) {
					repeat(50) { append('~') }
					append(" +")
					val nanoDiff = renderNanos - lastRenderNanos
					append(NANOSECONDS.toMillis(nanoDiff))
					appendLine("ms")
				}
				lastRenderNanos = renderNanos

				appendLine(output)
			}
		})
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
