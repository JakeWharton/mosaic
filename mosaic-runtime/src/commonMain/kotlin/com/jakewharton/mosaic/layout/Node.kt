package com.jakewharton.mosaic.layout

import com.jakewharton.mosaic.TextCanvas
import com.jakewharton.mosaic.TextSurface
import com.jakewharton.mosaic.layout.Placeable.PlacementScope
import com.jakewharton.mosaic.modifier.Modifier
import com.jakewharton.mosaic.ui.AnsiLevel
import com.jakewharton.mosaic.ui.unit.Constraints

internal fun interface DebugPolicy {
	fun MosaicNode.renderDebug(): String
}

internal abstract class MosaicNodeLayer(
	private val next: MosaicNodeLayer?,
	private val isStatic: Boolean,
) : Placeable(),
	Measurable,
	PlacementScope,
	MeasureScope {

	private var measureResult: MeasureResult = NotMeasured

	final override var parentData: Any? = null

	final override val width get() = measureResult.width
	final override val height get() = measureResult.height

	override fun measure(constraints: Constraints): Placeable = apply {
		measureResult = doMeasure(constraints)
	}

	protected open fun doMeasure(constraints: Constraints): MeasureResult {
		val placeable = next!!.measure(constraints)
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

	open fun drawTo(canvas: TextCanvas) {
		next?.drawTo(canvas)
	}

	override fun minIntrinsicWidth(height: Int): Int {
		return next?.minIntrinsicWidth(height) ?: 0
	}

	override fun maxIntrinsicWidth(height: Int): Int {
		return next?.maxIntrinsicWidth(height) ?: 0
	}

	override fun minIntrinsicHeight(width: Int): Int {
		return next?.minIntrinsicHeight(width) ?: 0
	}

	override fun maxIntrinsicHeight(width: Int): Int {
		return next?.maxIntrinsicHeight(width) ?: 0
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

	fun setModifier(modifier: Modifier) {
		topLayer = modifier.foldOut(bottomLayer) { element, nextLayer ->
			when (element) {
				is LayoutModifier -> LayoutLayer(element, nextLayer)

				is DrawModifier -> DrawLayer(element, nextLayer)

				is ParentDataModifier -> {
					parentData = element.modifyParentData(parentData)
					nextLayer
				}

				else -> nextLayer
			}
		}
	}

	override fun measure(constraints: Constraints): Placeable =
		topLayer.apply { measure(constraints) }

	val width: Int get() = topLayer.width
	val height: Int get() = topLayer.height
	val x: Int get() = topLayer.x
	val y: Int get() = topLayer.y

	fun measureAndPlace() {
		val placeable = measure(Constraints())
		topLayer.run { placeable.place(0, 0) }
	}

	/**
	 * Draw this node to a [TextCanvas].
	 * A call to [measureAndPlace] must precede calls to this function.
	 */
	fun paint(textCanvas: TextCanvas) {
		topLayer.drawTo(textCanvas)
	}

	/**
	 * Append any static [TextSurfaces][TextSurface] to [statics].
	 * A call to [measureAndPlace] must precede calls to this function.
	 */
	fun paintStatics(statics: MutableList<TextSurface>, ansiLevel: AnsiLevel) {
		for (child in children) {
			if (isStatic) {
				val textSurface = TextSurface(ansiLevel, child.width, child.height)
				child.paint(textSurface)
				statics += textSurface
			}
			child.paintStatics(statics, ansiLevel)
		}
		onStaticDraw?.invoke()
	}

	override fun minIntrinsicWidth(height: Int): Int {
		return topLayer.minIntrinsicWidth(height)
	}

	override fun maxIntrinsicWidth(height: Int): Int {
		return topLayer.maxIntrinsicWidth(height)
	}

	override fun minIntrinsicHeight(width: Int): Int {
		return topLayer.minIntrinsicHeight(width)
	}

	override fun maxIntrinsicHeight(width: Int): Int {
		return topLayer.maxIntrinsicHeight(width)
	}

	override fun toString() = debugPolicy.run { renderDebug() }
}

private class BottomLayer(
	private val node: MosaicNode,
) : MosaicNodeLayer(null, node.isStatic) {
	override fun doMeasure(constraints: Constraints): MeasureResult {
		return node.measurePolicy.run { measure(node.children, constraints) }
	}

	override fun drawTo(canvas: TextCanvas) {
		for (child in node.children) {
			if (child.width != 0 && child.height != 0) {
				child.topLayer.drawTo(canvas)
			}
		}
	}

	override fun minIntrinsicWidth(height: Int): Int {
		return node.measurePolicy.run { minIntrinsicWidth(node.children, height) }
	}

	override fun maxIntrinsicWidth(height: Int): Int {
		return node.measurePolicy.run { maxIntrinsicWidth(node.children, height) }
	}

	override fun minIntrinsicHeight(width: Int): Int {
		return node.measurePolicy.run { minIntrinsicHeight(node.children, width) }
	}

	override fun maxIntrinsicHeight(width: Int): Int {
		return node.measurePolicy.run { maxIntrinsicHeight(node.children, width) }
	}
}

private class LayoutLayer(
	private val element: LayoutModifier,
	private val lowerLayer: MosaicNodeLayer,
) : MosaicNodeLayer(lowerLayer, false) {
	override fun doMeasure(constraints: Constraints): MeasureResult {
		return element.run { measure(lowerLayer, constraints) }
	}

	override fun minIntrinsicWidth(height: Int): Int {
		return element.minIntrinsicWidth(lowerLayer, height)
	}

	override fun maxIntrinsicWidth(height: Int): Int {
		return element.maxIntrinsicWidth(lowerLayer, height)
	}

	override fun minIntrinsicHeight(width: Int): Int {
		return element.minIntrinsicHeight(lowerLayer, width)
	}

	override fun maxIntrinsicHeight(width: Int): Int {
		return element.maxIntrinsicHeight(lowerLayer, width)
	}
}

private class DrawLayer(
	private val element: DrawModifier,
	private val lowerLayer: MosaicNodeLayer,
) : MosaicNodeLayer(lowerLayer, false) {
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
