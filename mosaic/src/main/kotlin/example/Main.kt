package example

import androidx.compose.runtime.AbstractApplier
import androidx.compose.runtime.Composable
import androidx.compose.runtime.EmbeddingContext
import androidx.compose.runtime.FrameManager
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.Recomposer
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.compositionFor
import androidx.compose.runtime.dispatch.BroadcastFrameClock
import androidx.compose.runtime.dispatch.MonotonicFrameClock
import androidx.compose.runtime.emit
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshots.withMutableSnapshot
import androidx.compose.runtime.yoloGlobalEmbeddingContext
import kotlinx.coroutines.CoroutineStart.UNDISPATCHED
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.Channel.Factory.RENDEZVOUS
import kotlinx.coroutines.channels.Channel.Factory.UNLIMITED
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.coroutines.yield
import kotlin.coroutines.Continuation
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

sealed class Node {
	abstract val children: MutableList<Node>
}

class Line : Node() {
	var value: String = ""
	override fun toString() = value

	override val children: MutableList<Node>
		get() = throw UnsupportedOperationException()
}

class Lines : Node() {
	override val children = mutableListOf<Node>()
	override fun toString() = buildString {
		children.forEachIndexed { index, node ->
			if (index > 0) {
				append('\n')
			}
			append(node)
		}
	}
}

class NodeApplier(root: Node) : AbstractApplier<Node>(root) {
	override fun insert(index: Int, instance: Node) {
		root.children.add(index, instance)
	}

	override fun remove(index: Int, count: Int) {
		root.children.remove(index, count)
	}

	override fun move(from: Int, to: Int, count: Int) {
		root.children.move(from, to, count)
	}

	override fun onClear() {
	}
}

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

		val recomposer = Recomposer(embeddingContext)

		val lines = Lines()
		val applier = NodeApplier(lines)

		val composition = compositionFor(Any(), applier, recomposer)
		composeContext[Job]!!.invokeOnCompletion {
			composition.dispose()
		}

		withContext(composeContext) {
			launch(start = UNDISPATCHED) {
				recomposer.runRecomposeAndApplyChanges()
			}

			val counts = mutableStateOf(0)
			composition.setContent {
				val count by remember { counts }
				Line("The count is $count")
			}

			launch(start = UNDISPATCHED) {
				while (true) {
					clock.sendFrame(System.nanoTime())
					println(lines)
					delay(500)
				}
			}

			for (i in 1..10) {
				delay(1_000)
				withMutableSnapshot {
					counts.value = i
				}
			}
			// Wait for final frame.
			clock.withFrameNanos { }

			cancel()
		}
	}
}

@Composable
fun Line(value: String) {
	emit<Line, NodeApplier>(::Line) {
		set(value) {
			this.value = value
		}
	}
}
