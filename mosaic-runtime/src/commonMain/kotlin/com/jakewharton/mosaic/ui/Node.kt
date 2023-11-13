package com.jakewharton.mosaic.ui

import androidx.compose.runtime.Applier
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ComposeNode
import com.jakewharton.mosaic.layout.DebugPolicy
import com.jakewharton.mosaic.layout.Measurable
import com.jakewharton.mosaic.layout.MeasurePolicy
import com.jakewharton.mosaic.layout.MeasureResult
import com.jakewharton.mosaic.layout.MeasureScope
import com.jakewharton.mosaic.layout.MosaicNode
import com.jakewharton.mosaic.modifier.Modifier
import com.jakewharton.mosaic.ui.unit.Constraints

@Composable
@MosaicComposable
internal inline fun Node(
	content:
	@Composable @MosaicComposable
	() -> Unit = {},
	modifiers: Modifier,
	measurePolicy: MeasurePolicy,
	debugPolicy: DebugPolicy,
	noinline factory: () -> MosaicNode,
) {
	ComposeNode<MosaicNode, Applier<Any>>(
		factory = factory,
		update = {
			set(measurePolicy) { this.measurePolicy = measurePolicy }
			set(modifiers) { this.modifiers = modifiers }
			set(debugPolicy) { this.debugPolicy = debugPolicy }
		},
		content = content,
	)
}

internal val NodeFactory: () -> MosaicNode = {
	MosaicNode(
		measurePolicy = ThrowingPolicy,
		debugPolicy = ThrowingPolicy,
		onStaticDraw = null,
	)
}

internal fun staticNodeFactory(onDraw: () -> Unit): () -> MosaicNode = {
	MosaicNode(
		measurePolicy = ThrowingPolicy,
		debugPolicy = ThrowingPolicy,
		onStaticDraw = onDraw,
	)
}

private val ThrowingPolicy = object : MeasurePolicy, DebugPolicy {
	override fun MeasureScope.measure(
		measurables: List<Measurable>,
		constraints: Constraints,
	): MeasureResult = throw AssertionError()

	override fun MosaicNode.renderDebug() = throw AssertionError()
}
