package com.jakewharton.mosaic.animation.internal

import androidx.collection.FloatFloatPair
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.acos
import kotlin.math.cbrt
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt

private const val Tau = PI * 2.0
private const val Epsilon = 1e-7

// We use a fairly high epsilon here because it's post double->float conversion
// and because we use a fast approximation of cbrt(). The epsilon we use here is
// slightly larger than the max error of fastCbrt() in the -1f..1f range
// (8.3446500e-7f) but smaller than 1.0f.ulp * 10.
private const val FloatEpsilon = 1.05e-6f

/**
 * Finds the first real root of a cubic Bézier curve:
 * - [p0]: coordinate of the start point
 * - [p1]: coordinate of the first control point
 * - [p2]: coordinate of the second control point
 * - [p3]: coordinate of the end point
 *
 * If no root can be found, this method returns [Float.NaN].
 */
internal fun findFirstCubicRoot(p0: Float, p1: Float, p2: Float, p3: Float): Float {
	// This function implements Cardano's algorithm as described in "A Primer on Bézier Curves":
	// https://pomax.github.io/bezierinfo/#yforx
	//
	// The math used to find the roots is explained in "Solving the Cubic Equation":
	// http://www.trans4mind.com/personal_development/mathematics/polynomials/cubicAlgebra.htm

	var a = 3.0 * (p0 - 2.0 * p1 + p2)
	var b = 3.0 * (p1 - p0)
	var c = p0.toDouble()
	val d = -p0 + 3.0 * (p1 - p2) + p3

	// Not a cubic
	if (d.closeTo(0.0)) {
		// Not a quadratic
		if (a.closeTo(0.0)) {
			// No solutions
			if (b.closeTo(0.0)) {
				return Float.NaN
			}
			return clampValidRootInUnitRange((-c / b).toFloat())
		} else {
			val q = sqrt(b * b - 4.0 * a * c)
			val a2 = 2.0 * a

			val root = clampValidRootInUnitRange(((q - b) / a2).toFloat())
			if (!root.isNaN()) return root

			return clampValidRootInUnitRange(((-b - q) / a2).toFloat())
		}
	}

	a /= d
	b /= d
	c /= d

	val o3 = (3.0 * b - a * a) / 9.0
	val q2 = (2.0 * a * a * a - 9.0 * a * b + 27.0 * c) / 54.0
	val discriminant = q2 * q2 + o3 * o3 * o3
	val a3 = a / 3.0

	if (discriminant < 0.0) {
		val mp33 = -(o3 * o3 * o3)
		val r = sqrt(mp33)
		val t = -q2 / r
		val cosPhi = t.coerceIn(-1.0, 1.0)
		val phi = acos(cosPhi)
		val t1 = 2.0f * cbrt(r.toFloat())

		var root = clampValidRootInUnitRange((t1 * cos(phi / 3.0) - a3).toFloat())
		if (!root.isNaN()) return root

		root = clampValidRootInUnitRange((t1 * cos((phi + Tau) / 3.0) - a3).toFloat())
		if (!root.isNaN()) return root

		return clampValidRootInUnitRange((t1 * cos((phi + 2.0 * Tau) / 3.0) - a3).toFloat())
	} else if (discriminant == 0.0) {
		val u1 = -cbrt(q2.toFloat())

		val root = clampValidRootInUnitRange(2.0f * u1 - a3.toFloat())
		if (!root.isNaN()) return root

		return clampValidRootInUnitRange(-u1 - a3.toFloat())
	}

	val sd = sqrt(discriminant)
	val u1 = cbrt((-q2 + sd).toFloat())
	val v1 = cbrt((q2 + sd).toFloat())

	return clampValidRootInUnitRange((u1 - v1 - a3).toFloat())
}

/**
 * Returns [r] if it's in the [0..1] range, and [Float.NaN] otherwise. To account for numerical
 * imprecision in computations, values in the [-FloatEpsilon..1+FloatEpsilon] range are considered
 * to be in the [0..1] range and clamped appropriately.
 */
private inline fun clampValidRootInUnitRange(r: Float): Float {
	// The code below is a branchless version of:
	// if (r < 0.0f) {
	//     if (r >= -FloatEpsilon) 0.0f else Float.NaN
	// } else if (r > 1.0f) {
	//     if (r <= 1.0f + FloatEpsilon) 1.0f else Float.NaN
	// } else {
	//     r
	// }
	val s = r.coerceIn(0f, 1f)
	return if (abs(s - r) > FloatEpsilon) Float.NaN else s
}

