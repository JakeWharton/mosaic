package example

import androidx.compose.runtime.Composable
import com.jakewharton.mosaic.LocalTerminal
import com.jakewharton.mosaic.layout.background
import com.jakewharton.mosaic.layout.height
import com.jakewharton.mosaic.layout.size
import com.jakewharton.mosaic.modifier.Modifier
import com.jakewharton.mosaic.runMosaicBlocking
import com.jakewharton.mosaic.text.SpanStyle
import com.jakewharton.mosaic.text.buildAnnotatedString
import com.jakewharton.mosaic.text.withStyle
import com.jakewharton.mosaic.ui.Box
import com.jakewharton.mosaic.ui.Color
import com.jakewharton.mosaic.ui.Column
import com.jakewharton.mosaic.ui.Filler
import com.jakewharton.mosaic.ui.Row
import com.jakewharton.mosaic.ui.Spacer
import com.jakewharton.mosaic.ui.Text
import com.jakewharton.mosaic.ui.TextStyle
import kotlinx.coroutines.suspendCancellableCoroutine

private val BrightGreen = Color(100, 255, 100)
private val BrightBlue = Color(60, 140, 230)

fun main() = runMosaicBlocking {
	setContent {
		Column {
			val terminal = LocalTerminal.current
			Text(
				buildAnnotatedString {
					append("\uD83D\uDDA5\uFE0F")
					append("  ")
					append("Terminal(")
					withStyle(SpanStyle(color = BrightGreen)) {
						append("width=")
					}
					withStyle(SpanStyle(color = BrightBlue, textStyle = TextStyle.Bold + TextStyle.Underline)) {
						append(terminal.size.width.toString())
					}
					append(", ")
					withStyle(SpanStyle(color = BrightGreen)) {
						append("height=")
					}
					withStyle(SpanStyle(color = BrightBlue, textStyle = TextStyle.Bold + TextStyle.Underline)) {
						append(terminal.size.height.toString())
					}
					append(")")
					append(" ")
					append("\uD83D\uDDA5\uFE0F")
				},
			)
			Spacer(modifier = Modifier.height(1))
			Gradient(
				repeatedWord = "Red",
				textColorProvider = { percent -> Color(1.0f - percent, 0.0f, 0.0f) },
				backgroundColorProvider = { percent -> Color(percent, 0.0f, 0.0f) },
			)
			Gradient(
				repeatedWord = "Green",
				textColorProvider = { percent -> Color(0.0f, 1.0f - percent, 0.0f) },
				backgroundColorProvider = { percent -> Color(0.0f, percent, 0.0f) },
			)
			Gradient(
				repeatedWord = "Blue",
				textColorProvider = { percent -> Color(0.0f, 0.0f, 1.0f - percent) },
				backgroundColorProvider = { percent -> Color(0.0f, 0.0f, percent) },
			)
		}
	}

	// Run forever!
	suspendCancellableCoroutine { }
}

@Composable
private fun Gradient(
	repeatedWord: String,
	textColorProvider: (percent: Float) -> Color,
	backgroundColorProvider: (percent: Float) -> Color,
) {
	val width = LocalTerminal.current.size.width / 2
	Box {
		Row {
			var wordCharIndex = 0
			repeat(width) { index ->
				if (wordCharIndex == repeatedWord.length) {
					wordCharIndex = 0
				}
				Filler(
					char = repeatedWord[wordCharIndex],
					foreground = textColorProvider.invoke(index / width.toFloat()),
					modifier = Modifier.size(1),
				)
				wordCharIndex++
			}
		}
		Row {
			repeat(width) { index ->
				Spacer(
					modifier = Modifier
						.size(1)
						.background(backgroundColorProvider.invoke(index / width.toFloat())),
				)
			}
		}
	}
}
