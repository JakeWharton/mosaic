package com.jakewharton.mosaic.ui

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable

@Immutable
public class Color private constructor(
	internal val fg: Int,
	internal val bg: Int,
) {
	override fun toString(): String = "Color($fg)"

	public companion object {
		@Stable
		public val Black: Color = Color(30, 40)
		@Stable
		public val Red: Color = Color(31, 41)
		@Stable
		public val Green: Color = Color(32, 42)
		@Stable
		public val Yellow: Color = Color(33, 43)
		@Stable
		public val Blue: Color = Color(34, 44)
		@Stable
		public val Magenta: Color = Color(35, 45)
		@Stable
		public val Cyan: Color = Color(36, 46)
		@Stable
		public val White: Color = Color(37, 47)
		@Stable
		public val BrightBlack: Color = Color(90, 100)
		@Stable
		public val BrightRed: Color = Color(91, 101)
		@Stable
		public val BrightGreen: Color = Color(92, 102)
		@Stable
		public val BrightYellow: Color = Color(93, 103)
		@Stable
		public val BrightBlue: Color = Color(94, 104)
		@Stable
		public val BrightMagenta: Color = Color(95, 105)
		@Stable
		public val BrightCyan: Color = Color(96, 106)
		@Stable
		public val BrightWhite: Color = Color(97, 107)
	}
}
