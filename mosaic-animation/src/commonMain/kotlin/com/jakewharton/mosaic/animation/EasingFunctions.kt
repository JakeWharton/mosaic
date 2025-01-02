package com.jakewharton.mosaic.animation

import kotlin.math.PI
import kotlin.math.pow
import kotlin.math.sin

/**
 * Easing Curve that speeds up quickly and ends slowly.
 *
 * ![EaseCurve](https://developer.android.com/images/reference/androidx/compose/animation-core/ease.gif)
 */
public val Ease: Easing = CubicBezierEasing(0.25f, 0.1f, 0.25f, 1.0f)

/**
 * Easing Curve that starts quickly and ends slowly.
 *
 * ![EaseOutCurve](https://developer.android.com/images/reference/androidx/compose/animation-core/ease_out.gif)
 */
public val EaseOut: Easing = CubicBezierEasing(0f, 0f, 0.58f, 1f)

/**
 * Easing Curve that starts slowly and ends quickly.
 *
 * ![EaseInCurve](https://developer.android.com/images/reference/androidx/compose/animation-core/ease_in.gif)
 */
public val EaseIn: Easing = CubicBezierEasing(0.42f, 0f, 1f, 1f)

/**
 * Easing Curve that starts slowly, speeds up and then ends slowly.
 *
 * ![EaseInOutCurve](https://developer.android.com/images/reference/androidx/compose/animation-core/ease_in_out.gif)
 */
public val EaseInOut: Easing = CubicBezierEasing(0.42f, 0.0f, 0.58f, 1.0f)

/**
 * Easing Curve that starts slowly and ends quickly. Similar to EaseIn, but with slightly less
 * abrupt beginning
 *
 * ![EaseInSineCurve](https://developer.android.com/images/reference/androidx/compose/animation-core/ease_in_sine.gif)
 */
public val EaseInSine: Easing = CubicBezierEasing(0.12f, 0f, 0.39f, 0f)

/**
 * ![EaseOutSineCurve](https://developer.android.com/images/reference/androidx/compose/animation-core/ease_out_sine.gif)
 */
public val EaseOutSine: Easing = CubicBezierEasing(0.61f, 1f, 0.88f, 1f)

/**
 * ![EaseInOutSineCurve](https://developer.android.com/images/reference/androidx/compose/animation-core/ease_in_out_sine.gif)
 */
public val EaseInOutSine: Easing = CubicBezierEasing(0.37f, 0f, 0.63f, 1f)

/**
 * ![EaseInCubicCurve](https://developer.android.com/images/reference/androidx/compose/animation-core/ease_in_cubic.gif)
 */
public val EaseInCubic: Easing = CubicBezierEasing(0.32f, 0f, 0.67f, 0f)

/**
 * ![EaseOutCubicCurve](https://developer.android.com/images/reference/androidx/compose/animation-core/ease_out_cubic.gif)
 */
public val EaseOutCubic: Easing = CubicBezierEasing(0.33f, 1f, 0.68f, 1f)

/**
 * ![EaseInOutCubicCurve](https://developer.android.com/images/reference/androidx/compose/animation-core/ease_in_out_cubic.gif)
 */
public val EaseInOutCubic: Easing = CubicBezierEasing(0.65f, 0f, 0.35f, 1f)

/**
 * ![EaseInQuintCurve](https://developer.android.com/images/reference/androidx/compose/animation-core/ease_in_quint.gif)
 */
public val EaseInQuint: Easing = CubicBezierEasing(0.64f, 0f, 0.78f, 0f)

/**
 * ![EaseOutQuintCurve](https://developer.android.com/images/reference/androidx/compose/animation-core/ease_out_quint.gif)
 */
public val EaseOutQuint: Easing = CubicBezierEasing(0.22f, 1f, 0.36f, 1f)

/**
 * ![EaseInOutQuintCurve](https://developer.android.com/images/reference/androidx/compose/animation-core/ease_in_out_quint.gif)
 */
public val EaseInOutQuint: Easing = CubicBezierEasing(0.83f, 0f, 0.17f, 1f)

/**
 * ![EaseInCircCurve](https://developer.android.com/images/reference/androidx/compose/animation-core/ease_in_circ.gif)
 */
public val EaseInCirc: Easing = CubicBezierEasing(0.55f, 0f, 1f, 0.45f)

/**
 * ![EaseOutCircCurve](https://developer.android.com/images/reference/androidx/compose/animation-core/ease_out_circ.gif)
 */
public val EaseOutCirc: Easing = CubicBezierEasing(0f, 0.55f, 0.45f, 1f)

/**
 * ![EaseInOutCircCurve](https://developer.android.com/images/reference/androidx/compose/animation-core/ease_in_out_circ.gif)
 */
public val EaseInOutCirc: Easing = CubicBezierEasing(0.85f, 0f, 0.15f, 1f)

/**
 * ![EaseInQuadCurve](https://developer.android.com/images/reference/androidx/compose/animation-core/ease_in_quad.gif)
 */
public val EaseInQuad: Easing = CubicBezierEasing(0.11f, 0f, 0.5f, 0f)

/**
 * ![EaseOutQuadCurve](https://developer.android.com/images/reference/androidx/compose/animation-core/ease_out_quad.gif)
 */
public val EaseOutQuad: Easing = CubicBezierEasing(0.5f, 1f, 0.89f, 1f)

/**
 * ![EaseInOutQuadCurve](https://developer.android.com/images/reference/androidx/compose/animation-core/ease_in_out_quad.gif)
 */
public val EaseInOutQuad: Easing = CubicBezierEasing(0.45f, 0f, 0.55f, 1f)

