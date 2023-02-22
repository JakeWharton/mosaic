package com.jakewharton.mosaic

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ComposeNode
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import kotlinx.coroutines.flow.Flow

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

	LaunchedEffect(items) {
		items.collect {
			pending.add(Item(it, drawn = false))
		}
	}

	ComposeNode<StaticNode, MosaicNodeApplier>(
		factory = {
			StaticNode {
				pending.removeAll { it.drawn }
			}
		},
		update = {},
		content = {
			for (item in pending) {
				Row {
					// Draw item and mark it as such.
					content(item.value)
					item.drawn = true
				}
			}
		},
	)
}

internal class StaticNode(
	private val onPostDraw: () -> Unit,
) : ContainerNode() {
	// Delegate container column for static content.
	private val box = LinearNode(isRow = false)

	override val children: MutableList<MosaicNode>
		get() = box.children

	override fun measure() {
		// Not visible.
	}

	override fun layout() {
		// Not visible.
	}

	override fun drawTo(canvas: TextCanvas) {
		// No content.
	}

	override fun drawStatics(): List<TextCanvas> {
		val statics = mutableListOf<TextCanvas>()

		// Draw contents of static node to a separate node hierarchy.
		val static = box.draw()

		// Add display canvas to static canvases if it is not empty.
		if (static.width > 0 && static.height > 0) {
			statics.add(static)
		}

		// Propagate any static content of this static node.
		statics.addAll(box.drawStatics())

		onPostDraw()

		return statics
	}

	override fun toString() = box.children.joinToString(prefix = "Static(", postfix = ")")
}
