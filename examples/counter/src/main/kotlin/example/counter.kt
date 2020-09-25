package example

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshots.withMutableSnapshot
import com.jakewharton.mosaic.Text
import com.jakewharton.mosaic.runMosaic
import kotlinx.coroutines.delay

fun main() = runMosaic {
	val countValue = mutableStateOf(0)

	setContent {
		val count by remember { countValue }
		Text("The count is: $count")
	}

	for (i in 1..20) {
		delay(250)
		withMutableSnapshot {
			countValue.value = i
		}
	}
	// TODO this doesn't display 20 but should!
}
