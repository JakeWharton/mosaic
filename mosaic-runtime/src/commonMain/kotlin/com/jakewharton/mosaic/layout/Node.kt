package com.jakewharton.mosaic.layout

import com.jakewharton.mosaic.TextCanvas
import com.jakewharton.mosaic.TextSurface
import com.jakewharton.mosaic.layout.Placeable.PlacementScope

internal fun interface DrawPolicy {
	fun performDraw(canvas: TextCanvas)
}

internal fun interface StaticPaintPolicy {
	fun MosaicNode.performPaintStatics(statics: MutableList<TextSurface>)

	companion object {
		val Children = StaticPaintPolicy { statics ->
			for (child in children) {
				child.paintStatics(statics)
			}
		}
	}
}

internal fun interface DebugPolicy {
	fun MosaicNode.renderDebug(): String
}

internal abstract class MosaicNodeLayer : Placeable(), PlacementScope, MeasureScope {
	abstract fun measure(): MeasureResult
	abstract fun drawTo(canvas: TextCanvas)
}

internal abstract class AbstractMosaicNodeLayer(
	private val next: MosaicNodeLayer?,
) : MosaicNodeLayer() {
	private var measureResult: MeasureResult = NotMeasured

	final override val width get() = measureResult.width
	final override val height get() = measureResult.height

	final override fun measure(): MeasureResult {
		return doMeasure().also { measureResult = it }
	}

	open fun doMeasure(): MeasureResult {
		return checkNotNull(next).measure()
	}

	final override var x = 0
		private set
	final override var y = 0
		private set

	final override fun placeAt(x: Int, y: Int) {
		this.x = x
		this.y = y
		measureResult.placeChildren()
	}

	final override fun drawTo(canvas: TextCanvas) {
		drawLayer(canvas)
		next?.drawTo(canvas)
	}

	open fun drawLayer(canvas: TextCanvas) {}
}

internal object NotMeasured : MeasureResult {
	override val width get() = 0
	override val height get() = 0
	override fun placeChildren() = throw UnsupportedOperationException("Not measured")
}

internal class MosaicNode(
	var measurePolicy: MeasurePolicy,
	var staticPaintPolicy: StaticPaintPolicy?,
	drawPolicy: DrawPolicy?,
	var debugPolicy: DebugPolicy,
) : Measurable {
	val children = mutableListOf<MosaicNode>()

	private val bottomLayer: MosaicNodeLayer = object : AbstractMosaicNodeLayer(null) {
		override fun doMeasure(): MeasureResult {
			return measurePolicy.run { measure(children) }
		}

		override fun drawLayer(canvas: TextCanvas) {
			for (child in children) {
				if (child.width != 0 && child.height != 0) {
					child.topLayer.drawTo(canvas)
				}
			}
		}
	}

	private var topLayer = bottomLayer

	var drawPolicy: DrawPolicy? = drawPolicy
		set(value) {
			topLayer = if (value == null) {
				bottomLayer
			} else {
				object : AbstractMosaicNodeLayer(bottomLayer) {
					override fun drawLayer(canvas: TextCanvas) {
						canvas.translationX += x
						canvas.translationY += y
						value.performDraw(canvas)
						canvas.translationX -= x
						canvas.translationY -= y
					}
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
	fun paintStatics(statics: MutableList<TextSurface>) = staticPaintPolicy?.run { performPaintStatics(statics) }

	override fun toString() = debugPolicy.run { renderDebug() }
}
