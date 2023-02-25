package com.jakewharton.mosaic

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import de.cketti.codepoints.codePointCount
import kotlinx.coroutines.CoroutineStart.UNDISPATCHED
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

@Composable
public fun Text(
	value: String,
	color: Color? = null,
	background: Color? = null,
	style: TextStyle? = null,
) {
	Node(
		measurePolicy = {
			val lines = value.split('\n')
			width = lines.maxOf { it.codePointCount(0, it.length) }
			height = lines.size
		},
		layoutPolicy = { },
		drawPolicy = { canvas ->
			value.split('\n').forEachIndexed { index, line ->
				canvas.write(index, 0, line, color, background, style)
			}
		},
		staticDrawPolicy = { emptyList() },
		debugPolicy = {
			"""Text("$value", x=$x, y=$y, width=$width, height=$height)"""
		}
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
	Node(
		content = content,
		measurePolicy = {
			var width = 0
			var height = 0
			for (child in children) {
				child.measure()
				width += child.width
				height = maxOf(height, child.height)
			}
			this.width = width
			this.height = height
		},
		layoutPolicy = {
			var childX = 0
			for (child in children) {
				child.x = childX
				child.y = 0
				child.layout()
				childX += child.width
			}
		},
		drawPolicy = DrawPolicy.Children,
		staticDrawPolicy = StaticDrawPolicy.Children,
		debugPolicy = {
			children.joinToString(prefix = "Row()") { "\n" + it.toString().prependIndent("  ") }
		},
	)
}

@Composable
public fun Column(content: @Composable () -> Unit) {
	Node(
		content = content,
		measurePolicy = {
			var width = 0
			var height = 0
			for (child in children) {
				child.measure()
				width = maxOf(width, child.width)
				height += child.height
			}
			this.width = width
			this.height = height
		},
		layoutPolicy = {
			var childY = 0
			for (child in children) {
				child.x = 0
				child.y = childY
				child.layout()
				childY += child.height
			}
		},
		drawPolicy = DrawPolicy.Children,
		staticDrawPolicy = StaticDrawPolicy.Children,
		debugPolicy = {
			children.joinToString(prefix = "Column()") { "\n" + it.toString().prependIndent("  ") }
		},
	)
}

/**
 * Will render each value emitted by [items] as permanent output above the
 * regular display.
 */
@Composable
public fun <T> Static(
	items: Flow<T>,
	content: @Composable (T) -> Unit,
) {
	class Item(val value: T, var drawn: Boolean)

	// Keep list of items which have not yet been drawn.
	val pending = remember { mutableStateListOf<Item>() }

	// We use all this manual scope/job/launch stuff instead of a LaunchedEffect so that
	// the items collection occurs within the same recomposition as it is created.
	val scope = rememberCoroutineScope()
	var job by remember { mutableStateOf<Job?>(null) }
	var seenItems by remember { mutableStateOf<Flow<T>?>(null) }
	if (seenItems !== items) {
		job?.cancel()

		seenItems = items
		job = scope.launch(start = UNDISPATCHED) {
			items.collect {
				pending.add(Item(it, drawn = false))
			}
		}
	}

	Node(
		content = {
			for (item in pending) {
				content(item.value)
				item.drawn = true
			}
		},
		measurePolicy = {
			for (child in children) {
				child.measure()
			}
		},
		layoutPolicy = {},
		drawPolicy = {},
		staticDrawPolicy = {
			val statics = if (children.isNotEmpty()) {
				buildList {
					for (child in children) {
						add(child.draw())
						addAll(child.drawStatics())
					}
					pending.removeAll { it.drawn }
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
