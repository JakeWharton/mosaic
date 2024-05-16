package example

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.jakewharton.mosaic.LocalTerminal
import com.jakewharton.mosaic.layout.background
import com.jakewharton.mosaic.layout.fillMaxWidth
import com.jakewharton.mosaic.layout.height
import com.jakewharton.mosaic.layout.padding
import com.jakewharton.mosaic.layout.width
import com.jakewharton.mosaic.modifier.Modifier
import com.jakewharton.mosaic.text.SpanStyle
import com.jakewharton.mosaic.text.buildAnnotatedString
import com.jakewharton.mosaic.text.withStyle
import com.jakewharton.mosaic.ui.Alignment
import com.jakewharton.mosaic.ui.Box
import com.jakewharton.mosaic.ui.Text
import example.common.Key
import example.common.key
import example.common.keyEventFlow
import example.common.utf16CodePoint
import example.screens.CmdScreen
import example.screens.MainScreen
import example.screens.RawScreen
import example.screens.SlowScreen
import example.screens.StatScreen
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun RrtopApp(rrtopViewModel: RrtopViewModel, colorsPalette: RrtopColorsPalette) {
	val terminal = LocalTerminal.current
	val rrtopUiState by rrtopViewModel.uiStateFlow.collectAsState()
	CompositionLocalProvider(LocalRrtopColorsPalette provides colorsPalette) {
		Box(
			modifier = Modifier
				.width(terminal.size.width)
				.height(terminal.size.height - 1) // subtraction of one is necessary, because there is a line with a cursor at the bottom, which moves up all the content
				.background(LocalRrtopColorsPalette.current.mainBg),
		) {
			when (rrtopUiState.currentScreen) {
				RrtopUiState.Screen.Main -> MainScreen(
					rrtopUiState.screenUiState as MainScreenUiState,
					modifier = Modifier.padding(bottom = 1),
				)
				RrtopUiState.Screen.Cmd -> CmdScreen(
					rrtopUiState.screenUiState as CmdScreenUiState,
					modifier = Modifier.padding(bottom = 1),
				)
				RrtopUiState.Screen.Stat -> StatScreen(
					rrtopUiState.screenUiState as StatScreenUiState,
					modifier = Modifier.padding(bottom = 1),
				)
				RrtopUiState.Screen.Slow -> SlowScreen(
					rrtopUiState.screenUiState as SlowScreenUiState,
					modifier = Modifier.padding(bottom = 1),
				)
				RrtopUiState.Screen.Raw -> RawScreen(
					rrtopUiState.screenUiState as RawScreenUiState,
					modifier = Modifier.padding(bottom = 1),
				)
			}
			BottomStatusBar(
				currentScreen = rrtopUiState.currentScreen,
				commonInfo = rrtopUiState.commonInfo,
				modifier = Modifier.fillMaxWidth()
					.height(1)
					.padding(horizontal = 1)
					.align(Alignment.BottomCenter),
			)
		}
	}

	LaunchedEffect(Unit) {
		withContext(Dispatchers.IO) {
			keyEventFlow.collect { keyEvent ->
				when (keyEvent.key) {
					Key.DirectionLeft -> rrtopViewModel.onArrowLeftPress()
					Key.DirectionRight -> rrtopViewModel.onArrowRightPress()
					Key.Tab -> rrtopViewModel.onTabPress()
					Key.Type -> {
						if (keyEvent.utf16CodePoint == 'q'.code || keyEvent.utf16CodePoint == 'Q'.code) {
							rrtopViewModel.onQPress()
						}
					}
				}
			}
		}
	}
}

@Composable
private fun BottomStatusBar(
	currentScreen: RrtopUiState.Screen,
	commonInfo: String,
	modifier: Modifier = Modifier,
) {
	Box(modifier = modifier.background(LocalRrtopColorsPalette.current.menuBg)) {
		Text(
			buildAnnotatedString {
				RrtopUiState.Screen.entries.forEachIndexed { index, screen ->
					val screenName = when (screen) {
						RrtopUiState.Screen.Main -> "Main"
						RrtopUiState.Screen.Cmd -> "Cmd"
						RrtopUiState.Screen.Stat -> "Stat"
						RrtopUiState.Screen.Slow -> "Slow"
						RrtopUiState.Screen.Raw -> "Raw"
					}
					if (screen == currentScreen) {
						withStyle(SpanStyle(LocalRrtopColorsPalette.current.menuHighlightFg)) {
							append(screenName)
						}
					} else {
						append(screenName)
					}
					if (index < RrtopUiState.Screen.entries.lastIndex) {
						withStyle(SpanStyle(LocalRrtopColorsPalette.current.menuDividerFg)) {
							append(" | ")
						}
					}
				}
			},
			modifier = Modifier.align(Alignment.CenterStart),
			color = LocalRrtopColorsPalette.current.menuFg,
		)
		Text(
			commonInfo,
			color = LocalRrtopColorsPalette.current.statusBarFg,
			modifier = Modifier.align(Alignment.CenterEnd),
		)
	}
}
