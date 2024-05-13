package example.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import com.jakewharton.mosaic.LocalTerminal
import com.jakewharton.mosaic.layout.drawBehind
import com.jakewharton.mosaic.layout.fillMaxHeight
import com.jakewharton.mosaic.layout.fillMaxSize
import com.jakewharton.mosaic.layout.fillMaxWidth
import com.jakewharton.mosaic.layout.height
import com.jakewharton.mosaic.layout.padding
import com.jakewharton.mosaic.modifier.Modifier
import com.jakewharton.mosaic.text.SpanStyle
import com.jakewharton.mosaic.text.buildAnnotatedString
import com.jakewharton.mosaic.text.withStyle
import com.jakewharton.mosaic.ui.Alignment
import com.jakewharton.mosaic.ui.Box
import com.jakewharton.mosaic.ui.Color
import com.jakewharton.mosaic.ui.Column
import com.jakewharton.mosaic.ui.Row
import com.jakewharton.mosaic.ui.Spacer
import com.jakewharton.mosaic.ui.Text
import example.MainScreenUiState
import example.common.BorderedTitledBox
import example.common.Table
import example.common.TableConfig
import example.common.TableData

@Composable
fun MainScreen(uiState: MainScreenUiState, modifier: Modifier = Modifier) {
	Column(modifier = modifier) {
		val withStatSection = LocalTerminal.current.size.height > 30
		CpuSection(
			uiState = uiState,
			modifier = Modifier.fillMaxHeight(if (withStatSection) 0.4f else 0.55f),
		)
		Row(modifier = Modifier.fillMaxHeight(if (withStatSection) 0.6f else 1.0f)) {
			NetworkSection(uiState = uiState, modifier = Modifier.fillMaxWidth(0.5f))
			MemorySection(uiState = uiState)
		}
		if (withStatSection) {
			StatSection(uiState = uiState, modifier = Modifier.fillMaxSize())
		}
	}
}

@Composable
private fun CpuSection(uiState: MainScreenUiState, modifier: Modifier = Modifier) {
	BorderedTitledBox(title = "cpu", modifier = modifier) {
		Row {
			val endOffsetForLabels = 7
			Column(
				modifier = Modifier
					.fillMaxWidth(0.6f)
					.padding(right = 1, bottom = 1)
					.drawBehind {
						drawText(
							row = height / 2,
							column = 0,
							string = "·".repeat(width - endOffsetForLabels),
							foreground = Color.BrightBlack,
						)
					},
			) {
				LabeledDottedPlot(
					plotInfo = PlotInfo(
						items = uiState.items,
						color = Color.Red,
						numberFromItem = { it.sysCpuUtilization },
						stringFromItem = { it.sysCpuUtilizationFormatted },
					),
					endOffsetForLabels = endOffsetForLabels,
					modifier = Modifier.fillMaxWidth().fillMaxHeight(0.5f),
				)
				LabeledDottedPlot(
					plotInfo = PlotInfo(
						items = uiState.items,
						color = Color.Green,
						numberFromItem = { it.userCpuUtilization },
						stringFromItem = { it.userCpuUtilizationFormatted },
					),
					endOffsetForLabels = endOffsetForLabels,
					reversedVertically = true,
					modifier = Modifier.fillMaxSize(),
				)
			}
			CpuThroughputSubsection(uiState, Modifier.padding(vertical = 1))
		}
		CpuLegendInfo(
			sysCpuUtilization = uiState.items.lastOrNull()?.sysCpuUtilizationFormatted ?: "",
			userCpuUtilization = uiState.items.lastOrNull()?.userCpuUtilizationFormatted ?: "",
			modifier = Modifier.align(Alignment.BottomStart),
		)
	}
}

@Composable
private fun CpuThroughputSubsection(uiState: MainScreenUiState, modifier: Modifier = Modifier) {
	BorderedTitledBox(title = "throughput", modifier = modifier) {
		TitledSolidPlot(
			firstTitle = "Total commands: ${uiState.items.lastOrNull()?.totalOperations ?: "0"}",
			secondTitle = "          Op/s: ${uiState.items.lastOrNull()?.operationsPerSecondFormatted ?: "0"}",
			plotInfo = PlotInfo(
				items = uiState.items,
				color = Color.Blue,
				numberFromItem = { it.operationsPerSecond },
				stringFromItem = { it.operationsPerSecondFormatted },
			),
			modifier = Modifier.padding(top = 1),
		)
	}
}

