package example

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.jakewharton.mosaic.ui.ColumnScope
import com.jakewharton.mosaic.ui.Filler
import com.jakewharton.mosaic.ui.Row
import com.jakewharton.mosaic.ui.Spacer
import com.jakewharton.mosaic.ui.Text
import com.jakewharton.mosaic.ui.TextStyle
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.delay

private val BrightGreen = Color(100, 255, 100)
private val BrightBlue = Color(60, 140, 230)

fun main() = runMosaicBlocking {
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
				withStyle(
					SpanStyle(
						color = BrightBlue,
						textStyle = TextStyle.Bold + TextStyle.Underline,
					),
				) {
					append(terminal.size.width.toString())
				}
				append(", ")
				withStyle(SpanStyle(color = BrightGreen)) {
					append("height=")
				}
				withStyle(
					SpanStyle(
						color = BrightBlue,
						textStyle = TextStyle.Bold + TextStyle.Underline,
					),
				) {
					append(terminal.size.height.toString())
				}
				append(")")
				append(" ")
				append("\uD83D\uDDA5\uFE0F")
			},
		)
		Spacer(modifier = Modifier.height(1))
		GradientsBlock()
	}

	LaunchedEffect(Unit) {
		awaitCancellation()
	}
}

@Suppress("UnusedReceiverParameter") // instead of ignore rule: compose:multiple-emitters-check
@Composable
private fun ColumnScope.GradientsBlock() {
	val screenHalfWidth = LocalTerminal.current.size.width / 2
	var gradientWidth by remember { mutableIntStateOf(0) }
	val gradientWidthDiff by remember(screenHalfWidth) {
		derivedStateOf { (screenHalfWidth - gradientWidth) / 5 }
	}
	Gradient(
		repeatedWord = "Red",
		width = gradientWidth,
		textColorProvider = { percent -> Color(1.0f - percent, 0.0f, 0.0f) },
		backgroundColorProvider = { percent -> Color(percent, 0.0f, 0.0f) },
	)
	Gradient(
		repeatedWord = "Yellow",
		width = gradientWidth,
		textColorProvider = { percent -> Color(1.0f - percent, 1.0f - percent, 0.0f) },
		backgroundColorProvider = { percent -> Color(percent, percent, 0.0f) },
	)
	Gradient(
		repeatedWord = "Green",
		width = gradientWidth,
		textColorProvider = { percent -> Color(0.0f, 1.0f - percent, 0.0f) },
		backgroundColorProvider = { percent -> Color(0.0f, percent, 0.0f) },
	)
	Gradient(
		repeatedWord = "Cyan",
		width = gradientWidth,
		textColorProvider = { percent -> Color(0.0f, 1.0f - percent, 1.0f - percent) },
		backgroundColorProvider = { percent -> Color(0.0f, percent, percent) },
	)
	Gradient(
		repeatedWord = "Blue",
		width = gradientWidth,
		textColorProvider = { percent -> Color(0.0f, 0.0f, 1.0f - percent) },
		backgroundColorProvider = { percent -> Color(0.0f, 0.0f, percent) },
	)
	Gradient(
		repeatedWord = "Magenta",
		width = gradientWidth,
		textColorProvider = { percent -> Color(1.0f - percent, 0.0f, 1.0f - percent) },
		backgroundColorProvider = { percent -> Color(percent, 0.0f, percent) },
	)
	LaunchedEffect(screenHalfWidth) {
		while (true) {
			delay(100L)
			gradientWidth += gradientWidthDiff
		}
	}
}

@Composable
private fun Gradient(
	repeatedWord: String,
	width: Int,
	textColorProvider: (percent: Float) -> Color,
	backgroundColorProvider: (percent: Float) -> Color,
) {
	var textBias by remember { mutableIntStateOf(0) }
	Box {
		Row {
			var wordCharIndex = textBias
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
	LaunchedEffect(Unit) {
		while (true) {
			delay(200L)
			textBias--
			if (textBias < 0) {
				textBias = repeatedWord.length - 1
			}
		}
	}
}
