package example

import com.jakewharton.mosaic.LocalTerminal
import com.jakewharton.mosaic.runMosaicBlocking
import com.jakewharton.mosaic.text.SpanStyle
import com.jakewharton.mosaic.text.buildAnnotatedString
import com.jakewharton.mosaic.text.withStyle
import com.jakewharton.mosaic.ui.Color
import com.jakewharton.mosaic.ui.Text
import kotlinx.coroutines.suspendCancellableCoroutine

fun main() = runMosaicBlocking {
	setContent {
		val terminal = LocalTerminal.current
		Text(
			buildAnnotatedString {
				append("Terminal(")
				withStyle(SpanStyle(color = Color.BrightGreen)) {
					append("width=")
				}
				withStyle(SpanStyle(color = Color.BrightBlue)) {
					append(terminal.size.width.toString())
				}
				append(", ")
				withStyle(SpanStyle(color = Color.BrightGreen)) {
					append("height=")
				}
				withStyle(SpanStyle(color = Color.BrightBlue)) {
					append(terminal.size.height.toString())
				}
				append(")")
			},
		)
	}

	// Run forever!
	suspendCancellableCoroutine { }
}