@Composable
private fun CpuLegendInfo(
	sysCpuUtilization: String,
	userCpuUtilization: String,
	modifier: Modifier = Modifier,
) {
	Text(
		buildAnnotatedString {
			withStyle(SpanStyle(color = Color.Red)) {
				append("•")
			}
			append("sys cpu: ")
			withStyle(SpanStyle(color = Color.Red)) {
				append(sysCpuUtilization)
			}
			append("   ")
			withStyle(SpanStyle(color = Color.Green)) {
				append("•")
			}
			append("user cpu: ")
			withStyle(SpanStyle(color = Color.Green)) {
				append(userCpuUtilization)
			}
		},
		modifier = modifier,
	)
}

@Composable
private fun NetworkSection(uiState: MainScreenUiState, modifier: Modifier = Modifier) {
	BorderedTitledBox(title = "network", modifier = modifier) {
		Column {
			TitledSolidPlot(
				firstTitle = "Total rx: ${uiState.items.lastOrNull()?.totalRx ?: ""}",
				secondTitle = "    Rx/s: ${uiState.items.lastOrNull()?.rxPerSecondFormatted ?: ""}",
				plotInfo = PlotInfo(
					items = uiState.items,
					color = Color.Red,
					numberFromItem = { it.rxPerSecond },
					stringFromItem = { it.rxPerSecondFormatted },
				),
				modifier = Modifier.fillMaxHeight(0.5f).padding(top = 1),
			)
			TitledSolidPlot(
				firstTitle = "Total tx: ${uiState.items.lastOrNull()?.totalTx ?: ""}",
				secondTitle = "    Tx/s: ${uiState.items.lastOrNull()?.txPerSecondFormatted ?: ""}",
				plotInfo = PlotInfo(
					items = uiState.items,
					color = Color.Blue,
					numberFromItem = { it.txPerSecond },
					stringFromItem = { it.txPerSecondFormatted },
				),
				modifier = Modifier.padding(top = 1),
			)
		}
	}
}

@Composable
private fun MemorySection(uiState: MainScreenUiState, modifier: Modifier = Modifier) {
	BorderedTitledBox(title = "memory", modifier = modifier) {
		Column {
			TitledSolidPlot(
				firstTitle = " Max memory: ${uiState.items.lastOrNull()?.maxMemory ?: ""}",
				secondTitle = "Used memory: ${uiState.items.lastOrNull()?.memoryUsageFormatted ?: ""}",
				plotInfo = PlotInfo(
					items = uiState.items,
					color = Color.Blue,
					numberFromItem = { it.memoryUsage },
					stringFromItem = { it.memoryUsageFormatted },
				),
				modifier = Modifier.fillMaxHeight(0.5f).padding(top = 1),
			)
			TitledSolidPlot(
				firstTitle = " Frag ratio: ${uiState.items.lastOrNull()?.fragmentationRatio ?: ""}",
				secondTitle = " RSS memory: ${uiState.items.lastOrNull()?.memoryRssFormatted ?: ""}",
				plotInfo = PlotInfo(
					items = uiState.items,
					color = Color.Green,
					numberFromItem = { it.memoryRss },
					stringFromItem = { it.memoryRssFormatted },
				),
				modifier = Modifier.padding(top = 1),
			)
		}
	}
}

