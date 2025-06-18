package example

import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshots.SnapshotStateMap

class Game {
	val board = Board()

	private val _player = mutableStateOf(Mark.X)
	val player get() = _player.value

	fun markPosition(position: Position) {
		if (board.markPosition(position, _player.value)) {
			_player.value = if (_player.value == Mark.X) Mark.O else Mark.X
		}
	}

	fun newGame() {
		_player.value = Mark.X
		board.newGame()
	}
}

class Board {
	private val _map = SnapshotStateMap<Position, Mark>()
	val map: Map<Position, Mark> = _map

	val winnerLine: List<Position>? by derivedStateOf {
		winningLines.firstOrNull { line ->
			val (a, b, c) = line.map(_map::get)
			a != null && a == b && a == c
		}
	}

	val winner: Mark? by derivedStateOf {
		winnerLine?.let { _map[it.first()] }
	}

	val gameEnded: Boolean by derivedStateOf {
		winner != null || _map.size == 9
	}

	fun markPosition(position: Position, mark: Mark): Boolean {
		return if (winnerLine == null && _map[position] == null) {
			_map[position] = mark
			true
		} else {
			false
		}
	}

	fun newGame() {
		_map.clear()
	}
}

val winningLines = listOf(
	listOf(Position.TOP_LEFT, Position.TOP_CENTER, Position.TOP_RIGHT),
	listOf(Position.CENTER_LEFT, Position.CENTER_CENTER, Position.CENTER_RIGHT),
	listOf(Position.BOTTOM_LEFT, Position.BOTTOM_CENTER, Position.BOTTOM_RIGHT),

	listOf(Position.TOP_LEFT, Position.CENTER_LEFT, Position.BOTTOM_LEFT),
	listOf(Position.TOP_CENTER, Position.CENTER_CENTER, Position.BOTTOM_CENTER),
	listOf(Position.TOP_RIGHT, Position.CENTER_RIGHT, Position.BOTTOM_RIGHT),

	listOf(Position.TOP_LEFT, Position.CENTER_CENTER, Position.BOTTOM_RIGHT),
	listOf(Position.TOP_RIGHT, Position.CENTER_CENTER, Position.BOTTOM_LEFT),
)

enum class Position {
	TOP_LEFT,
	TOP_CENTER,
	TOP_RIGHT,
	CENTER_LEFT,
	CENTER_CENTER,
	CENTER_RIGHT,
	BOTTOM_LEFT,
	BOTTOM_CENTER,
	BOTTOM_RIGHT,
}

enum class Mark {
	X,
	O,
}
