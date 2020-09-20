package com.jakewharton.mosaic

import androidx.compose.runtime.Composable
import androidx.compose.runtime.emit
import com.facebook.yoga.YogaFlexDirection

@Composable
fun Text(value: String) {
	emit<TextNode, MosaicNodeApplier>(::TextNode) {
		set(value) {
			this.value = value
		}
	}
}

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
