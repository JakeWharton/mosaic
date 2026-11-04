package example

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.staticCompositionLocalOf
import com.jakewharton.mosaic.terminal.Terminal
import com.jakewharton.mosaic.ui.Color

val LocalSnakeColorsPalette: ProvidableCompositionLocal<SnakeColorsPalette> =
	staticCompositionLocalOf { throw IllegalStateException("No colors palette") }

@Immutable
data class SnakeColorsPalette(
	val snakeHead: Color,
	val snakeTail: Color,
	val food: Color,
)

val DarkSnakeColorsPalette = SnakeColorsPalette(
	snakeHead = Color(255, 255, 255),
	snakeTail = Color(90, 90, 90),
	food = Color(235, 60, 60),
)

val LightSnakeColorsPalette = SnakeColorsPalette(
	snakeHead = Color(0, 0, 0),
	snakeTail = Color(170, 170, 170),
	food = Color(215, 30, 30),
)

fun snakeColorsPaletteFor(theme: Terminal.Theme): SnakeColorsPalette {
	return when (theme) {
		Terminal.Theme.Light -> LightSnakeColorsPalette
		Terminal.Theme.Dark, Terminal.Theme.Unknown -> DarkSnakeColorsPalette
	}
}
