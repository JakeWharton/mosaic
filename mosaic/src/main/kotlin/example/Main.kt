package example

import androidx.compose.runtime.EmbeddingContext
import androidx.compose.runtime.Recomposer
import androidx.compose.runtime.compositionFor
import androidx.compose.runtime.dispatch.BroadcastFrameClock
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshots.withMutableSnapshot
import androidx.compose.runtime.yoloGlobalEmbeddingContext
import com.facebook.yoga.YogaConstants.UNDEFINED
import com.jakewharton.mosaic.BoxNode
import com.jakewharton.mosaic.Column
import com.jakewharton.mosaic.MosaicNodeApplier
import com.jakewharton.mosaic.Row
import com.jakewharton.mosaic.Text
import com.jakewharton.mosaic.renderToString
import kotlinx.coroutines.CoroutineStart.UNDISPATCHED
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlin.coroutines.CoroutineContext

fun main() {
	runBlocking {
		val mainThread = Thread.currentThread()

		val clock = BroadcastFrameClock()
		val composeContext = coroutineContext + clock

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
		fun render() {
			val root = rootNode.yoga
			root.calculateLayout(UNDEFINED, UNDEFINED)
			println(rootNode.renderToString())
		}

		val applier = MosaicNodeApplier(rootNode)
		val recomposer = Recomposer(embeddingContext)
		val composition = compositionFor(Any(), applier, recomposer)
		composeContext[Job]!!.invokeOnCompletion {
			composition.dispose()
		}

		val counts = mutableStateOf(0)
		composition.setContent {
			val count by remember { counts }
			Column {
				Row {
					Text("one")
					Text("two")
				}
				Row {
					Text("three$count")
					Text("four")
				}
			}
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

		for (i in 1..10) {
			delay(1_000)
			withMutableSnapshot {
				counts.value = i * 10
			}
		}

		// TODO how do we wait for the final frame?
		coroutineContext[Job]!!.cancelChildren()
	}
}
