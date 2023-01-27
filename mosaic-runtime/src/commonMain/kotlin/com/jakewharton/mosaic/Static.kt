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

	Static(
		postRender = {
			// Remove any items which have been rendered.
			pending.removeAll { it.rendered }
		}
	) {
		for (item in pending) {
			Row {
				// Render item and mark it as having been included in render.
				content(item.value)
				item.rendered = true
			}
		}
	}
}

/**
 * Renders [content] permanently above the normal canvas. When content has
 * actually been written to a [TextCanvas], the [postRender] callback will be
 * invoked to allow clearing of content.
 *
 * @param postRender Callback after rendering to a [TextCanvas] is complete.
 * @param content Content which should be rendered permanently above normal
 * canvas.
 */
@Composable
internal fun Static(
	postRender: () -> Unit = {},
	content: @Composable () -> Unit,
) {
	ComposeNode<StaticNode, MosaicNodeApplier>(
		factory = ::StaticNode,
		update = {
			set(postRender) {
				this.postRender = postRender
			}
		},
		content = content,
	)
}

internal class StaticNode : ContainerNode() {
	// Delegate container column for static content.
	private val box = LinearNode(isRow = false)

	override val children: MutableList<MosaicNode>
		get() = box.children

	var postRender: () -> Unit = {}

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
