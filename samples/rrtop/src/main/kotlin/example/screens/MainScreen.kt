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
import example.LocalRrtopColorsPalette
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
	BorderedTitledBox(
		title = "cpu",
		titleColor = LocalRrtopColorsPalette.current.cpuTitleFg,
		borderColor = LocalRrtopColorsPalette.current.cpuBorderFg,
		modifier = modifier,
	) {
		Row {
			val endOffsetForLabels = 7
			val chartLineForeground = LocalRrtopColorsPalette.current.cpuChartLineFg
			Column(
				modifier = Modifier
					.fillMaxWidth(0.6f)
					.padding(right = 1, bottom = 1)
					.drawBehind {
						drawText(
							row = height / 2,
							column = 0,
							string = "·".repeat(width - endOffsetForLabels),
							foreground = chartLineForeground,
						)
					},
			) {
				LabeledDottedPlot(
					labelColor = LocalRrtopColorsPalette.current.cpuChartAxisFg,
					plotInfo = PlotInfo(
						items = uiState.items,
						color = LocalRrtopColorsPalette.current.cpuSysCpuDatasetFg,
						numberFromItem = { it.sysCpuUtilization },
						stringFromItem = { it.sysCpuUtilizationFormatted },
					),
					endOffsetForLabels = endOffsetForLabels,
					modifier = Modifier.fillMaxWidth().fillMaxHeight(0.5f),
				)
				LabeledDottedPlot(
					labelColor = LocalRrtopColorsPalette.current.cpuChartAxisFg,
					plotInfo = PlotInfo(
						items = uiState.items,
						color = LocalRrtopColorsPalette.current.cpuUserCpuDatasetFg,
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
	BorderedTitledBox(
		title = "throughput",
		titleColor = LocalRrtopColorsPalette.current.throughputTitleFg,
		borderColor = LocalRrtopColorsPalette.current.throughputBorderFg,
		modifier = modifier,
	) {
		TitledSolidPlot(
			firstTitle = "Total commands: ${uiState.items.lastOrNull()?.totalOperations ?: "0"}",
			firstTitleColor = LocalRrtopColorsPalette.current.throughputTotalCommandsTextFg,
			secondTitle = "          Op/s: ${uiState.items.lastOrNull()?.operationsPerSecondFormatted ?: "0"}",
			secondTitleColor = LocalRrtopColorsPalette.current.throughputOpsTextFg,
			plotInfo = PlotInfo(
				items = uiState.items,
				color = LocalRrtopColorsPalette.current.throughputSparklineFg,
				baselineColor = LocalRrtopColorsPalette.current.throughputSparklineBaselineFg,
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
			withStyle(SpanStyle(color = LocalRrtopColorsPalette.current.cpuSysCpuText2Fg)) {
				append("•")
			}
			withStyle(SpanStyle(color = LocalRrtopColorsPalette.current.cpuSysCpuText1Fg)) {
				append("sys cpu: ")
			}
			withStyle(SpanStyle(color = LocalRrtopColorsPalette.current.cpuSysCpuText2Fg)) {
				append(sysCpuUtilization)
			}
			append("   ")
			withStyle(SpanStyle(color = LocalRrtopColorsPalette.current.cpuUserCpuText2Fg)) {
				append("•")
			}
			withStyle(SpanStyle(color = LocalRrtopColorsPalette.current.cpuUserCpuText1Fg)) {
				append("user cpu: ")
			}
			withStyle(SpanStyle(color = LocalRrtopColorsPalette.current.cpuUserCpuText2Fg)) {
				append(userCpuUtilization)
			}
		},
		modifier = modifier,
	)
}

@Composable
private fun NetworkSection(uiState: MainScreenUiState, modifier: Modifier = Modifier) {
	BorderedTitledBox(
		title = "network",
		titleColor = LocalRrtopColorsPalette.current.networkTitleFg,
		borderColor = LocalRrtopColorsPalette.current.networkBorderFg,
		modifier = modifier,
	) {
		Column {
			TitledSolidPlot(
				firstTitle = "Total rx: ${uiState.items.lastOrNull()?.totalRx ?: ""}",
				firstTitleColor = LocalRrtopColorsPalette.current.networkRxTotalTextFg,
				secondTitle = "    Rx/s: ${uiState.items.lastOrNull()?.rxPerSecondFormatted ?: ""}",
				secondTitleColor = LocalRrtopColorsPalette.current.networkRxSTextFg,
				plotInfo = PlotInfo(
					items = uiState.items,
					color = LocalRrtopColorsPalette.current.networkRxSparklineFg,
					baselineColor = LocalRrtopColorsPalette.current.networkRxSparklineBaselineFg,
					numberFromItem = { it.rxPerSecond },
					stringFromItem = { it.rxPerSecondFormatted },
				),
				modifier = Modifier.fillMaxHeight(0.5f).padding(top = 1),
			)
			TitledSolidPlot(
				firstTitle = "Total tx: ${uiState.items.lastOrNull()?.totalTx ?: ""}",
				firstTitleColor = LocalRrtopColorsPalette.current.networkTxTotalTextFg,
				secondTitle = "    Tx/s: ${uiState.items.lastOrNull()?.txPerSecondFormatted ?: ""}",
				secondTitleColor = LocalRrtopColorsPalette.current.networkTxSTextFg,
				plotInfo = PlotInfo(
					items = uiState.items,
					color = LocalRrtopColorsPalette.current.networkTxSparklineFg,
					baselineColor = LocalRrtopColorsPalette.current.networkTxSparklineBaselineFg,
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
	BorderedTitledBox(
		title = "memory",
		titleColor = LocalRrtopColorsPalette.current.memoryTitleFg,
		borderColor = LocalRrtopColorsPalette.current.memoryBorderFg,
		modifier = modifier,
	) {
		Column {
			TitledSolidPlot(
				firstTitle = " Max memory: ${uiState.items.lastOrNull()?.maxMemory ?: ""}",
				firstTitleColor = LocalRrtopColorsPalette.current.memoryMaxMemoryTextFg,
				secondTitle = "Used memory: ${uiState.items.lastOrNull()?.memoryUsageFormatted ?: ""}",
				secondTitleColor = LocalRrtopColorsPalette.current.memoryUsedMemoryTextFg,
				plotInfo = PlotInfo(
					items = uiState.items,
					color = LocalRrtopColorsPalette.current.memoryUsedMemorySparklineFg,
					baselineColor = LocalRrtopColorsPalette.current.memoryUsedMemorySparklineBaselineFg,
					numberFromItem = { it.memoryUsage },
					stringFromItem = { it.memoryUsageFormatted },
				),
				modifier = Modifier.fillMaxHeight(0.5f).padding(top = 1),
			)
			TitledSolidPlot(
				firstTitle = " Frag ratio: ${uiState.items.lastOrNull()?.fragmentationRatio ?: ""}",
				firstTitleColor = LocalRrtopColorsPalette.current.memoryFragRatioTextFg,
				secondTitle = " RSS memory: ${uiState.items.lastOrNull()?.memoryRssFormatted ?: ""}",
				secondTitleColor = LocalRrtopColorsPalette.current.memoryRssMemoryTextFg,
				plotInfo = PlotInfo(
					items = uiState.items,
					color = LocalRrtopColorsPalette.current.memoryRssMemorySparklineFg,
					baselineColor = LocalRrtopColorsPalette.current.memoryRssMemorySparklineBaselineFg,
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
						stringFromItem = { it.operationsPerSecondFormatted },
						valueColor = LocalRrtopColorsPalette.current.statTableRowTop2Fg,
					),
					TableConfig.ColumnConfig.StringColumnConfig(
						title = "user",
						stringFromItem = { it.userCpuUtilizationFormatted },
						valueColor = LocalRrtopColorsPalette.current.statTableRowTop2Fg,
					),
					TableConfig.ColumnConfig.StringColumnConfig(
						title = "sys",
						stringFromItem = { it.sysCpuUtilizationFormatted },
						valueColor = LocalRrtopColorsPalette.current.statTableRowTop2Fg,
					),
					TableConfig.ColumnConfig.StringColumnConfig(
						title = "mem use",
						stringFromItem = { it.memoryUsageFormatted },
						valueColor = LocalRrtopColorsPalette.current.statTableRowTop1Fg,
					),
					TableConfig.ColumnConfig.StringColumnConfig(
						title = "mem rss",
						stringFromItem = { it.memoryRssFormatted },
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

@Immutable
private data class PlotInfo<T>(
	val items: List<T>,
	val color: Color,
	val baselineColor: Color = color,
	val numberFromItem: (T) -> Number,
	val stringFromItem: (T) -> String,
)

@Composable
private fun LabeledDottedPlot(
	labelColor: Color,
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
						foreground = labelColor,
					)
					drawText(
						row = maxValueRow,
						column = width - maxValueText.length,
						string = maxValueText,
						foreground = labelColor,
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
	firstTitleColor: Color,
	secondTitle: String,
	secondTitleColor: Color,
	plotInfo: PlotInfo<MainScreenUiState.MainItem>,
	modifier: Modifier = Modifier,
) {
	Column(modifier = modifier) {
		SolitPlotTitle(firstTitle, firstTitleColor)
		SolitPlotTitle(secondTitle, secondTitleColor)
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
					foreground = plotInfo.baselineColor,
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
