package com.jakewharton.mosaic.ui

import androidx.compose.runtime.Applier
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReusableComposeNode
import com.jakewharton.mosaic.TextCanvas
import com.jakewharton.mosaic.TextSurface
import com.jakewharton.mosaic.layout.DebugPolicy
import com.jakewharton.mosaic.layout.DrawPolicy
import com.jakewharton.mosaic.layout.Measurable
import com.jakewharton.mosaic.layout.MeasurePolicy
import com.jakewharton.mosaic.layout.MeasureScope
import com.jakewharton.mosaic.layout.MosaicNode
import com.jakewharton.mosaic.layout.StaticPaintPolicy

@Composable
internal inline fun Node(
	content: @Composable () -> Unit = {},
	measurePolicy: MeasurePolicy,
	drawPolicy: DrawPolicy?,
	staticPaintPolicy: StaticPaintPolicy?,
	debugPolicy: DebugPolicy,
) {
	ReusableComposeNode<MosaicNode, Applier<Any>>(
		factory = NodeFactory,
		update = {
			set(measurePolicy) { this.measurePolicy = measurePolicy }
			set(drawPolicy) { this.drawPolicy = drawPolicy }
			set(staticPaintPolicy) { this.staticPaintPolicy = staticPaintPolicy }
			set(debugPolicy) { this.debugPolicy = debugPolicy }
		},
		content = content,
	)
}

internal val NodeFactory: () -> MosaicNode = {
	MosaicNode(
		measurePolicy = ThrowingPolicy,
		drawPolicy = ThrowingPolicy,
		staticPaintPolicy = ThrowingPolicy,
		debugPolicy = ThrowingPolicy,
	)
}

private val ThrowingPolicy = object : MeasurePolicy, DrawPolicy, StaticPaintPolicy, DebugPolicy {
	override fun MeasureScope.measure(measurables: List<Measurable>) = throw AssertionError()
	override fun performDraw(canvas: TextCanvas) = throw AssertionError()
	override fun MosaicNode.performPaintStatics(statics: MutableList<TextSurface>) = throw AssertionError()
	override fun MosaicNode.renderDebug() = throw AssertionError()
}
