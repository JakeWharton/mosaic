package com.jakewharton.mosaic.animation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import com.jakewharton.mosaic.ui.Color
import com.jakewharton.mosaic.ui.unit.IntOffset
import com.jakewharton.mosaic.ui.unit.IntSize
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch

private val defaultAnimation = spring<Float>()

/**
 * Fire-and-forget animation function for [Float]. This Composable function is overloaded for
 * different parameter types such as [Color], [IntOffset],
 * etc. When the provided [targetValue] is changed, the animation will run automatically. If there
 * is already an animation in-flight when [targetValue] changes, the on-going animation will adjust
 * course to animate towards the new target value.
 *
 * [animateFloatAsState] returns a [State] object. The value of the state object will continuously
 * be updated by the animation until the animation finishes.
 *
 * Note, [animateFloatAsState] cannot be canceled/stopped without removing this composable function
 * from the tree. See [com.jakewharton.mosaic.animation.Animatable] for cancelable animations.
 *
 * @param targetValue Target value of the animation
 * @param animationSpec The animation that will be used to change the value through time. [com.jakewharton.mosaic.animation.spring]
 *   will be used by default.
 * @param visibilityThreshold An optional threshold for deciding when the animation value is
 *   considered close enough to the targetValue.
 * @param label An optional label to differentiate from other animations.
 * @param finishedListener An optional end listener to get notified when the animation is finished.
 * @return A [State] object, the value of which is updated by animation.
 */
@Composable
public fun animateFloatAsState(
	targetValue: Float,
	animationSpec: AnimationSpec<Float> = defaultAnimation,
	visibilityThreshold: Float = 0.01f,
	label: String = "FloatAnimation",
	finishedListener: ((Float) -> Unit)? = null,
): State<Float> {
	val resolvedAnimSpec =
		if (animationSpec === defaultAnimation) {
			remember(visibilityThreshold) { spring(visibilityThreshold = visibilityThreshold) }
		} else {
			animationSpec
		}
	return animateValueAsState(
		targetValue,
		Float.VectorConverter,
		resolvedAnimSpec,
		visibilityThreshold,
		label,
		finishedListener,
	)
}

/**
 * Fire-and-forget animation function for [Int]. This Composable function is overloaded for
 * different parameter types such as [Color], [IntOffset],
 * etc. When the provided [targetValue] is changed, the animation will run automatically. If there
 * is already an animation in-flight when [targetValue] changes, the on-going animation will adjust
 * course to animate towards the new target value.
 *
 * [animateIntAsState] returns a [State] object. The value of the state object will continuously be
 * updated by the animation until the animation finishes.
 *
 * Note, [animateIntAsState] cannot be canceled/stopped without removing this composable function
 * from the tree. See [com.jakewharton.mosaic.animation.Animatable] for cancelable animations.
 *
 * @param targetValue Target value of the animation
 * @param animationSpec The animation that will be used to change the value through time. Physics
 *   animation will be used by default.
 * @param label An optional label to differentiate from other animations.
 * @param finishedListener An optional end listener to get notified when the animation is finished.
 * @return A [State] object, the value of which is updated by animation.
 */
@Composable
public fun animateIntAsState(
	targetValue: Int,
	animationSpec: AnimationSpec<Int> = intDefaultSpring,
	label: String = "IntAnimation",
	finishedListener: ((Int) -> Unit)? = null,
): State<Int> {
	return animateValueAsState(
		targetValue,
		Int.VectorConverter,
		animationSpec,
		label = label,
		finishedListener = finishedListener,
	)
}

private val intDefaultSpring = spring(visibilityThreshold = Int.VisibilityThreshold)

/**
 * Fire-and-forget animation function for [IntOffset]. This Composable function is overloaded for
 * different parameter types such as [Color], [IntOffset],
 * etc. When the provided [targetValue] is changed, the animation will run automatically. If there
 * is already an animation in-flight when [targetValue] changes, the on-going animation will adjust
 * course to animate towards the new target value.
 *
 * [animateIntOffsetAsState] returns a [State] object. The value of the state object will
 * continuously be updated by the animation until the animation finishes.
 *
 * Note, [animateIntOffsetAsState] cannot be canceled/stopped without removing this composable
 * function from the tree. See [com.jakewharton.mosaic.animation.Animatable] for cancelable animations.
 *
 * @param targetValue Target value of the animation
 * @param animationSpec The animation that will be used to change the value through time. Physics
 *   animation will be used by default.
 * @param label An optional label to differentiate from other animations.
 * @param finishedListener An optional end listener to get notified when the animation is finished.
 * @return A [State] object, the value of which is updated by animation.
 */
@Composable
public fun animateIntOffsetAsState(
	targetValue: IntOffset,
	animationSpec: AnimationSpec<IntOffset> = intOffsetDefaultSpring,
	label: String = "IntOffsetAnimation",
	finishedListener: ((IntOffset) -> Unit)? = null,
): State<IntOffset> {
	return animateValueAsState(
		targetValue,
		IntOffset.VectorConverter,
		animationSpec,
		label = label,
		finishedListener = finishedListener,
	)
}

private val intOffsetDefaultSpring = spring(visibilityThreshold = IntOffset.VisibilityThreshold)

