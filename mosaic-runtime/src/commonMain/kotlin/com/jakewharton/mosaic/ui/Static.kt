@file:JvmName("Static")

package com.jakewharton.mosaic.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import com.jakewharton.mosaic.modifier.Modifier
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
		measurePolicy = { measurables, constraints ->
			val placeables = measurables.map { measurable ->
				measurable.measure(constraints)
			}

			layout(0, 0) {
				// Despite reporting no size to our parent, we still place each child at
				// 0,0 since they will be individually rendered.
				placeables.forEach { placeable ->
					placeable.place(0, 0)
				}
			}
		},
		modifiers = Modifier,
		debugPolicy = {
			children.joinToString(prefix = "Static()") { "\n" + it.toString().prependIndent("  ") }
		},
		factory = StaticNodeFactory {
			lastDrawn = lastRendered
		},
	)
}
