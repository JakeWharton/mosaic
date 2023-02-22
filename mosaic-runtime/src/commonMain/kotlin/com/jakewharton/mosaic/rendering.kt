package com.jakewharton.mosaic

import kotlin.time.ExperimentalTime
import kotlin.time.TimeMark
import kotlin.time.TimeSource

internal interface Rendering {
	/**
	 * Render [canvas] and [statics] to a single string for display.
	 *
	 * Note: The returned [CharSequence] is only valid until the next call to this function,
	 * as implementations are free to reuse buffers across invocations.
	 */
	fun render(canvas: TextCanvas, statics: List<TextCanvas> = emptyList()): CharSequence
}

@ExperimentalTime
internal class DebugRendering(
	private val systemClock: TimeSource = TimeSource.Monotonic,
) : Rendering {
	private var lastRender: TimeMark? = null

	override fun render(canvas: TextCanvas, statics: List<TextCanvas>): CharSequence {
		return buildString {
			lastRender?.let { lastRender ->
				repeat(50) { append('~') }
				append(" +")
				appendLine(lastRender.elapsedNow())
			}
			lastRender = systemClock.markNow()

			for (static in statics) {
				appendLine(static.render())
			}

			appendLine(canvas.render())
		}
	}
}

internal class AnsiRendering : Rendering {
	private val stringBuilder = StringBuilder(100)
	private var lastHeight = 0

	override fun render(canvas: TextCanvas, statics: List<TextCanvas>): CharSequence {
		return stringBuilder.apply {
			clear()

			repeat(lastHeight) {
				append("\u001B[F") // Cursor up line.
			}

			val staticLines = statics.flatMap { it.render().split("\n") }
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
	}
}
