package com.jakewharton.mosaic

import kotlinx.coroutines.runBlocking

public fun runMosaicBlocking(body: suspend MosaicScope.() -> Unit) {
	runBlocking {
		runMosaic(body)
	}
}
