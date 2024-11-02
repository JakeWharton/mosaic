package example

import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import com.jakewharton.mosaic.runMosaicBlocking

fun main() {
	runMosaicBlocking {
		val coroutineScope = rememberCoroutineScope()
		val snakeViewModel = remember { SnakeViewModel(coroutineScope) }
		SnakeScreen(snakeViewModel)
	}
}