/**
 * ![EaseInQuartCurve](https://developer.android.com/images/reference/androidx/compose/animation-core/ease_in_quart.gif)
 */
public val EaseInQuart: Easing = CubicBezierEasing(0.5f, 0f, 0.75f, 0f)

/**
 * ![EaseOutQuartCurve](https://developer.android.com/images/reference/androidx/compose/animation-core/ease_out_quart.gif)
 */
public val EaseOutQuart: Easing = CubicBezierEasing(0.25f, 1f, 0.5f, 1f)

/**
 * ![EaseInOutQuartCurve](https://developer.android.com/images/reference/androidx/compose/animation-core/ease_in_out_quart.gif)
 */
public val EaseInOutQuart: Easing = CubicBezierEasing(0.76f, 0f, 0.24f, 1f)

/**
 * ![EaseInExpoCurve](https://developer.android.com/images/reference/androidx/compose/animation-core/ease_in_expo.gif)
 */
public val EaseInExpo: Easing = CubicBezierEasing(0.7f, 0f, 0.84f, 0f)

/**
 * ![EaseOutExpoCurve](https://developer.android.com/images/reference/androidx/compose/animation-core/ease_out_expo.gif)
 */
public val EaseOutExpo: Easing = CubicBezierEasing(0.16f, 1f, 0.3f, 1f)

/**
 * ![EaseInOutExpoCurve](https://developer.android.com/images/reference/androidx/compose/animation-core/ease_in_out_expo.gif)
 */
public val EaseInOutExpo: Easing = CubicBezierEasing(0.87f, 0f, 0.13f, 1f)

/**
 * ![EaseInBackCurve](https://developer.android.com/images/reference/androidx/compose/animation-core/ease_in_back.gif)
 */
public val EaseInBack: Easing = CubicBezierEasing(0.36f, 0f, 0.66f, -0.56f)

/**
 * ![EaseOutBackCurve](https://developer.android.com/images/reference/androidx/compose/animation-core/ease_out_back.gif)
 */
public val EaseOutBack: Easing = CubicBezierEasing(0.34f, 1.56f, 0.64f, 1f)

/**
 * ![EaseInOutBackCurve](https://developer.android.com/images/reference/androidx/compose/animation-core/ease_in_out_back.gif)
 */
public val EaseInOutBack: Easing = CubicBezierEasing(0.68f, -0.6f, 0.32f, 1.6f)

/**
 * ![EaseInElasticCurve](https://developer.android.com/images/reference/androidx/compose/animation-core/ease_in_elastic.gif)
 */
public val EaseInElastic: Easing = Easing { fraction: Float ->
	val c4 = (2f * PI) / 3f

	return@Easing when (fraction) {
		0f -> 0f
		1f -> 1f
		else ->
			(-(2.0f).pow(10f * fraction - 10.0f) * sin((fraction * 10f - 10.75f) * c4)).toFloat()
	}
}

/**
 * ![EaseOutElasticCurve](https://developer.android.com/images/reference/androidx/compose/animation-core/ease_out_elastic.gif)
 */
public val EaseOutElastic: Easing = Easing { fraction ->
	val c4 = (2f * PI) / 3f

	return@Easing when (fraction) {
		0f -> 0f
		1f -> 1f
		else -> ((2.0f).pow(-10.0f * fraction) * sin((fraction * 10f - 0.75f) * c4) + 1f).toFloat()
	}
}

/**
 * ![EaseInOutElasticCurve](https://developer.android.com/images/reference/androidx/compose/animation-core/ease_in_out_elastic.gif)
 */
public val EaseInOutElastic: Easing = Easing { fraction ->
	val c5 = (2f * PI) / 4.5f
	return@Easing when (fraction) {
		0f -> 0f
		1f -> 1f
		in 0f..0.5f ->
			(-(2.0f.pow(20.0f * fraction - 10.0f) * sin((20.0f * fraction - 11.125f) * c5)) / 2.0f)
				.toFloat()

		else ->
			((2.0f.pow(-20.0f * fraction + 10.0f) * sin((fraction * 20f - 11.125f) * c5)) / 2f)
				.toFloat() + 1f
	}
}

/**
 * ![EaseOutBounceCurve](https://developer.android.com/images/reference/androidx/compose/animation-core/ease_out_bounce.gif)
 */
public val EaseOutBounce: Easing = Easing { fraction ->
	val n1 = 7.5625f
	val d1 = 2.75f
	var newFraction = fraction

	return@Easing if (newFraction < 1f / d1) {
		n1 * newFraction * newFraction
	} else if (newFraction < 2f / d1) {
		newFraction -= 1.5f / d1
		n1 * newFraction * newFraction + 0.75f
	} else if (newFraction < 2.5f / d1) {
		newFraction -= 2.25f / d1
		n1 * newFraction * newFraction + 0.9375f
	} else {
		newFraction -= 2.625f / d1
		n1 * newFraction * newFraction + 0.984375f
	}
}

/**
 * ![EaseInBounceCurve](https://developer.android.com/images/reference/androidx/compose/animation-core/ease_in_bounce.gif)
 */
public val EaseInBounce: Easing = Easing { fraction ->
	return@Easing 1 - EaseOutBounce.transform(1f - fraction)
}

/**
 * ![EaseInOutBounceCurve](https://developer.android.com/images/reference/androidx/compose/animation-core/ease_in_out_bounce.gif)
 */
public val EaseInOutBounce: Easing = Easing { fraction ->
	return@Easing if (fraction < 0.5) {
		(1 - EaseOutBounce.transform(1f - 2f * fraction)) / 2f
	} else {
		(1 + EaseOutBounce.transform((2f * fraction - 1f))) / 2f
	}
}
