package com.jakewharton.mosaic.ui

import androidx.compose.runtime.Applier
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ComposeNode
import androidx.compose.runtime.currentComposer
import com.jakewharton.mosaic.layout.DebugPolicy
import com.jakewharton.mosaic.layout.Measurable
import com.jakewharton.mosaic.layout.MeasurePolicy
import com.jakewharton.mosaic.layout.MeasureResult
import com.jakewharton.mosaic.layout.MeasureScope
import com.jakewharton.mosaic.layout.MosaicNode
import com.jakewharton.mosaic.modifier.Modifier
import com.jakewharton.mosaic.modifier.materialize
import com.jakewharton.mosaic.ui.unit.Constraints
import kotlin.jvm.JvmField

@Composable
@MosaicComposable
@Suppress("ktlint:compose:content-trailing-lambda") // Not for public use.
internal inline fun Node(
	measurePolicy: MeasurePolicy,
	debugPolicy: DebugPolicy,
	modifier: Modifier = Modifier,
	content:
	@Composable @MosaicComposable
	() -> Unit = {},
	isStatic: Boolean = false,
) {
	val materializedModifier = currentComposer.materialize(modifier)
	ComposeNode<MosaicNode, Applier<Any>>(
		factory = if (isStatic) StaticFactory else NodeFactory,
		update = {
			set(measurePolicy, SetMeasurePolicy)
			set(materializedModifier, SetModifier)
			set(debugPolicy, SetDebugPolicy)
		},
		content = content,
	)
}

@JvmField
internal val SetModifier: MosaicNode.(Modifier) -> Unit = { setModifier(it) }

@JvmField
internal val SetMeasurePolicy: MosaicNode.(MeasurePolicy) -> Unit = { measurePolicy = it }

@JvmField
internal val SetDebugPolicy: MosaicNode.(DebugPolicy) -> Unit = { debugPolicy = it }

@JvmField
internal val NodeFactory: () -> MosaicNode = {
	MosaicNode(
		measurePolicy = ThrowingPolicy,
		debugPolicy = ThrowingPolicy,
		isStatic = false,
	)
}

private val StaticFactory: () -> MosaicNode = {
	MosaicNode(
		measurePolicy = ThrowingPolicy,
		debugPolicy = ThrowingPolicy,
		isStatic = true,
	)
}

private val ThrowingPolicy = object : MeasurePolicy, DebugPolicy {
	override fun MeasureScope.measure(
		measurables: List<Measurable>,
		constraints: Constraints,
	): MeasureResult = throw AssertionError()

	override fun MosaicNode.renderDebug() = throw AssertionError()
}
