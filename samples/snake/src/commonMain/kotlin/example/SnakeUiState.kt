package example

import androidx.compose.runtime.Immutable
import example.models.Phase
import example.models.Point
import example.models.Size
import example.models.Snake

@Immutable
data class SnakeUiState(
	val phase: Phase,
	val field: Size,
	val score: Int,
	val snake: Snake,
	val food: Point,
)
