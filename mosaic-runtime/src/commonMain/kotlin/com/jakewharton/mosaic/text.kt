@file:Suppress("NOTHING_TO_INLINE")

package com.jakewharton.mosaic

import androidx.compose.runtime.Stable

internal const val SpaceCharCodePoint = ' '.code

/**
 * Unicode code point cannot contain a negative value.
 */
internal const val UnspecifiedCodePoint: Int = -1

/**
 * `true` when this is [UnspecifiedCodePoint].
 */
@Stable
internal inline val Int.isUnspecifiedCodePoint: Boolean get() = this == UnspecifiedCodePoint

/**
 * `false` when this is [UnspecifiedCodePoint].
 */
@Stable
internal inline val Int.isSpecifiedCodePoint: Boolean get() = this != UnspecifiedCodePoint
