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
	class Item(val value: T, var rendered: Boolean)

	// Keep list of items which have not yet been rendered.
	val pending = remember { mutableStateListOf<Item>() }

	LaunchedEffect(items) {
		items.collect {
			pending.add(Item(it, rendered = false))
		}
	}

	ComposeNode<StaticNode, MosaicNodeApplier>(
		factory = {
			StaticNode {
				pending.removeAll { it.rendered }
			}
		},
		update = {},
		content = {
			for (item in pending) {
				Row {
					// Render item and mark it as having been included in render.
					content(item.value)
					item.rendered = true
				}
			}
		},
	)
}

internal class StaticNode(
	private val postRender: () -> Unit,
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

	override fun renderTo(canvas: TextCanvas) {
		// No content.
	}

	override fun renderStatics(): List<TextCanvas> {
		val statics = mutableListOf<TextCanvas>()

		// Render contents of static node to a separate display.
		val static = box.render()

		// Add display canvas to static canvases if it is not empty.
		if (static.width > 0 && static.height > 0) {
			statics.add(static)
		}

		// Propagate any static content of the display.
		statics.addAll(box.renderStatics())

		postRender()

		return statics
	}

	override fun toString() = box.children.joinToString(prefix = "Static(", postfix = ")")
}
