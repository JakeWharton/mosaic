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
		return buildString {
			lastRender?.let { lastRender ->
				repeat(50) { append('~') }
				append(" +")
				appendLine(lastRender.elapsedNow())
			}
			lastRender = systemClock.markNow()

			val surface = node.draw()
			val staticSurfaces = node.drawStatics()

			appendLine("NODES:")
			appendLine(node)
			appendLine()
			if (staticSurfaces.isNotEmpty()) {
				appendLine("STATIC:")
				for (staticSurface in staticSurfaces) {
					appendLine(staticSurface.render())
				}
				appendLine()
			}
			appendLine("OUTPUT:")
			appendLine(surface.render())
		}
	}
}

internal class AnsiRendering : Rendering {
	private val stringBuilder = StringBuilder(100)
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

			val staticSurfaces = node.drawStatics()
			for (staticSurface in staticSurfaces) {
				appendSurface(staticSurface)
			}

			val surface = node.draw()
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
