package example

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.snapshots.withMutableSnapshot
import com.jakewharton.mosaic.Column
import com.jakewharton.mosaic.Text
import com.jakewharton.mosaic.launchMosaic
import example.TestState.Fail
import example.TestState.Pass
import example.TestState.Running
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.fusesource.jansi.Ansi
import org.fusesource.jansi.Ansi.ansi
import kotlin.random.Random

fun main() = runBlocking {
	val tests = mutableStateListOf<Test>()

	val job = launchMosaic {
		val (done, running) = tests.partition { it.state != Running }
		Column {
			if (done.isNotEmpty()) {
				for (test in done) {
					TestRow(test)
				}
				Text("") // Blank line
			}

			if (running.isNotEmpty()) {
				for (test in running) {
					TestRow(test)
				}
				Text("") // Blank line
			}

			Summary(tests)
		}
	}

	// This scope is the "test runner" which interacts with the UI solely through 'tests'.
	coroutineScope {
		val paths = ArrayDeque(listOf(
			"tests/login.kt",
			"tests/signup.kt",
			"tests/forgot-password.kt",
			"tests/reset-password.kt",
			"tests/view-profile.kt",
			"tests/edit-profile.kt",
			"tests/delete-profile.kt",
			"tests/posts.kt",
			"tests/post.kt",
			"tests/comments.kt",
		))
		repeat(4) { // Number of test workers.
			launch {
				while (true) {
					val path = paths.removeFirstOrNull() ?: break
					val index = withMutableSnapshot {
						val nextIndex = tests.size
						tests += Test(path, Running)
						nextIndex
					}
					delay(Random.nextLong(2_000L, 4_000L))
					withMutableSnapshot {
						// Flip a coin biased 60% to pass to produce the final state of the test.
						val newState = if (Random.nextFloat() < .6f) Pass else Fail
						tests[index] = tests[index].copy(state = newState)
					}
				}
			}
		}
	}

	// TODO how do we wait for the final frame?
	delay(200) // HACK!

	job.cancel()
}

@Composable
fun TestRow(test: Test) {
	val bg = when (test.state) {
		Running -> Ansi.Color.YELLOW
		Pass -> Ansi.Color.GREEN
		Fail -> Ansi.Color.RED
	}
	val state = when (test.state) {
		Running -> "RUNS"
		Pass -> "PASS"
		Fail -> "FAIL"
	}
	val dir = test.path.substringBeforeLast('/')
	val name = test.path.substringAfterLast('/')
	Text(ansi()
		.bg(bg).fgBlack().a(' ').a(state).a(' ').reset()
		.a(' ')
		.a(dir).a('/').fgBrightDefault().bold().a(name).reset()
		.toString())
}

@Composable
private fun Summary(tests: SnapshotStateList<Test>) {
	val passed = tests.count { it.state == Pass }
	val failed = tests.count { it.state == Fail }
	val total = tests.size
	Text(ansi()
		.fgRed().a(failed).a(" failed").reset()
		.a(", ")
		.fgGreen().a(passed).a(" passed").reset()
		.a(", ")
		.a(total).a(" total")
		.toString())
}

data class Test(
	val path: String,
	val state: TestState,
)

enum class TestState {
	Running,
	Pass,
	Fail,
}
