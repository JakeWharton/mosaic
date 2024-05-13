package example

import kotlin.system.exitProcess
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class RrtopViewModel(private val coroutineScope: CoroutineScope) {
	private val currentScreenFlow = MutableStateFlow(RrtopUiState.Screen.Main)
	private val commonInfoFlow = MutableStateFlow("")

	private val mainScreenUiStateFlow = MutableStateFlow(MainScreenUiState())
	private val cmdScreenUiStateFlow = MutableStateFlow(CmdScreenUiState())
	private val statScreenUiStateFlow = MutableStateFlow(StatScreenUiState())
	private val slowScreenUiStateFlow = MutableStateFlow(SlowScreenUiState())
	private val rawScreenUiStateFlow = MutableStateFlow(RawScreenUiState())

	@OptIn(ExperimentalCoroutinesApi::class)
	val uiStateFlow: StateFlow<RrtopUiState> = currentScreenFlow
		.flatMapLatest { currentScreen ->
			when (currentScreen) {
				RrtopUiState.Screen.Main -> mainScreenUiStateFlow
				RrtopUiState.Screen.Cmd -> cmdScreenUiStateFlow
				RrtopUiState.Screen.Stat -> statScreenUiStateFlow
				RrtopUiState.Screen.Slow -> slowScreenUiStateFlow
				RrtopUiState.Screen.Raw -> rawScreenUiStateFlow
			}
		}
		.map { screenUiState ->
			RrtopUiState(
				currentScreen = currentScreenFlow.value,
				screenUiState = screenUiState,
				commonInfo = commonInfoFlow.value,
			)
		}
		.flowOn(Dispatchers.Default)
		.stateIn(coroutineScope, SharingStarted.Lazily, RrtopUiState())

	init {
		coroutineScope.launch(Dispatchers.Default) {
			FakeDataGenerator.fakeDataItemFlow.collect { newItem ->
				commonInfoFlow.value = newItem.toCommonInfo()
				mainScreenUiStateFlow.value = MainScreenUiState(
					mainScreenUiStateFlow.value.items + newItem.toMainScreenUiStateItem(),
				)
				cmdScreenUiStateFlow.value = CmdScreenUiState(newItem.toCmdScreenUiStateItems())
				statScreenUiStateFlow.value = StatScreenUiState(
					statScreenUiStateFlow.value.items + newItem.toStatScreenUiStateItem(),
				)
				slowScreenUiStateFlow.value = SlowScreenUiState(
					slowScreenUiStateFlow.value.items + newItem.toSlowScreenUiStateItems(),
				)
				rawScreenUiStateFlow.value = RawScreenUiState(newItem.toRawScreenUiStateItems())
			}
		}
	}

	fun onArrowLeftPress() {
		switchScreenToLeft()
	}

	fun onArrowRightPress() {
		switchScreenToRight()
	}

	fun onTabPress() {
		switchScreenToRight()
	}

	fun onQPress() {
		coroutineScope.cancel()
		exitProcess(0)
	}

	private fun switchScreenToLeft() {
		val currentScreen = currentScreenFlow.value
		currentScreenFlow.value = RrtopUiState.Screen.entries[
			if (currentScreen.ordinal > 0) {
				currentScreen.ordinal - 1
			} else {
				RrtopUiState.Screen.entries.lastIndex
			},
		]
	}

	private fun switchScreenToRight() {
		val currentScreen = currentScreenFlow.value
		currentScreenFlow.value = RrtopUiState.Screen.entries[
			if (currentScreen.ordinal < RrtopUiState.Screen.entries.lastIndex) {
				currentScreen.ordinal + 1
			} else {
				0
			},
		]
	}
}
