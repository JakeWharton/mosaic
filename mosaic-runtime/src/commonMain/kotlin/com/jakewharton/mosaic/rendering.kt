package com.jakewharton.mosaic

import com.jakewharton.mosaic.terminal.AnsiLevel
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
	private val ansiLevel: AnsiLevel,
	private val supportsKittyUnderlines: Boolean,
	private val systemClock: TimeSource,
) : Rendering {
	private var lastRender: TimeMark? = null

	fun StringBuilder.appendSurface(canvas: TextCanvas) {
		for (row in 0 until canvas.height) {
			canvas.appendRowTo(this, row, ansiLevel, supportsKittyUnderlines)
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
			append(mosaic.dumpNodes().replace("\n", "\r\n"))
			append("\r\n\r\n")

			try {
				mosaic.static()?.let { static ->
					append("STATIC:\r\n")
					append(static)
					append("\r\n\r\n")
				}
			} catch (t: Throwable) {
				failed = true
				append(t.stackTraceToString().replace("\n", "\r\n"))
				append("\r\n")
			}

			append("OUTPUT:\r\n")
			try {
				appendSurface(mosaic.draw())
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
	private val ansiLevel: AnsiLevel,
	private val synchronizedOutput: Boolean,
	private val supportsKittyUnderlines: Boolean,
) : Rendering {
	private val stringBuilder = StringBuilder(100)
	private var lastHeight = 0

	override fun render(mosaic: Mosaic): CharSequence {
		return stringBuilder.apply {
			clear()

			if (synchronizedOutput) {
				append(synchronizedOutputEnable)
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
					canvas.appendRowTo(this, row, ansiLevel, supportsKittyUnderlines)
					append("\r\n")
				}
			}

			mosaic.static()?.let { static ->
				// We don't know the width or height of static strings, and we don't want to waste time
				// parsing them to get an accurate count of each to minimally clear only the lines it will
				// write. Instead, just clear everything to ensure we neither leave old cells nor attempt
				// a clear to end-of-line when the cursor is on the last column (thus clearing that cell).
				if (staleLines > 0) {
					append(clearDisplay)
					staleLines = 0
				}
				append(static)
				append("\r\n")
			}

			val surface = mosaic.draw()
			appendSurface(surface)

			// If the new output contains fewer lines than the last output, clear those old lines.
			if (staleLines > 0) {
				append(clearDisplay)
			}

			if (synchronizedOutput) {
				append(synchronizedOutputDisable)
			}

			lastHeight = surface.height
		}
	}
}