@Composable
private fun StatSection(uiState: MainScreenUiState, modifier: Modifier = Modifier) {
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
						stringFromItem = { it.operationsPerSecondFormatted },
					),
					TableConfig.ColumnConfig.StringColumnConfig(
						title = "user",
						valueColor = Color.Blue,
						stringFromItem = { it.userCpuUtilizationFormatted },
					),
					TableConfig.ColumnConfig.StringColumnConfig(
						title = "sys",
						valueColor = Color.Blue,
						stringFromItem = { it.sysCpuUtilizationFormatted },
					),
					TableConfig.ColumnConfig.StringColumnConfig(
						title = "mem use",
						stringFromItem = { it.memoryUsageFormatted },
					),
					TableConfig.ColumnConfig.StringColumnConfig(
						title = "mem rss",
						stringFromItem = { it.memoryRssFormatted },
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

@Immutable
private data class PlotInfo<T>(
	val items: List<T>,
	val color: Color,
	val numberFromItem: (T) -> Number,
	val stringFromItem: (T) -> String,
)

@Composable
private fun LabeledDottedPlot(
	plotInfo: PlotInfo<MainScreenUiState.MainItem>,
	endOffsetForLabels: Int,
	modifier: Modifier = Modifier,
	reversedVertically: Boolean = false,
) {
	Box(
		modifier = modifier
			.drawBehind {
				val lastRangeItems = plotInfo.items.takeLast(width - endOffsetForLabels)
				val minValueItem = lastRangeItems.minByOrNull { plotInfo.numberFromItem(it).toDouble() }
				val maxValueItem = lastRangeItems.maxByOrNull { plotInfo.numberFromItem(it).toDouble() }
				if (minValueItem != null && maxValueItem != null) {
					val minValueText = plotInfo.stringFromItem(minValueItem)
					val maxValueText = plotInfo.stringFromItem(maxValueItem)
					val minValueRow = if (reversedVertically) 0 else height - 1
					val maxValueRow = if (reversedVertically) height - 1 else 0
					drawText(
						row = minValueRow,
						column = width - minValueText.length,
						string = minValueText,
					)
					drawText(
						row = maxValueRow,
						column = width - maxValueText.length,
						string = maxValueText,
					)
				}
			},
	) {
		DottedPlot(
			plotInfo = plotInfo,
			reversedVertically = reversedVertically,
			modifier = Modifier.fillMaxSize().padding(right = endOffsetForLabels),
		)
	}
}

@Composable
private fun DottedPlot(
	plotInfo: PlotInfo<MainScreenUiState.MainItem>,
	modifier: Modifier = Modifier,
	reversedVertically: Boolean = false,
) {
	Spacer(
		modifier = modifier
			.drawBehind {
				val lastRangeItems = plotInfo.items.takeLast(width)
				val lastRange = lastRangeItems.map { plotInfo.numberFromItem(it).toDouble() }
				val minNullableValue = lastRange.minOrNull()
				val maxNullableValue = lastRange.maxOrNull()
				if (minNullableValue == null || maxNullableValue == null) {
					return@drawBehind
				}
				val valueRange = maxNullableValue - minNullableValue
				val startColumn = width - lastRange.size
				var previousValueHeight: Int? = null
				for (i in lastRange.indices) {
					val valueHeight = if (valueRange > 0.0f) {
						((height - 1) * (lastRange[i] - minNullableValue) / valueRange).toInt()
					} else {
						height / 2
					}
					val valueOffset = valueHeight - (previousValueHeight ?: valueHeight)

					@Suppress("KotlinConstantConditions")
					val rowOffsetRange = if (valueOffset > 0) {
						0 until valueOffset
					} else if (valueOffset < 0) {
						(valueOffset + 1)..0
					} else {
						0..valueOffset
					}
					for (rowOffset in rowOffsetRange) {
						drawText(
							row = if (reversedVertically) {
								valueHeight - rowOffset
							} else {
								height - valueHeight - 1 + rowOffset
							},
							column = startColumn + i,
							string = if (rowOffset == rowOffsetRange.first || rowOffset == rowOffsetRange.last) "·" else ":",
							foreground = plotInfo.color,
						)
					}
					previousValueHeight = valueHeight
				}
			},
	)
}

@Composable
private fun TitledSolidPlot(
	firstTitle: String,
	secondTitle: String,
	plotInfo: PlotInfo<MainScreenUiState.MainItem>,
	modifier: Modifier = Modifier,
) {
	Column(modifier = modifier) {
		SolitPlotTitle(firstTitle, plotInfo.color)
		SolitPlotTitle(secondTitle, plotInfo.color)
		SolidPlot(plotInfo, Modifier.fillMaxSize())
	}
}

@Suppress("NOTHING_TO_INLINE")
@Composable
private inline fun SolitPlotTitle(
	title: String,
	color: Color,
	modifier: Modifier = Modifier,
) {
	Text(value = title, color = color, modifier = modifier.height(1))
}

@Composable
private fun SolidPlot(
	plotInfo: PlotInfo<MainScreenUiState.MainItem>,
	modifier: Modifier = Modifier,
) {
	Spacer(
		modifier = modifier
			.drawBehind {
				val lastRangeItems = plotInfo.items.takeLast(width)
				val lastRange = lastRangeItems.map { plotInfo.numberFromItem(it).toDouble() }
				val startColumn = width - lastRange.size
				drawText(
					row = height - 1,
					column = 0,
					string = "_".repeat(startColumn),
					foreground = Color.BrightBlack,
				)
				val minNullableValue = lastRange.minOrNull()
				val maxNullableValue = lastRange.maxOrNull()
				if (minNullableValue != null && maxNullableValue != null) {
					val valueRange = maxNullableValue - minNullableValue
					for (i in lastRange.indices) {
						val valueHeight = if (valueRange > 0.0f) {
							(height * (lastRange[i] - minNullableValue) / valueRange).toInt()
						} else {
							height / 2
						}
						if (valueHeight == 0) {
							drawText(
								row = height - 1,
								column = startColumn + i,
								string = "▄",
								foreground = plotInfo.color,
							)
						} else {
							drawRect(
								color = plotInfo.color,
								row = height - valueHeight,
								column = startColumn + i,
								width = 1,
								height = valueHeight,
							)
						}
					}
				}
			},
	)
}
