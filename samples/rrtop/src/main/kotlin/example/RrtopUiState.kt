package example

import androidx.compose.runtime.Immutable

@Immutable
data class RrtopUiState(
	val currentScreen: Screen = Screen.Main,
	val screenUiState: RrtopScreenUiState = MainScreenUiState(),
	val commonInfo: String = "",
) {

	enum class Screen {
		Main,
		Cmd,
		Stat,
		Slow,
		Raw,
	}
}

@Immutable
sealed interface RrtopScreenUiState

@Immutable
data class MainScreenUiState(val items: List<MainItem> = emptyList()) : RrtopScreenUiState {
	@Immutable
	data class MainItem(
		val time: String,
		val totalOperations: String,
		val operationsPerSecond: Long,
		val operationsPerSecondFormatted: String,
		val sysCpuUtilization: Float,
		val sysCpuUtilizationFormatted: String,
		val userCpuUtilization: Float,
		val userCpuUtilizationFormatted: String,
		val totalRx: String,
		val rxPerSecond: Long,
		val rxPerSecondFormatted: String,
		val totalTx: String,
		val txPerSecond: Long,
		val txPerSecondFormatted: String,
		val maxMemory: String,
		val memoryUsage: Long,
		val memoryUsageFormatted: String,
		val fragmentationRatio: String,
		val memoryRss: Long,
		val memoryRssFormatted: String,
		val hitRate: Float,
		val hitRateFormatted: String,
		val totalKeys: String,
		val keysToExpire: String,
		val keysToExpirePerSecond: String,
		val evictedKeysPerSecond: String,
	)
}

@Immutable
data class CmdScreenUiState(val items: List<CmdItem> = emptyList()) : RrtopScreenUiState {
	@Immutable
	data class CmdItem(
		val command: String,
		val calls: String,
		val callsPercent: Float,
		val callsPercentFormatted: String,
		val usec: String,
		val usecPercent: Float,
		val usecPercentFormatted: String,
		val usecPerCall: String,
		val usecPerCallPercent: Float,
		val usecPerCallPercentFormatted: String,
	)
}

@Immutable
data class StatScreenUiState(val items: List<StatItem> = emptyList()) : RrtopScreenUiState {
	@Immutable
	data class StatItem(
		val time: String,
		val operationsPerSecond: String,
		val userCpuUtilization: String,
		val sysCpuUtilization: String,
		val memoryUsage: String,
		val fragmentationRatio: String,
		val memoryRss: String,
		val hitRate: Float,
		val hitRateFormatted: String,
		val totalKeys: String,
		val keysToExpire: String,
		val keysToExpirePerSecond: String,
		val evictedKeysPerSecond: String,
	)
}

@Immutable
data class SlowScreenUiState(val items: List<SlowItem> = emptyList()) : RrtopScreenUiState {
	@Immutable
	data class SlowItem(
		val id: String,
		val time: String,
		val execTime: String,
		val command: String,
		val clientIp: String,
		val clientName: String,
	)
}

@Immutable
data class RawScreenUiState(val items: List<RawItem> = emptyList()) : RrtopScreenUiState {
	@Immutable
	data class RawItem(
		val key: String,
		val value: String,
	)
}
