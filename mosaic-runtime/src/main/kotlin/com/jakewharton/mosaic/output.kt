package com.jakewharton.mosaic

import java.nio.CharBuffer
import java.nio.charset.StandardCharsets.UTF_8
import kotlin.time.ExperimentalTime
import kotlin.time.TimeMark
import kotlin.time.TimeSource
import org.fusesource.jansi.AnsiConsole

internal interface Output {
	fun display(canvas: TextCanvas)
}

@OptIn(ExperimentalTime::class) // Not used in production.
internal object DebugOutput : Output {
	private val systemClock = TimeSource.Monotonic
	private var lastRender: TimeMark? = null

	override fun display(canvas: TextCanvas) {
		println(buildString {
			lastRender?.let { lastRender ->
				repeat(50) { append('~') }
				append(" +")
				appendLine(lastRender.elapsedNow())
			}
			lastRender = systemClock.markNow()

			for (static in canvas.static) {
				appendLine(static.render())
			}

			appendLine(canvas.render())
		})
	}
}

internal object AnsiOutput : Output {
	private val out = AnsiConsole.out()!!
	private val encoder = UTF_8.newEncoder()!!
	private val stringBuilder = StringBuilder(100)
	private var lastHeight = 0

	override fun display(canvas: TextCanvas) {
		stringBuilder.apply {
			clear()

			repeat(lastHeight) {
				append("\u001B[F") // Cursor up line.
			}

			val staticLines = canvas.static.flatMap { it.render().split("\n") }
			val lines = canvas.render().split("\n")
			for (line in staticLines + lines) {
				append(line)
				append("\u001B[K") // Clear rest of line.
				append('\n')
			}

			// If the new output contains fewer lines than the last output, clear those old lines.
			val extraLines = lastHeight - (lines.size + staticLines.size)
			for (i in 0 until extraLines) {
				if (i > 0) {
					append('\n')
				}
				append("\u001B[K") // Clear line.
			}

			// Move cursor back up to end of the new output.
			repeat(extraLines - 1) {
				append("\u001B[F") // Cursor up line.
			}

			lastHeight = lines.size
		}

		// Write a single byte array to stdout to create an atomic visual change. If you instead write
		// the string, it will be UTF-8 encoded using an intermediate buffer that appears to be
		// periodically flushed to the underlying byte stream. This will cause fraction-of-a-second
		// flickers of broken content. Note that this only occurs with the AnsiConsole stream, but
		// there's no harm in doing it unconditionally.
		val bytes = encoder.encode(CharBuffer.wrap(stringBuilder))
		out.write(bytes.array(), 0, bytes.limit())

		// Explicitly flush to ensure the trailing line clear is sent. Empirically, this appears to be
		// buffered and not processed until the next frame, or not at all on the final frame.
		out.flush()
	}
}
