package com.jakewharton.mosaic.animation

import androidx.compose.runtime.withFrameMillis
import androidx.compose.runtime.withFrameNanos
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.coroutineContext

/**
 * Provides a policy that will be applied to animations that get their frame time from
 * [withInfiniteAnimationFrameNanos][withInfiniteAnimationFrameNanos]
 * or
 * [withInfiniteAnimationFrameMillis][withInfiniteAnimationFrameMillis]
 * This can be used to intervene in infinite animations to make them finite, for example by
 * cancelling such coroutines.
 */
public interface InfiniteAnimationPolicy : CoroutineContext.Element {
	/**
	 * Call this to apply the policy on the given suspending [block]. Execution of the block is
	 * determined by the policy implementation. For example, a test policy could decide not to run
	 * the block, or trace its execution.
	 *
	 * The block is intended to be part of and will therefore be treated as an infinite animation,
	 * one that after returning from [onInfiniteOperation] will call it again. If the block is not
	 * part of an infinite animation, the policy will still be applied.
	 */
	public suspend fun <R> onInfiniteOperation(block: suspend () -> R): R

	override val key: CoroutineContext.Key<*>
		get() = Key

	public companion object Key : CoroutineContext.Key<InfiniteAnimationPolicy>
}

/**
 * Like [withFrameNanos], but applies the [InfiniteAnimationPolicy] from the calling
 * [CoroutineContext] if there is one.
 */
public suspend fun <R> withInfiniteAnimationFrameNanos(onFrame: (frameTimeNanos: Long) -> R): R =
	when (val policy = coroutineContext[InfiniteAnimationPolicy]) {
		null -> withFrameNanos(onFrame)
		else -> policy.onInfiniteOperation { withFrameNanos(onFrame) }
	}

/**
 * Like [withFrameMillis], but applies the [InfiniteAnimationPolicy] from the calling
 * [CoroutineContext] if there is one.
 */
@Suppress("UnnecessaryLambdaCreation")
public suspend inline fun <R> withInfiniteAnimationFrameMillis(
	crossinline onFrame: (frameTimeMillis: Long) -> R,
): R = withInfiniteAnimationFrameNanos { onFrame(it / 1_000_000L) }
