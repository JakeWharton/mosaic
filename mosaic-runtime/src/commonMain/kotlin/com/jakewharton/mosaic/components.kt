package com.jakewharton.mosaic

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import de.cketti.codepoints.codePointCount

@Composable
public fun Text(
	value: String,
	color: Color? = null,
	background: Color? = null,
	style: TextStyle? = null,
) {
	Layout(
		debugInfo = {
			"""Text("$value")"""
		},
		measurePolicy = {
			val lines = value.split('\n')
			val width = lines.maxOf { it.codePointCount(0, it.length) }
			val height = lines.size
			layout(width, height) {
				// Nothing to do. No children.
			}
		},
		drawPolicy = { canvas ->
			value.split('\n').forEachIndexed { index, line ->
				canvas.write(index, 0, line, color, background, style)
			}
		},
	)
}

@Immutable
public class Color private constructor(
	internal val fg: Int,
	internal val bg: Int,
) {
	public companion object {
		@Stable
		public val Black: Color = Color(30, 40)
		@Stable
		public val Red: Color = Color(31, 41)
		@Stable
		public val Green: Color = Color(32, 42)
		@Stable
		public val Yellow: Color = Color(33, 43)
		@Stable
		public val Blue: Color = Color(34, 44)
		@Stable
		public val Magenta: Color = Color(35, 45)
		@Stable
		public val Cyan: Color = Color(36, 46)
		@Stable
		public val White: Color = Color(37, 47)
		@Stable
		public val BrightBlack: Color = Color(90, 100)
		@Stable
		public val BrightRed: Color = Color(91, 101)
		@Stable
		public val BrightGreen: Color = Color(92, 102)
		@Stable
		public val BrightYellow: Color = Color(93, 103)
		@Stable
		public val BrightBlue: Color = Color(94, 104)
		@Stable
		public val BrightMagenta: Color = Color(95, 105)
		@Stable
		public val BrightCyan: Color = Color(96, 106)
		@Stable
		public val BrightWhite: Color = Color(97, 107)
	}
}

@Immutable
public class TextStyle private constructor(
	private val bits: Int,
) {
	public operator fun plus(other: TextStyle): TextStyle = TextStyle(bits or other.bits)
	public operator fun contains(style: TextStyle): Boolean = (style.bits and bits) != 0

	public companion object {
		@Stable
		public val None: TextStyle = TextStyle(0)
		@Stable
		public val Underline: TextStyle = TextStyle(1)
		@Stable
		public val Strikethrough: TextStyle = TextStyle(2)
		@Stable
		public val Bold: TextStyle = TextStyle(4)
		@Stable
		public val Dim: TextStyle = TextStyle(8)
		@Stable
		public val Italic: TextStyle = TextStyle(16)
		@Stable
		public val Invert: TextStyle = TextStyle(32)
	}
}

@Composable
public fun Row(content: @Composable () -> Unit) {
	Layout(content, { "Row()" }) { measurables ->
		var width = 0
		var height = 0
		val placeables = measurables.map { measurable ->
			measurable.measure().also { placeable ->
				width += placeable.width
				height = maxOf(height, placeable.height)
			}
		}
		layout(width, height) {
			var x = 0
			for (placeable in placeables) {
				placeable.place(x, 0)
				x += placeable.width
			}
		}
	}
}

@Composable
public fun Column(content: @Composable () -> Unit) {
	Layout(content, { "Column()" }) { measurables ->
		var width = 0
		var height = 0
		val placeables = measurables.map { measurable ->
			measurable.measure().also { placeable ->
				width = maxOf(width, placeable.width)
				height += placeable.height
			}
		}
		layout(width, height) {
			var y = 0
			for (placeable in placeables) {
				placeable.place(0, y)
				y += placeable.height
			}
		}
	}
}

/**
 * Will render each value emitted by [items] as permanent output above the
 * regular display.
 */
@Composable
public fun <T> Static(
	items: SnapshotStateList<T>,
	content: @Composable (T) -> Unit,
) {
	var lastDrawn by remember { mutableStateOf(0) }
	var lastRendered by remember { mutableStateOf(0) }

	Node(
		content = {
			for (i in lastDrawn until items.size) {
				val item = items[i]
				content(item)
			}
			lastRendered = items.size
		},
		measurePolicy = {
			layout(0, 0) {
				// Nothing to do. Children rendered separately.
			}
		},
		drawPolicy = {
			// Nothing to do. Children rendered separately.
		},
		staticDrawPolicy = {
			val statics = if (children.isNotEmpty()) {
				buildList {
					for (child in children) {
						add(child.draw())
						addAll(child.drawStatics())
					}
					lastDrawn = lastRendered
				}
			} else {
				emptyList()
			}

			statics
		},
		debugPolicy = {
			children.joinToString(prefix = "Static()") { "\n" + it.toString().prependIndent("  ") }
		},
	)
}
