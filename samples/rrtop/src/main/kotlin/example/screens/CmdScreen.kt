package example.screens

import androidx.compose.runtime.Composable
import com.jakewharton.mosaic.modifier.Modifier
import com.jakewharton.mosaic.ui.Color
import example.CmdScreenUiState
import example.common.BorderedTitledBox
import example.common.Table
import example.common.TableConfig
import example.common.TableData

@Composable
fun CmdScreen(uiState: CmdScreenUiState, modifier: Modifier = Modifier) {
	BorderedTitledBox(title = "by calls", modifier = modifier) {
		Table(
			TableData(uiState.items),
			TableConfig(
				listOf(
					TableConfig.ColumnConfig.StringColumnConfig(
						title = "command",
						stringFromItem = { it.command },
					),
					TableConfig.ColumnConfig.StringColumnConfig(
						title = "calls",
						stringFromItem = { it.calls },
						valueColor = Color.Blue,
						valueAlignment = TableConfig.ColumnConfig.ColumnAligment.END,
					),
					TableConfig.ColumnConfig.StringColumnConfig(
						title = "",
						stringFromItem = { it.callsPercentFormatted },
						valueColor = Color.Blue,
						valueAlignment = TableConfig.ColumnConfig.ColumnAligment.END,
					),
					TableConfig.ColumnConfig.ProgressColumnConfig(
						filledColor = Color.Blue,
						emptyColor = Color.BrightBlack,
						progressFromItem = { it.callsPercent / 100.0f },
					),
					TableConfig.ColumnConfig.StringColumnConfig(
						title = "usec",
						stringFromItem = { it.usec },
						valueColor = Color.Blue,
						valueAlignment = TableConfig.ColumnConfig.ColumnAligment.END,
					),
					TableConfig.ColumnConfig.StringColumnConfig(
						title = "",
						stringFromItem = { it.usecPercentFormatted },
						valueColor = Color.Blue,
						valueAlignment = TableConfig.ColumnConfig.ColumnAligment.END,
					),
					TableConfig.ColumnConfig.ProgressColumnConfig(
						filledColor = Color.Blue,
						emptyColor = Color.BrightBlack,
						progressFromItem = { it.usecPercent / 100.0f },
					),
					TableConfig.ColumnConfig.StringColumnConfig(
						title = "usec per call",
						stringFromItem = { it.usecPerCall },
						valueColor = Color.Blue,
						valueAlignment = TableConfig.ColumnConfig.ColumnAligment.END,
					),
					TableConfig.ColumnConfig.StringColumnConfig(
						title = "",
						stringFromItem = { it.usecPerCallPercentFormatted },
						valueColor = Color.Blue,
						valueAlignment = TableConfig.ColumnConfig.ColumnAligment.END,
					),
					TableConfig.ColumnConfig.ProgressColumnConfig(
						filledColor = Color.Blue,
						emptyColor = Color.BrightBlack,
						progressFromItem = { it.usecPerCallPercent / 100.0f },
					),
				),
			),
		)
	}
}
