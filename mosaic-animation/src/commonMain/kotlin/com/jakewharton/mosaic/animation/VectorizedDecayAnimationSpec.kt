package com.jakewharton.mosaic.animation

/**
 * [VectorizedDecayAnimationSpec]s are stateless vector based decay animation specifications. They
 * do not assume any starting/ending conditions. Nor do they manage a lifecycle. All it stores is
 * the configuration that is particular to the type of the decay animation: friction multiplier for
 * [exponentialDecay]. Its stateless nature allows the same [VectorizedDecayAnimationSpec] to be
 * reused by a few different running animations with different starting and ending values.
 *
 * Since [VectorizedDecayAnimationSpec]s are stateless, it requires starting value/velocity and
 * ending value to be passed in, along with playtime, to calculate the value or velocity at that
 * time. Play time here is the progress of the animation in terms of milliseconds, where 0 means the
 * start of the animation and [getDurationNanos] returns the play time for the end of the animation.
 *
 * __Note__: For use cases where the starting values/velocity and ending values aren't expected to
 * change, it is recommended to use [DecayAnimation] that caches these static values and hence does
 * not require them to be supplied in the value/velocity calculation.
 *
 * @see DecayAnimation
 */
public interface VectorizedDecayAnimationSpec<V : AnimationVector> {
	/**
	 * This is the absolute value of a velocity threshold, below which the animation is considered
	 * finished.
	 */
	public val absVelocityThreshold: Float

	/**
	 * Returns the value of the animation at the given time.
	 *
	 * @param playTimeNanos The time elapsed in milliseconds since the initialValue of the animation
	 * @param initialValue The initialValue value of the animation
	 * @param initialVelocity The initialValue velocity of the animation
	 */
	public fun getValueFromNanos(playTimeNanos: Long, initialValue: V, initialVelocity: V): V

	/**
	 * Returns the duration of the decay animation, in nanoseconds.
	 *
	 * @param initialValue initialValue value of the animation
	 * @param initialVelocity initialValue velocity of the animation
	 */
	@Suppress("MethodNameUnits")
	public fun getDurationNanos(initialValue: V, initialVelocity: V): Long

	/**
	 * Returns the velocity of the animation at the given time.
	 *
	 * @param playTimeNanos The time elapsed in milliseconds since the initialValue of the animation
	 * @param initialValue The initialValue value of the animation
	 * @param initialVelocity The initialValue velocity of the animation
	 */
	public fun getVelocityFromNanos(playTimeNanos: Long, initialValue: V, initialVelocity: V): V

	/**
	 * Returns the target value of the animation based on the initial condition of the animation (
	 * i.e. initial value and initial velocity).
	 *
	 * @param initialValue The initial value of the animation
	 * @param initialVelocity The initial velocity of the animation
	 */
	public fun getTargetValue(initialValue: V, initialVelocity: V): V
}
