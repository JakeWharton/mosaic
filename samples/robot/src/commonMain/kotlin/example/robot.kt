package example

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.jakewharton.mosaic.layout.DrawStyle
import com.jakewharton.mosaic.layout.KeyEvent
import com.jakewharton.mosaic.layout.PointerEvent
import com.jakewharton.mosaic.layout.drawBehind
import com.jakewharton.mosaic.layout.height
import com.jakewharton.mosaic.layout.offset
import com.jakewharton.mosaic.layout.onKeyEvent
import com.jakewharton.mosaic.layout.onPointerEvent
import com.jakewharton.mosaic.layout.padding
import com.jakewharton.mosaic.layout.size
import com.jakewharton.mosaic.modifier.Modifier
import com.jakewharton.mosaic.runMosaicBlocking
import com.jakewharton.mosaic.ui.Box
import com.jakewharton.mosaic.ui.Column
import com.jakewharton.mosaic.ui.Spacer
import com.jakewharton.mosaic.ui.Static
import com.jakewharton.mosaic.ui.Text
import com.jakewharton.mosaic.ui.unit.IntOffset
import kotlinx.coroutines.awaitCancellation

private const val worldWidth = 20
private const val worldHeight = 10

private const val worldBorderChar = '*'
private const val worldBorderWidth = 1

private const val robotWidth = 3
private const val robotHeight = 1

fun main() = runMosaicBlocking {
	var x by remember { mutableIntStateOf(0) }
	var y by remember { mutableIntStateOf(0) }
	var exit by remember { mutableStateOf(false) }

	val pointerEvents = remember { mutableStateListOf<PointerEvent>() }
	Static(pointerEvents) {
		Text("Pointer: $it")
	}

	Column(
		modifier = Modifier
			.onPointerEvent { event ->
				pointerEvents += event
				true
			}
			.onKeyEvent {
				when (it) {
					KeyEvent("ArrowUp") -> y = (y - 1).coerceAtLeast(0)
					KeyEvent("ArrowDown") -> y = (y + 1).coerceAtMost(worldHeight - robotHeight)
					KeyEvent("ArrowLeft") -> x = (x - 1).coerceAtLeast(0)
					KeyEvent("ArrowRight") -> x = (x + 1).coerceAtMost(worldWidth - robotWidth)
					KeyEvent("q") -> exit = true
					else -> return@onKeyEvent false
				}
				true
			},
	) {
		Text("-".repeat(100))
		Text("Use arrow keys to move the face. Press “q” to exit.")
		Text("Position: $x, $y   Robot: $robotWidth, $robotHeight   World: $worldWidth, $worldHeight")
		Spacer(Modifier.height(1))
		Box(
			modifier = Modifier
				.drawBehind { drawRect(worldBorderChar, drawStyle = DrawStyle.Stroke(worldBorderWidth)) }
				.padding(worldBorderWidth)
				.size(worldWidth, worldHeight),
		) {
			Text("^_^", modifier = Modifier.offset { IntOffset(x, y) })
		}
	}

	if (!exit) {
		LaunchedEffect(Unit) {
			awaitCancellation()
		}
	}
}
