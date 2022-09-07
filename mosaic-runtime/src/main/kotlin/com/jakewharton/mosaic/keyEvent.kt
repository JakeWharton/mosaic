package com.jakewharton.mosaic

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.compositionLocalOf

internal val KeyHandlers: ProvidableCompositionLocal<MutableList<(KeyEvent) -> Unit>> =
	compositionLocalOf {
		throw AssertionError()
	}

@Composable
fun onKeyEvent(listener: (event: KeyEvent) -> Unit) {
	val keyHandlers = KeyHandlers.current

	DisposableEffect(listener) {
		keyHandlers += listener
		onDispose {
			keyHandlers -= listener
		}
	}
}

enum class KeyEvent {
	Q,
	UP,
	DOWN,
	LEFT,
	RIGHT,
}
