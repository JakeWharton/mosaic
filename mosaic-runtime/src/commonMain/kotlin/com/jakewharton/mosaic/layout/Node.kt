package com.jakewharton.mosaic.layout

import com.jakewharton.mosaic.TextCanvas
import com.jakewharton.mosaic.TextSurface
import com.jakewharton.mosaic.layout.Placeable.PlacementScope
import com.jakewharton.mosaic.modifier.Modifier

internal fun interface DebugPolicy {
	fun MosaicNode.renderDebug(): String
}

internal abstract class MosaicNodeLayer : Measurable, Placeable(), PlacementScope, MeasureScope {
	abstract fun drawTo(canvas: TextCanvas)
}

internal abstract class AbstractMosaicNodeLayer(
	private val next: MosaicNodeLayer?,
	private val isStatic: Boolean,
) : MosaicNodeLayer() {
	private var measureResult: MeasureResult = NotMeasured

	final override var parentData: Any? = null

	final override val width get() = measureResult.width
	final override val height get() = measureResult.height

	override fun measure() = apply {
		measureResult = doMeasure()
	}

	protected open fun doMeasure(): MeasureResult {
		val placeable = next!!.measure()
		return object : MeasureResult {
			override val width: Int get() = placeable.width
			override val height: Int get() = placeable.height

			override fun placeChildren() {
				placeable.place(0, 0)
			}
		}
	}

	final override var x = 0
		private set
	final override var y = 0
		private set

	final override fun placeAt(x: Int, y: Int) {
		// If this layer belongs to a static node, ignore the placement coordinates from the parent.
		// We reset the coordinate system to draw at 0,0 since static drawing will be on a canvas
		// sized to this node's width and height.
		if (!isStatic) {
			this.x = x
			this.y = y
		}
		measureResult.placeChildren()
	}

	override fun drawTo(canvas: TextCanvas) {
		next?.drawTo(canvas)
	}
}

internal object NotMeasured : MeasureResult {
	override val width get() = 0
	override val height get() = 0
	override fun placeChildren() = throw UnsupportedOperationException("Not measured")
}

internal class MosaicNode(
	var measurePolicy: MeasurePolicy,
	var debugPolicy: DebugPolicy,
	val onStaticDraw: (() -> Unit)?,
) : Measurable {
	val isStatic get() = onStaticDraw != null
	val children = mutableListOf<MosaicNode>()

	private val bottomLayer: MosaicNodeLayer = BottomLayer(this)
	var topLayer: MosaicNodeLayer = bottomLayer
		private set

	override var parentData: Any? = null
		private set

	var modifiers: Modifier = Modifier
		set(value) {
			topLayer = value.foldOut(bottomLayer) { element, lowerLayer ->
				when (element) {
					is LayoutModifier -> LayoutLayer(element, lowerLayer)
					is DrawModifier -> DrawLayer(element, lowerLayer)
					is ParentDataModifier -> {
						parentData = element.modifyParentData(parentData)
						lowerLayer
					}
					else -> lowerLayer
				}
			}
			field = value
		}

	override fun measure(): Placeable = topLayer.apply { measure() }

	val width: Int get() = topLayer.width
	val height: Int get() = topLayer.height
	val x: Int get() = topLayer.x
	val y: Int get() = topLayer.y

	fun measureAndPlace() {
		val placeable = measure()
		topLayer.run { placeable.place(0, 0) }
	}

	/**
	 * Draw this node to a [TextSurface].
	 * A call to [measureAndPlace] must precede calls to this function.
	 */
	fun paint(): TextSurface {
		val surface = TextSurface(width, height)
		topLayer.drawTo(surface)
		return surface
	}

	/**
	 * Append any static [TextSurfaces][TextSurface] to [statics].
	 * A call to [measureAndPlace] must precede calls to this function.
	 */
	fun paintStatics(statics: MutableList<TextSurface>) {
		for (child in children) {
			if (isStatic) {
				statics += child.paint()
			}
			child.paintStatics(statics)
		}
		onStaticDraw?.invoke()
	}

	override fun toString() = debugPolicy.run { renderDebug() }
}

private class BottomLayer(
	private val node: MosaicNode,
) : AbstractMosaicNodeLayer(null, node.isStatic) {
	override fun doMeasure(): MeasureResult {
		return node.measurePolicy.run { measure(node.children) }
	}

	override fun drawTo(canvas: TextCanvas) {
		for (child in node.children) {
			if (child.width != 0 && child.height != 0) {
				child.topLayer.drawTo(canvas)
			}
		}
	}
}

private class LayoutLayer(
	private val element: LayoutModifier,
	private val lowerLayer: MosaicNodeLayer,
) : AbstractMosaicNodeLayer(lowerLayer, false) {
	override fun doMeasure(): MeasureResult {
		return element.run { measure(lowerLayer) }
	}
}

private class DrawLayer(
	private val element: DrawModifier,
	private val lowerLayer: MosaicNodeLayer,
) : AbstractMosaicNodeLayer(lowerLayer, false) {
	override fun drawTo(canvas: TextCanvas) {
		val oldX = canvas.translationX
		val oldY = canvas.translationY
		canvas.translationX = x
		canvas.translationY = y
		val scope = object : TextCanvasDrawScope(canvas, width, height), ContentDrawScope {
			override fun drawContent() {
				lowerLayer.drawTo(canvas)
			}
		}
		element.run { scope.draw() }
		canvas.translationX = oldX
		canvas.translationY = oldY
	}
}
