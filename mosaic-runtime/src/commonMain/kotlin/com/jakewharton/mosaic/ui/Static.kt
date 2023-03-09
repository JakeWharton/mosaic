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
