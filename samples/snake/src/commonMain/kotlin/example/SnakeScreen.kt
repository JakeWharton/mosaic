package example

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import com.jakewharton.mosaic.LocalTerminalState
import com.jakewharton.mosaic.layout.DrawScope
import com.jakewharton.mosaic.layout.drawBehind
import com.jakewharton.mosaic.layout.height
import com.jakewharton.mosaic.layout.padding
import com.jakewharton.mosaic.layout.size
import com.jakewharton.mosaic.layout.width
import com.jakewharton.mosaic.modifier.Modifier
import com.jakewharton.mosaic.text.buildAnnotatedString
import com.jakewharton.mosaic.ui.Alignment
import com.jakewharton.mosaic.ui.Box
import com.jakewharton.mosaic.ui.Color
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
import kotlinx.coroutines.delay

@Composable
fun SnakeScreen(viewModel: SnakeViewModel) {
	val uiState by viewModel.uiStateFlow.collectAsState()
	val colorsPalette = snakeColorsPaletteFor(LocalTerminalState.current.theme)
	CompositionLocalProvider(LocalSnakeColorsPalette provides colorsPalette) {
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
	val borderPhase = rememberBorderPhase()
	val fieldWidth = uiState.field.width
	val fieldHeight = uiState.field.height
	Spacer(
		modifier = Modifier
			.drawBehind { drawRainbowBorder(borderPhase.value) }
			.padding(1)
			.size(fieldWidth, fieldHeight / 2)
			.drawBehind {
				val cells = arrayOfNulls<Color>(fieldWidth * fieldHeight)
				if (uiState.food != INVALID_FOOD) {
					cells[uiState.food.y * fieldWidth + uiState.food.x] = colorsPalette.food
				}
				val lastIndex = uiState.snake.lastIndex
				uiState.snake.forEachIndexed { index, rawPoint ->
					val point = rawPoint.asPoint()
					val fraction = if (lastIndex == 0) 0f else index.toFloat() / lastIndex
					val color = lerp(colorsPalette.snakeHead, colorsPalette.snakeTail, fraction)
					cells[point.y * fieldWidth + point.x] = if (rawPoint in uiState.eaten) {
						lerp(colorsPalette.food, color, fraction)
					} else {
						color
					}
				}

				for (row in 0 until fieldHeight / 2) {
					for (x in 0 until fieldWidth) {
						val upper = cells[row * 2 * fieldWidth + x]
						val lower = cells[(row * 2 + 1) * fieldWidth + x]
						val topLeft = IntOffset(x, row)
						when {
							upper != null -> drawRect(
								char = '▀',
								foreground = upper,
								background = lower ?: Color.Unspecified,
								topLeft = topLeft,
								size = IntSize(1, 1),
							)

							lower != null -> drawRect(
								char = '▄',
								foreground = lower,
								topLeft = topLeft,
								size = IntSize(1, 1),
							)
						}
					}
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

@Composable
private fun rememberBorderPhase(): State<Float> {
	return produceState(0f) {
		while (true) {
			delay(BorderFrameMs)
			value = (value + BorderFrameMs.toFloat() / BorderCycleMs) % 1f
		}
	}
}

private const val BorderFrameMs = 50L
private const val BorderCycleMs = 4_000L

private fun DrawScope.drawRainbowBorder(phase: Float) {
	val perimeter = 2 * (width - 1) + 2 * (height - 1)
	var index = 0
	fun cell(x: Int, y: Int, char: Char) {
		val hue = (index++.toFloat() / perimeter + phase) % 1f
		drawRect(
			char = char,
			foreground = hsvColor(hue, saturation = 0.65f, value = 1f),
			topLeft = IntOffset(x, y),
			size = IntSize(1, 1),
		)
	}

	cell(0, 0, '╭')
	for (x in 1 until width - 1) cell(x, 0, '─')
	cell(width - 1, 0, '╮')
	for (y in 1 until height - 1) cell(width - 1, y, '│')
	cell(width - 1, height - 1, '╯')
	for (x in width - 2 downTo 1) cell(x, height - 1, '─')
	cell(0, height - 1, '╰')
	for (y in height - 2 downTo 1) cell(0, y, '│')
}

private fun lerp(start: Color, stop: Color, fraction: Float): Color {
	val (startRed, startGreen, startBlue) = start
	val (stopRed, stopGreen, stopBlue) = stop
	return Color(
		red = startRed + (stopRed - startRed) * fraction,
		green = startGreen + (stopGreen - startGreen) * fraction,
		blue = startBlue + (stopBlue - startBlue) * fraction,
	)
}

private fun hsvColor(hue: Float, saturation: Float, value: Float): Color {
	fun channel(n: Int): Float {
		val k = (n + hue * 6f) % 6f
		return value - value * saturation * maxOf(0f, minOf(k, 4f - k, 1f))
	}
	return Color(red = channel(5), green = channel(3), blue = channel(1))
}
