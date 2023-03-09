package example

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.jakewharton.mosaic.MosaicScope
import com.jakewharton.mosaic.ui.Text
import kotlinx.coroutines.delay

suspend fun MosaicScope.Counter() {
	// TODO https://github.com/JakeWharton/mosaic/issues/3
	var count by mutableStateOf(0)

	setContent {
		Text("The count is: $count")
	}

	for (i in 1..20) {
		delay(250)
		count = i
	}
}
