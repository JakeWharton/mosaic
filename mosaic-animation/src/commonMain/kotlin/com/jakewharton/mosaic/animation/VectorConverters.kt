@file:Suppress("NOTHING_TO_INLINE", "KotlinRedundantDiagnosticSuppress")

package com.jakewharton.mosaic.animation

import com.jakewharton.mosaic.ui.Color
import com.jakewharton.mosaic.ui.unit.IntOffset
import com.jakewharton.mosaic.ui.unit.IntSize
import kotlin.math.pow
import kotlin.math.roundToInt

/**
 * [TwoWayConverter] class contains the definition on how to convert from an arbitrary type [T] to a
 * [AnimationVector], and convert the [AnimationVector] back to the type [T]. This allows animations
 * to run on any type of objects, e.g. position, rectangle, color, etc.
 */
public interface TwoWayConverter<T, V : AnimationVector> {
	/**
	 * Defines how a type [T] should be converted to a Vector type (i.e. [AnimationVector1D],
	 * [AnimationVector2D], [AnimationVector3D] or [AnimationVector4D], depends on the dimensions of
	 * type T).
	 */
	public val convertToVector: (T) -> V

	/**
	 * Defines how to convert a Vector type (i.e. [AnimationVector1D], [AnimationVector2D],
	 * [AnimationVector3D] or [AnimationVector4D], depends on the dimensions of type T) back to type
	 * [T].
	 */
	public val convertFromVector: (V) -> T
}

/**
 * Factory method to create a [TwoWayConverter] that converts a type [T] from and to an
 * [AnimationVector] type.
 *
 * @param convertToVector converts from type [T] to [AnimationVector]
 * @param convertFromVector converts from [AnimationVector] to type [T]
 */
public fun <T, V : AnimationVector> TwoWayConverter(
	convertToVector: (T) -> V,
	convertFromVector: (V) -> T,
): TwoWayConverter<T, V> = TwoWayConverterImpl(convertToVector, convertFromVector)

/** Type converter to convert type [T] to and from a [AnimationVector1D]. */
private class TwoWayConverterImpl<T, V : AnimationVector>(
	override val convertToVector: (T) -> V,
	override val convertFromVector: (V) -> T,
) : TwoWayConverter<T, V>

internal inline fun lerp(start: Float, stop: Float, fraction: Float) =
	(start * (1 - fraction) + stop * fraction)

/** A [TwoWayConverter] that converts [Float] from and to [AnimationVector1D] */
public val Float.Companion.VectorConverter: TwoWayConverter<Float, AnimationVector1D>
	get() = FloatToVector

/** A [TwoWayConverter] that converts [Int] from and to [AnimationVector1D] */
public val Int.Companion.VectorConverter: TwoWayConverter<Int, AnimationVector1D>
	get() = IntToVector

private val FloatToVector: TwoWayConverter<Float, AnimationVector1D> =
	TwoWayConverter({ AnimationVector1D(it) }, { it.value })

private val IntToVector: TwoWayConverter<Int, AnimationVector1D> =
	TwoWayConverter({ AnimationVector1D(it.toFloat()) }, { it.value.toInt() })

/** A type converter that converts a [IntOffset] to a [AnimationVector2D], and vice versa. */
public val IntOffset.Companion.VectorConverter: TwoWayConverter<IntOffset, AnimationVector2D>
	get() = IntOffsetToVector

/**
 * A type converter that converts a [IntSize] to a [AnimationVector2D], and vice versa.
 *
 * Clamps negative values to zero when converting back to [IntSize].
 */
public val IntSize.Companion.VectorConverter: TwoWayConverter<IntSize, AnimationVector2D>
	get() = IntSizeToVector

/** A type converter that converts a [IntOffset] to a [AnimationVector2D], and vice versa. */
private val IntOffsetToVector: TwoWayConverter<IntOffset, AnimationVector2D> =
	TwoWayConverter(
		convertToVector = { AnimationVector2D(it.x.toFloat(), it.y.toFloat()) },
		convertFromVector = { IntOffset(it.v1.roundToInt(), it.v2.roundToInt()) },
	)

/**
 * A type converter that converts a [IntSize] to a [AnimationVector2D], and vice versa.
 *
 * Clamps negative values to zero when converting back to [IntSize].
 */
