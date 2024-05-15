package example.screens

import androidx.compose.runtime.Composable
import com.jakewharton.mosaic.modifier.Modifier
import com.jakewharton.mosaic.ui.Color
import example.SlowScreenUiState
import example.common.BorderedTitledBox
import example.common.Table
import example.common.TableConfig
import example.common.TableData

@Composable
fun SlowScreen(uiState: SlowScreenUiState, modifier: Modifier = Modifier) {
	BorderedTitledBox(title = "slow log", modifier = modifier) {
		Table(
			TableData(uiState.items),
			TableConfig(
				listOf(
					TableConfig.ColumnConfig.StringColumnConfig(
						title = "id",
						stringFromItem = { it.id },
					),
					TableConfig.ColumnConfig.StringColumnConfig(
						title = "time",
						stringFromItem = { it.time },
						valueColor = Color.Blue,
					),
					TableConfig.ColumnConfig.StringColumnConfig(
						title = "exec time",
						stringFromItem = { it.execTime },
						valueColor = Color.Blue,
					),
					TableConfig.ColumnConfig.StringColumnConfig(
						title = "command",
						stringFromItem = { it.command },
						valueColor = Color.Blue,
					),
					TableConfig.ColumnConfig.StringColumnConfig(
						title = "client ip",
						stringFromItem = { it.clientIp },
					),
					TableConfig.ColumnConfig.StringColumnConfig(
						title = "client name",
						stringFromItem = { it.clientName },
					),
				),
			),
		)
	}
}
