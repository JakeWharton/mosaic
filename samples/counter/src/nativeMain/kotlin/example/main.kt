package example

import com.jakewharton.mosaic.runMosaic
import kotlinx.coroutines.runBlocking

fun main() = runBlocking {
	runMosaic {
		Counter()
	}
}
