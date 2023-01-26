package com.jakewharton.mosaic

import kotlinx.coroutines.runBlocking

actual fun runMosaicBlocking(body: suspend MosaicScope.() -> Unit) {
	runBlocking {
		runMosaic(body)
	}
}
