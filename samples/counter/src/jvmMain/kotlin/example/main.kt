@file:JvmName("Main")

package example

import com.jakewharton.mosaic.runMosaic

suspend fun main() = runMosaic {
	Counter()
}
