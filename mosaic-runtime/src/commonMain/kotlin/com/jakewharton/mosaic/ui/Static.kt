@file:JvmName("Static")

package com.jakewharton.mosaic.ui

import androidx.compose.runtime.Applier
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ComposeNode
import androidx.compose.runtime.Composition
import androidx.compose.runtime.CompositionContext
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCompositionContext
import androidx.compose.runtime.snapshots.SnapshotStateList
import com.jakewharton.mosaic.MosaicNodeApplier
import com.jakewharton.mosaic.layout.MosaicNode
import kotlin.jvm.JvmName

/** Render each value emitted by [items] as permanent output above the regular display. */
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
 * Render [content] once as permanent output above the regular display.
 *
 * The [content] function will be recomposed once and then never again.
 * Any contained [SideEffect]s or [DisposableEffect]s will run (and be disposed),
 * but [LaunchedEffect]s will not launch.
 */
@Composable
public fun Static(
	content: @Composable () -> Unit,
) {
	val compositionContext = rememberCompositionContext()
	val state = remember {
		StaticState(compositionContext, content)
	}

	ComposeNode<MosaicNode, Applier<Any>>(
		factory = StaticFactory,
		update = {
			set(state, BindStateToNode)
		},
	)
}

private val BindStateToNode: MosaicNode.(StaticState) -> Unit = {
	staticState = it
	it.setNode(this)
}

private val StaticFactory: () -> MosaicNode = {
	MosaicNode(
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
		isStatic = true,
	)
}

@Stable
internal class StaticState(
	private val compositionContext: CompositionContext,
	private val content: @Composable () -> Unit,
) {
	fun setNode(parent: MosaicNode) {
		val applier = MosaicNodeApplier(parent)
		val composition = Composition(applier, compositionContext)
		composition.setContent(content)
		composition.dispose()
	}
}