/**
 * Fire-and-forget animation function for [IntSize]. This Composable function is overloaded for
 * different parameter types such as [Color][Color], [IntOffset],
 * etc. When the provided [targetValue] is changed, the animation will run automatically. If there
 * is already an animation in-flight when [targetValue] changes, the on-going animation will adjust
 * course to animate towards the new target value.
 *
 * [animateIntSizeAsState] returns a [State] object. The value of the state object will continuously
 * be updated by the animation until the animation finishes.
 *
 * Note, [animateIntSizeAsState] cannot be canceled/stopped without removing this composable
 * function from the tree. See [com.jakewharton.mosaic.animation.Animatable] for cancelable animations.
 *
 * @param targetValue Target value of the animation
 * @param animationSpec The animation that will be used to change the value through time. Physics
 *   animation will be used by default.
 * @param label An optional label to differentiate from other animations.
 * @param finishedListener An optional end listener to get notified when the animation is finished.
 * @return A [State] object, the value of which is updated by animation.
 */
@Composable
public fun animateIntSizeAsState(
	targetValue: IntSize,
	animationSpec: AnimationSpec<IntSize> = intSizeDefaultSpring,
	label: String = "IntSizeAnimation",
	finishedListener: ((IntSize) -> Unit)? = null,
): State<IntSize> {
	return animateValueAsState(
		targetValue,
		IntSize.VectorConverter,
		animationSpec,
		label = label,
		finishedListener = finishedListener,
	)
}

private val intSizeDefaultSpring = spring(visibilityThreshold = IntSize.VisibilityThreshold)

/**
 * Fire-and-forget animation function for [Color]. This Composable function is overloaded for
 * different parameter types such as [Float], [Int], [IntSize], [IntOffset], etc. When the provided
 * [targetValue] is changed, the animation will run automatically. If there is already an animation
 * in-flight when [targetValue] changes, the on-going animation will adjust course to animate
 * towards the new target value.
 *
 * [animateColorAsState] returns a [State] object. The value of the state object will continuously
 * be updated by the animation until the animation finishes.
 *
 * Note, [animateColorAsState] cannot be canceled/stopped without removing this composable function
 * from the tree. See [Animatable][Animatable] for cancelable animations.
 *
 * @param targetValue Target value of the animation
 * @param animationSpec The animation that will be used to change the value through time, [spring]
 *   by default
 * @param label An optional label to differentiate from other animations.
 * @param finishedListener An optional listener to get notified when the animation is finished.
 */
@Composable
public fun animateColorAsState(
	targetValue: Color,
	animationSpec: AnimationSpec<Color> = colorDefaultSpring,
	label: String = "ColorAnimation",
	finishedListener: ((Color) -> Unit)? = null,
): State<Color> {
	return animateValueAsState(
		targetValue,
		Color.VectorConverter,
		animationSpec,
		label = label,
		finishedListener = finishedListener,
	)
}

private val colorDefaultSpring = spring<Color>()

/**
 * Fire-and-forget animation function for any value. This Composable function is overloaded for
 * different parameter types such as [Color], [IntOffset],
 * etc. When the provided [targetValue] is changed, the animation will run automatically. If there
 * is already an animation in-flight when [targetValue] changes, the on-going animation will adjust
 * course to animate towards the new target value.
 *
 * [animateValueAsState] returns a [State] object. The value of the state object will continuously
 * be updated by the animation until the animation finishes.
 *
 * Note, [animateValueAsState] cannot be canceled/stopped without removing this composable function
 * from the tree. See [com.jakewharton.mosaic.animation.Animatable] for cancelable animations.
 *
 * @param targetValue Target value of the animation
 * @param typeConverter A [com.jakewharton.mosaic.animation.TwoWayConverter] to convert from the animation value from and to an
 *   [AnimationVector]
 * @param animationSpec The animation that will be used to change the value through time. Physics
 *   animation will be used by default.
 * @param visibilityThreshold An optional threshold to define when the animation value can be
 *   considered close enough to the targetValue to end the animation.
 * @param label An optional label to differentiate from other animations.
 * @param finishedListener An optional end listener to get notified when the animation is finished.
 * @return A [State] object, the value of which is updated by animation.
 */
@Composable
public fun <T, V : AnimationVector> animateValueAsState(
	targetValue: T,
	typeConverter: TwoWayConverter<T, V>,
	animationSpec: AnimationSpec<T> = remember { spring() },
	visibilityThreshold: T? = null,
	label: String = "ValueAnimation",
	finishedListener: ((T) -> Unit)? = null,
): State<T> {
	val toolingOverride = remember { mutableStateOf<State<T>?>(null) }
	val animatable = remember { Animatable(targetValue, typeConverter, visibilityThreshold, label) }
	val listener by rememberUpdatedState(finishedListener)
	val animSpec: AnimationSpec<T> by
		rememberUpdatedState(
			animationSpec.run {
				if (
					visibilityThreshold != null &&
					this is SpringSpec &&
					this.visibilityThreshold != visibilityThreshold
				) {
					spring(dampingRatio, stiffness, visibilityThreshold)
				} else {
					this
				}
			},
		)
	val channel = remember { Channel<T>(Channel.CONFLATED) }
	SideEffect { channel.trySend(targetValue) }
	LaunchedEffect(channel) {
		for (target in channel) {
			// This additional poll is needed because when the channel suspends on receive and
			// two values are produced before consumers' dispatcher resumes, only the first value
			// will be received.
			// It may not be an issue elsewhere, but in animation we want to avoid being one
			// frame late.
			val newTarget = channel.tryReceive().getOrNull() ?: target
			launch {
				if (newTarget != animatable.targetValue) {
					animatable.animateTo(newTarget, animSpec)
					listener?.invoke(animatable.value)
				}
			}
		}
	}
	return toolingOverride.value ?: animatable.asState()
}
