package com.jakewharton.mosaic.animation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@RequiresOptIn(
	message = "This is an experimental animation API for Transition. It may change in the future.",
)
@Retention(AnnotationRetention.BINARY)
public annotation class ExperimentalAnimatableApi

/**
 * [DeferredTargetAnimation] is intended for animations where the target is unknown at the time of
 * instantiation. Such use cases include, but are not limited to, size or position animations
 * created during composition or the initialization of a Modifier.Node, yet the target size or
 * position stays unknown until the later measure and placement phase.
 *
 * [DeferredTargetAnimation] offers a declarative [updateTarget] function, which requires a target
 * to either set up the animation or update the animation, and to read the current value of the
 * animation.
 */
@ExperimentalAnimatableApi
public class DeferredTargetAnimation<T, V : AnimationVector>(
	private val vectorConverter: TwoWayConverter<T, V>,
) {
	/** Returns the target value from the most recent [updateTarget] call. */
	public val pendingTarget: T?
		get() = _pendingTarget

	private var _pendingTarget: T? by mutableStateOf(null)
	private val target: T?
		get() = animatable?.targetValue

	private var animatable: Animatable<T, V>? = null

	/**
	 * [updateTarget] sets up an animation, or updates an already running animation, based on the
	 * [target] in the given [coroutineScope]. [pendingTarget] will be updated to track the last
	 * seen [target].
	 *
	 * [updateTarget] will return the current value of the animation after launching the animation
	 * in the given [coroutineScope].
	 *
	 * @return current value of the animation
	 */
	public fun updateTarget(
		target: T,
		coroutineScope: CoroutineScope,
		animationSpec: FiniteAnimationSpec<T> = spring(),
	): T {
		_pendingTarget = target
		val anim = animatable ?: Animatable(
			target,
			vectorConverter,
		).also { animatable = it }
		coroutineScope.launch {
			if (anim.targetValue != _pendingTarget) {
				anim.animateTo(target, animationSpec)
			}
		}
		return anim.value
	}

	/**
	 * [isIdle] returns true when the animation has finished running and reached its
	 * [pendingTarget], or when the animation has not been set up (i.e. [updateTarget] has never
	 * been called).
	 */
	public val isIdle: Boolean
		get() = _pendingTarget == target && animatable?.isRunning != true
}
