package example

import kotlinx.datetime.format
import kotlinx.datetime.format.DateTimeComponents
import kotlinx.datetime.format.char
import kotlinx.datetime.toDateTimePeriod

private val dateTimeFormat = DateTimeComponents.Format {
	year()
	char('-')
	monthNumber()
	char('-')
	dayOfMonth()
	char(' ')
	hour()
	char(':')
	minute()
	char(':')
	second()
}
private val timeFormat = DateTimeComponents.Format {
	hour()
	char(':')
	minute()
	char(':')
	second()
}

fun FakeDataItem.toCommonInfo(): String {
	val uptimeSeconds = commonInfo.uptime.inWholeSeconds
	val uptime = String.format(
		"%dd %02d:%02d:%02d",
		uptimeSeconds / 86400, // days
		uptimeSeconds / 3600 % 24, // hours
		uptimeSeconds / 60 % 60, // minutes
		uptimeSeconds % 60, // seconds
	)
	return "\u21c5 ${commonInfo.latency.inWholeMilliseconds}ms \u2191 $uptime (\u2387 ${commonInfo.version}) pid:${commonInfo.processId}(${commonInfo.role})"
}

fun FakeDataItem.toMainScreenUiStateItem(): MainScreenUiState.MainItem {
	return MainScreenUiState.MainItem(
		time = statInfo.time.format(timeFormat),
		totalOperations = statInfo.totalOperations.formatToShortForm(),
		operationsPerSecond = statInfo.operationsPerSecond,
		operationsPerSecondFormatted = statInfo.operationsPerSecond.formatToShortForm(),
		sysCpuUtilization = statInfo.sysCpuUtilizationPercent,
		sysCpuUtilizationFormatted = statInfo.sysCpuUtilizationPercent.formatPercent(),
		userCpuUtilization = statInfo.userCpuUtilizationPercent,
		userCpuUtilizationFormatted = statInfo.userCpuUtilizationPercent.formatPercent(),
		totalRx = statInfo.totalRxBytes.formatBytes(),
		rxPerSecond = statInfo.rxBytesPerSecond,
		rxPerSecondFormatted = "${statInfo.rxBytesPerSecond.formatBytes()}/s",
		totalTx = statInfo.totalTxBytes.formatBytes(),
		txPerSecond = statInfo.txBytesPerSecond,
		txPerSecondFormatted = "${statInfo.txBytesPerSecond.formatBytes()}/s",
		maxMemory = statInfo.maxMemoryBytes.formatBytes(),
		memoryUsage = statInfo.memoryBytesUsage,
		memoryUsageFormatted = statInfo.memoryBytesUsage.formatBytes(),
		fragmentationRatio = String.format("%.2f", statInfo.fragmentationRatio),
		memoryRss = statInfo.memoryRssBytes,
		memoryRssFormatted = statInfo.memoryRssBytes.formatBytes(),
		hitRate = statInfo.hitRatePercent,
		hitRateFormatted = statInfo.hitRatePercent.formatPercent(),
		totalKeys = statInfo.totalKeys.formatToShortForm(),
		keysToExpire = statInfo.keysToExpire.formatToShortForm(),
		keysToExpirePerSecond = statInfo.keysToExpirePerSecond.formatToShortForm(),
		evictedKeysPerSecond = statInfo.evictedKeysPerSecond.formatToShortForm(),
	)
}

fun FakeDataItem.toCmdScreenUiStateItems(): List<CmdScreenUiState.CmdItem> {
	return commandsInfo.map {
		CmdScreenUiState.CmdItem(
			command = it.command,
			calls = it.calls.formatToShortForm(),
			callsPercent = it.callsPercent,
			callsPercentFormatted = it.callsPercent.formatPercent(),
			usec = it.usec.formatToShortForm(),
			usecPercent = it.usecPercent,
			usecPercentFormatted = it.usecPercent.formatPercent(),
			usecPerCall = it.usecPerCall.formatToShortForm(),
			usecPerCallPercent = it.usecPerCallPercent,
			usecPerCallPercentFormatted = it.usecPerCallPercent.formatPercent(),
		)
	}
}

fun FakeDataItem.toStatScreenUiStateItem(): StatScreenUiState.StatItem {
	return StatScreenUiState.StatItem(
		time = statInfo.time.format(timeFormat),
		operationsPerSecond = statInfo.operationsPerSecond.formatToShortForm(),
		sysCpuUtilization = statInfo.sysCpuUtilizationPercent.formatPercent(),
		userCpuUtilization = statInfo.userCpuUtilizationPercent.formatPercent(),
		memoryUsage = statInfo.memoryBytesUsage.formatBytes(),
		fragmentationRatio = String.format("%.2f", statInfo.fragmentationRatio),
		memoryRss = statInfo.memoryRssBytes.formatBytes(),
		hitRate = statInfo.hitRatePercent,
		hitRateFormatted = statInfo.hitRatePercent.formatPercent(),
		totalKeys = statInfo.totalKeys.formatToShortForm(),
		keysToExpire = statInfo.keysToExpire.formatToShortForm(),
		keysToExpirePerSecond = statInfo.keysToExpirePerSecond.formatToShortForm(),
		evictedKeysPerSecond = statInfo.evictedKeysPerSecond.formatToShortForm(),
	)
}

fun FakeDataItem.toSlowScreenUiStateItems(): List<SlowScreenUiState.SlowItem> {
	return slowLogRecords.map {
		SlowScreenUiState.SlowItem(
			id = it.id.toString(),
			time = it.time.format(dateTimeFormat),
			execTime = run {
				val dateTimePeriod = it.execTime.toDateTimePeriod()
				String.format(
					"%ds %3dms %3dµs",
					dateTimePeriod.seconds,
					dateTimePeriod.nanoseconds / 1_000_000L % 1000L,
					dateTimePeriod.nanoseconds / 1000L % 1000L,
				)
			},
			command = it.command,
			clientIp = it.clientIp,
			clientName = it.clientName,
		)
	}
}

fun FakeDataItem.toRawScreenUiStateItems(): List<RawScreenUiState.RawItem> {
	return rawKeyValuePairs
		.map { RawScreenUiState.RawItem(key = it.key, value = it.value) }
		.asReversed()
}
