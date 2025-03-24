package example.screens

import androidx.compose.runtime.Composable
import com.jakewharton.mosaic.modifier.Modifier
import example.LocalRrtopColorsPalette
import example.StatScreenUiState
import example.common.BorderedTitledBox
import example.common.Table
import example.common.TableConfig
import example.common.TableData

@Composable
fun StatScreen(uiState: StatScreenUiState, modifier: Modifier = Modifier) {
	BorderedTitledBox(
		title = "stat",
		titleColor = LocalRrtopColorsPalette.current.statTitleFg,
		borderColor = LocalRrtopColorsPalette.current.statBorderFg,
		modifier = modifier,
	) {
		Table(
			TableData(uiState.items),
			TableConfig(
				titleColor = LocalRrtopColorsPalette.current.statTableHeaderFg,
				columnConfigs = listOf(
					TableConfig.ColumnConfig.StringColumnConfig(
						title = "time",
						stringFromItem = { it.time },
						valueColor = LocalRrtopColorsPalette.current.statTableRowTop1Fg,
						weight = 2,
					),
					TableConfig.ColumnConfig.StringColumnConfig(
						title = "op/s",
						stringFromItem = { it.operationsPerSecond },
						valueColor = LocalRrtopColorsPalette.current.statTableRowTop2Fg,
					),
					TableConfig.ColumnConfig.StringColumnConfig(
						title = "user",
						stringFromItem = { it.userCpuUtilization },
						valueColor = LocalRrtopColorsPalette.current.statTableRowTop2Fg,
					),
					TableConfig.ColumnConfig.StringColumnConfig(
						title = "sys",
						stringFromItem = { it.sysCpuUtilization },
						valueColor = LocalRrtopColorsPalette.current.statTableRowTop2Fg,
					),
					TableConfig.ColumnConfig.StringColumnConfig(
						title = "mem use",
						stringFromItem = { it.memoryUsage },
						valueColor = LocalRrtopColorsPalette.current.statTableRowTop1Fg,
					),
					TableConfig.ColumnConfig.StringColumnConfig(
						title = "mem rss",
						stringFromItem = { it.memoryRss },
						valueColor = LocalRrtopColorsPalette.current.statTableRowTop1Fg,
					),
					TableConfig.ColumnConfig.StringColumnConfig(
						title = "fr rati",
						stringFromItem = { it.fragmentationRatio },
						valueColor = LocalRrtopColorsPalette.current.statTableRowTop2Fg,
					),
					TableConfig.ColumnConfig.StringColumnConfig(
						title = "hit rate",
						stringFromItem = { it.hitRateFormatted },
						valueColor = LocalRrtopColorsPalette.current.statTableRowTop2Fg,
					),
					TableConfig.ColumnConfig.ProgressColumnConfig(
						filledColor = LocalRrtopColorsPalette.current.statTableRowGaugeFg,
						emptyColor = LocalRrtopColorsPalette.current.statTableRowGaugeBg,
						progressFromItem = { it.hitRate / 100.0f },
					),
					TableConfig.ColumnConfig.StringColumnConfig(
						title = "keys",
						stringFromItem = { it.totalKeys },
						valueColor = LocalRrtopColorsPalette.current.statTableRowTop1Fg,
					),
					TableConfig.ColumnConfig.StringColumnConfig(
						title = "exp",
						stringFromItem = { it.keysToExpire },
						valueColor = LocalRrtopColorsPalette.current.statTableRowTop1Fg,
					),
					TableConfig.ColumnConfig.StringColumnConfig(
						title = "exp/s",
						stringFromItem = { it.keysToExpirePerSecond },
						valueColor = LocalRrtopColorsPalette.current.statTableRowTop2Fg,
					),
					TableConfig.ColumnConfig.StringColumnConfig(
						title = "evt/s",
						stringFromItem = { it.evictedKeysPerSecond },
						valueColor = LocalRrtopColorsPalette.current.statTableRowTop2Fg,
					),
				),
			),
		)
	}
}
