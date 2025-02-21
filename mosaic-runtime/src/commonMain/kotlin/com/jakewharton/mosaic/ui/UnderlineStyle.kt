@file:Suppress("NOTHING_TO_INLINE")

package com.jakewharton.mosaic.ui

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import kotlin.jvm.JvmInline

@Immutable
@JvmInline
public value class UnderlineStyle private constructor(
	@PublishedApi
	internal val value: Int,
) {
	public companion object {
		@Stable
		public val Unspecified: UnderlineStyle = UnderlineStyle(UnspecifiedUnderlineStyle)

		@Stable
		public val None: UnderlineStyle = UnderlineStyle(NoUnderlineStyle)

		@Stable
		public val Straight: UnderlineStyle = UnderlineStyle(1)

		@Stable
		public val Double: UnderlineStyle = UnderlineStyle(2)

		@Stable
		public val Curly: UnderlineStyle = UnderlineStyle(3)

		@Stable
		public val Dotted: UnderlineStyle = UnderlineStyle(4)

		@Stable
		public val Dashed: UnderlineStyle = UnderlineStyle(5)
	}
}

@PublishedApi
internal const val UnspecifiedUnderlineStyle: Int = -1

@PublishedApi
internal const val NoUnderlineStyle: Int = 0

/**
 * `false` when this is [UnderlineStyle.Unspecified].
 */
@Stable
public inline val UnderlineStyle.isSpecifiedUnderlineStyle: Boolean get() = value != UnspecifiedUnderlineStyle

/**
 * `true` when this is [UnderlineStyle.Unspecified].
 */
@Stable
public inline val UnderlineStyle.isUnspecifiedUnderlineStyle: Boolean get() = value == UnspecifiedUnderlineStyle

/**
 * If this text style [isSpecifiedUnderlineStyle] then this is returned, otherwise [block] is executed and its result
 * is returned.
 */
public inline fun UnderlineStyle.takeOrElse(block: () -> UnderlineStyle): UnderlineStyle =
	if (isSpecifiedUnderlineStyle) this else block()
