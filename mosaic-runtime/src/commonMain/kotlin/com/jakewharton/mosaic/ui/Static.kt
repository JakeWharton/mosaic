@file:JvmName("Static")

package com.jakewharton.mosaic.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import com.jakewharton.mosaic.Node
import kotlin.jvm.JvmName

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
		measurePolicy = { measurables ->
			val placeables = measurables.map { measurable ->
				measurable.measure()
			}

			layout(0, 0) {
				// Despite reporting no size to our parent, we still place each child at
				// 0,0 since they will be individually rendered.
				placeables.forEach { placeable ->
					placeable.place(0, 0)
				}
			}
		},
		drawPolicy = {
			// Nothing to do. Children rendered separately.
		},
		staticPaintPolicy = { statics ->
			if (children.isNotEmpty()) {
				for (child in children) {
					statics += child.paint()
					child.paintStatics(statics)
				}
				lastDrawn = lastRendered
			}
		},
		debugPolicy = {
			children.joinToString(prefix = "Static()") { "\n" + it.toString().prependIndent("  ") }
		},
	)
}
