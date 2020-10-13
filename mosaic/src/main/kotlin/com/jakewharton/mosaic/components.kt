package com.jakewharton.mosaic

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.emit
import com.facebook.yoga.YogaFlexDirection

@Composable
fun Text(
	value: String,
	style: TextStyle? = null,
) {
	emit<TextNode, MosaicNodeApplier>(::TextNode) {
		set(value) {
			this.value = value
		}
		set(style) {
			this.style = style
		}
	}
}

@Immutable
class TextStyle private constructor(
	internal val bits: Int,
) {
	operator fun plus(other: TextStyle) = TextStyle(bits or other.bits)

	companion object {
		@Stable
		val None = TextStyle(0)
		@Stable
		val Underline = TextStyle(1)
		@Stable
		val Strikethrough = TextStyle(2)
		@Stable
		val Bold = TextStyle(4)
		@Stable
		val Dim = TextStyle(8)
		@Stable
		val Italic = TextStyle(16)
		@Stable
		val Invert = TextStyle(32)
	}
}

operator fun TextStyle?.contains(style: TextStyle) = this != null && (style.bits and bits) != 0

@Composable
fun Row(children: @Composable () -> Unit) {
	Box(YogaFlexDirection.ROW, children)
}

@Composable
fun Column(children: @Composable () -> Unit) {
	Box(YogaFlexDirection.COLUMN, children)
}

@Composable
private fun Box(flexDirection: YogaFlexDirection, children: @Composable () -> Unit) {
	emit<BoxNode, MosaicNodeApplier>(::BoxNode) {
		set(flexDirection) {
			yoga.flexDirection = flexDirection
		}
		set(children) {
			children()
		}
	}
}
