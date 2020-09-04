package androidx.compose.runtime

// TODO The notion of a global EmbeddingContext actual should not exist! It breaks concurrent
//  usage on multiple threads which target multiple threads. This should always be pulled from the
//  Recomposer.
var yoloGlobalEmbeddingContext: EmbeddingContext? = null

actual fun EmbeddingContext(): EmbeddingContext = yoloGlobalEmbeddingContext!!

private val keyInfo = mutableMapOf<Int, String>()

private fun findSourceKey(key: Any): Int? =
	when (key) {
		is Int -> key
		is JoinedKey -> {
			key.left?.let { findSourceKey(it) } ?: key.right?.let { findSourceKey(it) }
		}
		else -> null
	}

internal actual fun recordSourceKeyInfo(key: Any) {
	val sk = findSourceKey(key)
	sk?.let {
		keyInfo.getOrPut(sk, {
			val stack = Thread.currentThread().stackTrace
			// On Android the frames looks like:
			//  0: getThreadStackTrace() (native method)
			//  1: getStackTrace()
			//  2: recordSourceKey()
			//  3: start()
			//  4: startGroup() or startNode()
			//  5: non-inline call/emit?
			//  5 or 6: <calling method>
			// On a desktop VM this looks like:
			//  0: getStackTrace()
			//  1: recordSourceKey()
			//  2: start()
			//  3: startGroup() or startNode()
			//  4: non-inline call/emit?
			//  4 or 5: <calling method>
			// If the stack method at 4 is startGroup assume we want 5 instead.
			val frameNumber = stack[4].let {
				if (it.methodName == "startGroup" || it.methodName == "startNode") 5 else 4
			}
			val frame = stack[frameNumber].let {
				if (it.methodName == "call" || it.methodName == "emit")
					stack[frameNumber + 1]
				else
					stack[frameNumber]
			}
			"${frame.className}.${frame.methodName} (${frame.fileName}:${frame.lineNumber})"
		})
	}
}

actual fun keySourceInfoOf(key: Any): String? = keyInfo[key]
actual fun resetSourceInfo(): Unit = keyInfo.clear()

internal actual object Trace {
	actual fun beginSection(name: String) {
	}
	actual fun endSection() {
	}
}

actual annotation class MainThread
actual annotation class CheckResult(actual val suggest: String)
