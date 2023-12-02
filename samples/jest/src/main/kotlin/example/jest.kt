package example

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.Snapshot
import androidx.compose.runtime.snapshots.SnapshotStateList
import com.jakewharton.mosaic.layout.background
import com.jakewharton.mosaic.layout.padding
import com.jakewharton.mosaic.modifier.Modifier
import com.jakewharton.mosaic.runMosaicBlocking
import com.jakewharton.mosaic.text.SpanStyle
import com.jakewharton.mosaic.text.buildAnnotatedString
import com.jakewharton.mosaic.text.withStyle
import com.jakewharton.mosaic.ui.Color.Companion.Black
import com.jakewharton.mosaic.ui.Color.Companion.BrightBlack
import com.jakewharton.mosaic.ui.Color.Companion.Green
import com.jakewharton.mosaic.ui.Color.Companion.Red
import com.jakewharton.mosaic.ui.Color.Companion.Yellow
import com.jakewharton.mosaic.ui.Column
import com.jakewharton.mosaic.ui.Row
import com.jakewharton.mosaic.ui.Static
import com.jakewharton.mosaic.ui.Text
import com.jakewharton.mosaic.ui.TextStyle.Companion.Bold
import example.TestState.Fail
import example.TestState.Pass
import example.TestState.Running
import kotlin.random.Random
import kotlinx.coroutines.CoroutineStart.UNDISPATCHED
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

fun main() = runMosaicBlocking {
	val paths = ArrayDeque(
		listOf(
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
		),
	)
	val totalTests = paths.size

	val complete = mutableStateListOf<Test>()
	val tests = mutableStateListOf<Test>()

	// TODO https://github.com/JakeWharton/mosaic/issues/3
	repeat(4) { // Number of test workers.
		launch(start = UNDISPATCHED) {
			while (true) {
				val path = paths.removeFirstOrNull() ?: break
				val index = Snapshot.withMutableSnapshot {
					val nextIndex = tests.size
					tests += Test(path, Running)
					nextIndex
				}
				delay(random.nextLong(2_500L, 4_000L))

				// Flip a coin biased 60% to pass to produce the final state of the test.
				tests[index] = when {
					random.nextFloat() < .7f -> tests[index].copy(state = Pass)
					else -> {
						val test = tests[index]
						val failures = buildList {
							repeat(1 + random.nextInt(2)) {
								add("Failure on line ${random.nextInt(50)} in ${test.path}")
							}
						}
						test.copy(state = Fail, failures = failures)
					}
				}
				complete += tests[index]
			}
		}
	}

	setContent {
		Column {
			Log(complete)
			Status(tests)
			Summary(totalTests, tests)
		}
	}
}

@Composable
fun TestRow(test: Test) {
	Row {
		val bg = when (test.state) {
			Running -> Yellow
			Pass -> Green
			Fail -> Red
		}
		val state = when (test.state) {
			Running -> "RUNS"
			Pass -> "PASS"
			Fail -> "FAIL"
		}
		Text(
			state,
			modifier = Modifier
				.background(bg)
				.padding(horizontal = 1),
			color = Black,
		)

		val dir = test.path.substringBeforeLast('/')
		val name = test.path.substringAfterLast('/')
		Text(
			buildAnnotatedString {
				append(" $dir/")
				withStyle(SpanStyle(textStyle = Bold)) {
					append(name)
				}
			},
		)
	}
}

// Should be placed as first composable in display.
@Composable
fun Log(complete: SnapshotStateList<Test>) {
	Static(complete) { test ->
		Column {
			TestRow(test)
			if (test.failures.isNotEmpty()) {
				for (failure in test.failures) {
					Text(" ‣ $failure")
				}
				Text("") // Blank line
			}
		}
	}

	// Separate logs from rest of display by a single line if latest test result is success.
	if (complete.lastOrNull()?.state == Pass) {
		Text("") // Blank line
	}
}

@Composable
fun Status(tests: SnapshotStateList<Test>) {
	val running = tests.filter { it.state == Running }
	if (running.isNotEmpty()) {
		for (test in running) {
			TestRow(test)
		}

		Text("") // Blank line
	}
}

@Composable
private fun Summary(totalTests: Int, tests: SnapshotStateList<Test>) {
	val counts = tests.groupingBy { it.state }.eachCount()
	val failed = counts[Fail] ?: 0
	val passed = counts[Pass] ?: 0
	val running = counts[Running] ?: 0

	var elapsed by remember { mutableStateOf(0) }
	LaunchedEffect(Unit) {
		while (true) {
			delay(1_000)
			Snapshot.withMutableSnapshot {
				elapsed++
			}
		}
	}

	Column {
		Text(
			buildAnnotatedString {
				append("Tests: ")

				if (failed > 0) {
					withStyle(SpanStyle(color = Red)) {
						append("$failed failed")
					}
					append(", ")
				}

				if (passed > 0) {
					withStyle(SpanStyle(color = Green)) {
						append("$passed passed")
					}
					append(", ")
				}

				if (running > 0) {
					withStyle(SpanStyle(color = Yellow)) {
						append("$running running")
					}
					append(", ")
				}

				append("$totalTests total")
			},
		)

		Text("Time:  ${elapsed}s")

		if (running > 0) {
			TestProgress(totalTests, passed, failed, running)
		}
	}
}

@Composable
fun TestProgress(totalTests: Int, passed: Int, failed: Int, running: Int) {
	var showRunning by remember { mutableStateOf(true) }
	LaunchedEffect(Unit) {
		while (true) {
			delay(500)
			Snapshot.withMutableSnapshot {
				showRunning = !showRunning
			}
		}
	}

	val totalWidth = 40
	val failedWidth = (failed.toDouble() * totalWidth / totalTests).toInt()
	val passedWidth = (passed.toDouble() * totalWidth / totalTests).toInt()
	val runningWidth = if (showRunning) (running.toDouble() * totalWidth / totalTests).toInt() else 0

	Text(
		buildAnnotatedString {
			withStyle(SpanStyle(background = Red)) {
				append(" ".repeat(failedWidth))
			}
			withStyle(SpanStyle(background = Green)) {
				append(" ".repeat(passedWidth))
			}
			withStyle(SpanStyle(background = Yellow)) {
				append(" ".repeat(runningWidth))
			}
			withStyle(SpanStyle(background = BrightBlack)) {
				append(" ".repeat(totalWidth - failedWidth - passedWidth - runningWidth))
			}
		},
	)
}

data class Test(
	val path: String,
	val state: TestState,
	val failures: List<String> = emptyList(),
)

enum class TestState {
	Running,
	Pass,
	Fail,
}

// Use a random with a fixed seed for deterministic output.
private val random = Random(1234)
