package com.jakewharton.mosaic.ui

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable

@Immutable
public class TextStyle private constructor(
	private val bits: Int,
) {
	public operator fun plus(other: TextStyle): TextStyle = TextStyle(bits or other.bits)
	public operator fun contains(style: TextStyle): Boolean = (style.bits and bits) != 0

	public companion object {
		@Stable
		public val None: TextStyle = TextStyle(0)
		@Stable
		public val Underline: TextStyle = TextStyle(1)
		@Stable
		public val Strikethrough: TextStyle = TextStyle(2)
		@Stable
		public val Bold: TextStyle = TextStyle(4)
		@Stable
		public val Dim: TextStyle = TextStyle(8)
		@Stable
		public val Italic: TextStyle = TextStyle(16)
		@Stable
		public val Invert: TextStyle = TextStyle(32)
	}
}
