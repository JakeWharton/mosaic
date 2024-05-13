package example

import com.jakewharton.mosaic.runMosaicBlocking
import kotlinx.coroutines.suspendCancellableCoroutine

fun main() = runMosaicBlocking {
	val rrtopViewModel = RrtopViewModel(this)

	setContent {
		RrtopApp(rrtopViewModel)
	}

	// Run forever!
	suspendCancellableCoroutine {}
}
