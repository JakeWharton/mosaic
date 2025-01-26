@file:JvmName("Static")

package com.jakewharton.mosaic.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.snapshots.SnapshotStateList
import com.jakewharton.mosaic.modifier.Modifier
import kotlin.jvm.JvmName

@Deprecated(
	"Loop over items list yourself and call Static on each item",
	ReplaceWith("items.forEach { Static { content(it) } }"),
)
@Composable
public fun <T> Static(
	items: SnapshotStateList<T>,
	content: @Composable (item: T) -> Unit,
) {
	items.forEach {
		Static {
			content(it)
		}
	}
}

/**
 * Will render each value emitted by [items] as permanent output above the
 * regular display.
 */
@Composable
public fun Static(
	content: @Composable () -> Unit,
) {
	Node(
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
		debugPolicy = {
			children.joinToString(prefix = "Static()") { "\n" + it.toString().prependIndent("  ") }
		},
		modifier = Modifier,
		content = content,
		isStatic = true,
	)
}
