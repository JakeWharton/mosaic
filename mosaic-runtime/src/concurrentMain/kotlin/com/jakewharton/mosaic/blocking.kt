package com.jakewharton.mosaic

import androidx.compose.runtime.Composable
import kotlinx.coroutines.runBlocking

public fun runMosaicBlocking(content: @Composable () -> Unit) {
	runBlocking {
		runMosaic(content)
	}
}
