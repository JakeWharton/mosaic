package example

import example.models.Direction
import example.models.Food
import example.models.GameState
import example.models.INVALID_FOOD
import example.models.Phase
import example.models.Point
import example.models.Size
import example.models.Snake
import example.models.copy
import example.models.head
import example.models.height
import example.models.rawHead
import example.models.width
import example.models.x
import example.models.y
import example.utils.asLong
import java.util.concurrent.atomic.AtomicReference
import kotlin.random.Random
import kotlin.system.exitProcess
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

private const val INITIAL_SNAKE_SIZE = 3

class SnakeViewModel(
	parentCoroutineScope: CoroutineScope,
	private val fieldSize: Size = Size(60, 20),
	private val updateDelayMs: Long = 200L,
) {

	private val coroutineScope =
		CoroutineScope(SupervisorJob(parentCoroutineScope.coroutineContext[Job]))

	private val gameState = AtomicReference(GameState(snake = createSnake()))

	private val updateUiStateFlow = MutableSharedFlow<Unit>(
		extraBufferCapacity = 1,
		onBufferOverflow = BufferOverflow.DROP_OLDEST,
	)

	private val moveDirectionFlow = MutableSharedFlow<Direction>(
		extraBufferCapacity = 1,
		onBufferOverflow = BufferOverflow.DROP_LATEST,
	)

	val uiStateFlow: StateFlow<SnakeUiState> = updateUiStateFlow
		.map { gameState.get() }
		.onEach { checkGameOver(it) }
		.map { it.toUiState() }
		.distinctUntilChanged()
		.stateIn(coroutineScope, SharingStarted.WhileSubscribed(), gameState.get().toUiState())

	private var gameCycleJob: Job? = null

	init {
		startGame()
	}

	private fun createInitialGameState(): GameState {
		val snake = createSnake()
		val food = updateFood(snake, fieldSize)
		return GameState(snake = snake, food = food)
	}

	private fun createSnake(): Snake {
		val initialX = fieldSize.width / 2
		val initialY = fieldSize.height / 2
		val snake = Snake(INITIAL_SNAKE_SIZE)
		for (i in 0 until INITIAL_SNAKE_SIZE) {
			snake.add(Point(initialX - i, initialY).asLong())
		}
		return snake
	}

	fun doAction(snakeUiAction: SnakeUiAction) {
		when (snakeUiAction) {
			is SnakeUiAction.ChangeDirection -> moveDirectionFlow.tryEmit(snakeUiAction.newDirection)
			is SnakeUiAction.SwitchPhase -> switchPhase()
			is SnakeUiAction.Exit -> exit()
			is SnakeUiAction.Restart -> startGame()
		}
	}

	private fun startGame() {
		gameCycleJob?.cancel()
		gameState.set(createInitialGameState())
		gameCycleJob = coroutineScope.launch {
			launch {
				moveDirectionFlow.collect { newDirection ->
					gameState.updateAndGet { nextFrame(changeDirection(it, newDirection)) }
					updateUiStateFlow.tryEmit(Unit)
				}
			}
			launch {
				var delayMs = updateDelayMs
				while (coroutineContext.isActive) {
					delay(delayMs)
					gameState.updateAndGet {
						val currentTimeMs = System.currentTimeMillis()
						val diffMs = currentTimeMs - it.lastUpdateMs
						if (diffMs < updateDelayMs) {
							delayMs = updateDelayMs - diffMs
							it
						} else {
							delayMs = updateDelayMs
							nextFrame(it).also {
								updateUiStateFlow.tryEmit(Unit)
							}
						}
					}
				}
			}
		}
	}

	private fun isGamePlaying(gameState: GameState): Boolean {
		return gameCycleJob != null && gameState.phase == Phase.PLAYED
	}

	private fun switchPhase() {
		if (gameCycleJob == null) {
			startGame()
			return
		}
		gameState.updateAndGet {
			it.copy(phase = if (it.phase == Phase.PAUSED) Phase.PLAYED else Phase.PAUSED)
		}
	}

	private fun exit() {
		coroutineScope.cancel()
		exitProcess(0)
	}

	private fun changeDirection(currentGameState: GameState, newDirection: Direction): GameState {
		if (!isGamePlaying(currentGameState)) {
			return currentGameState
		}
		val currentDirection = currentGameState.direction
		if (isOppositeDirection(currentDirection, newDirection)) {
			return currentGameState
		}
		return currentGameState.copy(direction = newDirection)
	}

	private fun nextFrame(gameState: GameState): GameState {
		if (isGamePlaying(gameState)) {
			return updateGameState(gameState)
		}
		return gameState
	}

	private fun isOppositeDirection(dir1: Direction, dir2: Direction): Boolean {
		return (dir1 == Direction.UP && dir2 == Direction.DOWN) ||
			(dir1 == Direction.DOWN && dir2 == Direction.UP) ||
			(dir1 == Direction.LEFT && dir2 == Direction.RIGHT) ||
			(dir1 == Direction.RIGHT && dir2 == Direction.LEFT)
	}

	private fun GameState.toUiState(): SnakeUiState {
		return SnakeUiState(
			field = fieldSize,
			score = snake.size - INITIAL_SNAKE_SIZE,
			snake = snake,
			food = food,
			phase = phase,
		)
	}

	private fun updateGameState(gameState: GameState): GameState {
		var snake = gameState.snake
		var food = gameState.food
		val direction = gameState.direction

		val isGrowing = snake.head == food
		snake = move(snake, direction, isGrowing)

		val newHead = snake.head

		if (newHead.x < 0 ||
			newHead.x >= fieldSize.width ||
			newHead.y < 0 ||
			newHead.y >= fieldSize.height
		) {
			return gameState.copy(phase = Phase.GAME_OVER_LOSE)
		}

		if (checkSelfSnakeCollision(snake)) {
			return gameState.copy(phase = Phase.GAME_OVER_LOSE)
		}

		if (isGrowing) {
			food = updateFood(snake, fieldSize)
			if (food == INVALID_FOOD) {
				return gameState.copy(phase = Phase.GAME_OVER_WIN)
			}
		}

		return gameState.copy(snake = snake, food = food, lastUpdateMs = System.currentTimeMillis())
	}

	private fun checkGameOver(gameState: GameState) {
		if (gameState.phase == Phase.GAME_OVER_LOSE || gameState.phase == Phase.GAME_OVER_WIN) {
			gameCycleJob?.cancel()
			gameCycleJob = null
		}
	}

	private fun move(snake: Snake, direction: Direction, isGrowing: Boolean): Snake {
		val newHead = getNextHeadPosition(snake, direction)
		val mutableSnake = snake.copy(if (isGrowing) snake.size + 1 else snake.size)
		if (!isGrowing) {
			mutableSnake.removeAt(mutableSnake.lastIndex)
		}
		mutableSnake.add(0, newHead.asLong())
		return mutableSnake
	}

	private fun getNextHeadPosition(snake: Snake, direction: Direction): Point {
		val head = snake.head
		return when (direction) {
			Direction.UP -> Point(head.x, head.y - 1)
			Direction.DOWN -> Point(head.x, head.y + 1)
			Direction.LEFT -> Point(head.x - 1, head.y)
			Direction.RIGHT -> Point(head.x + 1, head.y)
		}
	}

	private fun checkSelfSnakeCollision(snake: Snake): Boolean {
		val head = snake.rawHead
		snake.forEachIndexed { index, point ->
			if (index > 0 && point == head) {
				return true
			}
		}
		return false
	}

	private fun updateFood(snake: Snake, fieldSize: Size): Food {
		var food = updateFoodFast(snake, fieldSize)
		if (food != INVALID_FOOD) {
			return food
		}
		food = updateFoodSlow(snake, fieldSize)
		if (food != INVALID_FOOD) {
			return food
		}
		return INVALID_FOOD
	}

	private fun updateFoodFast(snake: Snake, fieldSize: Size): Food {
		for (i in 0 until 5) {
			val food = Food(Random.nextInt(0, fieldSize.width), Random.nextInt(0, fieldSize.height))
			if (food.asLong() !in snake) {
				return food
			}
		}
		return INVALID_FOOD
	}

	private fun updateFoodSlow(snake: Snake, fieldSize: Size): Food {
		val freePositions = buildList(fieldSize.width * fieldSize.height) {
			for (y in 0 until fieldSize.height) {
				for (x in 0 until fieldSize.width) {
					val food = Food(x, y)
					if (food.asLong() !in snake) {
						add(food)
					}
				}
			}
		}
		if (freePositions.isNotEmpty()) {
			return freePositions.random()
		}
		return INVALID_FOOD
	}
}
