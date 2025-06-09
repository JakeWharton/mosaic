package example

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.jakewharton.mosaic.animation.LinearEasing
import com.jakewharton.mosaic.animation.animateFloat
import com.jakewharton.mosaic.animation.infiniteRepeatable
import com.jakewharton.mosaic.animation.rememberInfiniteTransition
import com.jakewharton.mosaic.animation.tween
import com.jakewharton.mosaic.focus.FocusDirection
import com.jakewharton.mosaic.focus.LocalFocusManager
import com.jakewharton.mosaic.layout.drawBehind
import com.jakewharton.mosaic.layout.focusable
import com.jakewharton.mosaic.layout.onKeyEvent
import com.jakewharton.mosaic.layout.padding
import com.jakewharton.mosaic.modifier.Modifier
import com.jakewharton.mosaic.runMosaicMain
import com.jakewharton.mosaic.ui.Alignment
import com.jakewharton.mosaic.ui.Arrangement
import com.jakewharton.mosaic.ui.Box
import com.jakewharton.mosaic.ui.Column
import com.jakewharton.mosaic.ui.Row
import com.jakewharton.mosaic.ui.Text
import kotlin.math.roundToInt

fun main() = runMosaicMain {
	val game = remember { Game() }

	Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(2)) {
		Box {
			Column {
				GameRow(game, winningLines[0])
				Text("-----+-----+-----")
				GameRow(game, winningLines[1])
				Text("-----+-----+-----")
				GameRow(game, winningLines[2])
			}
			val winner = game.board.winner
			val winnerText = when {
				winner != null -> "Winner is $winner"
				game.board.map.size == 9 -> "It's a tie"
				else -> null
			}
			if (winnerText != null) {
				Text(
					winnerText,
					Modifier
						.align(Alignment.Center)
						.border('•')
						.focusable()
						.onKeyEvent {
							if (it.key == "Enter") {
								game.newGame()
								true
							} else {
								false
							}
						},
				)
			}
		}

		Text("Current player: ${game.player}")
	}
}

@Composable
fun GameRow(game: Game, row: List<Position>) {
	Row {
		MarkCell(game, row[0])
		Text("|\n|\n|")
		MarkCell(game, row[1])
		Text("|\n|\n|")
		MarkCell(game, row[2])
	}
}

@Composable
fun MarkCell(game: Game, position: Position) {
	var focused by remember { mutableStateOf(false) }
	val mark = game.board.map[position]
	val isWinnerMark = game.board.winnerLine?.contains(position) ?: false
	val winnerCharIndex by rememberInfiniteTransition("winnerAnimationChar")
		.animateFloat(
			0f,
			animationChars.lastIndex.toFloat(),
			infiniteRepeatable(tween(durationMillis = 5_000, easing = LinearEasing)),
		)
	val borderChar = when {
		isWinnerMark -> animationChars[winnerCharIndex.roundToInt().coerceIn(0, animationChars.lastIndex)]
		focused -> '•'
		else -> ' '
	}
	val focusManager = LocalFocusManager.current
	val focusModifier = if (game.board.gameEnded || mark != null) {
		focused = false
		Modifier
	} else {
		Modifier.focusable { focused = it.isFocused }
	}
	Text(
		" ${mark?.name ?: " "} ",
		modifier = focusModifier
			.onKeyEvent {
				if (it.key == "Enter") {
					game.markPosition(position)
					focusManager.moveFocus(FocusDirection.Next)
					true
				} else {
					false
				}
			}.border(borderChar),
	)
}

private const val animationChars = "X#WO+-*/?"

fun Modifier.border(char: Char): Modifier = this.drawBehind { drawRect(char) }.padding(1)
