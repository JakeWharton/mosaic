package example

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshots.withMutableSnapshot
import com.jakewharton.mosaic.Column
import com.jakewharton.mosaic.Row
import com.jakewharton.mosaic.Text
import com.jakewharton.mosaic.launchMosaic
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking

fun main() = runBlocking {
	val counts = mutableStateOf(0)

	val job = launchMosaic {
		val count by remember { counts }
		Column {
			Row {
				Text("one")
				Text("two")
			}
			Row {
				Text("three$count")
				Text("four")
			}
		}
	}

	for (i in 1..10) {
		delay(1_000)
		withMutableSnapshot {
			counts.value = i * 10
		}
	}

	// TODO how do we wait for the final frame?
	job.cancel()
}
