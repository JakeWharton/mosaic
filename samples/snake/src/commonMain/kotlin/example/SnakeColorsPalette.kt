package example

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.staticCompositionLocalOf

val LocalSnakeColorsPalette: ProvidableCompositionLocal<SnakeColorsPalette> =
	staticCompositionLocalOf { throw IllegalStateException("No colors palette") }

@Immutable
data class SnakeColorsPalette(
	val snakeHeadCodePoint: Int,
	val snakeBodyCodePoint: Int,
	val foodCodePoint: Int,
	val borderCodePoint: Int,
)

val AsciiSnakeColorsPalette = SnakeColorsPalette(
	snakeHeadCodePoint = '█'.code,
	snakeBodyCodePoint = '█'.code,
	foodCodePoint = '@'.code,
	borderCodePoint = '*'.code,
)
