package example

import example.models.Direction

sealed interface SnakeUiAction {

	object SwitchPhase : SnakeUiAction

	class ChangeDirection(val newDirection: Direction) : SnakeUiAction

	object Exit : SnakeUiAction

	object Restart : SnakeUiAction
}
