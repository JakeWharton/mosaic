package example.common

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import com.jakewharton.mosaic.layout.drawBehind
import com.jakewharton.mosaic.layout.fillMaxSize
import com.jakewharton.mosaic.modifier.Modifier
import com.jakewharton.mosaic.text.SpanStyle
import com.jakewharton.mosaic.text.buildAnnotatedString
import com.jakewharton.mosaic.text.withStyle
import com.jakewharton.mosaic.ui.Box
import com.jakewharton.mosaic.ui.Color
import com.jakewharton.mosaic.ui.Spacer
import kotlin.math.roundToInt

@Immutable
data class TableData<T>(val items: List<T>)

@Immutable
data class TableConfig<T>(
	val columnConfigs: List<ColumnConfig<T>>,
) {

	@Immutable
	sealed interface ColumnConfig<T> {
		val weight: Int

		@Immutable
		data class StringColumnConfig<T>(
			val title: String,
			val stringFromItem: (T) -> String,
			val titleColor: Color = Color.Cyan,
			val valueColor: Color = Color.White,
			val valueAlignment: ColumnAligment = ColumnAligment.START,
			override val weight: Int = 1,
		) : ColumnConfig<T>

		@Immutable
		data class ProgressColumnConfig<T>(
			val filledColor: Color,
			val emptyColor: Color,
			val progressFromItem: (T) -> Float,
			override val weight: Int = 1,
		) : ColumnConfig<T>

		enum class ColumnAligment {
			START,
			END,
		}
	}
}

@Composable
fun <T> Table(tableData: TableData<T>, config: TableConfig<T>, modifier: Modifier = Modifier) {
	Box(
		modifier = modifier
			.drawBehind {
				val weights = config.columnConfigs.sumOf { it.weight }
				val widthSinglePart = (width - config.columnConfigs.size) / weights
				val widths = config.columnConfigs.map { it.weight * widthSinglePart }

				val lastRange = tableData.items.takeLast(height - 1).asReversed()

				var column = 0
				config.columnConfigs.forEachIndexed { columnIndex, columnConfig ->
					val columnWidth = widths[columnIndex]
					if (columnConfig is TableConfig.ColumnConfig.StringColumnConfig) {
						val title = if (columnConfig.title.length < columnWidth) {
							columnConfig.title
						} else {
							columnConfig.title.substring(0, columnWidth)
						}
						drawText(
							row = 0,
							column = column,
							string = title,
							foreground = columnConfig.titleColor,
						)
					}

					lastRange.forEachIndexed { index, item ->
						when (columnConfig) {
							is TableConfig.ColumnConfig.StringColumnConfig -> {
								val string = columnConfig.stringFromItem(item)
								val text = if (string.length < columnWidth) {
									string
								} else {
									string.substring(0, columnWidth)
								}
								drawText(
									row = index + 1,
									column = if (columnConfig.valueAlignment == TableConfig.ColumnConfig.ColumnAligment.START) {
										column
									} else {
										column + columnWidth - text.length
									},
									string = text,
									foreground = columnConfig.valueColor,
								)
							}
							is TableConfig.ColumnConfig.ProgressColumnConfig -> {
								drawText(
									row = index + 1,
									column = column,
									string = buildAnnotatedString {
										val progress = columnConfig.progressFromItem(item)
										val filledPart = (columnWidth * progress).roundToInt()
										withStyle(SpanStyle(columnConfig.filledColor)) {
											append("━".repeat(filledPart))
										}
										withStyle(SpanStyle(columnConfig.emptyColor)) {
											append("━".repeat(columnWidth - filledPart))
										}
									},
								)
							}
						}
					}
					column += columnWidth + 1
				}
			},
	) {
		Spacer(modifier = Modifier.fillMaxSize())
	}
}