private val IntSizeToVector: TwoWayConverter<IntSize, AnimationVector2D> =
	TwoWayConverter(
		{ AnimationVector2D(it.width.toFloat(), it.height.toFloat()) },
		{
			IntSize(
				width = it.v1.roundToInt().coerceAtLeast(0),
				height = it.v2.roundToInt().coerceAtLeast(0),
			)
		},
	)

/**
 * Returns a converter that can both convert a [Color] to a
 * [AnimationVector3D], and convert a [AnimationVector3D]) back to a [Color].
 */
private val ColorToVector: TwoWayConverter<Color, AnimationVector3D> =
	TwoWayConverter(
		convertToVector = { color ->
			val (r, g, b) = color
			val okLab = rgbToOkLab(r, g, b)
			AnimationVector3D(okLab.l, okLab.a, okLab.b)
		},
		convertFromVector = { vector ->
			val l = vector.v1.coerceIn(0f, 1f) // L (red)
			val a = vector.v2.coerceIn(-0.5f, 0.5f) // a (blue)
			val b = vector.v3.coerceIn(-0.5f, 0.5f) // b (green)
			oklabToRGB(l, a, b)
		},
	)

private class OkLab(val l: Float, val a: Float, val b: Float)

private fun rgbToOkLab(r: Float, g: Float, b: Float): OkLab {
	// sRGB -> linear sRGB
	val linearR = if (r <= 0.04045) r / 12.92 else ((r + 0.055) / 1.055).pow(2.4)
	val linearG = if (g <= 0.04045) g / 12.92 else ((g + 0.055) / 1.055).pow(2.4)
	val linearB = if (b <= 0.04045) b / 12.92 else ((b + 0.055) / 1.055).pow(2.4)

	// Linear sRGB -> LMS
	val l = 0.4122214708 * linearR + 0.5363325363 * linearG + 0.0514459929 * linearB
	val m = 0.2119034982 * linearR + 0.6806995451 * linearG + 0.1073969566 * linearB
	val s = 0.0883024619 * linearR + 0.2817188376 * linearG + 0.6299787005 * linearB

	// LMS -> Lab
	val l2 = l.pow(1.0 / 3.0)
	val m2 = m.pow(1.0 / 3.0)
	val s2 = s.pow(1.0 / 3.0)

	return OkLab(
		l = (0.2104542553 * l2 + 0.7936177850 * m2 - 0.0040720468 * s2).toFloat(),
		a = (1.9779984951 * l2 - 2.4285922050 * m2 + 0.4505937099 * s2).toFloat(),
		b = (0.0259040371 * l2 + 0.7827717662 * m2 - 0.8086757660 * s2).toFloat(),
	)
}

private fun oklabToRGB(l: Float, a: Float, b: Float): Color {
	// Lab -> LMS
	val l2 = l + 0.3963377774 * a + 0.2158037573 * b
	val m2 = l - 0.1055613458 * a - 0.0638541728 * b
	val s2 = l - 0.0894841775 * a - 1.2914855480 * b

	// Inverse cube root transformation
	val l3 = l2 * l2 * l2
	val m3 = m2 * m2 * m2
	val s3 = s2 * s2 * s2

	// LMS -> Linear sRGB
	val linearR = 4.0767416621 * l3 - 3.3077115913 * m3 + 0.2309699292 * s3
	val linearG = -1.2684380046 * l3 + 2.6097574011 * m3 - 0.3413193965 * s3
	val linearB = -0.0041960863 * l3 - 0.7034186147 * m3 + 1.7076147010 * s3

	// Linear sRGB -> sRGB
	val r2 = if (linearR <= 0.0031308) 12.92 * linearR else 1.055 * linearR.pow(1.0 / 2.4) - 0.055
	val g2 = if (linearG <= 0.0031308) 12.92 * linearG else 1.055 * linearG.pow(1.0 / 2.4) - 0.055
	val b2 = if (linearB <= 0.0031308) 12.92 * linearB else 1.055 * linearB.pow(1.0 / 2.4) - 0.055

	return Color(
		red = r2.coerceIn(0.0, 1.0).toFloat(),
		green = g2.coerceIn(0.0, 1.0).toFloat(),
		blue = b2.coerceIn(0.0, 1.0).toFloat(),
	)
}

/**
 * Returns a converter that can both convert a [Color] to a
 * [AnimationVector3D], and convert a [AnimationVector3D]) back to a [Color].
 */
public val Color.Companion.VectorConverter: TwoWayConverter<Color, AnimationVector3D>
	get() = ColorToVector
