package example

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.jakewharton.mosaic.ui.Text
import kotlinx.coroutines.delay

@Composable
fun Counter() {
	var count by remember { mutableIntStateOf(0) }

	Text("The count is: $count")

	LaunchedEffect(Unit) {
		for (i in 1..20) {
			delay(250)
			count = i
		}
	}
}
