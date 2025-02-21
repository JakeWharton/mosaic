@file:Suppress("NOTHING_TO_INLINE")

package com.jakewharton.mosaic.ui

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import com.jakewharton.mosaic.ui.TextStyle.Companion.Empty
import kotlin.jvm.JvmInline

@Immutable
@JvmInline
public value class TextStyle internal constructor(
	@PublishedApi
	internal val bits: Int,
) {
	public operator fun plus(other: TextStyle): TextStyle = TextStyle(bits or other.bits)
	public operator fun contains(other: TextStyle): Boolean = (bits and other.bits) == other.bits

	public companion object {
		/**
		 * It is needed to indicate that there is no style (not the same as an [Empty] one),
		 * since it does not replace the style that was previously specified.
		 *
		 * In the example below, we will get `789456` in the console, where everything will be in bold,
		 * as indicated in the first [Text]. The second [Text] changes `123` to `789`,
		 * but does not change the previously specified style in any way.
		 *
		 * ```kotlin
		 * Box {
		 * 	Text("123456", textStyle = TextStyle.Invert)
		 * 	Text("789", textStyle = TextStyle.Unspecified)
		 * }
		 * ```
		 *
		 * or (because [TextStyle.Unspecified] is specified by default)
		 *
		 * ```kotlin
		 * Box {
		 * 	Text("123456", textStyle = TextStyle.Invert)
		 * 	Text("789")
		 * }
		 * ```
		 *
		 * Output to the console:
		 * **789456**
		 *
		 * When adding other styles to this, when using the [plus] operator,
		 * the added styles will be applied.
		 *
		 * If you need to reset the style, then explicitly use [TextStyle.Empty].
		 *
		 * @see TextStyle.Empty
		 */
		@Stable
		public val Unspecified: TextStyle = TextStyle(UnspecifiedTextStyle)

		/**
		 * It is needed to indicate that this style is empty and the rest of the styles
		 * need to be reset.
		 *
		 * In the example below, we will get `789456` in the console,
		 * where `789` will be without any style, and `456` will be in bold,
		 * as indicated in the first [Text]. The second [Text] with the content change to `789`
		 * will also reset the previously specified style.
		 *
		 * ```kotlin
		 * Box {
		 * 	Text("123456", textStyle = TextStyle.Bold)
		 * 	Text("789", textStyle = TextStyle.Empty)
		 * }
		 * ```
		 *
		 * Output to the console:
		 * 789**456**
		 *
		 * When adding other styles to this, when using the [plus] operator,
		 * the added styles will be applied.
		 *
		 * If you do not need to change the style specified earlier in any way,
		 * you can use [TextStyle.Unspecified].
		 *
		 * @see TextStyle.Unspecified
		 */
		@Stable
		public val Empty: TextStyle = TextStyle(EmptyTextStyle)

		@Stable
		public val Strikethrough: TextStyle = TextStyle(4)

		@Stable
		public val Bold: TextStyle = TextStyle(8)

		@Stable
		public val Dim: TextStyle = TextStyle(16)

		@Stable
		public val Italic: TextStyle = TextStyle(32)

		@Stable
		public val Invert: TextStyle = TextStyle(64)
	}
}

@PublishedApi
internal const val UnspecifiedTextStyle: Int = 0

@PublishedApi
internal const val EmptyTextStyle: Int = 1

/**
 * `false` when this is [TextStyle.Unspecified].
 */
@Stable
public inline val TextStyle.isSpecifiedTextStyle: Boolean get() = bits != UnspecifiedTextStyle

/**
 * `true` when this is [TextStyle.Unspecified].
 */
@Stable
public inline val TextStyle.isUnspecifiedTextStyle: Boolean get() = bits == UnspecifiedTextStyle

/**
 * `false` when this is [TextStyle.Empty].
 */
@Stable
public inline val TextStyle.isNotEmptyTextStyle: Boolean get() = bits != EmptyTextStyle

/**
 * `true` when this is [TextStyle.Empty].
 */
@Stable
public inline val TextStyle.isEmptyTextStyle: Boolean get() = bits == EmptyTextStyle

/**
 * If this text style [isSpecifiedTextStyle] then this is returned, otherwise [block] is executed and its result
 * is returned.
 */
public inline fun TextStyle.takeOrElse(block: () -> TextStyle): TextStyle =
	if (isSpecifiedTextStyle) this else block()
