package example.screens

import androidx.compose.runtime.Composable
import com.jakewharton.mosaic.modifier.Modifier
import example.LocalRrtopColorsPalette
import example.RawScreenUiState
import example.common.BorderedTitledBox
import example.common.Table
import example.common.TableConfig
import example.common.TableData

@Composable
fun RawScreen(uiState: RawScreenUiState, modifier: Modifier = Modifier) {
	BorderedTitledBox(
		title = "raw info",
		titleColor = LocalRrtopColorsPalette.current.rawTitleFg,
		borderColor = LocalRrtopColorsPalette.current.rawBorderFg,
		modifier = modifier,
	) {
		Table(
			TableData(uiState.items),
			TableConfig(
				titleColor = LocalRrtopColorsPalette.current.rawTableHeaderFg,
				columnConfigs = listOf(
					TableConfig.ColumnConfig.StringColumnConfig(
						title = "key",
						stringFromItem = { it.key },
						valueColor = LocalRrtopColorsPalette.current.rawTableRowTop1Fg,
					),
					TableConfig.ColumnConfig.StringColumnConfig(
						title = "value",
						stringFromItem = { it.value },
						valueColor = LocalRrtopColorsPalette.current.rawTableRowTop2Fg,
					),
				),
			),
		)
	}
}
