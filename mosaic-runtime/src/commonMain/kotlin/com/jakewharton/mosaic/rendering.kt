package com.jakewharton.mosaic

import com.jakewharton.mosaic.layout.MosaicNode
import com.jakewharton.mosaic.ui.AnsiLevel
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
	private val ansiLevel: AnsiLevel = AnsiLevel.TRUECOLOR,
) : Rendering {
	private var lastRender: TimeMark? = null

	private val staticTextSurfaces = mutableListOf<TextSurface>()
	private val textSurface = TextSurface(ansiLevel)

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
				node.paintStatics(staticTextSurfaces, ansiLevel)
				if (staticTextSurfaces.isNotEmpty()) {
					appendLine("STATIC:")
					for (surface in staticTextSurfaces) {
						appendLine(surface.render())
					}
					staticTextSurfaces.clear()
					appendLine()
				}
			} catch (t: Throwable) {
				failed = true
				appendLine("STATIC:")
				appendLine(t.stackTraceToString())
			}

			appendLine("OUTPUT:")
			try {
				textSurface.reset()
				textSurface.resize(node.width, node.height)
				node.paint(textSurface)
				appendLine(textSurface.render())
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

internal class AnsiRendering(
	private val ansiLevel: AnsiLevel = AnsiLevel.TRUECOLOR,
) : Rendering {
	private val stringBuilder = StringBuilder(128)
	private var lastHeight = 0

	private val staticTextSurfaces = mutableListOf<TextSurface>()
	private val textSurface = TextSurface(ansiLevel)

	override fun render(node: MosaicNode): CharSequence {
		return stringBuilder.apply {
			clear()

			append(ansiBeginSynchronizedUpdate)

			var staleLines = lastHeight
			repeat(staleLines) {
				append(cursorUp)
			}

			fun appendSurface(canvas: TextSurface) {
				if (canvas.width <= 0 || canvas.height <= 0) {
					return
				}
				for (row in 0 until canvas.height) {
					canvas.appendRowTo(this, row)
					if (staleLines-- > 0) {
						// We have previously drawn on this line. Clear the rest to be safe.
						append(clearLine)
					}
					append("\r\n")
				}
			}

			node.measureAndPlace()

			node.paintStatics(staticTextSurfaces, ansiLevel)
			if (staticTextSurfaces.isNotEmpty()) {
				for (surface in staticTextSurfaces) {
					appendSurface(surface)
				}
				staticTextSurfaces.clear()
			}

			textSurface.reset()
			textSurface.resize(node.width, node.height)
			node.paint(textSurface)
			appendSurface(textSurface)

			// If the new output contains fewer lines than the last output, clear those old lines.
			for (i in 0 until staleLines) {
				if (i > 0) {
					append("\r\n")
				}
				append(clearLine)
			}

			// Move cursor back up to end of the new output.
			repeat(staleLines - 1) {
				append(cursorUp)
			}

			append(ansiEndSynchronizedUpdate)

			lastHeight = textSurface.height
		}
	}
}
