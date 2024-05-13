package example.screens

import androidx.compose.runtime.Composable
import com.jakewharton.mosaic.modifier.Modifier
import com.jakewharton.mosaic.ui.Color
import example.RawScreenUiState
import example.common.BorderedTitledBox
import example.common.Table
import example.common.TableConfig
import example.common.TableData

@Composable
fun RawScreen(uiState: RawScreenUiState, modifier: Modifier = Modifier) {
	BorderedTitledBox(title = "raw info", modifier = modifier) {
		Table(
			TableData(uiState.items),
			TableConfig(
				listOf(
					TableConfig.ColumnConfig.StringColumnConfig(
						title = "key",
						stringFromItem = { it.key },
					),
					TableConfig.ColumnConfig.StringColumnConfig(
						title = "value",
						stringFromItem = { it.value },
						valueColor = Color.Blue,
					),
				),
			),
		)
	}
}
