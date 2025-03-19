package example.models

data class GameState(
	val lastUpdateMs: Long = 0L,
	val phase: Phase = Phase.PLAYED,
	val snake: Snake,
	val direction: Direction = Direction.RIGHT,
	val food: Food = INVALID_FOOD,
)
