package com.jakewharton.mosaic

import kotlin.time.ExperimentalTime
import kotlin.time.TimeMark
import kotlin.time.TimeSource

internal interface Rendering {
	/**
	 * Render [node] to a single string for display.
	 *
	 * Note: The returned [CharSequence] is only valid until the next call to this function,
	 * as implementations are free to reuse buffers across invocations.
	 */
	fun render(node: MosaicNode): CharSequence
}

@ExperimentalTime
internal class DebugRendering(
	private val systemClock: TimeSource = TimeSource.Monotonic,
) : Rendering {
	private var lastRender: TimeMark? = null

	override fun render(node: MosaicNode): CharSequence {
		var failed = false
		val output = buildString {
			lastRender?.let { lastRender ->
				repeat(50) { append('~') }
				append(" +")
				appendLine(lastRender.elapsedNow())
			}
			lastRender = systemClock.markNow()

			node.measureAndPlace()
			appendLine("NODES:")
			appendLine(node)
			appendLine()

			try {
				val statics = ArrayList<TextSurface>()
					.also { node.paintStatics(it) }
					.map { it.render() }
				if (statics.isNotEmpty()) {
					appendLine("STATIC:")
					for (static in statics) {
						appendLine(static)
					}
					appendLine()
				}
			} catch (t: Throwable) {
				failed = true
				appendLine("STATIC:")
				appendLine(t.stackTraceToString())
			}

			appendLine("OUTPUT:")
			try {
				appendLine(node.paint().render())
			} catch (t: Throwable) {
				failed = true
				append(t.stackTraceToString())
			}
		}
		if (failed) {
			throw RuntimeException("Failed\n\n$output")
		}
		return output
	}
}

internal class AnsiRendering : Rendering {
	private val stringBuilder = StringBuilder(100)
	private val staticSurfaces = ArrayList<TextSurface>()
	private var lastHeight = 0

	override fun render(node: MosaicNode): CharSequence {
		return stringBuilder.apply {
			clear()

			var staleLines = lastHeight
			repeat(staleLines) {
				append(cursorUp)
			}

			fun appendSurface(canvas: TextSurface) {
				for (row in 0 until canvas.height) {
					canvas.appendRowTo(this, row)
					if (staleLines-- > 0) {
						// We have previously drawn on this line. Clear the rest to be safe.
						append(clearLine)
					}
					append('\n')
				}
			}

			node.measureAndPlace()

			node.paintStatics(staticSurfaces)
			for (staticSurface in staticSurfaces) {
				appendSurface(staticSurface)
			}
			staticSurfaces.clear()

			val surface = node.paint()
			appendSurface(surface)

			// If the new output contains fewer lines than the last output, clear those old lines.
			for (i in 0 until staleLines) {
				if (i > 0) {
					append('\n')
				}
				append(clearLine)
			}

			// Move cursor back up to end of the new output.
			repeat(staleLines - 1) {
				append(cursorUp)
			}

			lastHeight = surface.height
		}
	}
}
