package example

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.jakewharton.mosaic.Column
import com.jakewharton.mosaic.KeyEvent
import com.jakewharton.mosaic.Terminal
import com.jakewharton.mosaic.Text
import com.jakewharton.mosaic.onKeyEvent
import com.jakewharton.mosaic.runMosaic
import kotlinx.coroutines.CompletableDeferred

fun main() = runMosaic {
	val exitSignal = CompletableDeferred<Unit>()

	setContent {
		var x by remember { mutableStateOf(0) }
		var y by remember { mutableStateOf(0) }
		val width = Terminal.current.size.width

		// 3 for header stuff, 1 for final newline, 1 for cursor line.
		val height = Terminal.current.size.height - 5

		onKeyEvent { event ->
			when (event) {
				KeyEvent.Q -> exitSignal.complete(Unit)
				KeyEvent.UP -> y = (y - 1).coerceAtLeast(0)
				KeyEvent.DOWN -> y = (y + 1).coerceAtMost(height)
				KeyEvent.RIGHT -> x = (x + 1).coerceAtMost(width)
				KeyEvent.LEFT -> x = (x - 1).coerceAtLeast(0)
				else -> {}
			}
		}

		Column {
			Text("Use arrow keys to move the face. Press “q” to exit.")
			Text("Position: $x, $y   World: $width, $height")
			Text("")
			// TODO https://github.com/JakeWharton/mosaic/issues/11
			Text(buildString {
				// TODO https://github.com/JakeWharton/mosaic/issues/7
				repeat(y) { append('\n') }
				repeat(x) { append(' ') }
				append("^_^")

				repeat(height - y) { append('\n') }
			})
		}
	}

	exitSignal.await()
}
