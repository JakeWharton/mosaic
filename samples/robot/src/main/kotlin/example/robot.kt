package example

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.jakewharton.mosaic.layout.padding
import com.jakewharton.mosaic.modifier.Modifier
import com.jakewharton.mosaic.runMosaicBlocking
import com.jakewharton.mosaic.ui.Column
import com.jakewharton.mosaic.ui.Text
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.withContext
import org.jline.terminal.TerminalBuilder

private const val width = 20
private const val height = 10

fun main() = runMosaicBlocking {
	// TODO https://github.com/JakeWharton/mosaic/issues/3
	var x by mutableStateOf(0)
	var y by mutableStateOf(0)

	setContent {
		Column {
			Text("Use arrow keys to move the face. Press “q” to exit.")
			Text("Position: $x, $y   World: $width, $height")
			Text("")
			// TODO https://github.com/JakeWharton/mosaic/issues/11
			Text("^_^", modifier = Modifier.padding(x, y, width - x, height - y))
		}
	}

	withContext(IO) {
		val terminal = TerminalBuilder.terminal()
		terminal.enterRawMode()
		val reader = terminal.reader()

		while (true) {
			// TODO https://github.com/JakeWharton/mosaic/issues/10
			when (reader.read()) {
				'q'.code -> break
				27 -> {
					when (reader.read()) {
						91 -> {
							when (reader.read()) {
								65 -> y = (y - 1).coerceAtLeast(0)
								66 -> y = (y + 1).coerceAtMost(height)
								67 -> x = (x + 1).coerceAtMost(width)
								68 -> x = (x - 1).coerceAtLeast(0)
							}
						}
					}
				}
			}
		}
	}
}
