package example

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.jakewharton.mosaic.layout.DrawStyle
import com.jakewharton.mosaic.layout.drawBehind
import com.jakewharton.mosaic.layout.height
import com.jakewharton.mosaic.layout.padding
import com.jakewharton.mosaic.layout.size
import com.jakewharton.mosaic.layout.width
import com.jakewharton.mosaic.modifier.Modifier
import com.jakewharton.mosaic.text.buildAnnotatedString
import com.jakewharton.mosaic.ui.Alignment
import com.jakewharton.mosaic.ui.Box
import com.jakewharton.mosaic.ui.Column
import com.jakewharton.mosaic.ui.Row
import com.jakewharton.mosaic.ui.Spacer
import com.jakewharton.mosaic.ui.Text
import com.jakewharton.mosaic.ui.TextStyle
import com.jakewharton.mosaic.ui.unit.IntOffset
import com.jakewharton.mosaic.ui.unit.IntSize
import example.models.INVALID_FOOD
import example.models.Phase
import example.models.asPoint
import example.models.height
import example.models.width
import example.models.x
import example.models.y

@Composable
fun SnakeScreen(viewModel: SnakeViewModel) {
	val uiState by viewModel.uiStateFlow.collectAsState()
	CompositionLocalProvider(LocalSnakeColorsPalette provides AsciiSnakeColorsPalette) {
		Row(modifier = Modifier.onSnakeKeyEvent(viewModel::doAction)) {
			Box(contentAlignment = Alignment.Center) {
				GameField(uiState)
				GameOverTitle(uiState)
			}
			Spacer(Modifier.width(4))
			Info(uiState)
		}
	}
}

@Composable
private fun GameOverTitle(uiState: SnakeUiState) {
	if (uiState.phase == Phase.GAME_OVER_LOSE) {
		Column(
			modifier = Modifier
				.drawBehind {
					drawRect(' ', textStyle = TextStyle.Invert, size = IntSize(width, height))
				}
				.padding(1),
			horizontalAlignment = Alignment.CenterHorizontally,
		) {
			Text("Game Over")
			Text("Score: ${uiState.score}")
			Spacer(Modifier.height(1))
			Text("Press Space to start new game")
		}
	}
}

@Composable
private fun GameField(uiState: SnakeUiState) {
	val colorsPalette = LocalSnakeColorsPalette.current
	Spacer(
		modifier = Modifier
			.drawBehind { drawRect(colorsPalette.borderCodePoint, drawStyle = DrawStyle.Stroke(1)) }
			.padding(1)
			.size(uiState.field.width, uiState.field.height)
			.drawBehind {
				if (uiState.food != INVALID_FOOD) {
					drawRect(
						codePoint = colorsPalette.foodCodePoint,
						topLeft = IntOffset(uiState.food.x, uiState.food.y),
						size = IntSize(1, 1),
					)
				}
				uiState.snake.forEachIndexed { index, rawPoint ->
					val point = rawPoint.asPoint()
					drawRect(
						codePoint = if (index == 0) {
							colorsPalette.snakeHeadCodePoint
						} else {
							colorsPalette.snakeBodyCodePoint
						},
						topLeft = IntOffset(point.x, point.y),
						size = IntSize(1, 1),
					)
				}
			},
	)
}

@Composable
private fun Info(uiState: SnakeUiState) {
	Column {
		Text("Score")
		Text(uiState.score.toString(), textStyle = TextStyle.Bold)
		Spacer(Modifier.height(8))
		Text("Controls", textStyle = TextStyle.Italic)
		Text("Arrows or WASD to move snake")
		Text(
			buildAnnotatedString {
				append("Space to ")
				when (uiState.phase) {
					Phase.PLAYED -> append("pause")
					Phase.PAUSED -> append("resume")
					Phase.GAME_OVER_LOSE, Phase.GAME_OVER_WIN -> append("start")
				}
			},
		)
		Text("R to restart game")
		Text("Q or Escape to exit")
	}
}
