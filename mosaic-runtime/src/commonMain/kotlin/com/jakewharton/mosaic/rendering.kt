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

	fun StringBuilder.appendSurface(canvas: TextCanvas) {
		for (row in 0 until canvas.height) {
			canvas.appendRowTo(this, row, ansiLevel)
			append("\r\n")
		}
	}

	override fun render(mosaic: Mosaic): CharSequence {
		var failed = false
		val output = buildString {
			lastRender?.let { lastRender ->
				repeat(50) { append('~') }
				append(" +")
				append(lastRender.elapsedNow())
				append("\r\n")
			}
			lastRender = systemClock.markNow()

			append("NODES:\r\n")
			append(mosaic.dump().replace("\n", "\r\n"))
			append("\r\n\r\n")

			val statics = mutableObjectListOf<TextCanvas>()
			try {
				mosaic.paintStaticsTo(statics)
				if (statics.isNotEmpty()) {
					append("STATIC:\r\n")
					statics.forEach { static ->
						appendSurface(static)
					}
					append("\r\n")
				}
			} catch (t: Throwable) {
				failed = true
				append(t.stackTraceToString().replace("\n", "\r\n"))
				append("\r\n")
			}

			append("OUTPUT:\r\n")
			try {
				appendSurface(mosaic.paint())
			} catch (t: Throwable) {
				failed = true
				append(t.stackTraceToString().replace("\n", "\r\n"))
			}
		}
		if (failed) {
			throw RuntimeException("Failed\r\n\r\n$output")
		}
		return output
	}
}

internal class AnsiRendering(
	private val ansiLevel: AnsiLevel = AnsiLevel.TRUECOLOR,
	private val synchronizedRendering: Boolean = false,
) : Rendering {
	private val stringBuilder = StringBuilder(100)
	private val staticSurfaces = mutableObjectListOf<TextCanvas>()
	private var lastHeight = 0

	override fun render(mosaic: Mosaic): CharSequence {
		return stringBuilder.apply {
			clear()

			if (synchronizedRendering) {
				append(synchronizedRenderingEnable)
			}

			var staleLines = lastHeight
			if (staleLines > 0) {
				// Move to start of previous output.
				append(CSI)
				append(staleLines)
				append('F')
			}

			fun appendSurface(canvas: TextCanvas) {
				for (row in 0 until canvas.height) {
					if (staleLines-- > 0) {
						// We have previously drawn on this line. Clear first to be safe. For terminals which
						// do not support synchronized rendering, this may allow seeing a partial row render.
						append(clearLine)
					}
					canvas.appendRowTo(this, row, ansiLevel)
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
			if (staleLines > 0) {
				append(clearDisplay)
			}

			if (synchronizedRendering) {
				append(synchronizedRenderingDisable)
			}

			lastHeight = surface.height
		}
	}
}
