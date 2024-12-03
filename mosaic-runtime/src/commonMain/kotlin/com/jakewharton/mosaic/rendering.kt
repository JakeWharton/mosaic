package com.jakewharton.mosaic

import androidx.collection.mutableObjectListOf
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

	override fun render(node: MosaicNode): CharSequence {
		var failed = false
		val output = buildString {
			lastRender?.let { lastRender ->
				repeat(50) { append('~') }
				append(" +")
				appendLine(lastRender.elapsedNow())
			}
			lastRender = systemClock.markNow()

			appendLine("NODES:")
			appendLine(node)
			appendLine()

			val statics = mutableObjectListOf<TextSurface>()
			try {
				node.paintStatics(statics, ansiLevel)
				if (statics.isNotEmpty()) {
					appendLine("STATIC:")
					statics.forEach { static ->
						appendLine(static.render())
					}
					appendLine()
				}
			} catch (t: Throwable) {
				failed = true
				appendLine(t.stackTraceToString())
			}

			appendLine("OUTPUT:")
			try {
				appendLine(node.paint(ansiLevel).render())
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
	private val staticSurfaces = mutableObjectListOf<TextSurface>()
	private var lastHeight = 0

	override fun render(node: MosaicNode): CharSequence {
		return stringBuilder.apply {
			clear()

			append(ansiBeginSynchronizedUpdate)

			// don't need to move cursor up if there was zero or one line
			if (lastHeight > 1) {
				ansiMoveCursorUp(lastHeight - 1)
			}
			append(ansiMoveCursorToFirstColumn)

			var addLineBreakAtBeginning = false
			staticSurfaces.let { staticSurfaces ->
				node.paintStatics(staticSurfaces, ansiLevel)
				if (staticSurfaces.isNotEmpty()) {
					staticSurfaces.forEach { staticSurface ->
						appendSurface(staticSurface, addLineBreakAtBeginning)
						if (!addLineBreakAtBeginning && staticSurface.height > 0) {
							addLineBreakAtBeginning = true
						}
					}
					staticSurfaces.clear()
				}
			}

			val surface = node.paint(ansiLevel)
			if (node.height > 0) {
				appendSurface(surface, addLineBreakAtBeginning)
			}
			lastHeight = surface.height

			append(ansiClearAllAfterCursor)
			append(ansiEndSynchronizedUpdate)
		}
	}

	private fun StringBuilder.appendSurface(canvas: TextSurface, addLineBreakAtBeginning: Boolean) {
		for (rowIndex in 0 until canvas.height) {
			if (rowIndex > 0 || (rowIndex == 0 && addLineBreakAtBeginning)) {
				append("\r\n")
			}
			canvas.appendRowTo(this, rowIndex)
			append(ansiClearLineAfterCursor)
		}
	}
}
