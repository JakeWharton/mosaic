package com.jakewharton.mosaic.layout

import androidx.collection.MutableObjectList
import com.jakewharton.mosaic.TextCanvas
import com.jakewharton.mosaic.TextSurface
import com.jakewharton.mosaic.layout.Placeable.PlacementScope
import com.jakewharton.mosaic.modifier.Modifier
import com.jakewharton.mosaic.ui.StaticState
import com.jakewharton.mosaic.ui.unit.Constraints

internal fun interface DebugPolicy {
	fun MosaicNode.renderDebug(): String
}

internal abstract class MosaicNodeLayer(
	private val isStatic: Boolean,
) : Placeable(),
	Measurable,
	PlacementScope,
	MeasureScope {
	abstract val next: MosaicNodeLayer?

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

	open fun drawTo(canvas: TextSurface) {
		next?.drawTo(canvas)
	}

	open fun sendKeyEvent(keyEvent: KeyEvent): Boolean {
		return next?.sendKeyEvent(keyEvent) ?: false
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
	val isStatic: Boolean,
) : Measurable {
	val children = ArrayList<MosaicNode>()
	var staticState: StaticState? = null

	private val bottomLayer: MosaicNodeLayer = BottomLayer(this)
	var topLayer: MosaicNodeLayer = bottomLayer
		private set

	override var parentData: Any? = null
		private set

	var testTag: String? = null
		private set

	fun setModifier(modifier: Modifier) {
		topLayer = modifier.foldOut(bottomLayer) { element, nextLayer ->
			var nextLayer = nextLayer
			// The Modifier class can inherit from several key Modifier types
			// with different processing logic.
			if (element is LayoutModifier) {
				nextLayer = LayoutLayer(element, nextLayer)
			}
			if (element is DrawModifier) {
				nextLayer = DrawLayer(element, nextLayer)
			}
			if (element is KeyModifier) {
				nextLayer = KeyLayer(element, nextLayer)
			}
			if (element is ParentDataModifier) {
				parentData = element.modifyParentData(parentData)
			}
			if (element is TestTagModifier) {
				testTag = element.tag
			}
			nextLayer
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
	 * Draw this node to a [TextSurface].
	 * A call to [measureAndPlace] must precede calls to this function.
	 */
	fun paint(): TextCanvas {
		val surface = TextSurface(width, height)
		topLayer.drawTo(surface)
		return surface
	}

	/**
	 * Append any static [TextSurfaces][TextSurface] to [statics].
	 * A call to [measureAndPlace] must precede calls to this function.
	 */
	fun paintStaticsTo(statics: MutableObjectList<TextCanvas>) {
		if (!isStatic) {
			for (index in children.indices) {
				children[index].paintStaticsTo(statics)
			}
			return
		}
		staticState?.let { staticState ->
			for (index in children.indices) {
				val child = children[index]
				statics += child.paint()
				child.paintStaticsTo(statics)
			}
			children.clear()
			this.staticState = null
		}
	}

	fun sendKeyEvent(keyEvent: KeyEvent): Boolean {
		return topLayer.sendKeyEvent(keyEvent)
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
) : MosaicNodeLayer(node.isStatic) {
	override val next: MosaicNodeLayer? get() = null

	override fun doMeasure(constraints: Constraints): MeasureResult {
		return node.measurePolicy.run { measure(node.children, constraints) }
	}

	override fun drawTo(canvas: TextSurface) {
		for (child in node.children) {
			if (child.width != 0 && child.height != 0) {
				child.topLayer.drawTo(canvas)
			}
		}
	}

	override fun sendKeyEvent(keyEvent: KeyEvent): Boolean {
		for (child in node.children) {
			if (child.sendKeyEvent(keyEvent)) {
				return true
			}
		}
		return false
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
	override val next: MosaicNodeLayer,
) : MosaicNodeLayer(false) {
	override fun doMeasure(constraints: Constraints): MeasureResult {
		return element.run { measure(next, constraints) }
	}

	override fun minIntrinsicWidth(height: Int): Int {
		return element.minIntrinsicWidth(next, height)
	}

	override fun maxIntrinsicWidth(height: Int): Int {
		return element.maxIntrinsicWidth(next, height)
	}

	override fun minIntrinsicHeight(width: Int): Int {
		return element.minIntrinsicHeight(next, width)
	}

	override fun maxIntrinsicHeight(width: Int): Int {
		return element.maxIntrinsicHeight(next, width)
	}
}

private class DrawLayer(
	private val element: DrawModifier,
	override val next: MosaicNodeLayer,
) : MosaicNodeLayer(false) {
	override fun drawTo(canvas: TextSurface) {
		val oldX = canvas.translationX
		val oldY = canvas.translationY
		canvas.translationX = x
		canvas.translationY = y
		val scope = object : TextCanvasDrawScope(canvas, width, height), ContentDrawScope {
			override fun drawContent() {
				next.drawTo(canvas)
			}
		}
		element.run { scope.draw() }
		canvas.translationX = oldX
		canvas.translationY = oldY
	}
}

private class KeyLayer(
	private val element: KeyModifier,
	override val next: MosaicNodeLayer,
) : MosaicNodeLayer(false) {
	override fun sendKeyEvent(keyEvent: KeyEvent) =
		element.onPreKeyEvent(keyEvent) ||
			next.sendKeyEvent(keyEvent) ||
			element.onKeyEvent(keyEvent)
}
