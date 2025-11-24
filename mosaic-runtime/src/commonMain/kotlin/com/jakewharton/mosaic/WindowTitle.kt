package com.jakewharton.mosaic

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf

/**
 * Sets the window title of the terminal.
 *
 * This function can be used multiple times in the composition. If multiple [WindowTitle]s are
 * present, the one that is called last in the composition (usually the one most deeply nested)
 * will be the one that sets the title. When a [WindowTitle] leaves the composition, the previous
 * title (if any) will be restored.
 *
 * The library does not filter or sanitize the provided [title] string. It is passed directly to the
 * terminal. This means you can pass any string, including those with newlines or other control
 * characters, but the result depends entirely on how the specific terminal emulator handles them.
 * Some terminals might display them correctly, while others might have issues or ignore them.
 *
 * @param title The text to display in the terminal window title bar. Passing an empty string is
 * allowed and will be sent to the terminal as-is. Sending an empty title resets it to the default
 * value (e.g., the shell name).
 */
@Composable
public fun WindowTitle(title: String) {
	val manager = LocalWindowTitleManager.current
	val entry = remember(manager) {
		manager.register(title)
	}
	SideEffect {
		entry.title = title
	}
	DisposableEffect(entry) {
		onDispose {
			entry.dispose()
		}
	}
}

internal val LocalWindowTitleManager = staticCompositionLocalOf<WindowTitleManager> {
	throw IllegalStateException("No WindowTitleManager provided")
}

internal class WindowTitleManager {
	private val titles = mutableStateListOf<EntryImpl>()

	val title: String? get() = titles.lastOrNull()?.title

	fun register(title: String): Entry {
		val entry = EntryImpl(title)
		titles.add(entry)
		return entry
	}

	interface Entry {
		var title: String

		fun dispose()
	}

	private inner class EntryImpl(initialTitle: String) : Entry {
		override var title by mutableStateOf(initialTitle)

		override fun dispose() {
			titles.remove(this)
		}
	}
}
