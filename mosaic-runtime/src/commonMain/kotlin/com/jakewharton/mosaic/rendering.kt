package com.jakewharton.mosaic

import androidx.collection.mutableObjectListOf
import com.jakewharton.mosaic.ui.AnsiLevel
import kotlin.time.TimeMark
import kotlin.time.TimeSource

internal interface Rendering {
	/**
	 * Render [node] to a single string for display.
	 *
	 * Note: The returned [CharSequence] is only valid until the next call to this function,
	 * as implementations are free to reuse buffers across invocations.
	 */
	fun render(mosaic: Mosaic): CharSequence
}

internal class DebugRendering(
	private val systemClock: TimeSource = TimeSource.Monotonic,
	private val ansiLevel: AnsiLevel = AnsiLevel.TRUECOLOR,
) : Rendering {
	private var lastRender: TimeMark? = null

	override fun render(mosaic: Mosaic): CharSequence {
		var failed = false
		val output = buildString {
			lastRender?.let { lastRender ->
				repeat(50) { append('~') }
				append(" +")
				appendLine(lastRender.elapsedNow())
			}
			lastRender = systemClock.markNow()

			appendLine("NODES:")
			appendLine(mosaic.dump())
			appendLine()

			val statics = mutableObjectListOf<TextCanvas>()
			try {
				mosaic.paintStaticsTo(statics)
				if (statics.isNotEmpty()) {
					appendLine("STATIC:")
					statics.forEach { static ->
						appendLine(static.render(ansiLevel))
					}
					appendLine()
				}
			} catch (t: Throwable) {
				failed = true
				appendLine(t.stackTraceToString())
			}

			appendLine("OUTPUT:")
			try {
				appendLine(mosaic.paint().render(ansiLevel))
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
	private val stringBuilder = StringBuilder(100)
	private val staticSurfaces = mutableObjectListOf<TextCanvas>()
	private var lastHeight = 0

	override fun render(mosaic: Mosaic): CharSequence {
		return stringBuilder.apply {
			clear()

			append(ansiBeginSynchronizedUpdate)

			var staleLines = lastHeight
			repeat(staleLines) {
				append(cursorUp)
			}

			fun appendSurface(canvas: TextCanvas) {
				for (row in 0 until canvas.height) {
					canvas.appendRowTo(this, row, ansiLevel)
					if (staleLines-- > 0) {
						// We have previously drawn on this line. Clear the rest to be safe.
						append(clearLine)
					}
					append("\r\n")
				}
			}

			staticSurfaces.let { staticSurfaces ->
				mosaic.paintStaticsTo(staticSurfaces)
				if (staticSurfaces.isNotEmpty()) {
					staticSurfaces.forEach { staticSurface ->
						appendSurface(staticSurface)
					}
					staticSurfaces.clear()
				}
			}

			val surface = mosaic.paint()
			appendSurface(surface)

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

			lastHeight = surface.height
		}
	}
}
