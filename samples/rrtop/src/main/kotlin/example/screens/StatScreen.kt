package example.screens

import androidx.compose.runtime.Composable
import com.jakewharton.mosaic.modifier.Modifier
import com.jakewharton.mosaic.ui.Color
import example.StatScreenUiState
import example.common.BorderedTitledBox
import example.common.Table
import example.common.TableConfig
import example.common.TableData

@Composable
fun StatScreen(uiState: StatScreenUiState, modifier: Modifier = Modifier) {
	BorderedTitledBox(title = "stat", modifier = modifier) {
		Table(
			TableData(uiState.items),
			TableConfig(
				listOf(
					TableConfig.ColumnConfig.StringColumnConfig(
						title = "time",
						stringFromItem = { it.time },
						weight = 2,
					),
					TableConfig.ColumnConfig.StringColumnConfig(
						title = "op/s",
						valueColor = Color.Blue,
						stringFromItem = { it.operationsPerSecond },
					),
					TableConfig.ColumnConfig.StringColumnConfig(
						title = "user",
						valueColor = Color.Blue,
						stringFromItem = { it.userCpuUtilization },
					),
					TableConfig.ColumnConfig.StringColumnConfig(
						title = "sys",
						valueColor = Color.Blue,
						stringFromItem = { it.sysCpuUtilization },
					),
					TableConfig.ColumnConfig.StringColumnConfig(
						title = "mem use",
						stringFromItem = { it.memoryUsage },
					),
					TableConfig.ColumnConfig.StringColumnConfig(
						title = "mem rss",
						stringFromItem = { it.memoryRss },
					),
					TableConfig.ColumnConfig.StringColumnConfig(
						title = "fr rati",
						valueColor = Color.Blue,
						stringFromItem = { it.fragmentationRatio },
					),
					TableConfig.ColumnConfig.StringColumnConfig(
						title = "hit rate",
						valueColor = Color.Blue,
						stringFromItem = { it.hitRateFormatted },
					),
					TableConfig.ColumnConfig.ProgressColumnConfig(
						filledColor = Color.Blue,
						emptyColor = Color.BrightBlack,
						progressFromItem = { it.hitRate / 100.0f },
					),
					TableConfig.ColumnConfig.StringColumnConfig(
						title = "keys",
						stringFromItem = { it.totalKeys },
					),
					TableConfig.ColumnConfig.StringColumnConfig(
						title = "exp",
						stringFromItem = { it.keysToExpire },
					),
					TableConfig.ColumnConfig.StringColumnConfig(
						title = "exp/s",
						valueColor = Color.Blue,
						stringFromItem = { it.keysToExpirePerSecond },
					),
					TableConfig.ColumnConfig.StringColumnConfig(
						title = "evt/s",
						valueColor = Color.Blue,
						stringFromItem = { it.evictedKeysPerSecond },
					),
				),
			),
		)
	}
}