private fun evaluateCubic(p0: Float, p1: Float, p2: Float, p3: Float, t: Float): Float {
	val a = p3 + 3.0f * (p1 - p2) - p0
	val b = 3.0f * (p2 - 2.0f * p1 + p0)
	val c = 3.0f * (p1 - p0)
	return ((a * t + b) * t + c) * t + p0
}

/**
 * Evaluates a cubic Bézier curve at position [t] along the curve. The curve is defined by the start
 * point (0, 0), the end point (0, 0) and two control points of respective coordinates [p1] and
 * [p2].
 */
@Suppress("UnnecessaryVariable")
internal fun evaluateCubic(p1: Float, p2: Float, t: Float): Float {
	val a = 1.0f / 3.0f + (p1 - p2)
	val b = (p2 - 2.0f * p1)
	val c = p1
	return 3.0f * ((a * t + b) * t + c) * t
}

internal inline fun Double.closeTo(b: Double) = abs(this - b) < Epsilon

internal fun computeCubicVerticalBounds(
	p0y: Float,
	p1y: Float,
	p2y: Float,
	p3y: Float,
	roots: FloatArray,
	index: Int = 0,
): FloatFloatPair {
	// Quadratic derivative of a cubic function
	// We do the computation inline to avoid using arrays of other data
	// structures to return the result
	val d0 = 3.0f * (p1y - p0y)
	val d1 = 3.0f * (p2y - p1y)
	val d2 = 3.0f * (p3y - p2y)
	var count = findQuadraticRoots(d0, d1, d2, roots, index)

	// Compute the second derivative as a line
	val dd0 = 2.0f * (d1 - d0)
	val dd1 = 2.0f * (d2 - d1)
	count += findLineRoot(dd0, dd1, roots, index + count)

	var minY = min(p0y, p3y)
	var maxY = max(p0y, p3y)

	for (i in 0 until count) {
		val t = roots[i]
		val y = evaluateCubic(p0y, p1y, p2y, p3y, t)
		minY = min(minY, y)
		maxY = max(maxY, y)
	}

	return FloatFloatPair(minY, maxY)
}

/**
 * Finds the real roots of a quadratic Bézier curve. To find the roots, only the X coordinates of
 * the four points are required:
 * - [p0]: x coordinate of the start point
 * - [p1]: x coordinate of the control point
 * - [p2]: x coordinate of the end point
 *
 * Any root found is written in the [roots] array, starting at [index]. The function returns the
 * number of roots found and written to the array.
 */
private fun findQuadraticRoots(
	p0: Float,
	p1: Float,
	p2: Float,
	roots: FloatArray,
	index: Int = 0,
): Int {
	val a = p0.toDouble()
	val b = p1.toDouble()
	val c = p2.toDouble()
	val d = a - 2.0 * b + c

	var rootCount = 0

	if (d != 0.0) {
		val v1 = -sqrt(b * b - a * c)
		val v2 = -a + b

		rootCount += writeValidRootInUnitRange((-(v1 + v2) / d).toFloat(), roots, index)
		rootCount += writeValidRootInUnitRange(((v1 - v2) / d).toFloat(), roots, index + rootCount)

		// Returns the roots sorted
		if (rootCount > 1) {
			val s = roots[index]
			val t = roots[index + 1]
			if (s > t) {
				roots[index] = t
				roots[index + 1] = s
			} else if (s == t) {
				// Don't report identical roots
				rootCount--
			}
		}
	} else if (b != c) {
		rootCount +=
			writeValidRootInUnitRange(((2.0 * b - c) / (2.0 * b - 2.0 * c)).toFloat(), roots, index)
	}

	return rootCount
}

/**
 * Finds the real root of a line defined by the X coordinates of its start ([p0]) and end ([p1])
 * points. The root, if any, is written in the [roots] array at [index]. Returns 1 if a root was
 * found, 0 otherwise.
 */
private inline fun findLineRoot(p0: Float, p1: Float, roots: FloatArray, index: Int = 0) =
	writeValidRootInUnitRange(-p0 / (p1 - p0), roots, index)

/**
 * Writes [r] in the [roots] array at [index], if it's in the [0..1] range. To account for numerical
 * imprecision in computations, values in the [-FloatEpsilon..1+FloatEpsilon] range are considered
 * to be in the [0..1] range and clamped appropriately. Returns 0 if no value was written, 1
 * otherwise.
 */
private fun writeValidRootInUnitRange(r: Float, roots: FloatArray, index: Int): Int {
	val v = clampValidRootInUnitRange(r)
	roots[index] = v
	return if (v.isNaN()) 0 else 1
}
